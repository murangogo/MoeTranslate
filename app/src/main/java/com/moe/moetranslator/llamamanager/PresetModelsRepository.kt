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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class PresetModelInfo(
    val fileName: String,
    val downloadUrl: String,
    val fileSize: Long,
    val md5Checksum: String,
)

/**
 * 从 https://repo.azuki.top/PresetModels/presetmodelsinfo.json 拉取预设模型列表。
 *
 * 与 NLLBDownloadFragment.fetchDownloadInfo 同款：直接 OkHttp 同步阻塞调用包在
 * Dispatchers.IO 里。复杂度不值得引入 Retrofit/Moshi/序列化等额外依赖。
 */
class PresetModelsRepository(
    private val client: OkHttpClient = defaultClient,
) {

    suspend fun fetch(): List<PresetModelInfo> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(PRESET_URL).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} ${response.message}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response body")
            parse(body)
        }
    }

    private fun parse(json: String): List<PresetModelInfo> {
        val root = JSONObject(json)
        val arr = root.getJSONArray("files")
        return List(arr.length()) { i ->
            val obj = arr.getJSONObject(i)
            PresetModelInfo(
                fileName = obj.getString("file_name"),
                downloadUrl = obj.getString("download_url"),
                fileSize = obj.getString("file_size").toLong(),
                md5Checksum = obj.getString("md5_checksum"),
            )
        }
    }

    companion object {
        private const val PRESET_URL = "https://repo.azuki.top/PresetModels/presetmodelsinfo.json"

        private val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
