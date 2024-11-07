package translationapi.volctranslation

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

class VolcTranslation(private val ak: String, private val sk: String): TranslationTextAPI {

    private var currentTask: Thread? = null
    private val region: String = "cn-north-1"
    private val service: String = "translate"
    private val endpoint = "translate.volcengineapi.com"
    private val schema = "https"
    private val path = "/"

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        currentTask = Thread {
            try {
                // 这里因为只支持一段文字，所以转为一个单元素列表
                val list = listOf(text)
                val result = translate(list, sourceLanguage, targetLanguage)
                callback(TranslationResult.Success(result))
            } catch (e: Exception) {
                callback(TranslationResult.Error(e))
            }
        }.apply { start() }
    }

    private fun translate(text: List<String>, from: String?, to: String): String{
        // 创建签名器
        val signer = VolcSign(
            region,
            service,
            schema,
            endpoint,
            path,
            ak,
            sk
        )

        // 构建请求体
        val requestBody = JSONObject().apply {
            put("TargetLanguage", to)
            from?.let { put("SourceLanguage", it) }
            put("TextList", JSONArray(text))
        }.toString()

        // 准备查询参数
        val queryParams = mapOf(
            "Action" to "TranslateText",
            "Version" to "2020-06-01"
        )

        // 执行签名请求
        val response = signer.kdoRequest(
            method = "POST",
            queryList = queryParams,
            body = requestBody.toByteArray(),
            date = Date(),
            action = "TranslateText",
            version = "2020-06-01"
        )

        // 解析响应
        val jsonResponse = JSONObject(response)

        // 检查是否有错误
        val metadata = jsonResponse.getJSONObject("ResponseMetadata")
        if (metadata.has("Error") && !metadata.isNull("Error")) {
            val error = metadata.getJSONObject("Error")
            throw Exception("Code：${error.getString("Code")} ${error.getString("Message")} ")
        }

        // 解析翻译结果
        val translationList = jsonResponse.getJSONArray("TranslationList")

        // 在本应用中，长度为1
        val sb = StringBuilder()
        Log.d("VOLC", translationList.length().toString())
        for (i in 0 until translationList.length()) {
            val item = translationList.getJSONObject(i)
            sb.append(item.getString("Translation"))
        }

        return sb.toString()
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

    private fun VolcSign.kdoRequest(
        method: String,
        queryList: Map<String, String>,
        body: ByteArray,
        date: Date,
        action: String,
        version: String
    ): String {
        return doRequest(method, queryList, body, date, action, version)
    }
}