package com.moe.moetranslator.translate

import android.content.Context
import android.util.Log
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

object TranslateTools {
    fun getLanguagesList(context: Context, type: Int): List<CustomLocale>? = runCatching {
        // 获取当前设置
        val prefs = CustomPreference.getInstance(context)
        val translateMode = prefs.getInt("Translate_Mode", 0)
        val ocrApi = prefs.getInt("OCR_API", 0)
        val ocrAi = prefs.getInt("OCR_AI", 0)
        val picApi = prefs.getInt("Pic_API", 0)

        // 获取与设置相匹配的语言列表
        val resourceId = when {
            translateMode == 0 -> when (ocrApi) {
                0 -> {
                    if (ocrAi == 0) {
                        when (type){
                            1 -> R.raw.ocr_support_languages
                            else -> R.raw.mlkit_support_languages
                        }
                    } else {
                        when (type){
                            1 -> R.raw.ocr_support_languages
                            else -> R.raw.nllb_support_languages
                        }
                    }
                }
                1 -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.volc_support_languages
                    }
                }
                2 -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.niutrans_support_languages
                    }
                }
                3 -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.baidu_text_support_languages
                    }
                }
                4 -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.tencent_text_support_language
                    }
                }
                else -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> {
                            Log.w("TranslateTools", "Custom OCR API selected")
                            return@runCatching null
                        }
                    }
                }
            }
            else -> when (picApi) {
                0 -> {
                    R.raw.baidu_pic_support_languages
                }
                1 -> {
                    when (type){
                        1 -> R.raw.tencent_pic_src_support_language
                        else -> R.raw.tencent_pic_tar_support_language
                    }
                }
                else -> {
                    Log.w("TranslateTools", "Custom Pic API selected")
                    return@runCatching null
                }
            }
        }

        // 转成List后返回
        context.resources.openRawResource(resourceId).use { inputStream ->
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(inputStream)
            val nodeList = document.getElementsByTagName("code")
            nodeList.toCustomLocaleList()
        }
    }.onFailure { exception ->
        // 打印错误堆栈
        exception.printStackTrace()
    }.getOrNull()

    private fun NodeList.toCustomLocaleList(): List<CustomLocale> =
        (0 until length).mapNotNull { i ->
            item(i)?.textContent?.let {
                Log.d("TEXTCONTENT", it)
                CustomLocale.getInstance(it)
            }
        }
}