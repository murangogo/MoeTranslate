package com.moe.moetranslator.translate

import android.text.TextUtils
import java.util.Locale

class CustomLocale {
    val locale: Locale

    // 包含 language, country, variant
    constructor(language: String, country: String, variant: String) {
        locale = Locale(language, country, variant)
    }

    // 包含 languageCode 和 countryCode
    constructor(languageCode: String, countryCode: String) {
        locale = Locale(languageCode, countryCode)
    }

    // 只包含 languageCode
    constructor(languageCode: String) {
        locale = Locale(languageCode)
    }

    // 包含 Locale 对象
    constructor(locale: Locale) {
        this.locale = locale
    }

    companion object {
        fun getInstance(code: String): CustomLocale {
            val languageCode = code.split("-")
            return when (languageCode.size) {
                1 -> CustomLocale(Locale(languageCode[0]))
                2 -> CustomLocale(Locale(languageCode[0], languageCode[1]))
                else -> CustomLocale(Locale(languageCode[0], languageCode[1], languageCode[2]))
            }
        }
    }

    fun getDisplayName(): String {
        val name = locale.displayName
        return name.substring(0, 1).uppercase(locale) + name.substring(1)
    }

    fun getCode(): String {
        val language = StringBuilder(locale.language)
        val country = locale.country
        if (!TextUtils.isEmpty(country)) {
            language.append("-")
            language.append(country)
        }
        return language.toString()
    }

    override fun toString(): String {
        return locale.displayName
    }

}