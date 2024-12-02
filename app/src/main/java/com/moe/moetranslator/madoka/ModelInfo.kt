package com.moe.moetranslator.madoka

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "live2d_models")
data class Live2DModel(
    @PrimaryKey val modelId: String, // 如 "model_1"
    val displayName: String,         // 用户定义的名称
    val createTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "live2d_expressions",
    foreignKeys = [ForeignKey(
        entity = Live2DModel::class,
        parentColumns = ["modelId"],
        childColumns = ["modelId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("modelId")]  // 添加索引提升查询性能
)
data class Live2DExpression(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: String,
    val fileName: String,    // 实际文件名
    val displayName: String  // 用户定义的名称
)

@Entity(
    tableName = "live2d_motions",
    foreignKeys = [ForeignKey(
        entity = Live2DModel::class,
        parentColumns = ["modelId"],
        childColumns = ["modelId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("modelId")]  // 添加索引提升查询性能
)
data class Live2DMotion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: String,
    val fileName: String,    // 实际文件名
    val displayName: String  // 用户定义的名称
)