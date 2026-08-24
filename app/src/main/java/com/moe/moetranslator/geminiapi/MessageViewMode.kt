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
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MessageViewModel(application: Application) : ViewModel() {

    private lateinit var repository: MessageRepository
    private val prefs: CustomPreference

    // 当前激活的会话 id（持久化到 prefs，重启后保持）
    private val _activeSessionId = MutableStateFlow(
        CustomPreference.getInstance(application).getLong("Chat_Active_Session", 0L)
    )
    val activeSessionId = _activeSessionId.asStateFlow()

    // 当前会话的消息流：切换会话时自动切换到新会话的数据
    val allMessages = _activeSessionId
        .flatMapLatest { repository.getMessages(it) }
        .catch { e -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val messagesDao = ChatMessageRoomDatabase.getDatabase(application).chatMessageDao()
        repository = MessageRepository(messagesDao)
        prefs = CustomPreference.getInstance(application)
    }

    // 获取当前会话全部消息的List
    suspend fun getAllMessagesList(): List<ChatMessage> {
        return repository.getAllMessagesList(_activeSessionId.value)
    }

    // 获取特定数量的最近聊天记录
    suspend fun getRecentMessages(limit: Int = 10): List<ChatMessage> {
        return repository.getRecentMessages(limit)
    }

    suspend fun insert(chatMessage: ChatMessage): Long {
        return repository.insert(chatMessage)
    }

    /** 删除当前会话全部消息。 */
    fun deleteCurrentSession() = viewModelScope.launch {
        repository.deleteSession(_activeSessionId.value)
    }

    /** 新建会话并切换过去。 */
    fun newSession() {
        switchSession(System.currentTimeMillis())
    }

    /** 切换当前会话。 */
    fun switchSession(sessionId: Long) {
        prefs.setLong("Chat_Active_Session", sessionId)
        _activeSessionId.value = sessionId
    }

    /** 所有会话摘要。 */
    suspend fun getSessions(): List<ChatSessionInfo> {
        return repository.getSessions()
    }

    /** 删除指定会话；若删除的是当前会话则回到默认会话 0。 */
    fun deleteSession(sessionId: Long) = viewModelScope.launch {
        repository.deleteSession(sessionId)
        if (_activeSessionId.value == sessionId) {
            switchSession(0L)
        }
    }

    suspend fun getMessageById(messageId: Long): ChatMessage? {
        return repository.getMessageById(messageId)
    }

    fun updateMessageContent(messageId: Long, content: String) = viewModelScope.launch {
        repository.updateMessageContent(messageId, content)
    }

    /** AI 回复完成：一次性写入正文与思考内容。 */
    fun updateMessageWithReasoning(messageId: Long, content: String, reasoning: String) =
        viewModelScope.launch {
            repository.updateMessageWithReasoning(messageId, content, reasoning)
        }

    fun appendContentById(messageId: Long, additionalContent: String) = viewModelScope.launch {
        repository.appendContentById(messageId, additionalContent)
    }

    fun clearMessageById(messageId: Long) = viewModelScope.launch {
        repository.clearContentById(messageId)
    }
}
