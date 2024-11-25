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
    // 全部消息的Flow
    @Query("SELECT * FROM chat_messages_table ORDER BY timestamp ASC")
    fun getMessages(): Flow<List<ChatMessage>>

    // 全部消息的List
    @Query("SELECT * FROM chat_messages_table ORDER BY timestamp ASC")
    suspend fun getAllMessagesList(): List<ChatMessage>

    // 获取最近的n条消息
    @Query("SELECT * FROM chat_messages_table ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatMessage: ChatMessage): Long

    @Query("DELETE FROM chat_messages_table")
    suspend fun deleteAll()

    @Query("UPDATE chat_messages_table SET content = content || :additionalContent WHERE id = :messageId")
    suspend fun appendContentById(messageId: Long, additionalContent: String)

    @Query("UPDATE chat_messages_table SET content = '' WHERE id = :messageId")
    suspend fun clearContentById(messageId: Long)
}