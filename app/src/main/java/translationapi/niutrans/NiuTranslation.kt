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

package translationapi.niutrans

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 小牛翻译的文本翻译
 * 请求方法：POST
 * URL没有查询参数
 * 请求头显式设置了Content-Type为application/json
 * 请求参数放在请求体中，以json形式发送
 * 没有签名机制
 */

class NiuTranslation(private val apiKey: String) : TranslationTextAPI {
    private var currentTask: Thread? = null
    private val API_HOST = "https://api.niutrans.com/NiuTransServer/translation"
    private val SOCKET_TIMEOUT = 10L // 10秒
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // 创建OkHttpClient实例
    private val client = OkHttpClient.Builder()
        .connectTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .build()

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        currentTask = Thread {
            try {
                val result = translate(text, sourceLanguage, targetLanguage)
                callback(TranslationResult.Success(result))
            } catch (e: Exception) {
                callback(TranslationResult.Error(e))
            }
        }.apply { start() }
    }

    private fun translate(text: String, from: String, to: String): String {
        // 构建JSON请求体
        val jsonBody = JSONObject().apply {
            put("from", modifyTargetLanguage(from))
            put("to", modifyTargetLanguage(to))
            put("apikey", apiKey)
            put("src_text", text)
        }.toString()

        Log.d("NIUTRANS", "Request: $jsonBody")

        // 创建请求
        val request = Request.Builder()
            .url(API_HOST)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response ${response.code}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")

            Log.d("NIUTRANS", "Response: $responseBody")
            return parseResponse(responseBody)
        }
    }

    private fun modifyTargetLanguage(to: String): String = when (to) {
        "zh-TW" -> "cht"
        else -> to
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)

            // 检查是否有错误
            if (jsonObject.has("error_code")) {
                val errorCode = jsonObject.getString("error_code")
                val errorMsg = jsonObject.getString("error_msg")
                throw IOException("Translation error (code: $errorCode): $errorMsg")
            }

            // 获取翻译结果
            return jsonObject.getString("tgt_text")
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }

    override fun cancelTranslation() {
        currentTask?.let {
            if (it.isAlive) {
                it.interrupt()
            }
        }
        currentTask = null
    }

    override fun release() {
        cancelTranslation()
    }
}