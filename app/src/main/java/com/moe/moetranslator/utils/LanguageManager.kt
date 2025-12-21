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

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    // 语言常量
    const val LANGUAGE_SYSTEM = "system"  // 跟随系统
    const val LANGUAGE_CHINESE = "zh"     // 简体中文
    const val LANGUAGE_ENGLISH = "en"     // 英文

    private const val PREF_KEY_LANGUAGE = "App_Language"

    /**
     * 获取用户选择的语言
     */
    fun getAppLanguage(context: Context): String {
        val prefs = CustomPreference.getInstance(context)
        return prefs.getString(PREF_KEY_LANGUAGE, LANGUAGE_SYSTEM)
    }

    /**
     * 设置应用语言
     * @param sync 是否同步写入（退出应用前必须同步写入）
     */
    fun setAppLanguage(context: Context, languageCode: String, sync: Boolean = false) {
        val prefs = CustomPreference.getInstance(context)
        if (sync) {
            // 同步写入，确保立即保存到磁盘
            prefs.setStringSync(PREF_KEY_LANGUAGE, languageCode)
        } else {
            // 异步写入
            prefs.setString(PREF_KEY_LANGUAGE, languageCode)
        }
    }

    /**
     * 应用语言设置到Context
     */
    fun applyLanguage(context: Context): Context {
        val languageCode = getAppLanguage(context)
        val locale = getLocaleFromCode(languageCode)

        return updateResources(context, locale)
    }

    /**
     * 根据语言代码获取Locale
     */
    private fun getLocaleFromCode(languageCode: String): Locale {
        return when (languageCode) {
            LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_SYSTEM -> getSystemLocale()
            else -> getSystemLocale()
        }
    }

    /**
     * 获取系统语言
     */
    private fun getSystemLocale(): Locale {
        val systemLocale = android.content.res.Resources.getSystem().configuration.locales[0]
        // 如果系统语言是简体中文，返回简体中文，否则返回英文
        return if (systemLocale.language == "zh" && systemLocale.country == "CN") {
            Locale.SIMPLIFIED_CHINESE
        } else {
            Locale.ENGLISH
        }
    }

    /**
     * 更新Context的语言资源
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * 退出应用以应用新语言
     * 用户重新打开应用时会使用新的语言设置
     */
    fun exitApplication(activity: Activity) {
        // 清除所有Activity栈
        activity.finishAffinity()
        // 退出进程
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}