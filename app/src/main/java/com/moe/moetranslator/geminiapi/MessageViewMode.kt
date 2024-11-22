package com.moe.moetranslator.geminiapi

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : ViewModel() {

    private val repository: MessageRepository
    private val _allMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val allMessages = _allMessages.asStateFlow()

    init {
        val messagesDao = ChatMessageRoomDatabase.getDatabase(application).chatMessageDao()
        repository = MessageRepository(messagesDao)
        // 在viewModelScope中收集Flow
        viewModelScope.launch {
            repository.allMessages
                .catch { e ->
                    e.printStackTrace()
                }
                .collect {
                    _allMessages.value = it
                }
        }
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
