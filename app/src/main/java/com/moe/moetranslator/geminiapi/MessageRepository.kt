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

package com.moe.moetranslator.geminiapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/** 会话摘要：用于会话切换列表。 */
data class ChatSessionInfo(
    val sessionId: Long,
    val title: String,       // 首条消息前若干字
    val lastTimestamp: Long,
)

class MessageRepository(private val chatMessageDao: ChatMessageDao) {

    /** 指定会话的消息流。 */
    fun getMessages(sessionId: Long): Flow<List<ChatMessage>> =
        chatMessageDao.getMessages(sessionId).flowOn(Dispatchers.IO)

    // 获取指定会话全部消息的List
    suspend fun getAllMessagesList(sessionId: Long): List<ChatMessage> {
        return chatMessageDao.getAllMessagesList(sessionId)
    }

    // 获取最近的消息
    suspend fun getRecentMessages(limit: Int): List<ChatMessage> {
        return chatMessageDao.getRecentMessages(limit)
    }

    suspend fun insert(chatMessage: ChatMessage): Long {
        return chatMessageDao.insert(chatMessage)
    }

    suspend fun deleteAll() {
        chatMessageDao.deleteAll()
    }

    /** 删除指定会话的全部消息。 */
    suspend fun deleteSession(sessionId: Long) {
        chatMessageDao.deleteSession(sessionId)
    }

    /** 所有会话摘要（按最近消息倒序）。 */
    suspend fun getSessions(): List<ChatSessionInfo> {
        val ids = chatMessageDao.getAllSessionIds()
        return ids.mapNotNull { id ->
            val first = chatMessageDao.getFirstMessage(id) ?: return@mapNotNull null
            val last = chatMessageDao.getLastMessage(id) ?: first
            ChatSessionInfo(
                sessionId = id,
                title = first.content.replace("\n", " ").take(20).ifBlank { "..." },
                lastTimestamp = last.timestamp,
            )
        }
    }

    suspend fun getMessageById(messageId: Long): ChatMessage? {
        return chatMessageDao.getMessageById(messageId)
    }

    suspend fun updateMessageContent(messageId: Long, content: String) {
        chatMessageDao.updateMessageContent(messageId, content)
    }

    suspend fun updateMessageWithReasoning(messageId: Long, content: String, reasoning: String) {
        chatMessageDao.updateMessageWithReasoning(messageId, content, reasoning)
    }

    suspend fun appendContentById(messageId: Long, additionalContent: String) {
        chatMessageDao.appendContentById(messageId, additionalContent)
    }

    suspend fun clearContentById(messageId: Long) {
        chatMessageDao.clearContentById(messageId)
    }
}
