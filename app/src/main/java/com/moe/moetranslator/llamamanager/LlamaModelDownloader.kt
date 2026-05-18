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

package com.moe.moetranslator.llamamanager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

/**
 * 单个 GGUF 模型文件的下载器。
 *
 * 关键能力：
 *   - 断点续传：通过 HTTP Range 头从 file.length() 字节继续；服务端不支持时由调用方
 *     处理（这里如果返回 200 而不是 206，将覆盖原文件，等价于重新下载，符合用户期望）。
 *   - MD5 校验：下载完成后核对；不匹配则删除文件、抛异常。
 *   - 取消：协程取消 + AtomicBoolean 双重判断，避免在 OkHttp 读流阻塞时漏掉协程取消。
 *
 * 与 NLLB 不同点：
 *   - 单文件，不需要多文件循环。
 *   - 写完后立刻插库（由调用方拿到返回的 LlamaModelEntity 后做）。
 */
class LlamaModelDownloader(
    private val context: Context,
    private val client: OkHttpClient = defaultClient,
) {

    private val canceled = AtomicBoolean(false)

    fun cancel() {
        canceled.set(true)
    }

    fun reset() {
        canceled.set(false)
    }

    /**
     * @param onProgress (downloadedBytes, totalBytes, bytesPerSecond) — 主线程或非主线程不限
     *                   调用方自己决定如何 marshal 到 UI。每秒最多回调一次。
     * @return 写入成功的本地 File；失败抛 IOException。
     */
    suspend fun download(
        info: PresetModelInfo,
        onProgress: suspend (downloaded: Long, total: Long, bps: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {

        val file = File(LlamaModelStorage.modelsDir(context), info.fileName)
        var downloaded = if (file.exists()) file.length() else 0L

        // 若已下载完且 MD5 对得上，直接复用
        if (downloaded == info.fileSize) {
            if (LlamaModelStorage.calculateMd5(file).equals(info.md5Checksum, ignoreCase = true)) {
                onProgress(info.fileSize, info.fileSize, 0L)
                return@withContext file
            } else {
                // 大小对但 MD5 不对：旧文件污损，删了重下
                file.delete()
                downloaded = 0L
            }
        } else if (downloaded > info.fileSize) {
            // 比远端还大，本地文件不可信，重头来
            file.delete()
            downloaded = 0L
        }

        val request = Request.Builder()
            .url(info.downloadUrl)
            .apply {
                if (downloaded > 0) addHeader("Range", "bytes=$downloaded-")
            }
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException("HTTP ${response.code} ${response.message}")
        }
        // 如果请求了 Range 但服务端回了 200 而非 206，需要从头写，覆盖已下载部分
        val gotPartial = response.code == 206
        if (downloaded > 0 && !gotPartial) {
            downloaded = 0L
        }

        val body = response.body ?: run {
            response.close()
            throw IOException("Empty response body")
        }
        val input = body.byteStream()
        val raf = RandomAccessFile(file, "rw")
        raf.seek(downloaded)

        val buffer = ByteArray(8 * 1024)
        var lastTick = System.currentTimeMillis()
        var lastBytes = downloaded
        try {
            input.use { ins ->
                raf.use { out ->
                    while (true) {
                        if (canceled.get()) throw CancellationException("Download canceled")
                        coroutineContext.ensureActive()

                        val read = ins.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                        downloaded += read

                        val now = System.currentTimeMillis()
                        if (now - lastTick >= 1000) {
                            val bps = downloaded - lastBytes
                            try {
                                onProgress(downloaded, info.fileSize, bps)
                            } catch (_: Throwable) { /* UI 回调失败不能影响下载 */ }
                            lastTick = now
                            lastBytes = downloaded
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            // 出错时保留已下载字节，下次可断点续传；只有 MD5 校验失败才删
            throw t
        }

        // 最后一次进度更新（确保 UI 显示 100%）
        try { onProgress(downloaded, info.fileSize, 0L) } catch (_: Throwable) {}

        // MD5 校验
        val actual = LlamaModelStorage.calculateMd5(file)
        if (!actual.equals(info.md5Checksum, ignoreCase = true)) {
            file.delete()
            throw IOException("MD5 mismatch: expected ${info.md5Checksum}, got $actual")
        }

        Log.i(TAG, "Downloaded ${file.absolutePath} (${file.length()} bytes)")
        file
    }

    companion object {
        private const val TAG = "LlamaModelDownloader"

        private val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
