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

class MessageRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getMessages()

    // 获取全部消息的List
    suspend fun getAllMessagesList(): List<ChatMessage> {
        return chatMessageDao.getAllMessagesList()
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


    suspend fun getMessageById(messageId: Long): ChatMessage? {
        return chatMessageDao.getMessageById(messageId)
    }

    suspend fun updateMessageContent(messageId: Long, content: String) {
        chatMessageDao.updateMessageContent(messageId, content)
    }

    suspend fun appendContentById(messageId: Long, additionalContent: String) {
        chatMessageDao.appendContentById(messageId, additionalContent)
    }

    suspend fun clearContentById(messageId: Long) {
        chatMessageDao.clearContentById(messageId)
    }
}