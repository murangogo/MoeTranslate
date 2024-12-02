package com.moe.moetranslator.madoka

import android.content.Context
import android.graphics.ColorSpace.Model
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Live2DModel::class, Live2DExpression::class, Live2DMotion::class],
    version = 1,
    exportSchema = false
)
abstract class ModelInfoRoomDatabase: RoomDatabase() {

    abstract fun ModelInfoDAO(): ModelInfoDAO

    companion object {
        @Volatile
        private var INSTANCE: ModelInfoRoomDatabase? = null

        fun getDatabase(context: Context): ModelInfoRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ModelInfoRoomDatabase::class.java,
                    "live2d_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}