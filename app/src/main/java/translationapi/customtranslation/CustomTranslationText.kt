package translationapi.customtranslation

import android.util.Log
import com.moe.moetranslator.me.CustomTextAPIConfig
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CustomTranslationText(private val config: CustomTextAPIConfig): TranslationTextAPI {
    // 创建协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null

    private val SOCKET_TIMEOUT = 10L // 10秒
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // 创建OkHttpClient实例
    private val client = OkHttpClient.Builder()
        .connectTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .build()

    /**
     * 该函数的sourceLanguage和targetLanguage没有用处，仅为了保持兼容性才接收这两个参数
     * 自定义API的源语言和目标语言用户自己指定
     */
    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        // 取消之前的任务（如果存在）
        currentJob?.cancel()

        currentJob = coroutineScope.launch {
            try {
                // 执行翻译请求
                val result = if (config.method == "GET") {
                    executeGetRequest(text)
                } else {
                    executePostRequest(text)
                }

                // 在主线程中回调结果
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Success(result))
                }
            } catch (e: CancellationException) {
                // 协程被取消，不调用callback
                throw e
            } catch (e: Exception) {
                Log.e("CUSTOM_TEXT", "Translation error", e)
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Error(e))
                }
            }
        }
    }

    private fun executeGetRequest(sourceText: String): String {
        // 构建URL，添加查询参数
        val urlBuilder = config.baseUrl.toHttpUrl().newBuilder()

        // 添加查询参数，替换占位符
        config.queryParams.forEach { param ->
            val value = if (param.value == "usesourcetext") {
                sourceText
            } else param.value
            urlBuilder.addQueryParameter(param.key, value)
        }

        // 构建请求
        val request = Request.Builder()
            .url(urlBuilder.build())
            .apply {
                // 添加请求头
                config.headers.forEach { header ->
                    addHeader(header.key, header.value)
                }
            }
            .get()
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unexpected response ${response.code}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            return parseResponse(responseBody)
        }
    }

    private fun executePostRequest(sourceText: String): String {
        // 构建JSON请求体
        val jsonBody = JSONObject().apply {
            config.jsonBody.forEach { field ->
                val value = if (field.value == "usesourcetext") {
                    sourceText
                } else field.value
                put(field.key, value)
            }
        }.toString()

        // 构建请求
        val request = Request.Builder()
            .url(config.baseUrl)
            .post(jsonBody.toRequestBody(JSON))
            .apply {
                // 添加请求头
                config.headers.forEach { header ->
                    addHeader(header.key, header.value)
                }
            }
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unexpected response ${response.code}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            return parseResponse(responseBody)
        }
    }

    private fun parseResponse(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            // 使用配置的JSON响应路径解析结果
            val parts = config.jsonResponsePath.split(".")
            var current: Any = jsonObject

            for (part in parts) {
                current = when (current) {
                    is JSONObject -> current.get(part)
                    else -> throw Exception("Invalid JSON path at: $part")
                }
            }

            current.toString()
        } catch (e: Exception) {
            throw Exception("Failed to parse response: ${e.message}")
        }
    }

    override fun cancelTranslation() {
        currentJob?.cancel()
        currentJob = null
    }

    override fun release() {
        cancelTranslation()
        coroutineScope.cancel() // 取消整个作用域
    }
}