package com.moe.moetranslator.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class CustomPreference private constructor(context: Context) {

    //KeyList
    //Translate_Mode(Int)：0为本地OCR后进行文本翻译；1为上传截图后进行图片翻译
    //OCR_API(Int)：0为使用本地大模型；1为使用百度翻译；2为使用腾讯云；3为使用自定义API
    //OCR_AI(Int)：0为使用MLKit套件；1为使用NLLB模型
    //Pic_API(Int)：0为使用百度翻译；1为使用腾讯云；2为使用自定义API
    //Custom_Text_API(Int)：用户选择的自定义的文本翻译API，用户最多可添加三个自定义的文本翻译API（0，1，2）
    //Custom_Pic_API(Int)：用户选择的自定义的图片翻译API，用户最多可添加三个自定义的图片翻译API（0，1，2）

    //Download_MLKit(Boolean)：用户是否已经下载ML Kit模型
    //Download_NLLB(Boolean)：用户是否已经下载NLLB模型

    //Source_Language(Str)：记录当前的源语言
    //Target_Language(Str)：记录当前的目标语言

    //Ignore_Version(Long)：记录用户忽略的版本号
    //Read_Notice(Long)：记录用户已读的通知号

    //Baidu_Translate_ACCOUNT_EncryptedKey(Str)：KeyStore参数，存储百度翻译API（APPID）
    //Baidu_Translate_ACCOUNT_IV(Str)：KeyStore参数，存储百度翻译API（APPID）

    //Baidu_Translate_SECRETKEY_EncryptedKey(Str)：KeyStore参数，存储百度翻译API（SecretKey）
    //Baidu_Translate_SECRETKEY_IV(Str)：KeyStore参数，存储百度翻译API（SecretKey）

    //Tencent_Cloud_ACCOUNT_EncryptedKey(Str)：KeyStore参数，存储腾讯云API（APPID）
    //Tencent_Cloud_ACCOUNT_IV(Str)：KeyStore参数，存储腾讯云API（APPID）

    //Tencent_Cloud_SECRETKEY_EncryptedKey(Str)：KeyStore参数，存储腾讯云API（SecretKey）
    //Tencent_Cloud_SECRETKEY_IV(Str)：KeyStore参数，存储腾讯云API（SecretKey）

    //Custom_Text_API_1(Str)：用户自定义的文本翻译API1
    //Custom_Text_JSON_Parse_1(Str)：用户自定义的文本翻译API1对应的JSON解析方法
    //Custom_Text_API_2(Str)：用户自定义的文本翻译API2
    //Custom_Text_JSON_Parse_2(Str)：用户自定义的文本翻译API2对应的JSON解析方法
    //Custom_Text_API_3(Str)：用户自定义的文本翻译API3
    //Custom_Text_JSON_Parse_3(Str)：用户自定义的文本翻译API3对应的JSON解析方法
    //Custom_Pic_API_1(Str)：用户自定义的图片翻译API1
    //Custom_Pic_JSON_Parse_1(Str)：用户自定义的图片翻译API1对应的JSON解析方法
    //Custom_Pic_API_2：用户自定义的图片翻译API2
    //Custom_Pic_JSON_Parse_2(Str)：用户自定义的图片翻译API2对应的JSON解析方法
    //Custom_Pic_API_3：用户自定义的图片翻译API3
    //Custom_Pic_JSON_Parse_3(Str)：用户自定义的图片翻译API3对应的JSON解析方法

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