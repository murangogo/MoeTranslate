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