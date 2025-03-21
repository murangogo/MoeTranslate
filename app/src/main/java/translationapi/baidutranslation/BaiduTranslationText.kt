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

package translationapi.baidutranslation

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

/**
 * 百度翻译的文本翻译
 * 请求方法：GET
 * 请求参数全部放在了URL中
 * 没有特殊的请求头，使用okhttp的默认请求头
 * 没有特殊的请求体
 */

class BaiduTranslationText(private val appId: String, private val secretKey: String) :
    TranslationTextAPI {
    private var currentTask: Thread? = null
    private val TRANS_API_HOST = "https://fanyi-api.baidu.com/api/trans/vip/translate"
    private val SOCKET_TIMEOUT = 10L // 10秒

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

    private fun translate(query: String, from: String, to: String): String {
        // 构建参数
        val params = buildParams(query, modifyTargetLanguage(from), modifyTargetLanguage(to))

        Log.d("BAIDU", "$query+ $from+ ${modifyTargetLanguage(to)}")

        // 构建URL
        val urlBuilder = TRANS_API_HOST.toHttpUrl().newBuilder()
        params.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        // 创建请求
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response ${response.code}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")


            Log.d("BAIDU", responseBody)
            return parseResponse(responseBody)
        }
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)

            // 检查是否有错误
            if (jsonObject.has("error_code")) {
                throw IOException(jsonObject.getString("error_msg"))
            }

            // 获取翻译结果
            val transResult = jsonObject.getJSONArray("trans_result")
            if (transResult.length() == 0) {
                throw IOException("Empty translation result")
            }

            // 返回翻译文本
            return buildString {
                for (i in 0 until transResult.length()) {
                    if (i > 0) append("\n")
                    append(transResult.getJSONObject(i).getString("dst"))
                }
            }
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }

    // 因为百度翻译的API没有使用BCP-47代码作为他们的语言代码，因此需要转换
    private fun modifyTargetLanguage(to: String): String = when (to) {
        "ko" -> "kor"
        "bg" -> "bul"
        "fi" -> "fin"
        "sl" -> "slo"
        "zh-TW" -> "cht"
        "fr" -> "fra"
        "ar" -> "ara"
        "et" -> "est"
        "sv" -> "swe"
        "vi" -> "vie"
        "ja" -> "jp"
        "es" -> "spa"
        "da" -> "dan"
        "ro" -> "rom"
        else -> to
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

    @Throws(NoSuchAlgorithmException::class)
    fun getMD5(str: String): String {
        try {
            // 获取MD5 MessageDigest实例
            val messageDigest = MessageDigest.getInstance("MD5")

            // 使用UTF-8编码获取字节数组并更新摘要
            val bytes = str.toByteArray(Charset.forName("UTF-8"))
            messageDigest.update(bytes)

            // 获取摘要字节数组
            val digest = messageDigest.digest()

            // 将字节数组转换为十六进制字符串
            return digest.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            throw NoSuchAlgorithmException("MD5 algorithm is not available: ${e.message}")
        }
    }

    private fun buildParams(
        query: String,
        from: String,
        to: String
    ): Map<String, String> = buildMap {
        // 基本参数
        put("q", query)
        put("from", from)
        put("to", to)
        put("appid", appId)

        // 随机数（使用时间戳）
        val salt = System.currentTimeMillis().toString()
        put("salt", salt)

        // 生成签名：MD5(appid + query + salt + secretKey)
        val src = "$appId$query$salt$secretKey"
        put("sign", getMD5(src))
    }
}