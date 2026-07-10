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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentOpenaiApiBinding
import com.moe.moetranslator.openaimanager.OpenAIPresetEntity
import com.moe.moetranslator.openaimanager.OpenAIPresetRepository
import com.moe.moetranslator.utils.Constants.defaultSystemPrompt
import com.moe.moetranslator.utils.Constants.defaultUserPrompt
import com.moe.moetranslator.utils.Constants.defaultOpenAITemperature
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.launch
import translationapi.openaitranslation.OpenAITranslation

/**
 * 单套「聚合AI翻译」预设的全屏编辑器。
 *
 * 通过 ManageActivity 的 EXTRA_OPENAI_PRESET_ID 传入预设 id：
 *   - id == NEW_PRESET_ID(-1)：新建，表单预填默认提示词、其余留空；
 *   - id >= 0：编辑，从数据库回填该预设。
 *
 * 保存写入数据库（新建 insert / 编辑 update）；若是当前激活预设，Repository 会顺手把值镜像回
 * OpenAI_* 键，运行时下次读取即生效。历史翻译记录等全局设置不在此页。
 */
class OpenAIText : Fragment() {
    private lateinit var binding: FragmentOpenaiApiBinding
    private lateinit var prefs: CustomPreference
    private lateinit var repo: OpenAIPresetRepository

    // 正在编辑的预设 id；-1 表示新建。编辑时缓存原实体以保留 isActive / createdAt。
    private var presetId: Long = NEW_PRESET_ID
    private var editingEntity: OpenAIPresetEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
        repo = OpenAIPresetRepository.getInstance(requireContext())
        presetId = arguments?.getLong(ManageActivity.EXTRA_OPENAI_PRESET_ID, NEW_PRESET_ID) ?: NEW_PRESET_ID
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOpenaiApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        loadConfig()

        if (!prefs.getBoolean("Read_OpenAI_API_Introduce", false)) {
            showIntroduce()
        }
    }

    private fun setupButtons() {
        binding.introduce.setOnClickListener { showIntroduce() }
        binding.btnAddExtraParam.setOnClickListener { addExtraParamRow() }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    private fun showIntroduce() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.introduce_openai_api_title)
            .setMessage(R.string.introduce_openai_api_content)
            .setCancelable(false)
            .setPositiveButton(R.string.user_known, null)
            .setNegativeButton(R.string.view_tutorial) { _, _ ->
                val urlt = "https://www.moetranslate.top/docs/translationapi/uniaitrans/"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse(urlt)
                startActivity(intent)
            }
            .setNeutralButton(R.string.introduce_not_show_again) { _, _ ->
                prefs.setBoolean("Read_OpenAI_API_Introduce", true)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    /** 在「自定义请求参数」区域新增一行键值对（复用通用的 item_key_value_pair 布局）。 */
    private fun addExtraParamRow(key: String = "", value: String = "") {
        val row = layoutInflater.inflate(R.layout.item_key_value_pair, binding.containerExtraParams, false)
        row.findViewById<TextInputEditText>(R.id.editKey).setText(key)
        row.findViewById<TextInputEditText>(R.id.editValue).setText(value)
        row.findViewById<ImageButton>(R.id.btnRemove).setOnClickListener {
            binding.containerExtraParams.removeView(row)
        }
        binding.containerExtraParams.addView(row)
    }

    /** 收集当前所有非空键的自定义参数行。 */
    private fun collectExtraParams(): List<Pair<String, String>> {
        val container = binding.containerExtraParams
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            val key = view.findViewById<TextInputEditText>(R.id.editKey).text.toString().trim()
            val value = view.findViewById<TextInputEditText>(R.id.editValue).text.toString().trim()
            if (key.isNotBlank()) {
                list.add(key to value)
            }
        }
        return list
    }

    private fun saveConfiguration() {
        try {
            if (binding.editApiKey.text.toString().trim().isBlank()) {
                throw Exception(getString(R.string.fill_blank))
            }

            val normalizedUrl = UrlUtils.normalizeUrl(requireContext(), binding.editBaseUrl.text.toString())

            if (binding.editModelName.text.toString().trim().isBlank()) {
                throw Exception(getString(R.string.fill_blank))
            }

            val model = binding.editModelName.text.toString()
            // 预设名称留空则回退到模型名，保证列表里每套预设都有可读的标题
            val name = binding.editPresetName.text.toString().trim().ifBlank { model.trim() }
            val system = binding.editSystemPrompt.text.toString().ifBlank { defaultSystemPrompt }
            val user = binding.editUserPrompt.text.toString().ifBlank { defaultUserPrompt }
            // 温度留空（或非法）即存空串，表示请求时不发送 temperature
            val tempText = binding.editTemperature.text.toString().trim()
            val temperature = if (tempText.isBlank()) "" else (tempText.toFloatOrNull()?.toString() ?: "")
            val extraParams = OpenAITranslation.encodeExtraParams(collectExtraParams())

            lifecycleScope.launch {
                // 用意图里的 presetId 而非异步加载的缓存来判定新建/编辑，避免用户在回填完成前就保存导致误插重复项；
                // 缓存缺失时按 id 重新取一次，取不到（编辑期间被并发删除）才回退为新建。
                val existing = if (presetId >= 0) (editingEntity ?: repo.findById(presetId)) else null
                if (existing == null) {
                    // 新建：insert 会在库空时自动把首套设为激活
                    repo.insert(
                        OpenAIPresetEntity(
                            displayName = name,
                            apiKey = binding.editApiKey.text.toString(),
                            baseUrl = normalizedUrl,
                            modelName = model,
                            systemPrompt = system,
                            userPrompt = user,
                            temperature = temperature,
                            extraParams = extraParams,
                        )
                    )
                } else {
                    // 编辑：保留原 id / isActive / createdAt，若是激活项 update 会重新镜像
                    repo.update(
                        existing.copy(
                            displayName = name,
                            apiKey = binding.editApiKey.text.toString(),
                            baseUrl = normalizedUrl,
                            modelName = model,
                            systemPrompt = system,
                            userPrompt = user,
                            temperature = temperature,
                            extraParams = extraParams,
                        )
                    )
                }
                showToast(getString(R.string.openai_preset_saved))
                requireActivity().finish()
            }
        } catch (e: Exception) {
            showToast(getString(R.string.failed_save_config, e.message))
        }
    }

    private fun loadConfig() {
        lifecycleScope.launch {
            try {
                val entity = if (presetId >= 0) repo.findById(presetId) else null
                editingEntity = entity

                if (entity != null) {
                    binding.editPresetName.setText(entity.displayName)
                    binding.editApiKey.setText(entity.apiKey)
                    binding.editBaseUrl.setText(entity.baseUrl)
                    binding.editModelName.setText(entity.modelName)
                    binding.editSystemPrompt.setText(entity.systemPrompt)
                    binding.editUserPrompt.setText(entity.userPrompt)
                    binding.editTemperature.setText(entity.temperature)
                    binding.containerExtraParams.removeAllViews()
                    OpenAITranslation.decodeExtraParams(entity.extraParams).forEach { (k, v) ->
                        addExtraParamRow(k, v)
                    }
                } else {
                    // 新建：只预填默认提示词，其余留空
                    binding.editSystemPrompt.setText(defaultSystemPrompt)
                    binding.editUserPrompt.setText(defaultUserPrompt)
                    binding.editTemperature.setText(defaultOpenAITemperature.toString())
                    binding.containerExtraParams.removeAllViews()
                }
            } catch (e: Exception) {
                showToast("Error loading configuration: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val NEW_PRESET_ID = -1L
    }
}
