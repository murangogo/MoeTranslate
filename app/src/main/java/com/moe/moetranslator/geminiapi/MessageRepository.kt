package com.moe.moetranslator.geminiapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MessageRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getMessages()
        .flowOn(Dispatchers.IO)  // 指定在IO线程执行数据库操作

    suspend fun insert(chatMessage: ChatMessage) {
        chatMessageDao.insert(chatMessage)
    }

    suspend fun deleteAll() {
        chatMessageDao.deleteAll()
    }

    suspend fun appendContentByTimestamp(timestamp: Long, additionalContent: String) {
        chatMessageDao.appendContentByTimestamp(timestamp, additionalContent)
    }
}