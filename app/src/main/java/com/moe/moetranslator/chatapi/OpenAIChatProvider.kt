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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        onReasoning: (String) -> Unit,
        onContent: (String) -> Unit,
    ): ChatReply {
        val body = buildChatBody(history, userInput)
        // 服务端 5xx 时自动重试一次（很多自建服务偶发 500/503）
        var attempt = 0
        while (true) {
            attempt++
            try {
                return executeChatRequest(body, onReasoning, onContent)
            } catch (e: ServerErrorException) {
                if (attempt >= 2) throw e
                delay(1000L)
            }
        }
    }

    private suspend fun executeChatRequest(
        body: String,
        onReasoning: (String) -> Unit,
        onContent: (String) -> Unit,
    ): ChatReply = suspendCancellableCoroutine { cont ->
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/chat/completions")
            .post(body.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        val call = client.newCall(request)
        // 协程被取消（用户点“停止”）时中断底层网络请求
        cont.invokeOnCancellation { call.cancel() }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                call.execute().use { response ->
                    val responseBody = response.body
                        ?: throw IOException("Empty response body")

                    if (!response.isSuccessful) {
                        val errorText = responseBody.string()
                        val error = extractErrorMessage(errorText)
                        if (response.code in 500..599) {
                            throw ServerErrorException(error ?: "HTTP ${response.code} ${response.message}")
                        }
                        throw IOException(error ?: "HTTP ${response.code} ${response.message}")
                    }

                    val reply = readChatStream(responseBody, onReasoning, onContent)
                    if (cont.isActive) cont.resume(reply)
                }
            } catch (e: Exception) {
                if (cont.isActive) cont.resumeWithException(e)
            }
        }
    }

    /**
     * 读取响应：优先按 SSE 流式解析（data: 行），逐段回调思考/正文增量；
     * 若服务端忽略 stream 参数返回一次性 JSON，则回退为非流式解析。
     */
    private fun readChatStream(
        responseBody: okhttp3.ResponseBody,
        onReasoning: (String) -> Unit,
        onContent: (String) -> Unit,
    ): ChatReply {
        val source = responseBody.source()

        // 跳过开头的空行/注释行
        var line = source.readUtf8Line()?.trim().orEmpty()
        while (line.isEmpty()) {
            line = source.readUtf8Line()?.trim() ?: break
        }

        if (line.startsWith("data:")) {
            // SSE 流式
            val sbReasoning = StringBuilder()
            val sbContent = StringBuilder()
            var current = line
            while (true) {
                val data = current.removePrefix("data:").trim()
                if (data == "[DONE]") break
                if (data.isNotEmpty()) {
                    val delta = parseDelta(data)
                    if (delta.first.isNotEmpty()) {
                        sbReasoning.append(delta.first)
                        onReasoning(delta.first)
                    }
                    if (delta.second.isNotEmpty()) {
                        sbContent.append(delta.second)
                        onContent(delta.second)
                    }
                }
                current = source.readUtf8Line()?.trim() ?: break
            }
            return ChatReply(
                content = sbContent.toString().trim(),
                reasoning = sbReasoning.toString().trim(),
            )
        }

        // 非流式 JSON：拼接完整响应体后按一次性格式解析
        val rest = source.readByteString().utf8()
        val full = if (line.isEmpty()) rest else line + "\n" + rest
        val parsed = parseResponse(full)
        if (parsed.reasoning.isNotEmpty()) onReasoning(parsed.reasoning)
        if (parsed.content.isNotEmpty()) onContent(parsed.content)
        return parsed
    }

    /** 解析 SSE 单条 data 行：返回 (思考增量, 正文增量)。 */
    private fun parseDelta(data: String): Pair<String, String> {
        return try {
            val o = JSONObject(data)
            val delta = o.getJSONArray("choices").getJSONObject(0).optJSONObject("delta")
                ?: return "" to ""
            delta.optString("reasoning_content", "") to delta.optString("content", "")
        } catch (e: Exception) {
            "" to ""
        }
    }

    /**
     * 两步连通性测试（参考主流客户端做法）：
     * 1. GET /models —— 验证网络、认证与平台可达（不需要分配推理 worker，最可靠）
     * 2. 最小 chat 请求 —— 验证该模型是否有可用推理资源（自建服务常见「无可用 worker」）
     */
    override suspend fun testConnection(): String {
        val report = StringBuilder()

        // 第一步：模型列表端点
        report.append("✅ ").append(testModelsEndpoint())

        // 第二步：推理资源探测
        try {
            val chatResult = testChatEndpoint()
            report.append("\n✅ 推理可用：").append(chatResult)
        } catch (e: Exception) {
            val msg = e.message ?: e.toString()
            if (msg.contains("candidate worker", ignoreCase = true) ||
                msg.contains("No candidate", ignoreCase = true)
            ) {
                report.append("\n⚠️ 连接与认证正常，但平台暂时没有该模型的可用推理资源")
                    .append("（No candidate worker）。\n模型可能未对您的账号开放、或平台正忙。")
                    .append("\n建议：稍后重试，或更换模型（如 hepai/deepseek-v4-flash）。")
            } else {
                report.append("\n⚠️ 推理测试未通过：").append(msg)
            }
        }
        return report.toString()
    }

    /** GET /models：验证网络与认证。5xx 自动重试。 */
    private suspend fun testModelsEndpoint(): String {
        var attempt = 0
        while (true) {
            attempt++
            try {
                return executeModelsRequest()
            } catch (e: ServerErrorException) {
                if (attempt >= 3) throw e
                delay(1500L)
            }
        }
    }

    private suspend fun executeModelsRequest(): String = suspendCancellableCoroutine { cont ->
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/models")
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) {
                    cont.resumeWithException(IOException("网络不可达：${e.message ?: "连接失败"}"))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val count = runCatching {
                            JSONObject(responseBody).getJSONArray("data").length()
                        }.getOrDefault(-1)
                        if (cont.isActive) {
                            cont.resume(
                                if (count >= 0) "连接成功，服务端返回 $count 个模型" else "连接成功"
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

    /** 最小 chat 请求：验证推理资源。 */
    private suspend fun testChatEndpoint(): String {
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
            put("stream", true)
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

    private fun parseResponse(responseBody: String): ChatReply {
        try {
            val jsonObject = JSONObject(responseBody)
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() == 0) {
                throw IOException("No content in response")
            }

            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message")
                ?: throw IOException("Malformed response: missing message")

            val rawContent = message.optString("content", "")

            // 思考内容：优先 reasoning_content 字段（DeepSeek 等），其次 <think>…</think> 标签
            val reasoningField = message.optString("reasoning_content", "")
            val reasoning = if (reasoningField.isNotBlank()) {
                reasoningField.trim()
            } else {
                extractThinkContent(rawContent)
            }

            // 正文：剥离 <think> 标签
            val content = stripThinking(rawContent).trim()
            if (content.isEmpty() && reasoning.isEmpty()) {
                throw IOException("Empty content in response")
            }
            return ChatReply(content = content, reasoning = reasoning)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }

    /** 提取 <think>…</think> 之间的思考内容；没有则返回空串。 */
    private fun extractThinkContent(content: String): String {
        val match = Regex("(?s)<think>(.*?)</think>").find(content) ?: return ""
        return match.groupValues[1].trim()
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
