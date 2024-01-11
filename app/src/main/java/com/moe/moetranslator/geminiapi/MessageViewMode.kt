package com.moe.moetranslator.geminiapi

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : ViewModel() {

    private val repository: MessageRepository
    val allMessages: LiveData<List<ChatMessage>>

    init {
        val messagesDao = ChatMessageRoomDatabase.getDatabase(application).chatMessageDao()
        repository = MessageRepository(messagesDao)
        allMessages = repository.allMessages
    }

    fun insert(chatMessage: ChatMessage) = viewModelScope.launch {
        repository.insert(chatMessage)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun appendContentByTimestamp(timestamp: Long, additionalContent: String) = viewModelScope.launch {
        repository.appendContentByTimestamp(timestamp, additionalContent)
    }
}
