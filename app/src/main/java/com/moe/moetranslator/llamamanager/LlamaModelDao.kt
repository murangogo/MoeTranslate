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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LlamaModelDao {

    @Query("SELECT * FROM llama_models ORDER BY createdAt DESC")
    abstract fun observeAll(): Flow<List<LlamaModelEntity>>

    @Query("SELECT * FROM llama_models ORDER BY createdAt DESC")
    abstract suspend fun listAll(): List<LlamaModelEntity>

    @Query("SELECT * FROM llama_models WHERE isActive = 1 LIMIT 1")
    abstract suspend fun getActive(): LlamaModelEntity?

    @Query("SELECT * FROM llama_models WHERE fileName = :fileName LIMIT 1")
    abstract suspend fun findByFileName(fileName: String): LlamaModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: LlamaModelEntity): Long

    @Query("DELETE FROM llama_models WHERE id = :id")
    abstract suspend fun deleteById(id: Long): Int

    @Query("UPDATE llama_models SET isActive = 0")
    abstract suspend fun clearAllActive()

    @Query("UPDATE llama_models SET isActive = 1 WHERE id = :id")
    abstract suspend fun markActive(id: Long): Int

    @Transaction
    open suspend fun setActive(id: Long) {
        clearAllActive()
        markActive(id)
    }

    @Query("UPDATE llama_models SET displayName = :name WHERE id = :id")
    abstract suspend fun renameDisplay(id: Long, name: String)

    @Query(
        "UPDATE llama_models SET systemPromptOverride = :system, userPromptOverride = :user, " +
            "enableThinking = :enableThinking, temperature = :temperature, maxTokens = :maxTokens WHERE id = :id"
    )
    abstract suspend fun updateConfig(
        id: Long, system: String?, user: String?,
        enableThinking: Boolean, temperature: Float, maxTokens: Int,
    )

    @Query("SELECT * FROM llama_models WHERE id = :id LIMIT 1")
    abstract suspend fun findById(id: Long): LlamaModelEntity?
}
