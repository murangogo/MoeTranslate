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

package com.moe.moetranslator.openaimanager

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一套「聚合AI翻译」(OpenAI 兼容接口) 的预设配置。
 *
 * 设计约束（对照 llamamanager.LlamaModelEntity）：
 *   - 任何时刻最多一行 isActive=true（由 DAO setActive 事务保证）。
 *   - 字段与旧版单套配置的 7 个 SharedPreferences 键一一对应；激活项由 Repository
 *     镜像回这些 OpenAI_* 键，运行时（FloatingBallService / TranslateFragment）继续读镜像。
 *   - temperature 存字符串，"" 表示请求时不发送 temperature，语义与旧 OpenAI_Temperature 一致。
 *   - extraParams 存 JSON 字符串，编码同旧 OpenAI_Extra_Params（OpenAITranslation.encode/decodeExtraParams）。
 *   - displayName 允许重复（用户可给同一模型建多套不同提示词的预设），故不设唯一索引。
 */
@Entity(tableName = "openai_presets")
data class OpenAIPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val apiKey: String,
    val baseUrl: String,
    val modelName: String,
    val systemPrompt: String,
    val userPrompt: String,
    val temperature: String,
    val extraParams: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
