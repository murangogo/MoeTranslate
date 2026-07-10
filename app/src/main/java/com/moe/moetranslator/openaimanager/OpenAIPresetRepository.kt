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

import android.content.Context
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.Constants.defaultSystemPrompt
import com.moe.moetranslator.utils.Constants.defaultUserPrompt
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.flow.Flow

/**
 * 单一入口：UI 层只跟 Repository 打交道，不直接碰 DAO / SharedPreferences。
 *
 * 关于 active 预设：DAO 的 isActive 是 UI 层（响应式列表）的真值；同时把激活预设的 7 个字段
 * 镜像写回现有的 OpenAI_* SharedPreferences 键，给运行时（FloatingBallService 构造
 * OpenAITranslation、TranslateFragment 的未配置校验）做同步快读——这两处因此无需改动。
 */
class OpenAIPresetRepository private constructor(
    private val dao: OpenAIPresetDao,
    private val prefs: CustomPreference,
) {

    fun observeAll(): Flow<List<OpenAIPresetEntity>> = dao.observeAll()

    suspend fun getActive(): OpenAIPresetEntity? = dao.getActive()

    suspend fun findById(id: Long): OpenAIPresetEntity? = dao.findById(id)

    /**
     * 新增一套预设。若当前没有任何激活预设（首套 / 之前全被删光），自动把它设为激活，
     * 保证「只要库里有预设，就有一套在用」，避免用户新建后还得手动再选一次。
     */
    suspend fun insert(entity: OpenAIPresetEntity): Long {
        val id = dao.upsert(entity)
        if (dao.getActive() == null) setActive(id)
        return id
    }

    /** 编辑已有预设（entity 携带原 id / isActive / createdAt）。若改的是当前激活项，重新镜像。 */
    suspend fun update(entity: OpenAIPresetEntity) {
        dao.upsert(entity)
        val active = dao.getActive()
        if (active?.id == entity.id) mirrorActiveToPrefs(active)
    }

    suspend fun setActive(id: Long) {
        dao.setActive(id)
        mirrorActiveToPrefs(dao.findById(id))
    }

    /**
     * 删除预设：删行后，若删的是激活项则清空镜像（不自动改选其它，让用户显式选择，
     * 与 llamamanager 一致）。清空后 OpenAI_Api_Key 为空 → 运行时命中既有的「API 未配置」提示。
     */
    suspend fun delete(entity: OpenAIPresetEntity): Boolean {
        val wasActive = entity.isActive
        val rows = dao.deleteById(entity.id)
        if (wasActive) mirrorActiveToPrefs(null)
        return rows > 0
    }

    /**
     * 一次性迁移：旧版本把单套聚合AI配置直接存在 OpenAI_* 键里。首次进入新版本时，
     * 若这些键里确有配置（ApiKey 非空），就据此建一条预设并设为激活；无论有无都置迁移标志，避免重复迁移。
     * insert 会在库空时自动激活，故迁移后该预设即为激活项；镜像值与旧键相同，幂等安全。
     */
    suspend fun migrateLegacyIfNeeded(context: Context) {
        if (prefs.getBoolean(PREF_MIGRATED, false)) return
        val oldApiKey = prefs.getString("OpenAI_Api_Key", "")
        if (oldApiKey.isNotBlank()) {
            val model = prefs.getString("OpenAI_Model_Name", "")
            val name = model.ifBlank { context.getString(R.string.openai_preset_default_name) }
            insert(
                OpenAIPresetEntity(
                    displayName = name,
                    apiKey = oldApiKey,
                    baseUrl = prefs.getString("OpenAI_Base_Url", ""),
                    modelName = model,
                    systemPrompt = prefs.getString("OpenAI_System_Prompt", defaultSystemPrompt),
                    userPrompt = prefs.getString("OpenAI_User_Prompt", defaultUserPrompt),
                    temperature = prefs.getString("OpenAI_Temperature", ""),
                    extraParams = prefs.getString("OpenAI_Extra_Params", ""),
                )
            )
        }
        prefs.setBoolean(PREF_MIGRATED, true)
    }

    /** 激活预设在 prefs 里的镜像，统一在这里写。entity 为 null（无激活）时清空到会触发未配置提示的状态。 */
    private fun mirrorActiveToPrefs(entity: OpenAIPresetEntity?) {
        prefs.setString("OpenAI_Api_Key", entity?.apiKey ?: "")
        prefs.setString("OpenAI_Base_Url", entity?.baseUrl ?: "")
        prefs.setString("OpenAI_Model_Name", entity?.modelName ?: "")
        prefs.setString("OpenAI_System_Prompt", entity?.systemPrompt ?: defaultSystemPrompt)
        prefs.setString("OpenAI_User_Prompt", entity?.userPrompt ?: defaultUserPrompt)
        prefs.setString("OpenAI_Temperature", entity?.temperature ?: "")
        prefs.setString("OpenAI_Extra_Params", entity?.extraParams ?: "")
    }

    companion object {
        const val PREF_MIGRATED = "OpenAI_Presets_Migrated"

        @Volatile
        private var INSTANCE: OpenAIPresetRepository? = null

        fun getInstance(context: Context): OpenAIPresetRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OpenAIPresetRepository(
                    dao = OpenAIPresetDatabase.getDatabase(context).openAIPresetDao(),
                    prefs = CustomPreference.getInstance(context),
                ).also { INSTANCE = it }
            }
        }
    }
}
