package com.moe.moetranslator.me

import com.moe.moetranslator.utils.CustomPreference
import org.json.JSONArray
import org.json.JSONObject

// 文本翻译数据模型
data class CustomTextAPIConfig(
    val method: String,
    val baseUrl: String,
    val queryParams: List<KeyValuePair>,
    val headers: List<KeyValuePair>,
    val jsonBody: List<KeyValuePair>,
    val jsonResponsePath: String
)

// 图片翻译数据模型
data class CustomPicAPIConfig(
    val method: String,                     // GET 或 POST
    val contentType: String?,               // POST时的Content-Type
    val baseUrl: String,                    // 基础URL
    val queryParams: List<KeyValuePair>,    // GET请求的查询参数
    val headers: List<KeyValuePair>,        // 请求头
    val body: List<KeyValuePair>,           // POST请求的body (JSON或Form)
    val jsonResponsePath: String            // JSON响应解析路径
)

data class KeyValuePair(
    val key: String,
    val value: String
)

// SharedPreferences存储
object ConfigurationStorage {
    private const val KEY_METHOD = "method"
    private const val KEY_CONTENT_TYPE = "contentType"
    private const val KEY_BASE_URL = "baseUrl"
    private const val KEY_QUERY_PARAMS = "queryParams"
    private const val KEY_HEADERS = "headers"
    private const val KEY_BODY = "body"
    private const val KEY_JSON_BODY = "jsonBody"
    private const val KEY_JSON_RESPONSE_PATH = "jsonResponsePath"
    private const val KEY_PAIR_KEY = "key"
    private const val KEY_PAIR_VALUE = "value"

    // 解析键值对列表的辅助函数
    private fun parseKeyValuePairs(jsonArray: JSONArray): List<KeyValuePair> {
        val pairs = mutableListOf<KeyValuePair>()
        for (i in 0 until jsonArray.length()) {
            val pairObject = jsonArray.getJSONObject(i)
            pairs.add(KeyValuePair(
                key = pairObject.getString(KEY_PAIR_KEY),
                value = pairObject.getString(KEY_PAIR_VALUE)
            ))
        }
        return pairs
    }

    fun saveTextConfig(prefs: CustomPreference, config: CustomTextAPIConfig, apiCode: Int) {
        try {
            // 创建主JSONObject
            val jsonObject = JSONObject().apply {
                put(KEY_METHOD, config.method)
                put(KEY_BASE_URL, config.baseUrl)
                put(KEY_JSON_RESPONSE_PATH, config.jsonResponsePath)

                // 转换查询参数列表
                put(KEY_QUERY_PARAMS, JSONArray().apply {
                    config.queryParams.forEach { pair ->
                        put(JSONObject().apply {
                            put(KEY_PAIR_KEY, pair.key)
                            put(KEY_PAIR_VALUE, pair.value)
                        })
                    }
                })

                // 转换请求头列表
                put(KEY_HEADERS, JSONArray().apply {
                    config.headers.forEach { pair ->
                        put(JSONObject().apply {
                            put(KEY_PAIR_KEY, pair.key)
                            put(KEY_PAIR_VALUE, pair.value)
                        })
                    }
                })

                // 转换JSON请求体列表
                put(KEY_JSON_BODY, JSONArray().apply {
                    config.jsonBody.forEach { pair ->
                        put(JSONObject().apply {
                            put(KEY_PAIR_KEY, pair.key)
                            put(KEY_PAIR_VALUE, pair.value)
                        })
                    }
                })
            }

            // 保存到SharedPreferences
            prefs.setString("Custom_Text_API_${apiCode}", jsonObject.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePicConfig(pref: CustomPreference, config: CustomPicAPIConfig, apiCode: Int) {
        try {
            val jsonObject = JSONObject().apply {
                put(KEY_METHOD, config.method)
                put(KEY_CONTENT_TYPE, config.contentType)
                put(KEY_BASE_URL, config.baseUrl)
                put(KEY_JSON_RESPONSE_PATH, config.jsonResponsePath)

                // 转换查询参数列表
                put(KEY_QUERY_PARAMS, JSONArray().apply {
                    config.queryParams.forEach { pair ->
                        put(JSONObject().apply {
                            put(KEY_PAIR_KEY, pair.key)
                            put(KEY_PAIR_VALUE, pair.value)
                        })
                    }
                })

                // 转换请求头列表
                put(KEY_HEADERS, JSONArray().apply {
                    config.headers.forEach { pair ->
                        put(JSONObject().apply {
                            put(KEY_PAIR_KEY, pair.key)
                            put(KEY_PAIR_VALUE, pair.value)
                        })
                    }
                })

                // 转换请求体列表
                put(KEY_BODY, JSONArray().apply {
                    config.body.forEach { pair ->
                        put(JSONObject().apply {
                            put(KEY_PAIR_KEY, pair.key)
                            put(KEY_PAIR_VALUE, pair.value)
                        })
                    }
                })
            }

            pref.setString("Custom_Pic_API_${apiCode}", jsonObject.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadTextConfig(prefs: CustomPreference, apiCode: Int): CustomTextAPIConfig? {
        return try {
            val jsonString = prefs.getString("Custom_Text_API_${apiCode}", "")
            if (jsonString.isEmpty()) return null

            val jsonObject = JSONObject(jsonString)

            CustomTextAPIConfig(
                method = jsonObject.getString(KEY_METHOD),
                baseUrl = jsonObject.getString(KEY_BASE_URL),
                queryParams = parseKeyValuePairs(jsonObject.getJSONArray(KEY_QUERY_PARAMS)),
                headers = parseKeyValuePairs(jsonObject.getJSONArray(KEY_HEADERS)),
                jsonBody = parseKeyValuePairs(jsonObject.getJSONArray(KEY_JSON_BODY)),
                jsonResponsePath = jsonObject.getString(KEY_JSON_RESPONSE_PATH)
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadPicConfig(pref: CustomPreference, apiCode: Int): CustomPicAPIConfig? {
        return try {
            val jsonString = pref.getString("Custom_Pic_API_$apiCode", "")
            if (jsonString.isEmpty()) return null

            val jsonObject = JSONObject(jsonString)

            CustomPicAPIConfig(
                method = jsonObject.getString(KEY_METHOD),
                contentType = jsonObject.optString(KEY_CONTENT_TYPE, null), // 使用optString防止出现错误
                baseUrl = jsonObject.getString(KEY_BASE_URL),
                queryParams = parseKeyValuePairs(jsonObject.getJSONArray(KEY_QUERY_PARAMS)),
                headers = parseKeyValuePairs(jsonObject.getJSONArray(KEY_HEADERS)),
                body = parseKeyValuePairs(jsonObject.getJSONArray(KEY_BODY)),
                jsonResponsePath = jsonObject.getString(KEY_JSON_RESPONSE_PATH)
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
