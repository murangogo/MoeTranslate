package com.moe.moetranslator.translate

import android.util.Log
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.*
import java.lang.StringBuilder


class AnalysisJson {
    companion object{
        @JvmStatic
        fun jsonParse(jsonStr:String,apicode:Int){
            try {
                Log.d("jsonanalysis", "apcode=$apicode")
                if(apicode==1){
                    Log.d("jsonanalysis","baidu")
                    val obj = JSONParser().parse(jsonStr)
                    val jo = obj as JSONObject
                    Result.ErrorCode = jo["error_code"] as String
                    Result.mp = jo["data"] as Map<String,String>
                    Result.OriginalWords = (Result.mp as Map<String,String>)["sumSrc"] as String
                    Result.ResultWords = (Result.mp as Map<String,String>)["sumDst"] as String
                }else{
                    Log.d("jsonanalysis","tencent")
                    Log.d("jsonanalysis",jsonStr)
                    val obj = JSONParser().parse(jsonStr)
                    val jo = obj as JSONObject
                    val resp = jo["Response"] as JSONObject
                    if (resp.containsKey("Error")) {
                        Log.d("jsonanalysis","发生错误")
                        // 如果存在"Error"字段，表示发生了错误
                        val error = resp["Error"] as JSONObject
                        Result.ResultWords = "发生错误，错误码为："+error["Code"]+"，您可在萌译的“Me”页面查找有关此错误码的信息。"
                    }else{
                        // 否则，表示运行正常，提取"TargetText"
                        Log.d("jsonanalysis","正常")
                        val imageRecord = resp["ImageRecord"] as JSONObject
                        val values = imageRecord["Value"] as JSONArray
                        val sb = StringBuilder()
                        for (value in values) {
                            val valueObject = value as JSONObject
                            sb.append(valueObject["TargetText"])
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