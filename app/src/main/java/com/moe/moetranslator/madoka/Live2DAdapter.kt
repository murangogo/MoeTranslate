package com.moe.moetranslator.madoka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class Live2DModelAdapter(
    private val onModelClick: (String) -> Unit,
    private val onModelLongClick: (Live2DModel) -> Unit
) : ListAdapter<Live2DModel, Live2DModelAdapter.ViewHolder>(ModelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(model: Live2DModel) {
            textView.text = model.displayName

            itemView.setOnClickListener { onModelClick(model.modelId) }
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
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expression = getItem(position)
        holder.bind(expression)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

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
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val motion = getItem(position)
        holder.bind(motion)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

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