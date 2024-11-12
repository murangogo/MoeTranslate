package translationapi.mlkittranslation

import android.util.Log
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MLKitTranslation : TranslationTextAPI {
    private var currentTranslator: Translator? = null
    private val modelManager = RemoteModelManager.getInstance()
    private var source: String = ""
    private var target: String = ""
    // 协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    // 用于追踪当前翻译任务
    private var currentJob: Job? = null

    // 定义需要下载的语言模型
    private val languageModels = listOf(
        TranslateLanguage.CHINESE,
        TranslateLanguage.JAPANESE
    )

    private suspend fun checkModelsStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var downloadedCount = 0
                for (language in languageModels) {
                    val model = TranslateRemoteModel.Builder(language).build()
                    if (modelManager.isModelDownloaded(model).await()) {
                        downloadedCount++
                    }
                }

                downloadedCount == languageModels.size
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        // 取消之前的翻译任务
        currentJob?.cancel()

        currentJob = coroutineScope.launch {
            try {
                // 如果已有translator且语言相同，直接使用
                if (currentTranslator != null && source == sourceLanguage && target == targetLanguage) {
                    translateText(text, callback)
                    return@launch
                }

                // 检查语言模型是否下载完成
                val modelsReady = checkModelsStatus()
                if (!modelsReady) {
                    callback(TranslationResult.Error(
                        Exception("Required language models are not downloaded yet")
                    ))
                    return@launch
                }

                // 释放之前的translator
                currentTranslator?.close()

                // 创建新的translator
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build()

                // 更新当前语言状态
                source = sourceLanguage
                target = targetLanguage

                currentTranslator = Translation.getClient(options)

                // 检查协程是否被取消
                ensureActive()
                translateText(text, callback)

            } catch (e: Exception) {
                if (e !is CancellationException) {
                    callback(TranslationResult.Error(e))
                }
            }
        }
    }

    private fun translateText(
        text: String,
        callback: (TranslationResult) -> Unit
    ) {
        currentTranslator?.translate(text)
            ?.addOnSuccessListener { translatedText ->
                Log.d("MLKIT", translatedText)
                callback(TranslationResult.Success(translatedText))
            }
            ?.addOnFailureListener { exception ->
                callback(TranslationResult.Error(exception))
            }
    }

    override fun cancelTranslation() {
        currentJob?.cancel() // 取消当前翻译任务
        currentTranslator?.close()
        currentTranslator = null
        source = ""
        target = ""
    }

    override fun release() {
        coroutineScope.cancel() // 取消所有协程
        currentJob?.cancel()
        currentTranslator?.close()
        currentTranslator = null
        source = ""
        target = ""
    }
}