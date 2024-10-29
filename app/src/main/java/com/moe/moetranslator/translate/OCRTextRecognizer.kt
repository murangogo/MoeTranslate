package com.moe.moetranslator.translate

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


// TODO 多行合并为一行可选
// TODO 测试没有文字的情况
object OCRTextRecognizer {
    private val recognizers = mutableMapOf<String, TextRecognizer>()

    private fun getOrCreateRecognizer(language: String): TextRecognizer {
        return recognizers.getOrPut(language) {
            when (language) {
                "zh" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                "en" -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                "ja" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
                else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
        }
    }

    // 使用suspend函数使其成为协程
    suspend fun getPicText(language: String, bitmap: Bitmap): String =
        withContext(Dispatchers.Default) {
            suspendCancellableCoroutine { continuation ->
                val recognizer = getOrCreateRecognizer(language)
                try {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            Log.d("OCR", visionText.text)
                            continuation.resume(visionText.text)
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

    // 添加清理方法
    fun cleanup() {
        recognizers.values.forEach { it.close() }
        recognizers.clear()
    }
}