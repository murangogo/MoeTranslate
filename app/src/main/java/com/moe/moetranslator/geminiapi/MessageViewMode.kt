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

    // 获取全部消息的List
    suspend fun getAllMessagesList(): List<ChatMessage> {
        return repository.getAllMessagesList()
    }

    // 获取特定数量的最近聊天记录
    suspend fun getRecentMessages(limit: Int = 10): List<ChatMessage> {
        return repository.getRecentMessages(limit)
    }

    suspend fun insert(chatMessage: ChatMessage): Long {
        return repository.insert(chatMessage)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun appendContentById(messageId: Long, additionalContent: String) = viewModelScope.launch {
        repository.appendContentById(messageId, additionalContent)
    }

    fun clearMessageById(messageId: Long) = viewModelScope.launch {
        repository.clearContentById(messageId)
    }
}
