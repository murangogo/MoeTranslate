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

import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
        // 聊天回复可能较长，但非流式等待过久体验差：连接 15s、读取 90s
        private const val CONNECT_TIMEOUT = 15L
        private const val READ_TIMEOUT = 90L

        private val JSON = "application/json; charset=utf-8".toMediaType()

        /** 服务端故障（5xx）：可自动重试。 */
        private class ServerErrorException(message: String) : IOException(message)

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

    override suspend fun chat(
        history: List<ChatTurn>,
        userInput: String,
        onChunk: (String) -> Unit,
    ): String {
        val body = buildChatBody(history, userInput)
        // 服务端 5xx 时自动重试一次（很多自建服务偶发 500/503）
        var attempt = 0
        while (true) {
            attempt++
            try {
                return executeChatRequest(body, onChunk)
            } catch (e: ServerErrorException) {
                if (attempt >= 2) throw e
                delay(1000L)
            }
        }
    }

    private suspend fun executeChatRequest(
        body: String,
        onChunk: (String) -> Unit,
    ): String = suspendCancellableCoroutine { cont ->
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/chat/completions")
            .post(body.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        val call = client.newCall(request)
        // 协程被取消（用户点“停止”）时中断底层网络请求
        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                        ?: throw IOException("Empty response body")

                    if (!response.isSuccessful) {
                        val error = extractErrorMessage(responseBody)
                        if (response.code in 500..599) {
                            throw ServerErrorException(error ?: "HTTP ${response.code} ${response.message}")
                        }
                        throw IOException(error ?: "HTTP ${response.code} ${response.message}")
                    }

                    val content = parseResponse(responseBody)
                    if (cont.isActive) {
                        onChunk(content)
                        cont.resume(content)
                    }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resumeWithException(e)
                } finally {
                    response.close()
                }
            }
        })
    }

    override suspend fun testConnection(): String {
        // 用最小 chat 请求测试连通性（所有 OpenAI 兼容服务都支持该端点）。
        // max_tokens 给足 512：部分推理模型会把小输出预算全花在思考上导致正文为空。
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", ChatTurn.ROLE_USER)
                put("content", "hi")
            })
        }
        val body = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("max_tokens", 512)
        }.toString()

        var attempt = 0
        while (true) {
            attempt++
            try {
                return executeTestRequest(body)
            } catch (e: ServerErrorException) {
                // 自建服务偶发「无可用 worker」类 5xx，多试几次
                if (attempt >= 3) throw e
                delay(1500L)
            }
        }
    }

    private suspend fun executeTestRequest(body: String): String = suspendCancellableCoroutine { cont ->
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/chat/completions")
            .post(body.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) {
                    cont.resumeWithException(IOException("连接失败：${e.message ?: "网络错误"}"))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val parsed = runCatching {
                            val o = JSONObject(responseBody)
                            val msg = o.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
                            msg.optString("content", "") to msg.optString("reasoning_content", "")
                        }.getOrDefault("" to "")
                        val (content, reasoning) = parsed
                        if (cont.isActive) {
                            cont.resume(
                                when {
                                    content.isNotBlank() -> "连接成功，模型回复：${content.take(30)}"
                                    reasoning.isNotBlank() -> "连接成功（模型已在思考，正文尚未生成）"
                                    else -> "连接成功"
                                }
                            )
                        }
                    } else {
                        val error = extractErrorMessage(responseBody)
                        val message = error ?: "HTTP ${response.code} ${response.message}"
                        if (cont.isActive) {
                            if (response.code in 500..599) {
                                cont.resumeWithException(ServerErrorException(message))
                            } else {
                                cont.resumeWithException(IOException(message))
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resumeWithException(e)
                } finally {
                    response.close()
                }
            }
        })
    }

    private fun buildChatBody(history: List<ChatTurn>, userInput: String): String {
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

        return JSONObject().apply {
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
    }

    /** 尝试从错误响应中提取可读信息：兼容 {"error":{"message"}}、{"detail":{"message"}}、{"message"} 等格式。 */
    private fun extractErrorMessage(body: String): String? {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return null
        val fromJson = try {
            val o = JSONObject(trimmed)
            val nestedError = o.optJSONObject("error")?.optString("message")
            val nestedDetail = o.optJSONObject("detail")?.optString("message")
            val topMessage = o.optString("message")
            val topCode = o.optString("error_code")
            when {
                !nestedError.isNullOrBlank() -> nestedError
                !nestedDetail.isNullOrBlank() -> nestedDetail
                !topMessage.isNullOrBlank() -> {
                    // 国产/自建平台常用：顶层 message + 可选 error_code
                    if (topCode.isNotBlank() && topCode != "null") "$topCode: $topMessage" else topMessage
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
        // 非 JSON 响应（HTML 错误页等）：返回 body 前 200 字符摘要
        return fromJson ?: trimmed.take(200)
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
