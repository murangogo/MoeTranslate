/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.moe.moetranslator.ppocr

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.operation.buffer.BufferOp
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * PP-OCRv6_small 本地 OCR 引擎（det + rec，纯 ONNX Runtime）。
 *
 * 流水线：det(DBNet) → 透视裁剪 → rec(CRNN/CTC)。不使用 cls 方向分类。
 * 预处理严格对齐官方 inference.yml（已用 Python 原型实测验证）：
 *   - det：BGR + ImageNet mean/std，长边≤960 且对齐 32
 *   - rec：BGR + [-1,1]，高 48
 *   - 字典：内嵌于 yml 的 character_dict，已导出为 assets/ppocrv6/ppocrv6_dict.txt
 *
 * 模型来源：官方 huggingface.co/PaddlePaddle/PP-OCRv6_small_{det,rec}_onnx（Apache-2.0）。
 */
object PPOcrV6Engine {

    private const val TAG = "PPOcrV6Engine"

    // assets 路径
    private const val DET_ASSET = "ppocrv6/det_v6_small.onnx"
    private const val REC_ASSET = "ppocrv6/rec_v6_small.onnx"
    private const val DICT_ASSET = "ppocrv6/ppocrv6_dict.txt"

    // ---- det 常量（small_det/inference.yml）----
    private const val DET_LIMIT_SIDE_LEN = 960
    // ImageNet mean/std，作用于 BGR 通道（索引 0→B, 1→G, 2→R）
    private val DET_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val DET_STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    private const val DET_THRESH = 0.2f
    private const val DET_BOX_THRESH = 0.45f
    private const val DET_UNCLIP_RATIO = 1.4
    private const val DET_MAX_CANDIDATES = 1000
    private const val DET_MIN_SIZE = 3

    // ---- rec 常量（small_rec/inference.yml: RecResizeImg [3,48,320]）----
    private const val REC_IMG_HEIGHT = 48

    // ---- 全局过滤 ----
    private const val TEXT_SCORE_THRESH = 0.5f

    @Volatile private var ortEnv: OrtEnvironment? = null
    @Volatile private var detSession: OrtSession? = null
    @Volatile private var recSession: OrtSession? = null
    private var dictionary: List<String> = emptyList()
    private val lock = Any()

    @Volatile
    var isInitialized = false
        private set

    /** 一行识别结果：文本 + 置信度 + 原图坐标系四点框（8 个 float：TL,TR,BR,BL）。 */
    data class OcrLine(val text: String, val score: Float, val box: FloatArray)

    // ========================================================================
    // 初始化 / 释放
    // ========================================================================

    fun initialize(context: Context) {
        synchronized(lock) {
            if (isInitialized) return
            try {
                val t0 = System.currentTimeMillis()
                ortEnv = OrtEnvironment.getEnvironment()
                val opts = OrtSession.SessionOptions().apply {
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                    setIntraOpNumThreads(4)
                }
                val app = context.applicationContext
                val detBytes = app.assets.open(DET_ASSET).use { it.readBytes() }
                detSession = ortEnv!!.createSession(detBytes, opts)
                val recBytes = app.assets.open(REC_ASSET).use { it.readBytes() }
                recSession = ortEnv!!.createSession(recBytes, opts)
                dictionary = loadDictionary(app)
                isInitialized = true
                Log.i(TAG, "PP-OCRv6 初始化完成，字典 ${dictionary.size} 类，耗时 ${System.currentTimeMillis() - t0}ms")
            } catch (e: Exception) {
                Log.e(TAG, "PP-OCRv6 初始化失败", e)
                release()
                throw e
            }
        }
    }

    fun release() {
        synchronized(lock) {
            try {
                detSession?.close()
                recSession?.close()
                ortEnv?.close()
            } catch (e: Exception) {
                Log.w(TAG, "释放失败", e)
            } finally {
                detSession = null
                recSession = null
                ortEnv = null
                dictionary = emptyList()
                isInitialized = false
            }
        }
    }

