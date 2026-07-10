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

package com.moe.moetranslator.openaimanager

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentOpenaiPresetManagerBinding
import com.moe.moetranslator.me.ManageActivity
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 聚合AI翻译预设管理页：列出已保存的预设，可添加 / 编辑 / 删除，并单选其中一套作为翻译使用的 API。
 * 结构照搬 llamamanager.LlamaModelManagerFragment（去掉下载/导入，添加/编辑走全屏编辑器 OpenAIText）。
 */
class OpenAIPresetManagerFragment : Fragment() {

    private var _binding: FragmentOpenaiPresetManagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: OpenAIPresetRepository
    private lateinit var adapter: OpenAIPresetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOpenaiPresetManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = OpenAIPresetRepository.getInstance(requireContext())

        adapter = OpenAIPresetAdapter(
            onSetActive = { entity ->
                lifecycleScope.launch {
                    repo.setActive(entity.id)
                    toast(getString(R.string.openai_preset_active_set, entity.displayName))
                }
            },
            onDelete = { entity -> confirmDelete(entity) },
            onEdit = { entity -> openEditor(entity.id) },
        )
        binding.recyclerPresets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPresets.adapter = adapter

        binding.btnAddPreset.setOnClickListener { openEditor(-1L) }

        viewLifecycleOwner.lifecycleScope.launch {
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

    /** 打开全屏编辑器；presetId 为 -1 表示新建。编辑器保存进数据库后 finish，返回本页由 Flow 自动刷新。 */
    private fun openEditor(presetId: Long) {
        val intent = Intent(requireContext(), ManageActivity::class.java).apply {
            putExtra(ManageActivity.EXTRA_FRAGMENT_TYPE, ManageActivity.TYPE_FRAGMENT_MANAGE_OPENAI_API)
            putExtra(ManageActivity.EXTRA_OPENAI_PRESET_ID, presetId)
        }
        startActivity(intent)
    }

    private fun confirmDelete(entity: OpenAIPresetEntity) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.openai_preset_delete_title)
            .setMessage(getString(R.string.openai_preset_delete_message, entity.displayName))
            .setNegativeButton(R.string.user_cancel, null)
            .setPositiveButton(R.string.delete_models) { _, _ ->
                lifecycleScope.launch { repo.delete(entity) }
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }
}
