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

    suspend fun getMessageById(messageId: Long): ChatMessage? {
        return repository.getMessageById(messageId)
    }

    fun updateMessageContent(messageId: Long, content: String) = viewModelScope.launch {
        repository.updateMessageContent(messageId, content)
    }

    fun appendContentById(messageId: Long, additionalContent: String) = viewModelScope.launch {
        repository.appendContentById(messageId, additionalContent)
    }

    fun clearMessageById(messageId: Long) = viewModelScope.launch {
        repository.clearContentById(messageId)
    }
}
