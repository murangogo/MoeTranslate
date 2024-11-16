package translationapi.tencentcloud

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.moe.moetranslator.translate.TranslationPicAPI
import com.moe.moetranslator.translate.TranslationResult
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TencentTranslationImage(
    private val secretId: String,
    private val secretKey: String
) : TranslationPicAPI {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null

    private val SOCKET_TIMEOUT = 10L
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS)
        .build()

    override fun getTranslation(
        pic: Bitmap,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        currentJob?.cancel()

        currentJob = coroutineScope.launch {
            try {
                // 转换图片为Base64
                val imageBase64 = convertBitmapToBase64(pic)
                pic.recycle()
                ensureActive()

                // 构建请求体
                // 生成 1 到 100 之间的随机整数
                val randomInt = Random.nextInt(1, 100000)
                // 将整数转换为三位字符串格式
                val formattedString = randomInt.toString().padStart(5, '0')
                val sessionUuid = "session-$formattedString"
//                val sessionUuid = UUID.randomUUID().toString()
                val requestBody = JSONObject().apply {
                    put("SessionUuid", sessionUuid)
                    put("Scene", "doc")
                    put("Data", imageBase64)
                    put("Source", sourceLanguage)
                    put("Target", targetLanguage)
                    put("ProjectId", 0)
                }.toString()

                ensureActive()

                // 获取签名头
                val headers = TencentSign.getSignature(secretId, secretKey, "ImageTranslate", requestBody)

                // 构建请求
                val request = Request.Builder()
                    .url("https://tmt.tencentcloudapi.com")
                    .post(requestBody.toRequestBody(JSON_TYPE))
                    .apply {
                        headers.forEach { (key, value) ->
                            addHeader(key, value)
                        }
                    }
                    .build()

                ensureActive()

                // 执行请求
                client.newCall(request).execute().use { response ->
                    ensureActive()

                    if (!response.isSuccessful) {
                        throw IOException("Unexpected response ${response.code}")
                    }

                    val responseBody = response.body?.string()
                        ?: throw IOException("Empty response body")

                    Log.d("TENCENT_PIC", "Response: $responseBody")

                    // 解析响应
                    val result = parseResponse(responseBody)
                    withContext(Dispatchers.Main) {
                        callback(TranslationResult.Success(result))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("TENCENT_PIC", "Translation error", e)
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Error(e))
                }
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun parseResponse(responseBody: String): String {
        val jsonObject = JSONObject(responseBody)

        // 检查错误
        if (!jsonObject.has("Response")) {
            throw IOException("Invalid response format")
        }

        val response = jsonObject.getJSONObject("Response")
        if (response.has("Error")) {
            val error = response.getJSONObject("Error")
            throw IOException("${error.getString("Code")}: ${error.getString("Message")}")
        }

        // 获取翻译结果
        val imageRecord = response.getJSONObject("ImageRecord")
        val records = imageRecord.getJSONArray("Value")

        // 合并所有翻译结果
        return buildString {
            for (i in 0 until records.length()) {
                val record = records.getJSONObject(i)
                if (isNotEmpty()) append("\n")
                append(record.getString("TargetText"))
            }
        }
    }

    override fun cancelTranslation() {
        currentJob?.cancel()
        currentJob = null
    }

    override fun release() {
        coroutineScope.cancel()
        currentJob?.cancel()
        currentJob = null
    }
}