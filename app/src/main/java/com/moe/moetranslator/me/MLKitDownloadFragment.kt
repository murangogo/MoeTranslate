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

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.moe.moetranslator.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.moe.moetranslator.databinding.FragmentMlkitDownloadBinding
import com.moe.moetranslator.translate.CustomLocale
import com.moe.moetranslator.utils.CustomPreference
import java.util.concurrent.atomic.AtomicBoolean

class MLKitDownloadFragment : Fragment() {

    private lateinit var binding: FragmentMlkitDownloadBinding
    private val modelManager = RemoteModelManager.getInstance()
    private var isDownloading = AtomicBoolean(false)
    private lateinit var prefs: CustomPreference

    // 定义需要下载的语言模型
    private val languageModels = listOf(
        TranslateLanguage.CHINESE,
        TranslateLanguage.JAPANESE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMlkitDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 检查初始状态
        lifecycleScope.launch(Dispatchers.Main) {
            val res = checkModelsStatus()  // 直接获取结果
            if (res) {
                updateStatus(getString(R.string.mlkit_status_download))
            } else {
                updateStatus(getString(R.string.mlkit_status_not_download))
            }
        }

        // 设置按钮点击事件
        binding.mlkitDownloadButton.setOnClickListener {
            if (isDownloading.get()) {
                stopDownload()
            } else {
                if (prefs.getBoolean("Download_MLKit",false)){
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle(R.string.model_hasbeen_download_title)
                        .setMessage(R.string.model_hasbeen_download_content)
                        .setCancelable(false)
                        .setPositiveButton(R.string.user_known, null)
                        .create()
                    dialog.show()
                    dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                } else {
                    startDownload()
                }
            }
        }

        binding.mlkitDeleteButton.setOnClickListener {
            deleteAllModels()
        }
    }

    private fun startDownload() {
        if (isDownloading.get()) return

        isDownloading.set(true)
        binding.mlkitDownloadButton.text = getString(R.string.stop_download)
        updateStatus(getString(R.string.please_wait))

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                for (language in languageModels) {
                    if (!isDownloading.get()) break // 检查是否被取消

                    val model = TranslateRemoteModel.Builder(language).build()
                    val isModelDownloaded = modelManager.isModelDownloaded(model).await()

                    if (!isModelDownloaded) {
                        updateStatus(getString(R.string.current_download_mlkit, getLanguageName(language)))
                        val conditions = DownloadConditions.Builder()
                            .requireWifi()
                            .build()
                        modelManager.download(model, conditions).await()
                    }
                }

                if (isDownloading.get()) {
                    withContext(Dispatchers.Main) {
                        isDownloading.set(false)
                        updateStatus(getString(R.string.mlkit_status_download))
                        binding.mlkitDownloadButton.text = getString(R.string.start_download)
                        prefs.setBoolean("Download_MLKit", true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    updateStatus(getString(R.string.download_failed, e.message))
                    isDownloading.set(false)
                    binding.mlkitDownloadButton.text = getString(R.string.start_download)
                    prefs.setBoolean("Download_MLKit", false)
                }
            }
        }
    }

    private fun stopDownload() {
        isDownloading.set(false)
        binding.mlkitDownloadButton.text = getString(R.string.start_download)
        updateStatus(getString(R.string.download_pause))
    }

    private fun deleteAllModels() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                updateStatus(getString(R.string.please_wait))
                for (language in languageModels) {
                    val model = TranslateRemoteModel.Builder(language).build()
                    if (modelManager.isModelDownloaded(model).await()) {
                        modelManager.deleteDownloadedModel(model).await()
                    }
                }
                updateStatus(getString(R.string.delete_download))
            } catch (e: Exception) {
                updateStatus(getString(R.string.delete_download_failed, e.message))
            }
        }
        prefs.setBoolean("Download_MLKit", false)
    }

    private fun updateStatus(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.mlkitStatusText.text = message
        }
    }

    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            TranslateLanguage.CHINESE -> CustomLocale.getInstance("zh").getDisplayName()
            TranslateLanguage.JAPANESE -> CustomLocale.getInstance("ja").getDisplayName()
            else -> languageCode
        }
    }

    private suspend fun checkModelsStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var downloadedCount = 0
                for (language in languageModels) {
                    val model = TranslateRemoteModel.Builder(language).build()
                    if (modelManager.isModelDownloaded(model).await()) {
                        downloadedCount++
                    }
                }

                if (downloadedCount == languageModels.size){
                    updateStatus(getString(R.string.mlkit_status_download))
                    prefs.setBoolean("Download_MLKit", true)
                    true
                } else {
                    updateStatus(getString(R.string.mlkit_status_not_download))
                    prefs.setBoolean("Download_MLKit", false)
                    false
                }

            } catch (e: Exception) {
                updateStatus(getString(R.string.check_mlkit_download_error, e.message))
                false
            }
        }
    }
}