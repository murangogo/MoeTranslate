package com.moe.moetranslator.geminiapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class MessageRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getMessages()
        .flowOn(Dispatchers.IO)  // 指定在IO线程执行数据库操作

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

    suspend fun appendContentById(messageId: Long, additionalContent: String) {
        chatMessageDao.appendContentById(messageId, additionalContent)
    }

    suspend fun clearContentById(messageId: Long) {
        chatMessageDao.clearContentById(messageId)
    }
}