    /** 字典：['blank'] + 18708 字符 + [' ']，共 18710，与模型输出类别一致。 */
    private fun loadDictionary(context: Context): List<String> {
        val lines = context.assets.open(DICT_ASSET).bufferedReader(Charsets.UTF_8).readLines()
        val dict = ArrayList<String>(lines.size + 1)
        dict.add("blank")       // index 0 = CTC blank
        dict.addAll(lines)      // 1..N（末尾含 space）
        return dict
    }

    /** 取第一个 Tensor 输出（兼容不同输出名）。 */
    private fun firstTensor(res: OrtSession.Result, session: OrtSession): OnnxTensor {
        for (name in session.outputNames) {
            val v = res.get(name)
            if (v.isPresent && v.get() is OnnxTensor) return v.get() as OnnxTensor
        }
        throw IllegalStateException("ONNX 无 Tensor 输出")
    }

    // ========================================================================
    // 主流程
    // ========================================================================

    /**
     * 对整张图运行 det + rec。
     * @return 阅读顺序（先上后下、再左右）排序、且置信度>阈值的文字行。
     */
    fun runOCR(bitmap: Bitmap): List<OcrLine> {
        check(isInitialized) { "PPOcrV6Engine 未初始化" }
        require(!bitmap.isRecycled) { "Bitmap 已回收" }

        // 1. det
        val boxes = runDet(bitmap)
        if (boxes.isEmpty()) return emptyList()

        // 2. 阅读顺序排序（先上后下，再左右）
        val ordered = boxes.sortedWith(compareBy({ minY(it) }, { minX(it) }))

        // 3. crop + rec
        val out = ArrayList<OcrLine>(ordered.size)
        for (box in ordered) {
            val crop = getRotateCropImage(bitmap, box) ?: continue
            try {
                val (text, score) = recognize(crop)
                Log.d(TAG, "rec crop=${crop.width}x${crop.height} score=%.3f len=${text.length}".format(score))
                if (text.isNotBlank() && score > TEXT_SCORE_THRESH) {
                    out.add(OcrLine(text, score, box))
                }
            } finally {
                if (!crop.isRecycled) crop.recycle()
            }
        }
        return out
    }

    private fun minX(b: FloatArray) = minOf(b[0], b[2], b[4], b[6])
    private fun minY(b: FloatArray) = minOf(b[1], b[3], b[5], b[7])

    // ========================================================================
    // DET
    // ========================================================================

    private fun runDet(bitmap: Bitmap): List<FloatArray> {
        val srcW = bitmap.width
        val srcH = bitmap.height

        // 缩放：长边≤960，再对齐 32
        var ratio = 1f
        if (max(srcW, srcH) > DET_LIMIT_SIDE_LEN) {
            ratio = DET_LIMIT_SIDE_LEN.toFloat() / max(srcW, srcH)
        }
        val rw = max(32, ((srcW * ratio).roundToInt() + 16) / 32 * 32)
        val rh = max(32, ((srcH * ratio).roundToInt() + 16) / 32 * 32)
        val scaled = Bitmap.createScaledBitmap(bitmap, rw, rh, true)
        val pixels = IntArray(rw * rh)
        scaled.getPixels(pixels, 0, rw, 0, 0, rw, rh)
        if (scaled !== bitmap) scaled.recycle()

        // BGR + ImageNet 归一化，HWC→CHW
        val plane = rw * rh
        val input = FloatArray(3 * plane)
        for (i in 0 until plane) {
            val px = pixels[i]
            val b = (px and 0xFF) / 255f
            val g = ((px shr 8) and 0xFF) / 255f
            val r = ((px shr 16) and 0xFF) / 255f
            input[i] = (b - DET_MEAN[0]) / DET_STD[0]
            input[plane + i] = (g - DET_MEAN[1]) / DET_STD[1]
            input[2 * plane + i] = (r - DET_MEAN[2]) / DET_STD[2]
        }

        val env = ortEnv!!
        val tensor = OnnxTensor.createTensor(
            env, FloatBuffer.wrap(input), longArrayOf(1, 3, rh.toLong(), rw.toLong())
        )
        val (pred, predH, predW) = tensor.use {
            detSession!!.run(mapOf("x" to it)).use { res ->
                val ot = firstTensor(res, detSession!!)
                val shape = ot.info.shape          // [1,1,H,W]
                val ph = shape[shape.size - 2].toInt()
                val pw = shape[shape.size - 1].toInt()
                val fb = ot.floatBuffer
                fb.rewind()
                val arr = FloatArray(ph * pw)
                fb.get(arr, 0, ph * pw)            // 取第 0 个 batch/通道
                Triple(arr, ph, pw)
            }
        }

        return postprocessDet(pred, predH, predW, srcH, srcW)
    }

