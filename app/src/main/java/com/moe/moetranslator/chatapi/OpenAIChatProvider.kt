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

package com.moe.moetranslator.chatapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OpenAI 兼容聊天提供商。
 *
 * 请求方法：POST {baseUrl}/chat/completions
 * 请求头：Authorization: Bearer {apiKey}
 * 请求体：{ model, messages:[{role,content},...], stream:false, ...extraParams }
 *
 * 适用于所有兼容 OpenAI 接口规范的服务（OpenAI、DeepSeek、Qwen、Moonshot、
 * 本地 Ollama / llama.cpp server 等），只需在聊天设置里填写 Base URL、模型名与 Key。
 */
class OpenAIChatProvider(
    private val apiKey: String,
    private val baseUrl: String,
    private val model: String,
    private val systemPrompt: String? = null,
    private val extraParams: List<Pair<String, String>> = emptyList(),
) : ChatProvider {

    companion object {
        private const val TAG = "OpenAIChatProvider"
        // 聊天回复可能较长，超时设置比翻译更宽松
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 180L

        private val JSON = "application/json; charset=utf-8".toMediaType()

        /** OkHttpClient 复用：线程安全，避免每次对话都重建连接池。 */
        private val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .build()
        }

        /**
         * 解析设置页里的自定义参数文本（每行一个 key=value；不含 '=' 的行视为值为空的开关参数）。
         */
        fun parseExtraParams(text: String): List<Pair<String, String>> {
            if (text.isBlank()) return emptyList()
            val list = mutableListOf<Pair<String, String>>()
            text.lines().forEach { raw ->
                val line = raw.trim()
                if (line.isEmpty()) return@forEach
                val idx = line.indexOf('=')
                if (idx < 0) {
                    list.add(line to "")
                } else {
                    list.add(line.substring(0, idx).trim() to line.substring(idx + 1).trim())
                }
            }
            return list
        }

        /** 把参数对列表格式化回设置页的每行 key=value 文本。 */
        fun formatExtraParams(pairs: List<Pair<String, String>>): String =
            pairs.joinToString("\n") { (k, v) -> if (v.isEmpty()) k else "$k=$v" }
    }

    override suspend fun chat(history: List<ChatTurn>, userInput: String): String =
        withContext(Dispatchers.IO) {
            val messages = JSONArray()
            // 系统提示词（可选）：留空则不发送
            if (!systemPrompt.isNullOrBlank()) {
                messages.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }
            history.forEach { turn ->
                messages.put(JSONObject().apply {
                    put("role", turn.role)
                    put("content", turn.content)
                })
            }
            messages.put(JSONObject().apply {
                put("role", ChatTurn.ROLE_USER)
                put("content", userInput)
            })

            val body = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("stream", false)
                // 合并自定义请求参数（与翻译的聚合 AI 相同的类型推断规则）
                extraParams.forEach { (key, raw) ->
                    val k = key.trim()
                    if (k.isEmpty() || k == "messages") return@forEach
                    put(k, inferJsonValue(raw))
                }
            }.toString()

            val url = baseUrl.trimEnd('/') + "/chat/completions"
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody(JSON))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                    ?: throw IOException("Empty response body")

                if (!response.isSuccessful) {
                    val error = extractErrorMessage(responseBody)
                    throw IOException(error ?: "HTTP ${response.code} ${response.message}")
                }

                parseResponse(responseBody)
            }
        }

    /** 尝试从错误响应中提取 error.message（如 401/404/429 时的服务端提示）。 */
    private fun extractErrorMessage(body: String): String? = try {
        JSONObject(body).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() == 0) {
                throw IOException("No content in response")
            }

            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message")
                ?: throw IOException("Malformed response: missing message")

            // 思考模型：忽略 reasoning_content 与 <think>…</think>，只取最终回答
            val content = stripThinking(message.optString("content", "")).trim()
            if (content.isEmpty()) {
                throw IOException("Empty content in response")
            }
            return content
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }

    /** 去除回答里夹带的思考：成对的 <think>…</think>，以及开头的孤立 </think>。 */
    private fun stripThinking(content: String): String {
        var result = content.replace(Regex("(?s)<think>.*?</think>"), "").trim()
        if (result.startsWith("</think>")) {
            result = result.removePrefix("</think>").trim()
        }
        return result
    }

    /**
     * 把字符串值推断成合适的 JSON 类型：
     * true/false → 布尔；整数/小数 → 数字；{...}/[...] → JSON 对象/数组；null → JSON null；其余按字符串。
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
}
