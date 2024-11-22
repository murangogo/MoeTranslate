package com.moe.moetranslator.geminiapi

import androidx.lifecycle.LiveData
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
    @Query("SELECT * FROM chat_message_table ORDER BY timestamp ASC")
    fun getMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatMessage: ChatMessage)

    @Query("DELETE FROM chat_message_table")
    suspend fun deleteAll()

    @Query("UPDATE chat_message_table SET content = content || :additionalContent WHERE timestamp = :timestamp")
    suspend fun appendContentByTimestamp(timestamp: Long, additionalContent: String)
}