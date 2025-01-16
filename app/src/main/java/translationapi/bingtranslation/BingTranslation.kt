package translationapi.bingtranslation

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Bing网页翻译API实现
 * 实现原理：
 * 1. 首先请求翻译页面获取必要的token
 * 2. 使用token构建翻译请求
 * 3. 发送翻译请求获取结果
 */

class BingTranslation : TranslationTextAPI {
    private var currentTask: Thread? = null
    private val TAG = "BING_TRANS"

    // URL配置
    private val CN_HOST_URL = "https://cn.bing.com/Translator"
    private val EN_HOST_URL = "https://www.bing.com/Translator"
    private val API_ENDPOINT = "ttranslatev3"

    // 超时设置
    private val SOCKET_TIMEOUT = 10L
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Token相关正则表达式
    private val IG_PATTERN = Pattern.compile("IG:\"(.*?)\"")
    private val IID_PATTERN = Pattern.compile("<div[ ]+id=\"tta_outGDCont\"[ ]+data-iid=\"(.*?)\">")
    private val TOKEN_PATTERN = Pattern.compile("var params_AbusePreventionHelper = (.*?);")

    // 创建OkHttpClient实例，启用Cookie支持
    private val client = OkHttpClient.Builder()
        .connectTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        })
        .build()

    // 存储token信息的数据类
    private data class TokenInfo(
        val ig: String,
        val iid: String,
        val key: String,
        val token: String
    )

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
                Log.e(TAG, "Translation error", e)
                callback(TranslationResult.Error(e))
            }
        }.apply { start() }
    }

    private fun translate(text: String, from: String, to: String): String {
        // 获取token
        val tokenInfo = getTokenInfo()

        // 构建翻译API URL
        val apiUrl = HttpUrl.Builder()
            .scheme("https")
            .host(if (useCnHost()) "cn.bing.com" else "www.bing.com")
            .addPathSegment(API_ENDPOINT)
            .addQueryParameter("isVertical", "1")
            .addQueryParameter("IG", tokenInfo.ig)
            .addQueryParameter("IID", tokenInfo.iid)
            .build()

        // 构建翻译请求表单
        val formBody = FormBody.Builder()
            .add("fromLang", modifyTargetLanguage(from))
            .add("to", modifyTargetLanguage(to))
            .add("text", text)
            .add("tryFetchingGenderDebiasedTranslations", "true")
            .add("token", tokenInfo.token)
            .add("key", tokenInfo.key)
            .build()

        // 创建请求
        val request = Request.Builder()
            .url(apiUrl)
            .header("User-Agent", USER_AGENT)
            .header("Referer", if (useCnHost()) CN_HOST_URL else EN_HOST_URL)
            .post(formBody)
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")

            return parseTranslationResponse(responseBody)
        }
    }

    private fun modifyTargetLanguage(to: String): String = when (to) {
        "zh" -> "zh-Hans"
        "zh-TW" -> "zh-Hant"
        else -> to
    }

    private fun getTokenInfo(): TokenInfo {
        // 请求翻译页面
        val request = Request.Builder()
            .url(if (useCnHost()) CN_HOST_URL else EN_HOST_URL)
            .header("User-Agent", USER_AGENT)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to get tokens, response: ${response.code}")
            }

            val html = response.body?.string()
                ?: throw IOException("Empty response when getting tokens")

            // 提取必要的token
            val igMatcher = IG_PATTERN.matcher(html)
            val iidMatcher = IID_PATTERN.matcher(html)
            val tokenMatcher = TOKEN_PATTERN.matcher(html)

            if (!igMatcher.find() || !iidMatcher.find() || !tokenMatcher.find()) {
                throw IOException("Failed to extract necessary tokens from response")
            }

            // 解析token参数
            val tokenParams = tokenMatcher.group(1)
                ?.trim()
                ?.removeSurrounding("[", "]")
                ?.split(",")
                ?.map { it.trim().removeSurrounding("\"") }
                ?: throw IOException("Invalid token parameters format")

            if (tokenParams.size < 2) {
                throw IOException("Incomplete token parameters")
            }

            return TokenInfo(
                ig = igMatcher.group(1),
                iid = iidMatcher.group(1),
                key = tokenParams[0],
                token = tokenParams[1]
            )
        }
    }

    private fun parseTranslationResponse(responseBody: String): String {
        try {
            val jsonArray = JSONArray(responseBody)
            if (jsonArray.length() == 0) {
                throw IOException("Empty translation response")
            }
            return jsonArray.getJSONObject(0)
                .getJSONArray("translations")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            throw IOException("Failed to parse translation response: ${e.message}")
        }
    }

    private fun useCnHost(): Boolean {
        // 默认使用中国服务器
        return true
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
        // 清理资源
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36"
    }
}