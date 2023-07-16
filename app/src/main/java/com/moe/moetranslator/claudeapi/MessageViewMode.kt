package com.moe.moetranslator.claudeapi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
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
}
