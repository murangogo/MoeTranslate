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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 创建DAO接口
 */

@Dao
interface ChatMessageDao {
    // 全部消息的Flow，按时间和id正序
    @Query("SELECT * FROM chat_messages_table ORDER BY timestamp ASC, id ASC")
    fun getMessages(): Flow<List<ChatMessage>>

    // 全部消息的List，按时间和id正序
    @Query("SELECT * FROM chat_messages_table ORDER BY timestamp ASC, id ASC")
    suspend fun getAllMessagesList(): List<ChatMessage>

    // 获取最近的n条消息，按时间和id倒序
    @Query("SELECT * FROM chat_messages_table ORDER BY timestamp DESC, id DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatMessage: ChatMessage): Long

    @Query("DELETE FROM chat_messages_table")
    suspend fun deleteAll()

    // 根据id获取消息
    @Query("SELECT * FROM chat_messages_table WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): ChatMessage?

    // 更新整条消息
    @Query("UPDATE chat_messages_table SET content = :content WHERE id = :messageId")
    suspend fun updateMessageContent(messageId: Long, content: String)

    // 根据id追加消息内容
    @Query("UPDATE chat_messages_table SET content = content || :additionalContent WHERE id = :messageId")
    suspend fun appendContentById(messageId: Long, additionalContent: String)

    // 根据id清除消息内容
    @Query("UPDATE chat_messages_table SET content = '' WHERE id = :messageId")
    suspend fun clearContentById(messageId: Long)
}