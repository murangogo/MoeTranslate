package com.moe.moetranslator.claudeapi

import androidx.lifecycle.LiveData

class MessageRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: LiveData<List<ChatMessage>> = chatMessageDao.getMessages()

    suspend fun insert(chatMessage: ChatMessage) {
        chatMessageDao.insert(chatMessage)
    }

    suspend fun deleteAll() {
        chatMessageDao.deleteAll()
    }
}