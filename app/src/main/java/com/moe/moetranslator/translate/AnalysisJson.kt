package com.moe.moetranslator.translate

import org.json.simple.JSONObject
import org.json.simple.parser.*


class AnalysisJson {
    companion object{
        @JvmStatic
        fun jsonParse(jsonStr:String){
            try {
                val obj = JSONParser().parse(jsonStr)
                val jo = obj as JSONObject
                Result.ErrorCode = jo["error_code"] as String
                Result.mp = jo["data"] as Map<String,String>
                Result.OriginalWords = (Result.mp as Map<String,String>)["sumSrc"] as String
                Result.ResultWords = (Result.mp as Map<String,String>)["sumDst"] as String
            }catch(_:Exception){

            }
        }
    }
}