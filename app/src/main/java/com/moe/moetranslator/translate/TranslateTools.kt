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

package com.moe.moetranslator.translate

import android.content.Context
import android.util.Log
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.Constants
import com.moe.moetranslator.utils.CustomPreference
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

object TranslateTools {
    fun getLanguagesList(context: Context, type: Int): List<CustomLocale>? = runCatching {
        // 获取当前设置
        val prefs = CustomPreference.getInstance(context)
        val translateMode = prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id)
        val textApi = prefs.getInt("Text_API", Constants.TextApi.BING.id)
        val textAi = prefs.getInt("Text_AI", Constants.TextAI.MLKIT.id)
        val picApi = prefs.getInt("Pic_API", Constants.PicApi.BAIDU.id)

        // 获取与设置相匹配的语言列表
        val resourceId = when {
            translateMode == Constants.TranslateMode.TEXT.id -> when (textApi) {
                Constants.TextApi.AI.id -> {
                    if (textAi == Constants.TextAI.MLKIT.id) {
                        when (type){
                            1 -> R.raw.ocr_support_languages
                            else -> R.raw.mlkit_text_support_languages
                        }
                    } else {
                        when (type){
                            1 -> R.raw.ocr_support_languages
                            else -> R.raw.nllb_text_support_languages
                        }
                    }
                }
                Constants.TextApi.BING.id -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.bing_text_support_languages
                    }
                }
                Constants.TextApi.NIUTRANS.id -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.niutrans_text_support_languages
                    }
                }
                Constants.TextApi.VOLC.id -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.volc_text_support_languages
                    }
                }
                Constants.TextApi.AZURE.id -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.azure_text_support_languages
                    }
                }
                Constants.TextApi.BAIDU.id -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.baidu_text_support_languages
                    }
                }
                Constants.TextApi.TENCENT.id -> {
                    when (type){
                        1 -> R.raw.ocr_support_languages
                        else -> R.raw.tencent_text_support_languages
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
                Constants.PicApi.BAIDU.id -> {
                    when (type){
                        1 -> R.raw.baidu_pic_src_support_languages
                        else -> R.raw.baidu_pic_tar_support_languages
                    }
                }
                Constants.PicApi.TENCENT.id -> {
                    when (type){
                        1 -> R.raw.tencent_pic_src_support_languages
                        else -> R.raw.tencent_pic_tar_support_languages
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