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