package translationapi.customtranslation

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.moe.moetranslator.me.CustomPicAPIConfig
import com.moe.moetranslator.translate.TranslationPicAPI
import com.moe.moetranslator.translate.TranslationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CustomTranslationImage(private val config: CustomPicAPIConfig): TranslationPicAPI {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null
    private lateinit var base64Image: String

    private val SOCKET_TIMEOUT = 10L
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()
    private val MULTIPART_FORM_DATA = "multipart/form-data".toMediaType()

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
            val tempFile = File.createTempFile("translate_", ".jpg")
            try {
                // 如果是multipart/form-data，需要保存临时文件
                if (config.method == "POST" && config.contentType == "multipart/form-data") {
                    withContext(Dispatchers.IO) {
                        FileOutputStream(tempFile).use { out ->
                            pic.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        }
                    }
                } else {
                    // 提前计算bitmap的base64
                    base64Image = convertBitmapToBase64(pic)
                }

                pic.recycle() // 回收bitmap

                // 根据不同的请求方式执行请求
                val result = when {
                    config.method == "GET" -> executeGetRequest()
                    config.contentType == "application/json" -> executePostJsonRequest()
                    config.contentType == "multipart/form-data" -> executePostMultipartRequest(tempFile)
                    else -> throw Exception("Unsupported request type")
                }

                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Success(result))
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CUSTOM_IMAGE", "Translation error", e)
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Error(e))
                }
            } finally {
                // 确保清理临时文件
                withContext(Dispatchers.IO) {
                    tempFile.delete()
                }
            }
        }
    }

    private fun executeGetRequest(): String {
        // 构建URL，添加查询参数
        val urlBuilder = config.baseUrl.toHttpUrl().newBuilder()

        // 添加查询参数，替换base64占位符
        config.queryParams.forEach { param ->
            val value = if (param.value == "useimgbase64") {
                base64Image
            } else param.value
            urlBuilder.addQueryParameter(param.key, value)
        }

        // 构建请求
        val request = Request.Builder()
            .url(urlBuilder.build())
            .apply {
                config.headers.forEach { header ->
                    addHeader(header.key, header.value)
                }
            }
            .get()
            .build()

        // 执行请求
        return executeRequest(request)
    }

    private fun executePostJsonRequest(): String {
        // 构建JSON请求体
        val jsonBody = JSONObject().apply {
            config.body.forEach { field ->
                val value = if (field.value == "useimgbase64") {
                    base64Image
                } else field.value
                put(field.key, value)
            }
        }.toString()

        // 构建请求
        val request = Request.Builder()
            .url(config.baseUrl)
            .post(jsonBody.toRequestBody(JSON_TYPE))
            .apply {
                config.headers.forEach { header ->
                    addHeader(header.key, header.value)
                }
            }
            .build()

        return executeRequest(request)
    }

    private fun executePostMultipartRequest(imageFile: File): String {
        // 构建MultipartBody
        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        // 添加form字段
        config.body.forEach { field ->
            if (field.value == "useimgfile") {
                // 添加文件部分
                multipartBuilder.addFormDataPart(
                    field.key,
                    "image.jpg",
                    imageFile.asRequestBody(MULTIPART_FORM_DATA)
                )
            } else {
                // 添加普通字段
                multipartBuilder.addFormDataPart(field.key, field.value)
            }
        }

        // 构建请求
        val request = Request.Builder()
            .url(config.baseUrl)
            .post(multipartBuilder.build())
            .apply {
                config.headers.forEach { header ->
                    addHeader(header.key, header.value)
                }
            }
            .build()

        return executeRequest(request)
    }

    private fun executeRequest(request: Request): String {
        // 执行请求
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unexpected response ${response.code}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            return parseResponse(responseBody)
        }
    }

    private fun parseResponse(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            // 使用配置的JSON响应路径解析结果
            val parts = config.jsonResponsePath.split(".")
            var current: Any = jsonObject

            for (part in parts) {
                current = when (current) {
                    is JSONObject -> current.get(part)
                    else -> throw Exception("Invalid JSON path at: $part")
                }
            }

            current.toString()
        } catch (e: Exception) {
            throw Exception("Failed to parse response: ${e.message}")
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    override fun cancelTranslation() {
        currentJob?.cancel()
        currentJob = null
    }

    override fun release() {
        cancelTranslation()
        coroutineScope.cancel()
    }
}