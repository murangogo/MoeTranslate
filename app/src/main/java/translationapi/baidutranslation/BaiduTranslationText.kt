package translationapi.baidutranslation

import com.moe.moetranslator.translate.TranslationAPI
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit


class BaiduTranslationText(private val appId: String, private val secretKey: String): TranslationAPI {
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
        callback: (TranslationAPI.TranslationResult) -> Unit
    ) {
        currentTask = Thread {
            try {
                val result = translate(text, sourceLanguage, targetLanguage)
                callback(TranslationAPI.TranslationResult.Success(result))
            } catch (e: Exception) {
                callback(TranslationAPI.TranslationResult.Error(e))
            }
        }.apply { start() }
    }

    private fun translate(query: String, from: String, to: String): String {
        // 构建参数
        val params = buildParams(query, from, to)

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

//            return parseResponse(responseBody)
            return responseBody
        }
    }

    private fun parseResponse(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)

            // 检查是否有错误
            if (jsonObject.has("error_code")) {
                throw IOException("Translation error: ${jsonObject.getString("error_msg")}")
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

    override fun cancelTranslation() {
        currentTask?.interrupt()
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