    /** DBPostProcess：概率图 → 原图坐标四点框。pred 已是 sigmoid 概率（值域[0,1]）。 */
    private fun postprocessDet(pred: FloatArray, predH: Int, predW: Int, srcH: Int, srcW: Int): List<FloatArray> {
        // 二值化 + 8 邻域 BFS 连通域
        val bin = BooleanArray(predW * predH) { pred[it] > DET_THRESH }
        val visited = BooleanArray(predW * predH)
        val contours = ArrayList<ArrayList<Coordinate>>()
        val dx = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
        val dy = intArrayOf(-1, -1, 0, 1, 1, 1, 0, -1)
        val queue = ArrayDeque<Int>()
        for (start in bin.indices) {
            if (visited[start] || !bin[start]) continue
            val comp = ArrayList<Coordinate>()
            queue.addLast(start); visited[start] = true
            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                val cx = cur % predW
                val cy = cur / predW
                comp.add(Coordinate(cx.toDouble(), cy.toDouble()))
                for (d in 0 until 8) {
                    val nx = cx + dx[d]
                    val ny = cy + dy[d]
                    if (nx < 0 || nx >= predW || ny < 0 || ny >= predH) continue
                    val nIdx = ny * predW + nx
                    if (!visited[nIdx] && bin[nIdx]) { visited[nIdx] = true; queue.addLast(nIdx) }
                }
            }
            if (comp.size >= DET_MIN_SIZE) contours.add(comp)
        }

        val limited = if (contours.size > DET_MAX_CANDIDATES)
            contours.sortedByDescending { it.size }.take(DET_MAX_CANDIDATES) else contours

