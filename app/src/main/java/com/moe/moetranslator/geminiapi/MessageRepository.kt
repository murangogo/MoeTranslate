package com.moe.moetranslator.geminiapi

import androidx.lifecycle.LiveData

class MessageRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: LiveData<List<ChatMessage>> = chatMessageDao.getMessages()

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