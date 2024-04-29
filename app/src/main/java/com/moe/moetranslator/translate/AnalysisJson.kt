package com.moe.moetranslator.translate

import android.util.Log
import org.json.JSONObject

import java.lang.StringBuilder


class AnalysisJson {
    companion object{
        @JvmStatic
        fun jsonParse(jsonStr:String,apicode:Int){
            try {
                if(apicode==1){
                    val jo = JSONObject(jsonStr)
                    Result.ErrorCode = jo.getString("error_code")
                    Result.data = jo.getJSONObject("data")
                    Result.OriginalWords = Result.data!!.getString("sumSrc")
                    Result.ResultWords = Result.data!!.getString("sumDst")
                }else{
                    val jo = JSONObject(jsonStr)
                    val resp = jo.getJSONObject("Response")
                    if (resp.has("Error")) {
                        // 如果存在"Error"字段，表示发生了错误
                        val error = resp.getJSONObject("Error")
                        Result.ResultWords = "发生错误，错误码为："+error.getString("Code")+"，您可在萌译的“Me”页面查找有关此错误码的信息。"
                    }else{
                        // 否则，表示运行正常，提取"TargetText"
                        val imageRecord = resp.getJSONObject("ImageRecord")
                        val values = imageRecord.getJSONArray("Value")
                        val sb = StringBuilder()
                        for (i in 0 until values.length()) {
                            val valueObject = values.getJSONObject(i)
                            sb.append(valueObject.getString("TargetText"))
                            sb.append("\n")
                        }
                        Result.ResultWords = sb.toString().trimEnd()
                    }
                }
            }catch(_:Exception){

            }
        }
    }
}