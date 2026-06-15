/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package translationapi.openaitranslation

import android.util.Log
import com.moe.moetranslator.translate.CustomLocale
import com.moe.moetranslator.translate.TranslationHistory
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OpenAI规范的AI翻译
 * 请求方法：POST
 * URL: {baseUrl}/chat/completions
 * 请求头设置Content-Type为application/json和Authorization
 * 请求参数放在请求体中，以json形式发送
 * 支持各种兼容OpenAI API的服务商
 */

class OpenAITranslation(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1",
    private val model: String = "gpt-3.5-turbo",
    private val systemPrompt: String,
    private val userPrompt: String,
    private val temperature: Float? = null,
    private val extraParams: List<Pair<String, String>> = emptyList(),
    // 历史翻译记录：开启后把最近 historyCount 条 (原文,译文) 以 historyPrompt 为前缀追加到用户提示词后
    private val historyEnabled: Boolean = false,
    private val historyPrompt: String = "",
    private val historyCount: Int = 5
) : TranslationTextAPI {

    companion object {
        private const val TAG = "OpenAITranslation"
        private const val SOCKET_TIMEOUT = 30L // 30秒，AI接口需要更长时间

        private const val EXTRA_KEY = "key"
        private const val EXTRA_VALUE = "value"

        /** 将设置页里的「自定义请求参数」键值对编码为 JSON 字符串，存入 SharedPreferences。 */
        fun encodeExtraParams(pairs: List<Pair<String, String>>): String {
            val arr = JSONArray()
            pairs.forEach { (k, v) ->
                if (k.isNotBlank()) {
                    arr.put(JSONObject().apply {
                        put(EXTRA_KEY, k)
                        put(EXTRA_VALUE, v)
                    })
                }
            }
            return arr.toString()
        }

        /** 从 SharedPreferences 的 JSON 字符串还原键值对列表；为空或解析失败时返回空表。 */
        fun decodeExtraParams(json: String): List<Pair<String, String>> {
            if (json.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                val list = mutableListOf<Pair<String, String>>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(o.getString(EXTRA_KEY) to o.optString(EXTRA_VALUE, ""))
                }
                list
            } catch (e: Exception) {
                Log.e(TAG, "decodeExtraParams failed", e)
                emptyList()
            }
        }
    }

    // 创建协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null

    private val JSON = "application/json; charset=utf-8".toMediaType()

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
        // 取消之前的任务（如果存在）
        currentJob?.cancel()

        currentJob = coroutineScope.launch {
            try {
                val result = translate(text, sourceLanguage, targetLanguage)
                // 记录成功的翻译，供后续翻译作为历史上下文参考（是否实际追加由 historyEnabled 决定）
                if (historyEnabled) {
                    TranslationHistory.record(text, result)
                }
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Success(result))
                }
            } catch (e: CancellationException) {
                // 协程被取消，不调用callback
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Translation error", e)
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Error(e))
                }
            }
        }
    }

    private suspend fun translate(text: String, from: String, to: String): String = withContext(Dispatchers.IO) {
        ensureActive()

        // 构建翻译提示词
        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPrompt(text, from, to)

        // 构建请求体
        val requestBody = buildRequestBody(systemPrompt, userPrompt)

        Log.d(TAG, "Request: $requestBody")

        // 构建请求
        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .post(requestBody.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            ensureActive()

            if (!response.isSuccessful) {
                throw IOException("Unexpected response ${response.code}: ${response.message}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")

            Log.d(TAG, "Response: $responseBody")
            parseResponse(responseBody)
        }
    }

    private fun buildSystemPrompt(): String {
        return systemPrompt
    }

    private fun buildUserPrompt(text: String, from: String, to: String): String {
        val fromLang = CustomLocale.getInstance(from).getDisplayName()
        val toLang = CustomLocale.getInstance(to).getDisplayName()

        var fullUserPrompt = userPrompt
            .replace("usefromlang", fromLang)
            .replace("usetolang", toLang)
            .replace("usesourcetext", text)

        // 开启历史记录时，把最近若干条 (原文,译文) 追加到用户提示词之后供模型参考
        if (historyEnabled) {
            fullUserPrompt = TranslationHistory.appendHistory(fullUserPrompt, historyPrompt, historyCount)
        }

        Log.d(TAG, "fullUserPrompt: $fullUserPrompt")

        return fullUserPrompt
    }

    private fun buildRequestBody(systemPrompt: String, userPrompt: String): String {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })
        }

        return JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("stream", false)
            // 温度留空则不发送：部分推理模型（如 OpenAI o 系列）只接受默认温度，发送会报 400
            temperature?.let { put("temperature", it.toDouble()) }
            // 合并用户自定义参数，按类型推断写入，可覆盖上面的字段（messages 除外）
            // 这是关闭思考 / 适配新模型的通用入口：enable_thinking=false、reasoning_effort=low、
            // max_completion_tokens=2048、top_p=0.9、chat_template_kwargs={"enable_thinking":false} 等
            extraParams.forEach { (key, raw) ->
                val k = key.trim()
                if (k.isEmpty() || k == "messages") return@forEach
                put(k, inferJsonValue(raw))
            }
        }.toString()
    }

    /**
     * 把用户在设置页填入的字符串值推断成合适的 JSON 类型，使其在请求体里表达正确：
     *   true/false → 布尔；整数/小数 → 数字；{...}/[...] → JSON 对象/数组；null → JSON null；其余按字符串。
     */
    private fun inferJsonValue(raw: String): Any {
        val v = raw.trim()
        return when {
            v.isEmpty() -> ""
            v.equals("true", ignoreCase = true) -> true
            v.equals("false", ignoreCase = true) -> false
            v.equals("null", ignoreCase = true) -> JSONObject.NULL
            v.toIntOrNull() != null -> v.toInt()
            v.toLongOrNull() != null -> v.toLong()
            v.toDoubleOrNull() != null -> v.toDouble()
            v.startsWith("{") -> runCatching { JSONObject(v) }.getOrElse { raw }
            v.startsWith("[") -> runCatching { JSONArray(v) }.getOrElse { raw }
            else -> raw
        }
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)

            // 检查是否有错误
            if (jsonObject.has("error")) {
                val error = jsonObject.getJSONObject("error")
                val message = error.optString("message", "Unknown error")
                val type = error.optString("type", "unknown")
                throw IOException("OpenAI API error ($type): $message")
            }

            // 获取翻译结果
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() == 0) {
                throw IOException("No translation result in response")
            }

            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            // 只取最终回答 content；思考模型单独放在 reasoning_content 的思考内容被忽略，
            // 夹带在 content 里的 <think>…</think> 也会被剥离
            val content = stripThinking(message.optString("content", "")).trim()

            if (content.isEmpty()) {
                throw IOException("Empty translation result")
            }

            return content
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }

    /** 去除回答里夹带的思考：成对的 <think>…</think>，以及模板只回传闭合标签时位于开头的孤立 </think>。 */
    private fun stripThinking(content: String): String {
        var result = content.replace(Regex("(?s)<think>.*?</think>"), "").trim()
        if (result.startsWith("</think>")) {
            result = result.removePrefix("</think>").trim()
        }
        return result
    }

    override fun cancelTranslation() {
        currentJob?.cancel()
        currentJob = null
    }

    override fun release() {
        cancelTranslation()
        coroutineScope.cancel() // 取消整个作用域
    }

    /**
     * 获取支持的模型列表（需要API支持）
     * @param callback 模型列表回调
     */
    fun getSupportedModels(callback: (List<String>?, String?) -> Unit) {
        coroutineScope.launch {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/models")
                    .get()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Failed to get models: ${response.code}")
                    }

                    val responseBody = response.body?.string()
                        ?: throw IOException("Empty response")

                    val jsonObject = JSONObject(responseBody)
                    val data = jsonObject.getJSONArray("data")
                    val models = mutableListOf<String>()

                    for (i in 0 until data.length()) {
                        val model = data.getJSONObject(i)
                        models.add(model.getString("id"))
                    }

                    withContext(Dispatchers.Main) {
                        callback(models, null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null, e.message)
                }
            }
        }
    }
}