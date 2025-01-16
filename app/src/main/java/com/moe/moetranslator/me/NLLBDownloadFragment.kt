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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.moe.moetranslator.R
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import com.moe.moetranslator.databinding.FragmentNllbDownloadBinding
import com.moe.moetranslator.utils.CustomPreference
import java.util.Locale

// 保存下载进度信息
data class DownloadProgress(
    var currentFileIndex: Int = 0,
    var downloadedBytes: Long = 0
)

// 所下载文件的信息
data class FileInfo(
    val fileName: String,
    val downloadUrl: String,
    val fileSize: Long,
    val md5Checksum: String
)

class NLLBDownloadFragment : Fragment() {

    // 初始化视图绑定
    private lateinit var binding: FragmentNllbDownloadBinding

    // 是否正在下载，默认false
    private val isDownloading = AtomicBoolean(false)

    // 下载Job
    private var downloadJob: Job? = null

    // OKHttp客户端
    private val client = OkHttpClient()

    // 下载进度
    private var currentProgress = DownloadProgress()

    // 获取下载信息
    private lateinit var fileInfoList: List<FileInfo>

    private lateinit var prefs: CustomPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNllbDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(prefs.getBoolean("Download_NLLB",false)){
            binding.progressBar.progress = 100
            binding.progressText.text = "100%"
            binding.sizeText.text = ""
            binding.speedText.text = ""
            binding.statusText.text = getString(R.string.nllb_status_download)
        }else{
            binding.statusText.text = getString(R.string.nllb_status_not_download)
        }

