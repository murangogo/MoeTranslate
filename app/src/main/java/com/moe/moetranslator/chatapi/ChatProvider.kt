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
     * 携带历史多轮消息请求 AI 回复。流式提供商会通过 [onChunk] 逐段回调增量内容；
     * 非流式提供商会一次性回调完整回答。协程被取消时实现应尽快中止网络请求
     * （OkHttp 调用会被 cancel、流式 Flow 会停止收集）。
     *
     * @param history   本次输入之前的完整对话历史（时间正序）
     * @param userInput 用户本次输入的内容
     * @param onChunk   增量内容回调（可能被多次调用；线程由实现保证为主线程或任意线程均可，
     *                  调用方需自行保证线程安全）
     * @return AI 回复完整文本
     */
    suspend fun chat(history: List<ChatTurn>, userInput: String, onChunk: (String) -> Unit): String

    /**
     * 测试当前配置的连通性：向服务端发送一个最小请求。
     *
     * @return 人类可读的成功信息（如模型数量）
     * @throws Exception 连接失败/鉴权失败/超时等，异常信息应可直接展示给用户
     */
    suspend fun testConnection(): String

    /** 释放资源。默认空实现，各提供商按需覆写。 */
    fun release() {}
}

/** 一轮对话消息。 */
data class ChatTurn(val role: String, val content: String) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
    }
}
