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

package com.moe.moetranslator.translate

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit 端侧 OCR 引擎。支持中(zh)/英(en)/日(ja)/韩(ko)四种语言，按语言惰性创建识别器。
 *
 * 由原 `OCRTextRecognizer` 单例重构而来：改为实现 [OCRProvider] 的普通类，
 * 与各翻译 API 一样在 [FloatingBallService] 中按需实例化、用完 [release]。
 */
class MLKitOCR : OCRProvider {

    // 每种语言一个识别器，按需创建并缓存
    private val recognizers = mutableMapOf<String, TextRecognizer>()

    private fun getOrCreateRecognizer(language: String): TextRecognizer {
        return recognizers.getOrPut(language) {
            when (language) {
                "zh" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                "en" -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                "ja" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
                "ko" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
                else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
        }
    }

    override suspend fun recognize(bitmap: Bitmap, sourceLanguage: String, mergeMode: Int): String =
        withContext(Dispatchers.Default) {
            suspendCancellableCoroutine { continuation ->
                val recognizer = getOrCreateRecognizer(sourceLanguage)
                try {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val resultText = if (mergeMode != 0) {
                                mergeText(visionText, sourceLanguage, mergeMode)
                            } else {
                                visionText.text
                            }
                            Log.d("OCR", resultText)
                            continuation.resume(resultText)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            Log.d("OCR", e.toString())
                            continuation.resumeWithException(e)
                        }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }

    private fun mergeText(visionText: Text, language: String, mergeMode: Int): String {

        // 没有blocks，直接返回空字符串
        if (visionText.textBlocks.isEmpty()) return ""

        return visionText.textBlocks.mapNotNull { block ->
            // 块内没有行，直接返回null，过滤掉
            if (block.lines.isEmpty()) null
            else {
                // 处理每个block中的lines
                block.lines.mapNotNull { line ->
                    line.text.trim().takeIf { it.isNotEmpty() }
                }.joinToString(
                    separator = if (language == "en") " " else "",
                    transform = { it }
                ).takeIf { it.isNotEmpty() }
            }
        }.joinToString(
            separator = when {
                mergeMode == 1 -> "\n\n"
                language == "en" -> " "
                else -> ""
            },
            transform = { it }
        )
    }

    override fun release() {
        recognizers.values.forEach { it.close() }
        recognizers.clear()
    }
}
