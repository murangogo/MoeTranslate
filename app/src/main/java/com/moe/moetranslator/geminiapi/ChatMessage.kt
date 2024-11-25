package com.moe.moetranslator.geminiapi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 定义消息实体(Entity)
 */

@Entity(tableName = "chat_messages_table")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "sender")
    val sender: Int  // 1表示AI，2表示用户
)