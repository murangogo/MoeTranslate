package com.moe.moetranslator.translate

// 定义翻译结果的封装类
sealed class TranslationResult {
    data class Success(val translatedText: String) : TranslationResult()
    data class Error(val error: Exception) : TranslationResult()
}

interface TranslationTextAPI {

    // 异步翻译方法
    fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    )

    // 用于取消正在进行的翻译任务
    fun cancelTranslation()

    // 释放资源
    fun release()
}