        // 初始化按钮
        setupButtons()
    }

    private fun setupButtons() {
        binding.downloadButton.setOnClickListener {

            // 判断是否正在下载
            if (isDownloading.get()) {
                // 正在下载，点击按钮为“停止下载”
                stopDownload()
            } else {
                // 未在下载，点击按钮为“开始下载”
                if (prefs.getBoolean("Download_NLLB",false)){
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle(R.string.model_hasbeen_download_title)
                        .setMessage(R.string.model_hasbeen_download_content)
                        .setCancelable(false)
                        .setPositiveButton(R.string.user_known, null)
                        .create()
                    dialog.show()
                    dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                } else {
                    prepareDownload()
                }
            }

        }

        binding.deleteButton.setOnClickListener {
            deleteAllFiles()
        }

        binding.downloadHandText.setOnClickListener {
            // TODO：手动下载的方法
            val urlt = "https://www.moetranslate.top/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(urlt)
            startActivity(intent)
        }
    }

    private fun prepareDownload() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main){
                    binding.downloadButton.text = getString(R.string.please_wait)
                    binding.downloadButton.isClickable = false
                    binding.statusText.text = getString(R.string.get_download_info)
                }

                // 获取下载信息
                fileInfoList = fetchDownloadInfo()

                // 切换到主线程显示对话框
                withContext(Dispatchers.Main) {
                    showDownloadConfirmDialog(fileInfoList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 显示错误信息
                    binding.statusText.text = getString(R.string.get_download_info_error, e.message)
                }
            }
        }
    }

    private fun showDownloadConfirmDialog(files: List<FileInfo>) {
        // 计算总大小
        val totalSize = files.sumOf { it.fileSize }

        // 构建文件列表字符串
        val fileListText = buildString {
            files.forEachIndexed { index, file ->
                appendLine("${index + 1}. ${file.fileName} (${formatFileSize(file.fileSize)})")
            }
        }

        // 构建对话框消息
        val message = buildString {
            appendLine(getString(R.string.download_files_prompt, files.size))
            appendLine()
            append(fileListText)
            appendLine()
            append(getString(R.string.download_total_size, formatFileSize(totalSize)))
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.download_start_tips)
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton(R.string.user_cancel){_, _ ->
                binding.downloadButton.text = getString(R.string.start_download)
                binding.downloadButton.isClickable = true
                if(prefs.getBoolean("Download_NLLB",false)){
                    binding.statusText.text = getString(R.string.nllb_status_download)
                }else{
                    binding.statusText.text = getString(R.string.nllb_status_not_download)
                }
            }
            .setPositiveButton(R.string.start_download) { _, _ ->
                startDownload()
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    // 格式化文件大小的辅助函数
    private fun formatFileSize(size: Long): String {
        val locale = Locale.getDefault()
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format(locale, "%.2f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format(locale, "%.2f MB", size / (1024.0 * 1024))
            else -> String.format(locale, "%.2f GB", size / (1024.0 * 1024 * 1024))
        }
    }

    private fun startDownload() {

        // 开始下载，若已处于下载状态，则直接返回
        if (isDownloading.get()) return

        // 开启下载Job
        downloadJob = lifecycleScope.launch(Dispatchers.IO) {
            try {

                // 设置开始下载状态，更新按钮文字
                isDownloading.set(true)

                withContext(Dispatchers.Main) {
                    binding.downloadButton.isClickable = true
                    updateUI(true)
                }


                // 总共的文件数
                val totalFiles = fileInfoList.size

                // 开始下载
                for (i in currentProgress.currentFileIndex until fileInfoList.size) {

                    // 检测到停止下载
                    if (!isDownloading.get()) {
                        // 抛出异常，取消下载
                        throw CancellationException("Download cancelled")
                    }

                    // 获取当前文件的信息
                    val fileInfo = fileInfoList[i]
                    // 获取文件序号
                    currentProgress.currentFileIndex = i

                    // 在主线程中更新UI
                    withContext(Dispatchers.Main) {
                        binding.statusText.text = getString(R.string.current_download, fileInfo.fileName, i + 1, totalFiles)
                        binding.progressBar.progress = 0
                    }

                    // 开始下载文件
                    downloadFile(fileInfo, i + 1, totalFiles)
                }

                // 在主线程中更新UI
                withContext(Dispatchers.Main) {
                    prefs.setBoolean("Download_NLLB", true)
                    binding.statusText.text = getString(R.string.nllb_status_download)
                    binding.progressBar.progress = 100
                    binding.progressText.text = "100%"
                }
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    binding.statusText.text = getString(R.string.download_pause)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.statusText.text = getString(R.string.download_failed, e.message)
                }
            } finally {
                isDownloading.set(false)
                withContext(Dispatchers.Main) {
                    updateUI(false)
                    binding.sizeText.text = ""
                    binding.speedText.text = ""
                }
            }
        }
    }

    private fun stopDownload() {
        // 取消下载Job
        downloadJob?.cancel()
        // 设置未在下载
        isDownloading.set(false)
        // 更新状态条
        binding.statusText.text = getString(R.string.download_pause)
        // 更新按钮
        updateUI(false)
    }

    // 获取需下载的文件信息
    private suspend fun fetchDownloadInfo(): List<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://repo.azuki.top/downloadinfo.json")
                .build()

            val response = client.newCall(request).execute()
            val jsonString = response.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(jsonString)
            val filesArray = json.getJSONArray("files")

            return@withContext List(filesArray.length()) { index ->
                val fileObject = filesArray.getJSONObject(index)
                FileInfo(
                    fileName = fileObject.getString("file_name"),
                    downloadUrl = fileObject.getString("download_url"),
                    fileSize = fileObject.getString("file_size").toLong(),
                    md5Checksum = fileObject.getString("md5_checksum")
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch download info: ${e.message}")
        }
    }

    // 下载文件的函数
    @SuppressLint("SetTextI18n")
    private suspend fun downloadFile(fileInfo: FileInfo, currentFile: Int, totalFiles: Int) {

        // 初始化UI的下载进度
        withContext(Dispatchers.Main) {
            binding.progressBar.progress = 0
            binding.progressText.text = "0%"
            binding.sizeText.text = ""
            binding.speedText.text = ""
        }

        // 创建文件夹
        val modelDir = File(requireContext().getExternalFilesDir(null), "models")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        // 创建文件
        val file = File(modelDir, fileInfo.fileName)

        // 检查是否存在未下载完成的文件（断点续传）
        var downloadedBytes = if (file.exists()) file.length() else 0

        // 更新下载进度
        currentProgress.downloadedBytes = downloadedBytes

        // 如果已经存在完整文件
        if (downloadedBytes >= fileInfo.fileSize){
            // 验证完整文件的MD5
            if (!verifyCompletedFile(fileInfo, file, fileInfo.md5Checksum)) {
                file.delete()
                throw Exception("MD5 checksum mismatch for ${fileInfo.fileName}")
            }else{
                return
            }
        }

        // 下载文件，支持断点续传
        val request = Request.Builder()
            .url(fileInfo.downloadUrl)
            .apply {
                if (downloadedBytes > 0) {
                    addHeader("Range", "bytes=$downloadedBytes-")
                }
            }
            .build()

        // 发送请求
        val response = client.newCall(request).execute()

        // 请求失败则抛出异常
        if (!response.isSuccessful) {
            throw Exception("Failed to download ${fileInfo.fileName}")
        }

        // 创建文件输入流
        val inputStream = response.body?.byteStream()
        val randomAccessFile = RandomAccessFile(file, "rw")
        randomAccessFile.seek(downloadedBytes)

        // 设置文件缓冲区大小为8192字节
        val buffer = ByteArray(8192)
        var bytesRead: Int
        var lastUpdateTime = System.currentTimeMillis()
        var lastBytesRead = downloadedBytes

        // 开始写入文件
        inputStream?.use { input ->
            randomAccessFile.use { output ->
                while (input.read(buffer).also { bytesRead = it } != -1) {

                    // 检测到取消下载则抛出异常
                    if (!isDownloading.get()) {
                        throw CancellationException("Download cancelled")
                    }

                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    currentProgress.downloadedBytes = downloadedBytes

                    // 更新下载速度，1s更新一次
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= 1000) {
                        val speed = downloadedBytes - lastBytesRead
                        val progress = (downloadedBytes * 100.0 / fileInfo.fileSize).roundToInt()

                        // 更新UI的下载速度和进度
                        withContext(Dispatchers.Main) {
                            binding.progressBar.progress = progress
                            binding.progressText.text = "${progress}%"
                            binding.sizeText.text = formatFileSize(downloadedBytes)+"/"+formatFileSize(fileInfo.fileSize)
                            binding.speedText.text = formatFileSize(speed)+"/s"
                        }

                        lastUpdateTime = currentTime
                        lastBytesRead = downloadedBytes
                    }
                }
            }
        }

        // 验证完整文件的MD5
        if (!verifyCompletedFile(fileInfo, file, fileInfo.md5Checksum)) {
            file.delete()
            throw Exception("MD5 checksum mismatch for ${fileInfo.fileName}")
        }
    }

    private fun verifyCompletedFile(fileInfo: FileInfo, file: File, expectedMd5: String): Boolean {
        return try {
            lifecycleScope.launch(Dispatchers.Main) {
                // 更新UI
                binding.progressBar.progress = 100
                binding.progressText.text = "100%"
                binding.sizeText.text = ""
                binding.speedText.text = ""
                binding.statusText.text = getString(R.string.verify_file, fileInfo.fileName)
            }
            Log.d("MD5", calculateMD5(file) + " except " + expectedMd5)
            calculateMD5(file) == expectedMd5
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun calculateMD5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun deleteAllFiles() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val modelDir = File(requireContext().getExternalFilesDir(null), "models")
                if (modelDir.exists()) {
                    modelDir.listFiles()?.forEach { it.delete() }
                }
                currentProgress = DownloadProgress()

                withContext(Dispatchers.Main) {
                    binding.statusText.text = getString(R.string.delete_download)
                    binding.sizeText.text = ""
                    binding.speedText.text = ""
                    binding.progressBar.progress = 0
                    binding.progressText.text = "0%"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.statusText.text = getString(R.string.delete_download_failed, e.message)
                }
            }
        }
        prefs.setBoolean("Download_NLLB", false)
    }

    private fun updateUI(isDownloading: Boolean) {
        binding.downloadButton.text = if (isDownloading) getString(R.string.stop_download) else getString(R.string.start_download)
    }

    private fun showToast(str: String, isShort: Boolean = false){
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消下载Job
        downloadJob?.cancel()
        // 设置未在下载
        isDownloading.set(false)
    }
}