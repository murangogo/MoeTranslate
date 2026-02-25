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

package translationapi.deepltranslation

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * DeepL翻译的文本翻译
 * 请求方法：POST
 * URL：{host}/v2/translate
 * 请求头需要设置Authorization和Content-Type
 * 请求参数放在请求体中，以json形式发送
 */

class DeepLTranslation(private val host: String, private val apiKey: String) : TranslationTextAPI {
    private var currentTask: Thread? = null
    private val SOCKET_TIMEOUT = 10L // 10秒
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // 标准化host地址
    private val normalizedHost: String by lazy {
        var url = host.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }
        url
    }

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
        // 构建JSON请求体，语言代码转为大写
        val jsonBody = JSONObject().apply {
            put("text", JSONArray().put(text))
            put("source_lang", from.uppercase())
            put("target_lang", to.uppercase())
        }.toString()

        Log.d("DEEPL", "Request: $jsonBody")

        // 创建请求
        val request = Request.Builder()
            .url("$normalizedHost/v2/translate")
            .addHeader("Authorization", "DeepL-Auth-Key $apiKey")
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

            Log.d("DEEPL", "Response: $responseBody")
            return parseResponse(responseBody)
        }
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)

            // 检查是否有错误消息
            if (jsonObject.has("message")) {
                val errorMsg = jsonObject.getString("message")
                throw IOException("DeepL error: $errorMsg")
            }

            // 获取翻译结果
            val translations = jsonObject.getJSONArray("translations")
            if (translations.length() == 0) {
                throw IOException("No translation result")
            }

            return translations.getJSONObject(0).getString("text")
        } catch (e: IOException) {
            throw e
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