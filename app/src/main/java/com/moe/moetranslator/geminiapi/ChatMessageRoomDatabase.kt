package com.moe.moetranslator.geminiapi

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class ChatMessageRoomDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatMessageRoomDatabase? = null

        fun getDatabase(context: Context): ChatMessageRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatMessageRoomDatabase::class.java,
                    "chat_message_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}