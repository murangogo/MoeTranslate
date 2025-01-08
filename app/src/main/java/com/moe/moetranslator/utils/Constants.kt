package com.moe.moetranslator.utils

object Constants {
    // 翻译相关常量
    enum class TranslateMode(val id: Int) {
        TEXT(0),
        PIC(1)
    }

    // API 相关常量
    enum class TextApi(val id: Int) {
        AI(0),
        BING(1),
        NIUTRANS(2),
        VOLC(3),
        AZURE(4),
        BAIDU(5),
        TENCENT(6),
        CUSTOM_TEXT(7)
    }

    enum class TextAI(val id: Int) {
        MLKIT(0),
        NLLB(1)
    }

    enum class PicApi(val id: Int) {
        BAIDU(0),
        TENCENT(1),
        CUSTOM_PIC(2)
    }
}