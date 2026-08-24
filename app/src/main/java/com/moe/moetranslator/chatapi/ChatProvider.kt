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

/**
 * AI 对话的统一抽象：聊天页只依赖本接口，新增模型提供商只需实现本接口
 * 并在 ChatwithGemini 的 buildChatProvider 中登记，无需改动界面调用链。
 */
interface ChatProvider {

    /**
     * 携带历史多轮消息请求 AI 回复。流式提供商会通过 [onChunk] 逐段回调正文增量内容；
     * 非流式提供商会一次性回调完整正文。协程被取消时实现应尽快中止网络请求
     * （OkHttp 调用会被 cancel、流式 Flow 会停止收集）。
     *
     * @param history   本次输入之前的完整对话历史（时间正序）
     * @param userInput 用户本次输入的内容
     * @param onChunk   正文增量回调（可能被多次调用；线程由实现保证为主线程或任意线程均可，
     *                  调用方需自行保证线程安全）
     * @return AI 回复：正文 + 思考内容（推理模型在正文之前的思考过程，无则空串）
     */
    suspend fun chat(history: List<ChatTurn>, userInput: String, onChunk: (String) -> Unit): ChatReply

    /**
     * 测试当前配置的连通性：先验证网络与认证（模型列表端点），再探测推理资源可用性。
     *
     * @return 人类可读的测试报告（可多行）
     * @throws Exception 网络不可达/认证失败等，异常信息应可直接展示给用户
     */
    suspend fun testConnection(): String

    /** 释放资源。默认空实现，各提供商按需覆写。 */
    fun release() {}
}

/** 一轮 AI 回复：正文 + 思考内容。 */
data class ChatReply(
    val content: String,
    val reasoning: String = "",
)

/** 一轮对话消息。 */
data class ChatTurn(val role: String, val content: String) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
    }
}
