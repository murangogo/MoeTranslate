package com.moe.moetranslator.me

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
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
import java.io.IOException


class PersonalizationConfig : PreferenceFragmentCompat() {
    private lateinit var prefs: CustomPreference
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleFileSelection(it) }
    }
    private lateinit var ballIcon: PreferenceWithPreview
    private lateinit var ballPress: Preference
    private lateinit var resultFont: Preference
    private lateinit var resultFontSize: Preference
    private lateinit var ocrMergeMode: ListPreference
    private lateinit var showSource: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = CustomPreference.getInstance(requireContext())
        setPreferencesFromResource(R.xml.personalization, rootKey)

        ballIcon = findPreference<PreferenceWithPreview>("floating_ball_pic")!!
        ballPress = findPreference<Preference>("floating_ball_press")!!
        resultFont = findPreference<Preference>("result_font")!!
        resultFontSize = findPreference<Preference>("result_font_size")!!
        ocrMergeMode = findPreference<ListPreference>("ocr_merge_mode")!!
        showSource = findPreference<ListPreference>("show_source_text")!!

        // 悬浮球图片
        ballIcon.setOnPreferenceClickListener {
            showBallOptionsDialog()
            true
        }

        ballPress.setOnPreferenceClickListener {
            showPressDialog()
            true
        }

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

        // 提示文本
        findPreference<SwitchPreference>("adjust_tip")?.setOnPreferenceChangeListener { preference, newValue ->
            prefs.setBoolean("Custom_Adjust_Not_Text", newValue as Boolean)
            true
        }

        ballIcon.refreshPreview()
        updateIconSummary()
        updatePressSummary()
        updateFontSummary()
        updateFontSizeSummary()
    }

    private fun updateIconSummary(){
        if (prefs.getString("Custom_Floating_Pic", "") == "") {
            ballIcon.summary = getString(R.string.floating_ball_pic_summary, getString(R.string.default_name))
        } else {
            ballIcon.summary = getString(R.string.floating_ball_pic_summary, prefs.getString("Custom_Floating_Pic", ""))
        }
    }

    private fun updatePressSummary() {
        ballPress.summary = getString(R.string.floating_ball_press_summary, prefs.getLong("Custom_Long_Press_Delay", 500L).toString())
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

    private fun showBallOptionsDialog(){
        val options = arrayOf(getString(R.string.ball_icon_default), getString(R.string.pic_choose))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.ball_setting)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> handleDefaultIcon()
                    1 -> pickPicFile()
                }
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showPressDialog() {
//        val input = EditText(requireContext()).apply {
//            hint = getString(R.string.current_judgment_time, prefs.getLong("Custom_Long_Press_Delay", 500L).toString())
//            inputType = android.text.InputType.TYPE_CLASS_NUMBER
//
//            // 设置padding
//            val padding = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                16f,
//                resources.displayMetrics
//            ).toInt()
//            setPadding(padding, padding, padding, padding)
//        }

        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_message_edittext, null)
        customView.findViewById<TextView>(R.id.dialog_top_message).apply {
            text = getString(R.string.int_only)
        }
        val input = customView.findViewById<EditText>(R.id.dialog_bottom_edittext).apply {
            hint = getString(R.string.current_judgment_time, prefs.getLong("Custom_Long_Press_Delay", 500L).toString())
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.set_floating_ball_press)
            .setView(customView)
            .setPositiveButton(R.string.save) { _, _ ->
                try {
                    val value = input.text.toString().toLong()
                    prefs.setLong("Custom_Long_Press_Delay", value)
                    updatePressSummary()
                } catch (e: Exception) {
                    showToast(getString(R.string.font_size_invalid), true)
                }
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
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

    private fun handleDefaultIcon() {
        // 清除自定义图片设置
        prefs.setString("Custom_Floating_Pic", "")

        // 删除存在的图片文件，节省空间
        File(requireContext().getExternalFilesDir(null), "icon").apply {
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
        ballIcon.refreshPreview()
        updateIconSummary()
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

    private fun pickPicFile() {
        pickFileLauncher.launch("image/*")
    }

    private fun pickFontFile() {
        pickFileLauncher.launch("font/ttf")
    }

    private fun handleFileSelection(uri: Uri) {
        try {
            // 获取原始文件名
            val originalFileName = requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: throw IllegalStateException("get file name error.")

            // 获取文件扩展名并转换为小写
            val fileExtension = originalFileName.substringAfterLast('.', "").lowercase()

            when (fileExtension) {
                "ttf" -> handleFontFile(uri, originalFileName)
                else -> handleImageFile(uri, originalFileName)
            }
        } catch (e: Exception) {
            // 通用错误处理
            showToast(e.message ?: "unknow error", false)
        }
    }

    private fun handleFontFile(uri: Uri, originalFileName: String) {
        // 创建字体目录
        val fontDir = File(requireContext().getExternalFilesDir(null), "font").apply {
            if (!exists()) {
                mkdirs()
            } else {
                // 删除目录中的所有文件
                listFiles()?.forEach { it.delete() }
            }
        }

        // 复制文件
        val destinationFile = File(fontDir, originalFileName)
        copyFile(uri, destinationFile)

        // 更新配置
        prefs.setString("Custom_Result_Font", originalFileName)
        updateFontSummary()
        showToast(getString(R.string.set_success), true)
    }

    private fun handleImageFile(uri: Uri, originalFileName: String) {
        // 创建icon目录
        val iconDir = File(requireContext().getExternalFilesDir(null), "icon").apply {
            if (!exists()) {
                mkdirs()
            } else {
                // 删除目录中的所有文件
                listFiles()?.forEach { it.delete() }
            }
        }

        // 复制文件
        val destinationFile = File(iconDir, originalFileName)
        copyFile(uri, destinationFile)

        // 通知PreferenceWithPreview更新预览
        prefs.setString("Custom_Floating_Pic", originalFileName)
        ballIcon.refreshPreview()
        updateIconSummary()
        showToast(getString(R.string.set_success), true)
    }

    // 文件复制方法
    private fun copyFile(uri: Uri, destinationFile: File) {
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("open file error")
    }

    private fun showToast(str: String, isShort: Boolean = false){
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }

}