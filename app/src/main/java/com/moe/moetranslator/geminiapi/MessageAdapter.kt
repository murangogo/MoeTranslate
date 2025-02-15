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

package com.moe.moetranslator.geminiapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter : ListAdapter<ChatMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    sealed class MessageViewType {
        object AI : MessageViewType()
        object User : MessageViewType()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).sender) {
            1 -> 0 // AI消息
            else -> 1 // 用户消息
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_message_adapter, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.message_time)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)
        private val aiAvatar: ImageView = itemView.findViewById(R.id.ai_avatar)
        private val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)

        fun bind(message: ChatMessage) {
            messageText.text = message.content
            messageTime.text = formatTime(message.timestamp)

            when (message.sender) {
                1 -> { // AI消息
                    aiAvatar.apply {
                        visibility = View.VISIBLE
                        setImageResource(R.drawable.gemini)
                    }
                    userAvatar.visibility = View.GONE

                    // 设置消息容器布局
                    (messageContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToEnd = R.id.ai_avatar
                        endToStart = R.id.user_avatar
                        horizontalBias = 0f
                        width = 0 // 确保使用约束宽度
                    }
                    messageContainer.background = ContextCompat.getDrawable(itemView.context, R.drawable.message_bubble_ai)
                }
                else -> { // 用户消息
                    userAvatar.apply {
                        visibility = View.VISIBLE
                        setImageResource(R.drawable.floating_ball_icon)
                    }
                    aiAvatar.visibility = View.GONE

                    // 设置消息容器布局
                    (messageContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToEnd = R.id.ai_avatar
                        endToStart = R.id.user_avatar
                        horizontalBias = 1f
                        width = 0 // 确保使用约束宽度
                    }
                    messageContainer.background = ContextCompat.getDrawable(itemView.context, R.drawable.message_bubble_user)
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}

// DiffUtil用于高效更新列表
private class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        // 使用id作为唯一标识
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}