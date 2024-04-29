package com.moe.moetranslator.translate

import org.json.JSONObject

class Result {
    companion object{
        @JvmField
        var ErrorCode: String? = null
        @JvmField
        var data: JSONObject? = null
        @JvmField
        var OriginalWords: String? = null
        @JvmField
        var ResultWords: String? = null
    }
}