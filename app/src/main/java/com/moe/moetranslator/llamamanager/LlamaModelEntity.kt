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

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * llama.cpp 本地 GGUF 模型的元数据。
 *
 * 文件本身存在 Context.filesDir/llamacppmodels/<fileName>；该表只存元数据。
 *
 * 设计约束：
 *   - fileName 在表内唯一（同名文件二次导入需先删除旧的，由 UI 处理）。
 *   - 任何时刻最多一行 isActive=true（由 DAO setActive 事务保证）。
 *   - systemPromptOverride / userPromptOverride 为每模型自定义提示词；为 null 时翻译器
 *     回退到 SharedPreferences 全局提示词。
 *   - enableThinking / temperature / maxTokens 为每模型推理参数，由模型列表的齿轮弹窗配置；
 *     默认值与 Constants.defaultLlama* 保持一致。
 */
@Entity(
    tableName = "llama_models",
    indices = [Index(value = ["fileName"], unique = true)]
)
data class LlamaModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val displayName: String,
    val sizeBytes: Long,
    val md5: String?,
    val source: String,
    val downloadUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val systemPromptOverride: String? = null,
    val userPromptOverride: String? = null,
    val enableThinking: Boolean = false,
    val temperature: Float = 0.2f,
    val maxTokens: Int = 512,
) {
    companion object {
        const val SOURCE_PRESET = "preset"
        const val SOURCE_IMPORT = "import"
    }
}
