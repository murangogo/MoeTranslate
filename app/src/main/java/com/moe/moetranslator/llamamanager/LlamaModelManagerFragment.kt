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
import android.content.Intent
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
import com.moe.moetranslator.databinding.DialogLlamaEditPromptsBinding
import com.moe.moetranslator.databinding.DialogLlamaPresetPickerBinding
import com.moe.moetranslator.databinding.FragmentLlamaModelManagerBinding
import com.moe.moetranslator.utils.Constants.defaultLlamaEnableThinking
import com.moe.moetranslator.utils.Constants.defaultLlamaMaxTokens
import com.moe.moetranslator.utils.Constants.defaultLlamaTemperature
import com.moe.moetranslator.utils.Constants.defaultSystemPrompt
import com.moe.moetranslator.utils.Constants.defaultUserPrompt
import com.moe.moetranslator.utils.CustomPreference
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
    private val TAG = "LlamaModelManager"

    private var _binding: FragmentLlamaModelManagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: LlamaModelRepository
    private lateinit var adapter: LlamaModelAdapter
    private lateinit var prefs: CustomPreference

    /** SAF: 让用户选一个本地 GGUF 文件 */
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) handleLocalImport(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
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
            onEditPrompts = { entity -> showEditPromptsDialog(entity) },
        )
        binding.recyclerModels.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerModels.adapter = adapter

        binding.btnAddModel.setOnClickListener { showAddModelSheet() }

        if(!prefs.getBoolean("Read_LlamaCpp_Introduce", false)){
            showIntroduce()
        }

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

    private fun showIntroduce(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.introduce_llamacpp_title)
            .setMessage(R.string.introduce_llamacpp_content)
            .setCancelable(false)
            .setPositiveButton(R.string.user_known, null)
            .setNegativeButton(R.string.view_tutorial){_,_->
                val urlt = "https://www.moetranslate.top/docs/translationapi/llamacpp/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlt)
                startActivity(intent)
            }
            .setNeutralButton(R.string.introduce_not_show_again){
                    _, _ ->
                prefs.setBoolean("Read_LlamaCpp_Introduce", true)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    // -------------------- 编辑提示词 --------------------

    /**
     * 弹一个对话框让用户编辑当前模型的提示词覆写 + 推理参数（思考开关 / 温度 / 最大 token）。
     *
     * UX：
     *   - 已有 override → 直接预填该 override，让用户在自己的基础上改；
     *   - 无 override → 预填当前的全局默认提示词（来自 SharedPreferences 或硬编码默认），
     *     让用户看到 "起点" 是什么，避免对着空白框猜该写什么；
     *   - 用户清空到全空 / 点 Reset → 保存时提示词归一为 null，运行时回退到全局默认；
     *   - 温度 / 最大 token 留空或非法 → 回退到默认值并夹紧到合理范围；
     *   - 保存按钮：把内容写回 DAO；若该模型是 active，repo.updateConfig 会顺手同步 prefs 镜像。
     */
    private fun showEditPromptsDialog(entity: LlamaModelEntity) {
        val dialogBinding = DialogLlamaEditPromptsBinding.inflate(layoutInflater)

        dialogBinding.textTitle.text = getString(R.string.llama_edit_prompts_title, entity.displayName)
        dialogBinding.editSystemPrompt.setText(entity.systemPromptOverride ?: defaultSystemPrompt)
        dialogBinding.editUserPrompt.setText(entity.userPromptOverride ?: defaultUserPrompt)
        dialogBinding.switchThinking.isChecked = entity.enableThinking
        dialogBinding.editTemperature.setText(entity.temperature.toString())
        dialogBinding.editMaxTokens.setText(entity.maxTokens.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.btnReset.setOnClickListener {
            dialogBinding.editSystemPrompt.setText(defaultSystemPrompt)
            dialogBinding.editUserPrompt.setText(defaultUserPrompt)
            dialogBinding.switchThinking.isChecked = defaultLlamaEnableThinking
            dialogBinding.editTemperature.setText(defaultLlamaTemperature.toString())
            dialogBinding.editMaxTokens.setText(defaultLlamaMaxTokens.toString())
        }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val sys = dialogBinding.editSystemPrompt.text?.toString()?.trim().orEmpty()
            val usr = dialogBinding.editUserPrompt.text?.toString()?.trim().orEmpty()
            // 与全局默认一致就当作 "无 override"，否则写入数据库
            val sysToStore = sys.ifEmpty { defaultSystemPrompt }
            val userToStore = usr.ifEmpty { defaultUserPrompt }
            val thinking = dialogBinding.switchThinking.isChecked
            // 解析数值并夹紧到合理范围；留空或非法时回退默认，避免把 0 / 超大值写进去
            val temperature = dialogBinding.editTemperature.text?.toString()?.trim()
                ?.toFloatOrNull()?.coerceIn(0f, 2f) ?: defaultLlamaTemperature
            val maxTokens = dialogBinding.editMaxTokens.text?.toString()?.trim()
                ?.toIntOrNull()?.coerceIn(16, 4096) ?: defaultLlamaMaxTokens
            lifecycleScope.launch {
                repo.updateConfig(entity.id, sysToStore, userToStore, thinking, temperature, maxTokens)
                toast(getString(R.string.llama_prompt_saved, entity.displayName))
                dialog.dismiss()
            }
        }

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
}
