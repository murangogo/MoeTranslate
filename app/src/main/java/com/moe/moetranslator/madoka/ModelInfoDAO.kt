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