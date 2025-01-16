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

package translationapi.azuretranslation

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
 * Azure文本翻译API实现
 * 请求方法：POST
 * URL包含查询参数：api-version, to, from, textType等
 * 请求头需要包含订阅密钥
 * 请求体为包含Text属性的对象数组
 */

class AzureTranslation(private val subscriptionKey: String) : TranslationTextAPI {
    private var currentTask: Thread? = null
    private val API_HOST = "https://api.cognitive.microsofttranslator.com/translate"
    private val API_VERSION = "3.0"
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
        // 构建URL，添加所有必要的查询参数
        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("api.cognitive.microsofttranslator.com")
            .addPathSegment("translate")
            .addQueryParameter("api-version", API_VERSION)
            .addQueryParameter("to", modifyTargetLanguage(to))
            .addQueryParameter("from", modifyTargetLanguage(from))
            .addQueryParameter("textType", "plain")
            .addQueryParameter("profanityAction", "NoAction")

        // 构建JSON请求体
        val jsonBody = JSONArray().apply {
            put(JSONObject().apply {
                put("Text", text)
            })
        }.toString()

        Log.d("AZURE", "Request: $jsonBody")

        // 创建请求
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
            .addHeader("Content-Type", "application/json; charset=UTF-8")
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

            Log.d("AZURE", "Response: $responseBody")
            return parseResponse(responseBody)
        }
    }

    private fun modifyTargetLanguage(to: String): String = when (to) {
        "zh" -> "zh-Hans"
        "zh-TW" -> "zh-Hant"
        else -> to
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonArray = JSONArray(responseBody)
            if (jsonArray.length() == 0) {
                throw IOException("Empty translation response")
            }

            // 获取第一个翻译结果对象
            val translationObject = jsonArray.getJSONObject(0)

            // 检查是否存在错误
            if (translationObject.has("error")) {
                val error = translationObject.getJSONObject("error")
                val errorCode = error.getString("code")
                val errorMessage = error.getString("message")
                throw IOException("Translation error (code: $errorCode): $errorMessage")
            }

            // 获取translations数组中的第一个翻译结果
            val translations = translationObject.getJSONArray("translations")
            if (translations.length() == 0) {
                throw IOException("No translation results")
            }

            return translations.getJSONObject(0).getString("text")
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