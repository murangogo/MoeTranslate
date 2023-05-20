package com.moe.moetranslator.translate

class Result {
    companion object{
        @JvmField
        var ErrorCode: String? = null
        @JvmField
        var mp: Map<String, String>? = null
        @JvmField
        var OriginalWords: String? = null
        @JvmField
        var ResultWords: String? = null
    }
}