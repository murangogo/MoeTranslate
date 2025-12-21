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

package com.moe.moetranslator.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class CustomPreference private constructor(context: Context) {

    //KeyList

    //----杂项----
    //Is_First_Run(Boolean)：是否是第一次运行
    //Read_Custom_Text_Introduce(Boolean)：是否阅读自定义文本翻译API的介绍
    //Read_Custom_Pic_Introduce(Boolean)：是否阅读自定义图片翻译API的介绍
    //Read_OpenAI_API_Introduce(Boolean)：是否阅读OpenAI API的介绍

    //----应用语言----
    //App_Language(Str)：用户选择的应用语言

    //----翻译选项----
    //Translate_Mode(Int)：0为本地OCR后进行文本翻译；1为上传截图后进行图片翻译
    //Text_API(Int)：0为使用本地大模型；1为使用必应翻译；2为使用小牛翻译；3为使用火山引擎；4为使用Azure AI翻译；5为使用百度翻译；6为使用腾讯云；7为使用自定义API；8为使用聚合AI翻译
    //Text_AI(Int)：0为使用MLKit套件；1为使用NLLB模型
    //Pic_API(Int)：0为使用百度翻译；1为使用腾讯云；2为使用自定义API
    //Custom_Text_API(Int)：用户选择的自定义的文本翻译API，用户最多可添加三个自定义的文本翻译API（0，1，2）
    //Custom_Pic_API(Int)：用户选择的自定义的图片翻译API，用户最多可添加三个自定义的图片翻译API（0，1，2）

    //----模型下载----
    //Download_MLKit(Boolean)：用户是否已经下载ML Kit模型
    //Download_NLLB(Boolean)：用户是否已经下载NLLB模型

    //----翻译配置----
    //Source_Language(Str)：记录当前的源语言
    //Target_Language(Str)：记录当前的目标语言

    //----更新与公告----
    //Ignore_Version(Long)：记录用户忽略的版本号
    //Read_Notice(Long)：记录用户已读的通知号

    //----Gemini相关----
    //Gemini_EncryptedKey(Str)：KeyStore参数，Gemini的API
    //Gemini_IV(Str)：KeyStore参数，Gemini的API

    //----API列表----
    //Niutrans_EncryptedKey(Str)：KeyStore参数，存储小牛翻译API（Key）
    //Niutrans_IV(Str)：KeyStore参数，存储小牛翻译API（Key）

    //OpenAI_Api_Key(Str)：存储OpenAI API
    //OpenAI_Base_Url(Str)：存储OpenAI API的Base URL
    //OpenAI_Model_Name(Str)：存储OpenAI API的模型
    //OpenAI_System_Prompt(Str)：存储OpenAI API的系统提示词
    //OpenAI_User_Prompt(Str)：存储OpenAI API的用户提示词

    //Volc_ACCOUNT_EncryptedKey(Str)：KeyStore参数，存储火山引擎API（APPID）
    //Volc_ACCOUNT_IV(Str)：KeyStore参数，存储火山引擎API（APPID）

    //Volc_SECRETKEY_EncryptedKey(Str)：KeyStore参数，存储火山引擎API（SecretKey）
    //Volc_SECRETKEY_IV(Str)：KeyStore参数，存储火山引擎API（SecretKey）

    //Azure_EncryptedKey(Str)：KeyStore参数，存储Azure API（Key）
    //Azure_IV(Str)：KeyStore参数，存储Azure翻译API（Key）

    //Baidu_Translate_ACCOUNT_EncryptedKey(Str)：KeyStore参数，存储百度翻译API（APPID）
    //Baidu_Translate_ACCOUNT_IV(Str)：KeyStore参数，存储百度翻译API（APPID）

    //Baidu_Translate_SECRETKEY_EncryptedKey(Str)：KeyStore参数，存储百度翻译API（SecretKey）
    //Baidu_Translate_SECRETKEY_IV(Str)：KeyStore参数，存储百度翻译API（SecretKey）

    //Tencent_Cloud_ACCOUNT_EncryptedKey(Str)：KeyStore参数，存储腾讯云API（APPID）
    //Tencent_Cloud_ACCOUNT_IV(Str)：KeyStore参数，存储腾讯云API（APPID）

    //Tencent_Cloud_SECRETKEY_EncryptedKey(Str)：KeyStore参数，存储腾讯云API（SecretKey）
    //Tencent_Cloud_SECRETKEY_IV(Str)：KeyStore参数，存储腾讯云API（SecretKey）

    //Custom_Text_API_0(Str)：用户自定义的文本翻译API0
    //Custom_Text_API_1(Str)：用户自定义的文本翻译API1
    //Custom_Text_API_2(Str)：用户自定义的文本翻译API2

    //Custom_Pic_API_0(Str)：用户自定义的图片翻译API0
    //Custom_Pic_API_1(Str)：用户自定义的图片翻译API1
    //Custom_Pic_API_2(Str)：用户自定义的图片翻译API2

    //----个性化选项----
    //Custom_Result_Font(Str)：用户自定义的翻译结果字体
    //Custom_Result_Font_Size(Float)：用户自定义的翻译结果字体大小
    //Custom_Result_Font_Color(Int)：用户自定义的翻译结果字体颜色
    //Custom_Result_Background_Color(Int)：用户自定义的翻译结果背景颜色
    //Custom_Result_Penetrability(Boolean)：翻译结果的可穿透性
    //Custom_OCR_Merge_Mode(Int)：合并OCR结果的模式
    //Custom_Show_Source_Mode(Int)：显示原文模式
    //Custom_Long_Press_Delay(Long)：长按判定时间
    //Custom_Floating_Pic(Str)：用户自定义的悬浮球图片
    //Custom_Adjust_Not_Text(Boolean)：是否显示提示文本

    //----自动翻译选项----
    //Auto_Translate_Interval(Long)：自动翻译时间间隔
    //Auto_Translate_Str_Length(Int)：自动翻译中，直接翻译的字符串阈值
    //Auto_Translate_Str_Similarity(Float)：自动翻译字符串相似度阈值

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        @Volatile
        private var instance: CustomPreference? = null

        fun getInstance(context: Context): CustomPreference {
            return instance ?: synchronized(this) {
                instance ?: CustomPreference(context.applicationContext).also { instance = it }
            }
        }
    }

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    /**
     * 同步写入字符串到 SharedPreferences
     */
    fun setStringSync(key: String, value: String) {
        prefs.edit().putString(key, value).commit()
    }

    /**
     * 同步写入整数到 SharedPreferences
     */
    fun setIntSync(key: String, value: Int) {
        prefs.edit().putInt(key, value).commit()
    }

    /**
     * 同步写入布尔值到 SharedPreferences
     */
    fun setBooleanSync(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).commit()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun setFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return prefs.getFloat(key, defaultValue)
    }

    fun setLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
}