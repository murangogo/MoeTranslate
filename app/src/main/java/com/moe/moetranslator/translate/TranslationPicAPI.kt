package com.moe.moetranslator.translate

import android.graphics.Bitmap

interface TranslationPicAPI {

    // 异步翻译方法
    fun getTranslation(
        pic: Bitmap,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    )

    // 用于取消正在进行的翻译任务
    fun cancelTranslation()

    // 释放资源
    fun release()
}
