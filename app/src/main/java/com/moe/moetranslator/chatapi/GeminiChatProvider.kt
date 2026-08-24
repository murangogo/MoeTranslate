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

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.moe.moetranslator.geminiapi.GeminiModelFactory

/**
 * Gemini 聊天提供商：封装 Google generativeai SDK，流式接收回复并拼接为整段文本。
 */
class GeminiChatProvider(
    private val modelName: String,
    private val apiKey: String,
    private val systemInstruction: String? = null,
) : ChatProvider {

    private var model: GenerativeModel? = null

    override suspend fun chat(
        history: List<ChatTurn>,
        userInput: String,
        onChunk: (String) -> Unit,
    ): String {
        if (model == null) {
            // 系统提示词（可选）：留空则不设置
            val instruction = systemInstruction
                ?.takeIf { it.isNotBlank() }
                ?.let { content("system") { text(it) } }
            model = GeminiModelFactory.createGeminiModel(modelName, apiKey, instruction)
        }

        // 将历史消息转换为 Gemini API 格式（AI 消息在 Gemini 中对应 model 角色）
        val chatHistory = mutableListOf<Content>()
        history.forEach { turn ->
            val role = if (turn.role == ChatTurn.ROLE_ASSISTANT) "model" else ChatTurn.ROLE_USER
            chatHistory.add(content(role) { text(turn.content) })
        }

        val chat = model!!.startChat(history = chatHistory)

        val sb = StringBuilder()
        chat.sendMessageStream(userInput).collect { chunk ->
            chunk.text?.let {
                sb.append(it)
                onChunk(it)
            }
        }
        return sb.toString().trim()
    }

    override suspend fun testConnection(): String {
        val model = model ?: GeminiModelFactory.createGeminiModel(
            modelName, apiKey,
            systemInstruction?.takeIf { it.isNotBlank() }?.let { content("system") { text(it) } }
        )
        val response = model.generateContent("ping")
        val text = response.text?.trim().orEmpty()
        return if (text.isNotEmpty()) {
            "连接成功，模型回复：${text.take(30)}"
        } else {
            "连接成功（模型返回空内容）"
        }
    }

    override fun release() {
        model = null
    }
}
