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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.databinding.ItemLlamaModelBinding

class LlamaModelAdapter(
    private val onSetActive: (LlamaModelEntity) -> Unit,
    private val onDelete: (LlamaModelEntity) -> Unit,
    private val onEditPrompts: (LlamaModelEntity) -> Unit,
) : ListAdapter<LlamaModelEntity, LlamaModelAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemLlamaModelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLlamaModelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.binding

        b.textName.text = item.displayName
        b.textMeta.text = buildString {
            append(LlamaModelStorage.formatSize(item.sizeBytes))
            append(" · ")
            append(
                when (item.source) {
                    LlamaModelEntity.SOURCE_PRESET -> "Preset"
                    LlamaModelEntity.SOURCE_IMPORT -> "Imported"
                    else -> item.source
                }
            )
        }
        b.radioActive.isChecked = item.isActive
        b.textActiveBadge.visibility = if (item.isActive) android.view.View.VISIBLE else android.view.View.GONE

        // 整行点击 / radio 点击 → 设为激活；已经是激活则无操作
        val activateClick = android.view.View.OnClickListener {
            if (!item.isActive) onSetActive(item)
        }
        b.root.setOnClickListener(activateClick)
        b.radioActive.setOnClickListener(activateClick)

        b.btnEditPrompts.setOnClickListener { onEditPrompts(item) }
        b.btnDelete.setOnClickListener { onDelete(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<LlamaModelEntity>() {
            override fun areItemsTheSame(old: LlamaModelEntity, new: LlamaModelEntity) =
                old.id == new.id

            override fun areContentsTheSame(old: LlamaModelEntity, new: LlamaModelEntity) =
                old == new
        }
    }
}
