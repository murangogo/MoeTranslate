package translationapi.tencentcloud

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 腾讯云的文本翻译
 * 请求方法：POST
 * URL简单直接，没有查询参数
 * 腾讯云API需要特殊的认证头信息，通过TencentSign.getSignature生成
 * 请求参数放在请求体中，以json形式发送
 */

class TencentTranslationText(
    private val secretId: String,
    private val secretKey: String
) : TranslationTextAPI {
    private var currentTask: Thread? = null
    private val TRANS_API_HOST = "https://tmt.tencentcloudapi.com"
    private val SOCKET_TIMEOUT = 10L // 10秒

    // 创建OkHttpClient实例
    private val client = OkHttpClient.Builder()
        .connectTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .build()

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        currentTask = Thread {
            try {
                val result = translate(text, sourceLanguage, targetLanguage)
                callback(TranslationResult.Success(result))
            } catch (e: Exception) {
                callback(TranslationResult.Error(e))
            }
        }.apply { start() }
    }

    private fun translate(query: String, from: String, to: String): String {
        try {
            // 构建请求体JSON
            val requestBody = JSONObject().apply {
                put("SourceText", query)
                put("Source", from)
                put("Target", to)
                put("ProjectId", 0)
            }.toString()

            Log.d("TENCENT", "Request: $query, from: $from, to: $to")

            // 获取签名和请求头
            val headers = TencentSign.getSignature(secretId,secretKey,requestBody)

            // 构建请求
            val request = Request.Builder()
                .url(TRANS_API_HOST)
                .post(requestBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()

            // 执行请求
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response ${response.code}")
                }

                // 解析响应
                val responseBody = response.body?.string()
                    ?: throw IOException("Empty response body")

                Log.d("TENCENT", "Response: $responseBody")
                return parseResponse(responseBody)
            }
        } catch (e: Exception) {
            Log.e("TENCENT", "Translation error $e")
            throw e
        }
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)

            // 检查是否有错误信息
            if (jsonObject.has("Error")) {
                val error = jsonObject.getJSONObject("Error")
                throw IOException("${error.getString("Code")}: ${error.getString("Message")}")
            }

            // 获取Response对象
            val response = jsonObject.getJSONObject("Response")

            // 检查Response中是否有错误
            if (response.has("Error")) {
                val error = response.getJSONObject("Error")
                throw IOException("${error.getString("Code")}: ${error.getString("Message")}")
            }

            // 返回翻译结果
            return response.getString("TargetText")
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }

    override fun cancelTranslation() {
        currentTask?.let {
            if (it.isAlive) {
                it.interrupt()
            }
        }
        currentTask = null
    }

    override fun release() {
        cancelTranslation()
    }
}