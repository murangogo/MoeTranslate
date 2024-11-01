package com.moe.moetranslator.me

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import java.io.File
import android.provider.OpenableColumns
import android.util.Log
import com.moe.moetranslator.translate.Dialogs
import java.io.FileOutputStream


class PersonalizationConfig : PreferenceFragmentCompat() {
    private lateinit var prefs: CustomPreference
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleFontFileSelection(it) }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = CustomPreference.getInstance(requireContext())
        setPreferencesFromResource(R.xml.personalization, rootKey)

        // 字体相关
        findPreference<Preference>("result_font")?.setOnPreferenceClickListener {
            showFontOptionsDialog()
            true
        }

        // 字体大小
        findPreference<Preference>("result_font_size")?.setOnPreferenceClickListener {
            showFontSizeDialog()
            true
        }

        loadFromSharedPreference()
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
            findPreference<Preference>("result_font_size")?.summary = getString(R.string.font_size_summary, prefs.getFloat("Custom_Result_Font_Size", 16f).toString())
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
        findPreference<Preference>("result_font")?.summary = getString(R.string.font_summary, getString(R.string.font_default))
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

            // 更新summary为字体名称
            findPreference<Preference>("result_font")?.summary = getString(R.string.font_summary, prefs.getString("Custom_Result_Font", ""))

            showToast(getString(R.string.font_success), true)
        } catch (e: Exception) {
            prefs.setString("Custom_Result_Font", "")
            findPreference<Preference>("result_font")?.summary = getString(R.string.font_summary, getString(R.string.font_default))
            showToast(getString(R.string.font_failed, e.message), true)
        }
    }

    private fun loadFromSharedPreference(){
        if (prefs.getString("Custom_Result_Font", "") == "") {
            findPreference<Preference>("result_font")?.summary = getString(R.string.font_summary, getString(R.string.font_default))
        } else {
            findPreference<Preference>("result_font")?.summary = getString(R.string.font_summary, prefs.getString("Custom_Result_Font", ""))
        }

        findPreference<Preference>("result_font_size")?.summary = getString(R.string.font_size_summary, prefs.getFloat("Custom_Result_Font_Size", 16f).toString())
    }

    private fun showToast(str: String, isShort: Boolean = false){
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }

}