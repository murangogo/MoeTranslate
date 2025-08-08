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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.Constants
import com.moe.moetranslator.utils.CustomPreference


class APIConfig : PreferenceFragmentCompat() {
    private lateinit var allTranslationKeys: List<String>
    private lateinit var prefs: CustomPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = CustomPreference.getInstance(requireContext())
        if(prefs.getInt("Translate_Mode", 0) == 0){
            setPreferencesFromResource(R.xml.preferences_ocr, rootKey)
            allTranslationKeys = listOf(
                "mlkit_translation", "nllb_translation",
                "ui_bing_translation_text", "ui_niu_translation_text",
                "ui_openai_translation_text",
                "ui_volc_translation_text", "ui_azure_translation_text",
                "ui_baidu_translation_text", "ui_tencent_translation_text",
                "ui_custom_api_1_text", "ui_custom_api_2_text", "ui_custom_api_3_text"
            )
        }else{
            setPreferencesFromResource(R.xml.preferences_pic, rootKey)
            allTranslationKeys = listOf(
                "ui_baidu_translation_pic", "ui_tencent_translation_pic",
                "ui_custom_api_1_pic", "ui_custom_api_2_pic", "ui_custom_api_3_pic"
            )
        }

        // 设置每个选项的监听器
        allTranslationKeys.forEach { key ->
            findPreference<SwitchPreferenceCompat>(key)?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    // 如果打开了这个选项，关闭其他所有选项
                    changeCustomPreferences(key)
                    setKey(key)
                    true
                }else{
                    Toast.makeText(requireContext(), getString(R.string.no_less_one), Toast.LENGTH_LONG).show()
                    false
                }
            }
        }

        if (prefs.getInt("Translate_Mode", 0) == 0){

            findPreference<Preference>("manage_mlkit_model")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_MLKIT)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("manage_nllb_model")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_NLLB)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_niu_api_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_NIU_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_openai_api_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_OPENAI_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_volc_api_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_VOLC_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_azure_api_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_AZURE_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_baidu_api_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_BAIDU_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_tencent_api_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_TENCENT_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_custom_api_1_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API)
                    putExtra(ManageActivity.EXTRA_CUSTOM_CODE, ManageActivity.CODE_CUSTOM_0)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_custom_api_2_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API)
                    putExtra(ManageActivity.EXTRA_CUSTOM_CODE, ManageActivity.CODE_CUSTOM_1)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_custom_api_3_text")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API)
                    putExtra(ManageActivity.EXTRA_CUSTOM_CODE, ManageActivity.CODE_CUSTOM_2)
                }
                startActivity(intent)
                true
            }
        }else{
            findPreference<Preference>("ui_manage_baidu_api_pic")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_BAIDU_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_tencent_api_pic")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_TENCENT_API)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_custom_api_1_pic")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API)
                    putExtra(ManageActivity.EXTRA_CUSTOM_CODE, ManageActivity.CODE_CUSTOM_0)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_custom_api_2_pic")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API)
                    putExtra(ManageActivity.EXTRA_CUSTOM_CODE, ManageActivity.CODE_CUSTOM_1)
                }
                startActivity(intent)
                true
            }

            findPreference<Preference>("ui_manage_custom_api_3_pic")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ManageActivity::class.java).apply {
                    putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API)
                    putExtra(ManageActivity.EXTRA_CUSTOM_CODE, ManageActivity.CODE_CUSTOM_2)
                }
                startActivity(intent)
                true
            }
        }

        // 从 SharedPreferences 加载设置
        loadSettingsFromSharedPreferences()
    }

    private fun changeCustomPreferences(key: String) {
        when (key){
            "mlkit_translation" -> {
                prefs.setInt("Text_API", Constants.TextApi.AI.id)
                prefs.setInt("Text_AI", Constants.TextAI.MLKIT.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "mlkit_translation")
            }
            "nllb_translation" -> {
                prefs.setInt("Text_API", Constants.TextApi.AI.id)
                prefs.setInt("Text_AI", Constants.TextAI.NLLB.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "nllb_translation")
            }
            "ui_bing_translation_text"->{
                prefs.setInt("Text_API", Constants.TextApi.BING.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR bing")
            }
            "ui_niu_translation_text"->{
                prefs.setInt("Text_API", Constants.TextApi.NIUTRANS.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR niu")
            }
            "ui_openai_translation_text"->{
                prefs.setInt("Text_API", Constants.TextApi.OPENAI.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR openai")
            }
            "ui_volc_translation_text"->{
                prefs.setInt("Text_API", Constants.TextApi.VOLC.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR volc")
            }
            "ui_azure_translation_text"->{
                prefs.setInt("Text_API", Constants.TextApi.AZURE.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR azure")
            }
            "ui_baidu_translation_text"->{
                prefs.setInt("Text_API", Constants.TextApi.BAIDU.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR baidu")
            }
            "ui_baidu_translation_pic"->{
                prefs.setInt("Pic_API", Constants.PicApi.BAIDU.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic baidu")
            }
            "ui_tencent_translation_text" -> {
                prefs.setInt("Text_API", Constants.TextApi.TENCENT.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR tencent")
            }
            "ui_tencent_translation_pic" -> {
                prefs.setInt("Pic_API", Constants.PicApi.TENCENT.id)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic tencent")
            }
            "ui_custom_api_1_text" -> {
                prefs.setInt("Text_API", Constants.TextApi.CUSTOM_TEXT.id)
                prefs.setInt("Custom_Text_API", 0)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR custom 1")
            }
            "ui_custom_api_1_pic" -> {
                prefs.setInt("Pic_API", Constants.PicApi.CUSTOM_PIC.id)
                prefs.setInt("Custom_Pic_API", 0)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic custom 1")
            }
            "ui_custom_api_2_text" -> {
                prefs.setInt("Text_API", Constants.TextApi.CUSTOM_TEXT.id)
                prefs.setInt("Custom_Text_API", 1)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR custom 2")
            }
            "ui_custom_api_2_pic" -> {
                prefs.setInt("Pic_API", Constants.PicApi.CUSTOM_PIC.id)
                prefs.setInt("Custom_Pic_API", 1)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic custom 2")
            }
            "ui_custom_api_3_text" -> {
                prefs.setInt("Text_API", Constants.TextApi.CUSTOM_TEXT.id)
                prefs.setInt("Custom_Text_API", 2)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR custom 3")
            }
            "ui_custom_api_3_pic" -> {
                prefs.setInt("Pic_API", Constants.PicApi.CUSTOM_PIC.id)
                prefs.setInt("Custom_Pic_API", 2)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic custom 3")
            }
        }
    }

    private fun loadSettingsFromSharedPreferences() {
        // 获取当前设置
        val translateMode = prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id)
        val textApi = prefs.getInt("Text_API", Constants.TextApi.BING.id)
        val textAi = prefs.getInt("Text_AI", Constants.TextAI.MLKIT.id)
        val picApi = prefs.getInt("Pic_API", Constants.PicApi.BAIDU.id)

        // 加载设置到UI上
        when {
            translateMode == Constants.TranslateMode.TEXT.id -> when (textApi) {
                Constants.TextApi.AI.id -> {
                    if (textAi == Constants.TextAI.MLKIT.id) {
                        val key = "mlkit_translation"
                        findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                        setKey(key)
                    } else {
                        val key = "nllb_translation"
                        findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                        setKey(key)
                    }
                }
                Constants.TextApi.BING.id -> {
                    val key = "ui_bing_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.TextApi.NIUTRANS.id -> {
                    val key = "ui_niu_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.TextApi.OPENAI.id -> {
                    val key = "ui_openai_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.TextApi.VOLC.id -> {
                    val key = "ui_volc_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.TextApi.AZURE.id -> {
                    val key = "ui_azure_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.TextApi.BAIDU.id -> {
                    val key = "ui_baidu_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.TextApi.TENCENT.id -> {
                    val key = "ui_tencent_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                else -> {
                    when (prefs.getInt("Custom_Text_API", 0)){
                        0 -> {
                            val key = "ui_custom_api_1_text"
                            findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                            setKey(key)
                        }
                        1 ->{
                            val key = "ui_custom_api_2_text"
                            findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                            setKey(key)
                        }
                        2->{
                            val key = "ui_custom_api_3_text"
                            findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                            setKey(key)
                        }
                    }
                }
            }
            else -> when (picApi) {
                Constants.PicApi.BAIDU.id -> {
                    val key = "ui_baidu_translation_pic"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                Constants.PicApi.TENCENT.id -> {
                    val key = "ui_tencent_translation_pic"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                else -> {
                    when (prefs.getInt("Custom_Pic_API", 0)){
                        0 -> {
                            val key = "ui_custom_api_1_pic"
                            findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                            setKey(key)
                        }
                        1 ->{
                            val key = "ui_custom_api_2_pic"
                            findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                            setKey(key)
                        }
                        2->{
                            val key = "ui_custom_api_3_pic"
                            findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                            setKey(key)
                        }
                    }
                }
            }
        }
    }

    private fun setKey(key: String){
        Log.d("APIConfig", "key=$key")
        allTranslationKeys.filter { it != key }.forEach { otherKey ->
            findPreference<SwitchPreferenceCompat>(otherKey)?.isChecked = false
        }
    }
}