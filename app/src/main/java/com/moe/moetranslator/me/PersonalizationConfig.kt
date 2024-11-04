package com.moe.moetranslator.me

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import com.moe.moetranslator.R
import com.moe.moetranslator.translate.Dialogs
import com.moe.moetranslator.utils.CustomPreference
import okhttp3.internal.notify
import java.io.File
import java.io.FileOutputStream


class PersonalizationConfig : PreferenceFragmentCompat() {
    private lateinit var prefs: CustomPreference
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleFontFileSelection(it) }
    }
    private lateinit var resultFont: Preference
    private lateinit var resultFontSize: Preference
    private lateinit var ocrMergeMode: ListPreference
    private lateinit var showSource: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = CustomPreference.getInstance(requireContext())
        setPreferencesFromResource(R.xml.personalization, rootKey)

        resultFont = findPreference<Preference>("result_font")!!
        resultFontSize = findPreference<Preference>("result_font_size")!!
        ocrMergeMode = findPreference<ListPreference>("ocr_merge_mode")!!
        showSource = findPreference<ListPreference>("show_source_text")!!

        // 字体相关
        resultFont.setOnPreferenceClickListener {
            showFontOptionsDialog()
            true
        }

        // 字体大小
        resultFontSize.setOnPreferenceClickListener {
            showFontSizeDialog()
            true
        }

        // 字体颜色
        findPreference<ColorPreferenceCompat>("result_view_font_color")?.setOnPreferenceChangeListener { preference, newValue ->
            prefs.setInt("Custom_Result_Font_Color", newValue as Int)
            true
        }

        // 背景颜色
        findPreference<ColorPreferenceCompat>("result_view_background_color")?.setOnPreferenceChangeListener { preference, newValue ->
            prefs.setInt("Custom_Result_Background_Color", newValue as Int)
            true
        }

        // 可穿透性
        findPreference<SwitchPreference>("result_penetrability")?.setOnPreferenceChangeListener { preference, newValue ->
            prefs.setBoolean("Custom_Result_Penetrability", newValue as Boolean)
            true
        }

        // OCR合并模式
        ocrMergeMode.setOnPreferenceChangeListener { _, newValue ->
            prefs.setInt("Custom_OCR_Merge_Mode", newValue.toString().toInt())
            true
        }
        ocrMergeMode.summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
            getString(R.string.merge_ocr_summary, ocrMergeMode.entry)
        }

        // 显示原文
        showSource.setOnPreferenceChangeListener { _, newValue ->
            prefs.setInt("Custom_Show_Source_Mode", newValue.toString().toInt())
            true
        }
        showSource.summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
            getString(R.string.show_source_text_summary, showSource.entry)
        }
        updateFontSummary()
        updateFontSizeSummary()
    }

    private fun updateFontSummary() {
        if (prefs.getString("Custom_Result_Font", "") == "") {
            resultFont.summary = getString(R.string.font_summary, getString(R.string.font_default))
        } else {
            resultFont.summary = getString(R.string.font_summary, prefs.getString("Custom_Result_Font", ""))
        }
    }

    private fun updateFontSizeSummary() {
        resultFontSize.summary = getString(R.string.font_size_summary, prefs.getFloat("Custom_Result_Font_Size", 16f).toString())
    }

    private fun showFontOptionsDialog() {
        val options = arrayOf(getString(R.string.font_default), getString(R.string.font_choose))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.font_setting)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> handleSystemFont()
                    1 -> pickFontFile()
                }
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showFontSizeDialog(){
        val dialog = Dialogs.fontSizeDialog(requireContext(), null){
            updateFontSizeSummary()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun handleSystemFont() {
        // 清除自定义字体设置
        prefs.setString("Custom_Result_Font", "")

        // 删除存在的字体文件，节省空间
        File(requireContext().getExternalFilesDir(null), "font").apply {
            if (!exists()) {
                mkdirs()
            } else {
                // 删除目录中的所有文件
                listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        }
        // 更新summary
        updateFontSummary()
    }

    private fun pickFontFile() {
        pickFileLauncher.launch("font/ttf")
    }

    private fun handleFontFileSelection(uri: Uri) {
        try {
            // 获取原始文件名
            val originalFileName = requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "custom_font.ttf"  // 如果无法获取文件名，使用默认名称

            // 创建字体目录
            val fontDir = File(requireContext().getExternalFilesDir(null), "font").apply {
                if (!exists()) {
                    mkdirs()
                } else {
                    // 删除目录中的所有文件
                    listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
            }

            // 使用原始文件名创建目标文件
            val destinationFile = File(fontDir, originalFileName)

            // 复制文件
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            // 保存字体名称到SharedPreferences
            prefs.setString("Custom_Result_Font", originalFileName)

            // 更新summary
            updateFontSummary()

            showToast(getString(R.string.font_success), true)
        } catch (e: Exception) {
            prefs.setString("Custom_Result_Font", "")
            // 更新summary
            updateFontSummary()
            showToast(getString(R.string.font_failed, e.message), true)
        }
    }

    private fun showToast(str: String, isShort: Boolean = false){
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }

}