        val ratioH = srcH.toFloat() / predH
        val ratioW = srcW.toFloat() / predW
        val boxes = ArrayList<FloatArray>()
        var skMini = 0; var skScore = 0; var skUnclip = 0
        val sample = ArrayList<Float>()
        for (contour in limited) {
            val mini = getMiniBoxes(contour)
            if (mini == null || mini.minSide < DET_MIN_SIZE) { skMini++; continue }
            val score = boxScoreFast(pred, predW, predH, mini.points)
            if (sample.size < 10) sample.add(score)
            if (score < DET_BOX_THRESH) { skScore++; continue }
            val expanded = unclip(mini.points, DET_UNCLIP_RATIO)
            if (expanded == null) { skUnclip++; continue }
            val mini2 = getMiniBoxes(expanded)
            if (mini2 == null || mini2.minSide < DET_MIN_SIZE + 2) { skUnclip++; continue }
            val mapped = FloatArray(8)
            for (k in 0 until 4) {
                mapped[k * 2] = (mini2.points[k].x.toFloat() * ratioW).coerceIn(0f, (srcW - 1).toFloat())
                mapped[k * 2 + 1] = (mini2.points[k].y.toFloat() * ratioH).coerceIn(0f, (srcH - 1).toFloat())
            }
            boxes.add(mapped)
        }
        Log.d(TAG, "postDet: contours=${limited.size} kept=${boxes.size} skMini=$skMini skScore=$skScore skUnclip=$skUnclip")
        return boxes
    }

    private class MiniBox(val points: Array<Coordinate>, val minSide: Float)

    /** 凸包 + 旋转卡壳求最小外接矩形，输出 TL,TR,BR,BL。 */
    private fun getMiniBoxes(contour: List<Coordinate>): MiniBox? {
        val hull = convexHull(contour)
        if (hull.size < 3) return null
        var minArea = Float.MAX_VALUE
        var best: MiniBox? = null
        for (i in hull.indices) {
            val j = (i + 1) % hull.size
            val ex = (hull[j].x - hull[i].x).toFloat()
            val ey = (hull[j].y - hull[i].y).toFloat()
            val len = sqrt(ex * ex + ey * ey)
            if (len < 1e-6f) continue
            val ux = ex / len; val uy = ey / len
            val vx = -uy; val vy = ux
            var minU = Float.MAX_VALUE; var maxU = -Float.MAX_VALUE
            var minV = Float.MAX_VALUE; var maxV = -Float.MAX_VALUE
            for (p in hull) {
                val px = (p.x - hull[i].x).toFloat()
                val py = (p.y - hull[i].y).toFloat()
                val pu = px * ux + py * uy
                val pv = px * vx + py * vy
                if (pu < minU) minU = pu; if (pu > maxU) maxU = pu
                if (pv < minV) minV = pv; if (pv > maxV) maxV = pv
            }
            val w = maxU - minU
            val h = maxV - minV
            val area = w * h
            if (area < minArea) {
                minArea = area
                val midU = (minU + maxU) / 2
                val midV = (minV + maxV) / 2
                val cx = hull[i].x.toFloat() + midU * ux + midV * vx
                val cy = hull[i].y.toFloat() + midU * uy + midV * vy
                val corners = arrayOf(
                    Coordinate((cx - w / 2 * ux - h / 2 * vx).toDouble(), (cy - w / 2 * uy - h / 2 * vy).toDouble()),
                    Coordinate((cx + w / 2 * ux - h / 2 * vx).toDouble(), (cy + w / 2 * uy - h / 2 * vy).toDouble()),
                    Coordinate((cx + w / 2 * ux + h / 2 * vx).toDouble(), (cy + w / 2 * uy + h / 2 * vy).toDouble()),
                    Coordinate((cx - w / 2 * ux + h / 2 * vx).toDouble(), (cy - w / 2 * uy + h / 2 * vy).toDouble())
                )
                best = MiniBox(orderClockwise(corners), min(w, h))
            }
        }
        return best
    }

    /** Andrew's monotone chain 凸包。 */
    private fun convexHull(points: List<Coordinate>): List<Coordinate> {
        if (points.size < 3) return points.toList()
        val sorted = points.sortedWith(compareBy({ it.x }, { it.y }))
        fun cross(o: Coordinate, a: Coordinate, b: Coordinate) =
            (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
        val lower = ArrayList<Coordinate>()
        for (p in sorted) {
            while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], p) <= 0) lower.removeAt(lower.size - 1)
            lower.add(p)
        }
        val upper = ArrayList<Coordinate>()
        for (i in sorted.indices.reversed()) {
            val p = sorted[i]
            while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], p) <= 0) upper.removeAt(upper.size - 1)
            upper.add(p)
        }
        lower.removeAt(lower.size - 1)
        upper.removeAt(upper.size - 1)
        return lower + upper
    }

    /** 排序四角点 → TL, TR, BR, BL。 */
    private fun orderClockwise(pts: Array<Coordinate>): Array<Coordinate> {
        val s = DoubleArray(4) { pts[it].x + pts[it].y }
        val d = DoubleArray(4) { pts[it].y - pts[it].x }
        val tl = pts[s.indices.minByOrNull { s[it] }!!]
        val br = pts[s.indices.maxByOrNull { s[it] }!!]
        val tr = pts[d.indices.minByOrNull { d[it] }!!]
        val bl = pts[d.indices.maxByOrNull { d[it] }!!]
        return arrayOf(tl, tr, br, bl)
    }

    /** 轮廓内平均概率（射线法 fill）。 */
    private fun boxScoreFast(prob: FloatArray, w: Int, h: Int, box: Array<Coordinate>): Float {
        var minX = Int.MAX_VALUE; var minY = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE; var maxY = Int.MIN_VALUE
        for (p in box) {
            minX = min(minX, p.x.toInt()); maxX = max(maxX, p.x.toInt())
            minY = min(minY, p.y.toInt()); maxY = max(maxY, p.y.toInt())
        }
        minX = minX.coerceIn(0, w - 1); maxX = maxX.coerceIn(0, w - 1)
        minY = minY.coerceIn(0, h - 1); maxY = maxY.coerceIn(0, h - 1)
        if (minX > maxX || minY > maxY) return 0f
        var sum = 0f; var cnt = 0
        for (y in minY..maxY) for (x in minX..maxX) {
            if (pointInPolygon(x.toDouble(), y.toDouble(), box)) { sum += prob[y * w + x]; cnt++ }
        }
        return if (cnt > 0) sum / cnt else 0f
    }

    private fun pointInPolygon(px: Double, py: Double, poly: Array<Coordinate>): Boolean {
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val yi = poly[i].y; val yj = poly[j].y; val xi = poly[i].x; val xj = poly[j].x
            if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) inside = !inside
            j = i
        }
        return inside
    }

    /** Vatti unclip：JTS BufferOp 外扩，distance = area*ratio/perimeter。 */
    private fun unclip(box: Array<Coordinate>, ratio: Double): List<Coordinate>? {
        val area = polygonArea(box)
        val perim = polygonPerimeter(box)
        if (perim < 1e-6) return null
        val distance = area * ratio / perim
        val factory = GeometryFactory()
        val ring = Array(box.size + 1) { i -> if (i < box.size) box[i] else box[0] }
        return try {
            val poly = factory.createPolygon(ring)
            val buffered = BufferOp.bufferOp(poly, distance) ?: return null
            if (buffered.isEmpty) null else buffered.getGeometryN(0).coordinates.toList()
        } catch (e: Exception) {
            null
        }
    }

    private fun polygonArea(box: Array<Coordinate>): Double {
        var a = 0.0
        for (i in box.indices) {
            val j = (i + 1) % box.size
            a += box[i].x * box[j].y - box[j].x * box[i].y
        }
        return abs(a) / 2.0
    }

    private fun polygonPerimeter(box: Array<Coordinate>): Double {
        var p = 0.0
        for (i in box.indices) {
            val j = (i + 1) % box.size
            p += sqrt((box[j].x - box[i].x) * (box[j].x - box[i].x) + (box[j].y - box[i].y) * (box[j].y - box[i].y))
        }
        return p
    }

    // ========================================================================
    // CROP
    // ========================================================================

    /** 透视裁剪（Matrix.setPolyToPoly），竖排自动转正。box = 8 floats(TL,TR,BR,BL)。 */
    private fun getRotateCropImage(src: Bitmap, box: FloatArray): Bitmap? {
        val w = max(
            dist(box[0], box[1], box[2], box[3]),
            dist(box[6], box[7], box[4], box[5])
        ).roundToInt().coerceIn(1, src.width)
        val h = max(
            dist(box[0], box[1], box[6], box[7]),
            dist(box[2], box[3], box[4], box[5])
        ).roundToInt().coerceIn(1, src.height)

        val srcPts = floatArrayOf(box[0], box[1], box[2], box[3], box[4], box[5], box[6], box[7])
        val dstPts = floatArrayOf(0f, 0f, (w - 1).toFloat(), 0f, (w - 1).toFloat(), (h - 1).toFloat(), 0f, (h - 1).toFloat())
        val m = Matrix().apply { setPolyToPoly(srcPts, 0, dstPts, 0, 4) }
        return try {
            val crop = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            Canvas(crop).drawBitmap(src, m, null)
            if (crop.height >= crop.width * 1.5f) {
                val rot = Bitmap.createBitmap(crop, 0, 0, crop.width, crop.height, Matrix().apply { setRotate(-90f) }, true)
                if (rot !== crop) crop.recycle()
                rot
            } else crop
        } catch (e: Exception) {
            Log.w(TAG, "裁剪失败", e)
            null
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) =
        sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble())

    // ========================================================================
    // REC + CTC
    // ========================================================================

    /** 单张识别（batch=1，简单可靠）。返回 (文本, 平均置信度)。 */
    private fun recognize(crop: Bitmap): Pair<String, Float> {
        val imgW = max(1, ceil(REC_IMG_HEIGHT.toDouble() * crop.width / crop.height).toInt())
        val resized = Bitmap.createScaledBitmap(crop, imgW, REC_IMG_HEIGHT, true)
        val pixels = IntArray(imgW * REC_IMG_HEIGHT)
        resized.getPixels(pixels, 0, imgW, 0, 0, imgW, REC_IMG_HEIGHT)
        if (resized !== crop) resized.recycle()

        // BGR + [-1,1]，HWC→CHW
        val plane = imgW * REC_IMG_HEIGHT
        val input = FloatArray(3 * plane)
        for (i in 0 until plane) {
            val px = pixels[i]
            input[i] = ((px and 0xFF) / 255f - 0.5f) / 0.5f
            input[plane + i] = (((px shr 8) and 0xFF) / 255f - 0.5f) / 0.5f
            input[2 * plane + i] = (((px shr 16) and 0xFF) / 255f - 0.5f) / 0.5f
        }

        val env = ortEnv!!
        val tensor = OnnxTensor.createTensor(
            env, FloatBuffer.wrap(input), longArrayOf(1, 3, REC_IMG_HEIGHT.toLong(), imgW.toLong())
        )
        return tensor.use {
            recSession!!.run(mapOf("x" to it)).use { res ->
                val ot = firstTensor(res, recSession!!)
                val shape = ot.info.shape          // [1, T, C]
                val seqLen = shape[1].toInt()
                val numClasses = shape[2].toInt()
                val data = FloatArray(seqLen * numClasses)
                ot.floatBuffer.get(data, 0, data.size)
                ctcDecode(data, seqLen, numClasses)
            }
        }
    }

    /** CTCLabelDecode：逐时间步 argmax → 去 blank(0)+去连续重复 → 查字典；置信度走 softmax。 */
    private fun ctcDecode(preds: FloatArray, seqLen: Int, numClasses: Int): Pair<String, Float> {
        val sb = StringBuilder()
        var totalScore = 0f
        var count = 0
        var prevIdx = -1
        for (t in 0 until seqLen) {
            val off = t * numClasses
            var bestIdx = 0
            var bestVal = preds[off]
            for (c in 1 until numClasses) {
                val v = preds[off + c]
                if (v > bestVal) { bestVal = v; bestIdx = c }
            }
            if (bestIdx > 0 && bestIdx != prevIdx && bestIdx < dictionary.size) {
                sb.append(dictionary[bestIdx])
                // rec 输出已是 softmax 概率（PP-OCRv6 内置 softmax），max 值即该步置信度。
                // 切勿再 softmax 一次：在 18710 类上重复 softmax 会把概率压到 ~0，导致被阈值误杀。
                totalScore += bestVal
                count++
            }
            prevIdx = bestIdx
        }
        val text = sb.toString()
        return if (text.isBlank()) "" to 0f
        else text to (if (count > 0) totalScore / count else 0f)
    }
}
