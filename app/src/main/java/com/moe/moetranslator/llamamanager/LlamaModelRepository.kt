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
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.flow.Flow

/**
 * 单一入口：UI 层只跟 Repository 打交道，不直接碰 DAO / SharedPreferences。
 *
 * 关于 active 模型：DAO 的 isActive 是 UI 层（响应式列表）的真值；同时镜像写一份
 * 文件名到 SharedPreferences 的 Llama_Model_Name，给运行时（FloatingBallService）做
 * 同步的快路径读取，避免在服务启动时阻塞等 DB。
 */
class LlamaModelRepository private constructor(
    private val dao: LlamaModelDao,
    private val prefs: CustomPreference,
) {

    fun observeAll(): Flow<List<LlamaModelEntity>> = dao.observeAll()

    suspend fun findByFileName(fileName: String): LlamaModelEntity? =
        dao.findByFileName(fileName)

    suspend fun getActive(): LlamaModelEntity? = dao.getActive()

    suspend fun insert(entity: LlamaModelEntity): Long = dao.upsert(entity)

    suspend fun setActive(id: Long) {
        dao.setActive(id)
        val entity = dao.listAll().firstOrNull { it.id == id }
        prefs.setString(PREF_ACTIVE_FILE_NAME, entity?.fileName ?: "")
    }

    /**
     * 删除模型：先 DAO 删行，再删磁盘文件；若被删的是 active，则清掉 SharedPreferences 的镜像。
     * 不要顺手把别的模型自动置为 active —— 让用户显式选择。
     */
    suspend fun delete(context: Context, entity: LlamaModelEntity): Boolean {
        val wasActive = entity.isActive
        val rows = dao.deleteById(entity.id)
        val file = LlamaModelStorage.modelFile(context, entity.fileName)
        if (file.exists()) file.delete()
        if (wasActive) prefs.setString(PREF_ACTIVE_FILE_NAME, "")
        return rows > 0
    }

    companion object {
        const val PREF_ACTIVE_FILE_NAME = "Llama_Model_Name"

        @Volatile
        private var INSTANCE: LlamaModelRepository? = null

        fun getInstance(context: Context): LlamaModelRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LlamaModelRepository(
                    dao = LlamaModelDatabase.getDatabase(context).llamaModelDao(),
                    prefs = CustomPreference.getInstance(context),
                ).also { INSTANCE = it }
            }
        }
    }
}
