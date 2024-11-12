package translationapi.baidutranslation

import android.graphics.Bitmap
import com.moe.moetranslator.translate.TranslationPicAPI
import com.moe.moetranslator.translate.TranslationResult
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

class BaiduTranslationImage(
    private val appId: String,
    private val secretKey: String
) : TranslationPicAPI {
    // 创建协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentJob: Job? = null

    private val TRANS_API_HOST = "https://fanyi-api.baidu.com/api/trans/sdk/picture"
    private val SOCKET_TIMEOUT = 10L // 10秒
    private val MULTIPART_FORM_DATA = "multipart/form-data".toMediaType()

    // 创建OkHttpClient实例
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
        // 取消之前的任务（如果存在）
        currentJob?.cancel()

        currentJob = coroutineScope.launch {
            try {
                // 创建临时文件
                val tempFile = File.createTempFile("translate_", ".jpg")
                try {
                    // 将Bitmap保存到临时文件
                    withContext(Dispatchers.IO) {
                        FileOutputStream(tempFile).use { out ->
                            pic.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        }
                    }
                    pic.recycle() // 回收bitmap

                    val result = translate(tempFile, sourceLanguage, targetLanguage)
                    withContext(Dispatchers.Main) {
                        callback(TranslationResult.Success(result))
                    }
                } finally {
                    // 确保清理临时文件
                    withContext(Dispatchers.IO) {
                        tempFile.delete()
                    }
                }
            } catch (e: CancellationException) {
                // 协程被取消，不调用callback
                throw e
            } catch (e: Exception) {
                Log.e("BAIDU_PIC", "Translation error", e)
                withContext(Dispatchers.Main) {
                    callback(TranslationResult.Error(e))
                }
            }
        }
    }

    private suspend fun translate(
        imageFile: File,
        from: String,
        to: String
    ): String = withContext(Dispatchers.IO) {
        ensureActive()
        // 计算图片MD5
        val imageMd5 = getMD5File(imageFile)

        // 构建签名参数
        val salt = System.currentTimeMillis().toString()
        val cuid = "APICUID"
        val mac = "mac"

        // 计算签名：md5(appid+md5(image)+salt+cuid+mac+密钥)
        val sign = getMD5(appId + imageMd5 + salt + cuid + mac + secretKey).lowercase()

        // 构建MultipartBody
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg",
                imageFile.asRequestBody(MULTIPART_FORM_DATA))
            .addFormDataPart("from", modifyTargetLanguage(from))
            .addFormDataPart("to", modifyTargetLanguage(to))
            .addFormDataPart("appid", appId)
            .addFormDataPart("salt", salt)
            .addFormDataPart("cuid", cuid)
            .addFormDataPart("mac", mac)
            .addFormDataPart("version", "3")
            .addFormDataPart("paste", "0")
            .addFormDataPart("erase", "0")
            .addFormDataPart("sign", sign)
            .build()

        // 构建请求
        val request = Request.Builder()
            .url(TRANS_API_HOST)
            .post(requestBody)
            .build()

        // 执行请求
        client.newCall(request).execute().use { response ->
            ensureActive()

            if (!response.isSuccessful) {
                throw IOException("Unexpected response ${response.code}")
            }

            // 解析响应
            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")

            Log.d("BAIDU_PIC", "Response: $responseBody")
            parseResponse(responseBody)
        }
    }

    private fun parseResponse(responseBody: String): String {
        val jsonObject = JSONObject(responseBody)

        // 检查是否有错误
        if (jsonObject.has("error_code") && jsonObject.getString("error_code") != "0") {
            throw IOException("${jsonObject.getString("error_code")}: ${jsonObject.getString("error_msg")}")
        }

        // 获取翻译结果
        val data = jsonObject.getJSONObject("data")
        return data.getString("sumDst") // 返回未分段的完整译文
    }

    private suspend fun getMD5File(file: File): String = withContext(Dispatchers.IO) {
        try {
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("MD5 algorithm not found", e)
        }
    }

    private fun getMD5(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            messageDigest.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("MD5 algorithm not found", e)
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
        currentJob?.cancel()
        currentJob = null
    }

    override fun release() {
        cancelTranslation()
        coroutineScope.cancel() // 取消整个作用域
    }

}