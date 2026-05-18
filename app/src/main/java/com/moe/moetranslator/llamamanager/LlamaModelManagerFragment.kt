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

package com.moe.moetranslator.llamamanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.DialogLlamaAddModelBinding
import com.moe.moetranslator.databinding.DialogLlamaPresetPickerBinding
import com.moe.moetranslator.databinding.FragmentLlamaModelManagerBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class LlamaModelManagerFragment : Fragment() {

    private var _binding: FragmentLlamaModelManagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: LlamaModelRepository
    private lateinit var adapter: LlamaModelAdapter

    /** SAF: 让用户选一个本地 GGUF 文件 */
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) handleLocalImport(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLlamaModelManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = LlamaModelRepository.getInstance(requireContext())

        binding.textStorageHint.text = getString(
            R.string.llama_storage_path_hint,
            LlamaModelStorage.modelsDir(requireContext()).absolutePath
        )

        adapter = LlamaModelAdapter(
            onSetActive = { entity ->
                lifecycleScope.launch {
                    repo.setActive(entity.id)
                    toast(getString(R.string.llama_active_set, entity.displayName))
                }
            },
            onDelete = { entity -> confirmDelete(entity) },
        )
        binding.recyclerModels.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerModels.adapter = adapter

        binding.btnAddModel.setOnClickListener { showAddModelSheet() }

        viewLifecycleOwner.lifecycleScope.launch {
            // viewLifecycleOwner.lifecycleScope 在 onDestroyView 之前就被取消，
            // 协程退出后才会清掉 _binding，所以这里直接访问 binding 是安全的。
            repo.observeAll().collectLatest { list ->
                adapter.submitList(list)
                binding.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------- 删除 --------------------

    private fun confirmDelete(entity: LlamaModelEntity) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.llama_delete_confirm_title)
            .setMessage(getString(R.string.llama_delete_confirm_message, entity.displayName))
            .setNegativeButton(R.string.user_cancel, null)
            .setPositiveButton(R.string.delete_models) { _, _ ->
                lifecycleScope.launch {
                    val ok = repo.delete(requireContext(), entity)
                    if (ok) toast(getString(R.string.delete_download))
                }
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    // -------------------- 添加模型入口 --------------------

    private fun showAddModelSheet() {
        val sheet = BottomSheetDialog(requireContext())
        val sheetBinding = DialogLlamaAddModelBinding.inflate(layoutInflater)
        sheet.setContentView(sheetBinding.root)

        sheetBinding.optionOnline.setOnClickListener {
            sheet.dismiss()
            showPresetPickerSheet()
        }
        sheetBinding.optionLocal.setOnClickListener {
            sheet.dismiss()
            // SAF：.gguf 没有标准 MIME，给 */* 让用户自己选；选完后由 handleLocalImport 校验扩展名
            try {
                openDocumentLauncher.launch(arrayOf("*/*"))
            } catch (e: Exception) {
                Log.e(TAG, "Launch SAF failed", e)
                toast(getString(R.string.llama_import_failed, e.message ?: "unknown"))
            }
        }

        sheet.show()
    }

    // -------------------- 从预设列表下载 --------------------

    private fun showPresetPickerSheet() {
        val sheet = BottomSheetDialog(requireContext())
        val sheetBinding = DialogLlamaPresetPickerBinding.inflate(layoutInflater)
        sheet.setContentView(sheetBinding.root)
        sheet.setCancelable(true)

        val repoNet = PresetModelsRepository()
        val downloader = LlamaModelDownloader(requireContext().applicationContext)
        var currentJob: Job? = null

        fun showLoading() {
            sheetBinding.stateLoading.visibility = View.VISIBLE
            sheetBinding.recyclerPresets.visibility = View.GONE
            sheetBinding.stateError.visibility = View.GONE
            sheetBinding.stateDownloading.visibility = View.GONE
        }

        fun showError(msg: String) {
            sheetBinding.stateLoading.visibility = View.GONE
            sheetBinding.recyclerPresets.visibility = View.GONE
            sheetBinding.stateError.visibility = View.VISIBLE
            sheetBinding.stateDownloading.visibility = View.GONE
            sheetBinding.textErrorMessage.text =
                getString(R.string.llama_preset_load_failed, msg)
        }

        fun showList(items: List<PresetModelInfo>) {
            sheetBinding.stateLoading.visibility = View.GONE
            sheetBinding.recyclerPresets.visibility = View.VISIBLE
            sheetBinding.stateError.visibility = View.GONE
            sheetBinding.stateDownloading.visibility = View.GONE
            sheetBinding.recyclerPresets.layoutManager = LinearLayoutManager(requireContext())
            sheetBinding.recyclerPresets.adapter = LlamaPresetAdapter(items) { picked ->
                startPresetDownload(sheet, sheetBinding, downloader, picked) { job -> currentJob = job }
            }
        }

        suspend fun fetchAndShow() {
            showLoading()
            try {
                val items = repoNet.fetch()
                showList(items)
            } catch (e: Exception) {
                Log.e(TAG, "Preset fetch failed", e)
                showError(e.message ?: e.javaClass.simpleName)
            }
        }

        sheetBinding.btnRetry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { fetchAndShow() }
        }
        sheetBinding.btnCancelDownload.setOnClickListener {
            downloader.cancel()
            currentJob?.cancel()
        }

        // 用户关掉 sheet 同时也取消未完成的下载（保留 .part 字节供下次续传）
        sheet.setOnDismissListener {
            downloader.cancel()
            currentJob?.cancel()
        }

        sheet.show()
        viewLifecycleOwner.lifecycleScope.launch { fetchAndShow() }
    }

    private fun startPresetDownload(
        sheet: BottomSheetDialog,
        sheetBinding: DialogLlamaPresetPickerBinding,
        downloader: LlamaModelDownloader,
        info: PresetModelInfo,
        onJob: (Job) -> Unit,
    ) {
        // 同名文件已在库中？提示用户先删除再重下
        viewLifecycleOwner.lifecycleScope.launch {
            val existing = withContext(Dispatchers.IO) { repo.findByFileName(info.fileName) }
            if (existing != null) {
                toast(getString(R.string.llama_already_exists, info.fileName))
                return@launch
            }

            // 切到下载状态
            sheetBinding.stateLoading.visibility = View.GONE
            sheetBinding.recyclerPresets.visibility = View.GONE
            sheetBinding.stateError.visibility = View.GONE
            sheetBinding.stateDownloading.visibility = View.VISIBLE
            sheetBinding.textDownloadName.text = info.fileName
            sheetBinding.progressDownload.progress = 0
            sheetBinding.textDownloadPercent.text = "0%"
            sheetBinding.textDownloadSpeed.text = ""

            downloader.reset()

            val job = viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val file = downloader.download(info) { downloaded, total, bps ->
                        // 回到 UI 线程刷新
                        withContext(Dispatchers.Main) {
                            val pct = if (total > 0) ((downloaded * 100.0) / total).toInt() else 0
                            sheetBinding.progressDownload.progress = pct
                            sheetBinding.textDownloadPercent.text = "$pct%"
                            sheetBinding.textDownloadSpeed.text = getString(
                                R.string.llama_size_speed,
                                LlamaModelStorage.formatSize(downloaded),
                                LlamaModelStorage.formatSize(total),
                                LlamaModelStorage.formatSize(bps),
                            )
                        }
                    }
                    // 入库
                    repo.insert(
                        LlamaModelEntity(
                            fileName = info.fileName,
                            displayName = info.fileName,
                            sizeBytes = file.length(),
                            md5 = info.md5Checksum,
                            source = LlamaModelEntity.SOURCE_PRESET,
                            downloadUrl = info.downloadUrl,
                        )
                    )
                    toast(getString(R.string.llama_download_completed, info.fileName))
                    sheet.dismiss()
                } catch (e: CancellationException) {
                    toast(getString(R.string.llama_download_canceled))
                } catch (e: Exception) {
                    Log.e(TAG, "Download failed", e)
                    toast(getString(R.string.llama_download_failed, e.message ?: e.javaClass.simpleName))
                }
            }
            onJob(job)
        }
    }

    // -------------------- 本地导入 (SAF) --------------------

    @SuppressLint("Range")
    private fun handleLocalImport(uri: Uri) {
        // 取一份显示名 + 大小（如果 ContentResolver 能给）
        var displayName = "imported.gguf"
        var sizeHint: Long = -1
        try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                    if (sizeIdx >= 0) sizeHint = cursor.getLong(sizeIdx)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Query SAF metadata failed", e)
        }

        if (!displayName.endsWith(".gguf", ignoreCase = true)) {
            toast(getString(R.string.llama_not_gguf))
            return
        }

        val finalName = LlamaModelStorage.resolveCollisionFreeName(requireContext(), displayName)
        val targetFile = LlamaModelStorage.modelFile(requireContext(), finalName)

        @Suppress("DEPRECATION")
        val progress = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.llama_import_started))
            setCancelable(false)
            if (sizeHint > 0) {
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                max = 100
                progress = 0
            }
            show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    copyAndDigest(uri, targetFile, sizeHint) { pct ->
                        if (sizeHint > 0) {
                            withContext(Dispatchers.Main) { progress.progress = pct }
                        }
                    }
                }
            }
            progress.dismiss()
            result.onSuccess { md5 ->
                repo.insert(
                    LlamaModelEntity(
                        fileName = finalName,
                        displayName = finalName,
                        sizeBytes = targetFile.length(),
                        md5 = md5,
                        source = LlamaModelEntity.SOURCE_IMPORT,
                        downloadUrl = null,
                    )
                )
                toast(getString(R.string.llama_import_completed, finalName))
            }.onFailure { e ->
                Log.e(TAG, "Import failed", e)
                if (targetFile.exists()) targetFile.delete()
                toast(getString(R.string.llama_import_failed, e.message ?: e.javaClass.simpleName))
            }
        }
    }

    private suspend fun copyAndDigest(
        src: Uri,
        dst: File,
        sizeHint: Long,
        onProgress: suspend (Int) -> Unit,
    ): String {
        val md = MessageDigest.getInstance("MD5")
        var copied = 0L
        var lastPct = -1
        requireContext().contentResolver.openInputStream(src)?.use { input ->
            FileOutputStream(dst).use { output ->
                val buf = ByteArray(16 * 1024)
                while (true) {
                    val read = input.read(buf)
                    if (read == -1) break
                    output.write(buf, 0, read)
                    md.update(buf, 0, read)
                    copied += read
                    if (sizeHint > 0) {
                        val pct = ((copied * 100.0) / sizeHint).toInt().coerceIn(0, 100)
                        if (pct != lastPct) {
                            lastPct = pct
                            onProgress(pct)
                        }
                    }
                }
            }
        } ?: throw java.io.IOException("Cannot open input stream for $src")
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    // -------------------- 工具 --------------------

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "LlamaModelManager"
    }
}
