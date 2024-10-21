package com.moe.moetranslator.me

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.moe.moetranslator.R
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
        }

        // 从 SharedPreferences 加载设置
        loadSettingsFromSharedPreferences()
    }

    private fun changeCustomPreferences(key: String) {
        when (key){
            "mlkit_translation" -> {
                prefs.setInt("OCR_API", 0)
                prefs.setInt("OCR_AI", 0)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "mlkit_translation")
            }
            "nllb_translation" -> {
                prefs.setInt("OCR_API", 0)
                prefs.setInt("OCR_AI", 1)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "nllb_translation")
            }
            "ui_baidu_translation_text"->{
                prefs.setInt("OCR_API", 1)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR baidu")
            }
            "ui_baidu_translation_pic"->{
                prefs.setInt("Pic_API", 0)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic baidu")
            }
            "ui_tencent_translation_text" -> {
                prefs.setInt("OCR_API", 2)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "OCR tencent")
            }
            "ui_tencent_translation_pic" -> {
                prefs.setInt("Pic_API", 1)
                prefs.setString("Source_Language", "ja")
                prefs.setString("Target_Language", "zh")
                Log.d("APIConfig", "pic tencent")
            }
            "ui_custom_api_1_text" -> {
                prefs.setInt("OCR_API", 3)
                prefs.setInt("Custom_Text_API", 0)
                Log.d("APIConfig", "OCR custom 1")
            }
            "ui_custom_api_1_pic" -> {
                prefs.setInt("Pic_API", 2)
                prefs.setInt("Custom_Pic_API", 0)
                Log.d("APIConfig", "pic custom 1")
            }
            "ui_custom_api_2_text" -> {
                prefs.setInt("OCR_API", 3)
                prefs.setInt("Custom_Text_API", 1)
                Log.d("APIConfig", "OCR custom 2")
            }
            "ui_custom_api_2_pic" -> {
                prefs.setInt("Pic_API", 2)
                prefs.setInt("Custom_Pic_API", 1)
                Log.d("APIConfig", "pic custom 2")
            }
            "ui_custom_api_3_text" -> {
                prefs.setInt("OCR_API", 3)
                prefs.setInt("Custom_Text_API", 2)
                Log.d("APIConfig", "OCR custom 3")
            }
            "ui_custom_api_3_pic" -> {
                prefs.setInt("Pic_API", 2)
                prefs.setInt("Custom_Pic_API", 2)
                Log.d("APIConfig", "pic custom 3")
            }
        }
    }

    private fun loadSettingsFromSharedPreferences() {
        // 获取当前设置
        val translateMode = prefs.getInt("Translate_Mode", 0)
        val ocrApi = prefs.getInt("OCR_API", 0)
        val ocrAi = prefs.getInt("OCR_AI", 0)
        val picApi = prefs.getInt("Pic_API", 0)

        // 获取与设置相匹配的语言列表
        when {
            translateMode == 0 -> when (ocrApi) {
                0 -> {
                    if (ocrAi == 0) {
                        val key = "mlkit_translation"
                        findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                        setKey(key)
                    } else {
                        val key = "nllb_translation"
                        findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                        setKey(key)
                    }
                }
                1 -> {
                    val key = "ui_baidu_translation_text"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                2 -> {
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
                0 -> {
                    val key = "ui_baidu_translation_pic"
                    findPreference<SwitchPreferenceCompat>(key)?.isChecked = true
                    setKey(key)
                }
                1 -> {
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