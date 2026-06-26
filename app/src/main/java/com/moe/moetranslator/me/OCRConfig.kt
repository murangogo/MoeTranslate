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

package com.moe.moetranslator.me

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.Constants
import com.moe.moetranslator.utils.CustomPreference

/**
 * 「OCR配置」页：选择截图取字所用的 OCR 引擎（ML Kit / PaddleOCR-V6）。
 *
 * 仿照翻译 API 配置页 [APIConfig] 的「开关二选一互斥」做法：两个开关同一时刻只允许一个开启，
 * 选择结果写入 SharedPreferences 的 OCR_Engine。不同引擎可识别的源语言不同（见
 * [com.moe.moetranslator.translate.TranslateTools.getLanguagesList]），故切换引擎时会把源语言
 * 重置为各引擎都支持的「日语」，避免停留在新引擎不支持的语言（如 PaddleOCR 不支持韩语）。
 */
class OCRConfig : PreferenceFragmentCompat() {

    private lateinit var prefs: CustomPreference

    // 全部 OCR 引擎开关的 key（用于互斥）
    private val allEngineKeys = listOf("ui_mlkit_ocr", "ui_paddleocr")

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = CustomPreference.getInstance(requireContext())
        setPreferencesFromResource(R.xml.preferences_ocr_engine, rootKey)

        // 为每个开关设置监听：开启某引擎时关闭其他引擎，并落盘选择；不允许把唯一选中的关掉
        allEngineKeys.forEach { key ->
            findPreference<SwitchPreferenceCompat>(key)?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    selectEngine(key)
                    setKey(key)
                    true
                } else {
                    Toast.makeText(requireContext(), getString(R.string.no_less_one), Toast.LENGTH_LONG).show()
                    false
                }
            }
        }

        // 加载当前已选引擎到 UI
        loadSettingsFromSharedPreferences()
    }

    /** 把所选引擎写入 OCR_Engine，并把源语言重置为各引擎通用的「日语」。 */
    private fun selectEngine(key: String) {
        when (key) {
            "ui_paddleocr" -> {
                prefs.setInt("OCR_Engine", Constants.OcrEngine.PADDLEOCR.id)
                prefs.setString("Source_Language", "ja")
                Log.d("OCRConfig", "engine = PaddleOCR")
            }
            "ui_mlkit_ocr" -> {
                prefs.setInt("OCR_Engine", Constants.OcrEngine.MLKIT.id)
                prefs.setString("Source_Language", "ja")
                Log.d("OCRConfig", "engine = ML Kit")
            }
        }
    }

    /** 按已保存的 OCR_Engine 勾选对应开关。 */
    private fun loadSettingsFromSharedPreferences() {
        val key = when (prefs.getInt("OCR_Engine", Constants.OcrEngine.PADDLEOCR.id)) {
            Constants.OcrEngine.PADDLEOCR.id -> "ui_paddleocr"
            else -> "ui_mlkit_ocr"
        }
        findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
        setKey(key)
    }

    /** 互斥：关闭除 key 之外的其他所有开关。 */
    private fun setKey(key: String) {
        allEngineKeys.filter { it != key }.forEach { otherKey ->
            findPreference<SwitchPreferenceCompat>(otherKey)?.isChecked = false
        }
    }
}
