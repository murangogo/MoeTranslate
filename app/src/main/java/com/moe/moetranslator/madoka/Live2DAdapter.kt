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

package com.moe.moetranslator.madoka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.R

class Live2DModelAdapter(
    private val onModelClick: (String) -> Unit,
    private val onModelLongClick: (Live2DModel) -> Unit
) : ListAdapter<Live2DModel, Live2DModelAdapter.ViewHolder>(ModelDiffCallback()) {


    private var selectedModelId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drawer_model, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model, model.modelId == selectedModelId)
    }

    fun setSelectedModel(modelId: String) {
        val oldSelection = selectedModelId
        selectedModelId = modelId

        oldSelection?.let { old ->
            val oldPosition = currentList.indexOfFirst { it.modelId == old }
            if (oldPosition != -1) notifyItemChanged(oldPosition)
        }

        val newPosition = currentList.indexOfFirst { it.modelId == modelId }
        if (newPosition != -1) notifyItemChanged(newPosition)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val imageCheck: ImageView = itemView.findViewById(R.id.image_check)

        fun bind(model: Live2DModel, isSelected: Boolean) {
            textName.text = model.displayName
            imageCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
            itemView.isSelected = isSelected  // 添加这行来控制背景

            itemView.setOnClickListener {
                onModelClick(model.modelId)
                setSelectedModel(model.modelId)
            }
            itemView.setOnLongClickListener {
                onModelLongClick(model)
                true
            }
        }
    }

    class ModelDiffCallback : DiffUtil.ItemCallback<Live2DModel>() {
        override fun areItemsTheSame(oldItem: Live2DModel, newItem: Live2DModel) =
            oldItem.modelId == newItem.modelId
        override fun areContentsTheSame(oldItem: Live2DModel, newItem: Live2DModel) =
            oldItem == newItem
    }
}

class Live2DExpressionAdapter(
    private val onExpressionClick: (String) -> Unit,
    private val onExpressionLongClick: (Live2DExpression) -> Unit
) : ListAdapter<Live2DExpression, Live2DExpressionAdapter.ViewHolder>(ExpressionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drawer_exp_mtn, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expression = getItem(position)
        holder.bind(expression)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text_name)

        fun bind(expression: Live2DExpression) {
            textView.text = expression.displayName

            itemView.setOnClickListener {
                onExpressionClick(expression.fileName)
            }
            itemView.setOnLongClickListener {
                onExpressionLongClick(expression)
                true
            }
        }
    }

    class ExpressionDiffCallback : DiffUtil.ItemCallback<Live2DExpression>() {
        override fun areItemsTheSame(oldItem: Live2DExpression, newItem: Live2DExpression) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Live2DExpression, newItem: Live2DExpression) =
            oldItem == newItem
    }
}

class Live2DMotionAdapter(
    private val onMotionClick: (String) -> Unit,
    private val onMotionLongClick: (Live2DMotion) -> Unit
) : ListAdapter<Live2DMotion, Live2DMotionAdapter.ViewHolder>(MotionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drawer_exp_mtn, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val motion = getItem(position)
        holder.bind(motion)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text_name)

        fun bind(motion: Live2DMotion) {
            textView.text = motion.displayName

            itemView.setOnClickListener {
                onMotionClick(motion.fileName)
            }
            itemView.setOnLongClickListener {
                onMotionLongClick(motion)
                true
            }
        }
    }

    class MotionDiffCallback : DiffUtil.ItemCallback<Live2DMotion>() {
        override fun areItemsTheSame(oldItem: Live2DMotion, newItem: Live2DMotion) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Live2DMotion, newItem: Live2DMotion) =
            oldItem == newItem
    }
}