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
 * 并在 [ChatProviderFactory] 中登记，无需改动界面调用链。
 */
interface ChatProvider {

    /**
     * 携带历史多轮消息请求 AI 回复。
     *
     * @param history   本次输入之前的完整对话历史（时间正序）
     * @param userInput 用户本次输入的内容
     * @return AI 回复文本
     */
    suspend fun chat(history: List<ChatTurn>, userInput: String): String

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
