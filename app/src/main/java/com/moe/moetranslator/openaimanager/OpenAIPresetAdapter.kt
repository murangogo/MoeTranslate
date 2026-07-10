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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.databinding.ItemOpenaiPresetBinding

class OpenAIPresetAdapter(
    private val onSetActive: (OpenAIPresetEntity) -> Unit,
    private val onDelete: (OpenAIPresetEntity) -> Unit,
    private val onEdit: (OpenAIPresetEntity) -> Unit,
) : ListAdapter<OpenAIPresetEntity, OpenAIPresetAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemOpenaiPresetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOpenaiPresetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.binding

        b.textName.text = item.displayName
        b.textMeta.text = item.modelName
        b.radioActive.isChecked = item.isActive
        b.textActiveBadge.visibility = if (item.isActive) View.VISIBLE else View.GONE

        // 整行点击 / radio 点击 → 设为激活；已经是激活则无操作
        val activateClick = View.OnClickListener {
            if (!item.isActive) onSetActive(item)
        }
        b.root.setOnClickListener(activateClick)
        b.radioActive.setOnClickListener(activateClick)

        b.btnEdit.setOnClickListener { onEdit(item) }
        b.btnDelete.setOnClickListener { onDelete(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OpenAIPresetEntity>() {
            override fun areItemsTheSame(old: OpenAIPresetEntity, new: OpenAIPresetEntity) =
                old.id == new.id

            override fun areContentsTheSame(old: OpenAIPresetEntity, new: OpenAIPresetEntity) =
                old == new
        }
    }
}
