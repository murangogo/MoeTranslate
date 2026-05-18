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

package com.moe.moetranslator.llamamanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LlamaModelEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LlamaModelDatabase : RoomDatabase() {

    abstract fun llamaModelDao(): LlamaModelDao

    companion object {
        @Volatile
        private var INSTANCE: LlamaModelDatabase? = null

        fun getDatabase(context: Context): LlamaModelDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LlamaModelDatabase::class.java,
                    "llama_models.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
