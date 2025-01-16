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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelInfoDAO {
    @Query("SELECT * FROM live2d_models ORDER BY createTime DESC")
    fun getAllModels(): Flow<List<Live2DModel>>

    @Query("SELECT * FROM live2d_expressions WHERE modelId = :modelId")
    fun getExpressionsForModel(modelId: String): Flow<List<Live2DExpression>>

    @Query("SELECT * FROM live2d_motions WHERE modelId = :modelId")
    fun getMotionsForModel(modelId: String): Flow<List<Live2DMotion>>

    @Query("DELETE FROM live2d_models WHERE modelId = :modelId")
    suspend fun deleteModel(modelId: String)

    @Insert
    suspend fun insertModel(model: Live2DModel)

    @Insert
    suspend fun insertExpressions(expressions: List<Live2DExpression>)

    @Insert
    suspend fun insertMotions(motions: List<Live2DMotion>)

    @Query("UPDATE live2d_models SET displayName = :newName WHERE modelId = :modelId")
    suspend fun updateModelName(modelId: String, newName: String)

    @Query("UPDATE live2d_expressions SET displayName = :newName WHERE id = :expressionId")
    suspend fun updateExpressionName(expressionId: Long, newName: String)

    @Query("UPDATE live2d_motions SET displayName = :newName WHERE id = :motionId")
    suspend fun updateMotionName(motionId: Long, newName: String)
}