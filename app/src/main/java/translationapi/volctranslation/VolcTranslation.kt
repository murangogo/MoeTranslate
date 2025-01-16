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

package translationapi.volctranslation

import android.util.Log
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * 火山引擎的文本翻译
 * 请求方法：POST
 * URL中有API版本和操作信息
 * 需要特殊的认证头信息，通过VolcSign签名器处理，显式设置Content-Type为application/json
 * 请求参数放在请求体中，以json形式发送
 */

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
                val result = translate(list, modifyTargetLanguage(sourceLanguage), modifyTargetLanguage(targetLanguage))
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

    private fun modifyTargetLanguage(to: String): String = when (to) {
        "zh-hk-Hant" -> "zh-Hant-hk"
        "zh-tw-Hant" -> "zh-Hant-tw"
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