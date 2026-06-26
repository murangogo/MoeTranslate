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

import android.content.Context
import android.graphics.Bitmap
import com.moe.moetranslator.translate.OCRProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * PaddleOCR PP-OCRv6_small 离线 OCR 引擎（[OCRProvider] 实现）。
 *
 * 仅作为 [PPOcrV6Engine]（自包含的 det+rec 推理引擎）的适配层：负责惰性初始化、
 * 把识别出的多行文本按合并模式拼接成一段文本，并把语言层面的差异（如拼接分隔符）封装在此。
 * 支持中/英/日/拉丁系语言，**不支持韩语**（识别字典无韩文）。
 *
 * @param context 任意 Context；内部仅取 applicationContext 加载 assets 模型，避免持有 Activity 引用。
 */
class PaddleOCR(context: Context) : OCRProvider {

    private val appContext = context.applicationContext

    override suspend fun recognize(bitmap: Bitmap, sourceLanguage: String, mergeMode: Int): String =
        withContext(Dispatchers.Default) {
            // 惰性初始化：首次识别时加载模型；PPOcrV6Engine.initialize 内部幂等，重复调用无副作用
            if (!PPOcrV6Engine.isInitialized) {
                PPOcrV6Engine.initialize(appContext)
            }
            // runOCR 返回阅读顺序（先上后下、再左右）的文字行，不会回收入参 bitmap
            val lines = PPOcrV6Engine.runOCR(bitmap).map { it.text }
            mergeLines(lines, sourceLanguage, mergeMode)
        }

    /**
     * 把识别出的多行文本拼接成一段。
     *
     * PP-OCRv6 没有 ML Kit 那样的「块/行」层级，只有逐行结果，故合并模式做如下等价映射：
     *  - 2 直接合并（默认）：全部拼为一行，拉丁语用空格、中日韩等用空字符串连接，最利于送入翻译；
     *  - 0 不合并 / 1 分段合并：每个识别行各占一行（用换行连接），保留版面分行。
     */
    private fun mergeLines(lines: List<String>, language: String, mergeMode: Int): String {
        val cleaned = lines.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleaned.isEmpty()) return ""
        return if (mergeMode == 2) {
            cleaned.joinToString(if (language in SPACED_LANGUAGES) " " else "")
        } else {
            cleaned.joinToString("\n")
        }
    }

    override fun release() {
        // PPOcrV6Engine 为全局单例；同一时刻只有一个 OCR 引擎在用，停止服务时释放其 ONNX 会话
        PPOcrV6Engine.release()
    }

    companion object {
        // 词与词之间用空格分隔的语言（拉丁系）；中文、日文不加分隔符
        private val SPACED_LANGUAGES = setOf("en", "fr", "de", "es", "it", "pt")
    }
}
