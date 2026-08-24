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

    // 已展开思考内容的会话消息 id
    private val expandedReasoningIds = mutableSetOf<Long>()
    // 用户点击切换展开状态的消息 id：这些消息在下一次绑定时强制滚动到底部（保证展开位置连贯）
    private val forceScrollIds = mutableSetOf<Long>()

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
        val message = getItem(position)
        holder.bind(
            message = message,
            reasoningExpanded = expandedReasoningIds.contains(message.id),
            forceScrollToEnd = forceScrollIds.remove(message.id),
            onReasoningClick = { id ->
                // 切换展开/收起
                if (!expandedReasoningIds.add(id)) {
                    expandedReasoningIds.remove(id)
                }
                forceScrollIds.add(id)
                notifyItemChanged(holder.adapterPosition)
            },
        )
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.message_time)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)
        private val aiAvatar: ImageView = itemView.findViewById(R.id.ai_avatar)
        private val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        private val reasoningScroll: android.widget.ScrollView =
            itemView.findViewById(R.id.message_reasoning_scroll)
        private val reasoningText: TextView = itemView.findViewById(R.id.message_reasoning)

        fun bind(
            message: ChatMessage,
            reasoningExpanded: Boolean,
            forceScrollToEnd: Boolean,
            onReasoningClick: (Long) -> Unit,
        ) {
            messageText.text = message.content
            messageTime.text = formatTime(message.timestamp)

            // 思考内容显示逻辑（参考 DeepSeek / Chatbox 交互）：
            // - 折叠态：容器只有一行高，始终滚到底部 → 显示“最新一行”，随流式输出滚动
            // - 展开态：容器为完整高度（280dp），内容可滚动；流式更新时仅当用户在
            //   底部才跟随滚动（用户上翻查看时不打扰）
            if (message.sender == 1 && message.reasoning.isNotBlank()) {
                reasoningScroll.visibility = View.VISIBLE
                reasoningText.text = "💭 " + message.reasoning
                reasoningText.maxLines = Int.MAX_VALUE

                // 折叠 = 一行高；展开 = 280dp
                val density = itemView.resources.displayMetrics.density
                val targetHeight = (if (reasoningExpanded) 280 * density else 34 * density).toInt()
                val lp = reasoningScroll.layoutParams
                if (lp.height != targetHeight) {
                    lp.height = targetHeight
                    reasoningScroll.layoutParams = lp
                }

                reasoningScroll.post {
                    val child = reasoningScroll.getChildAt(0) ?: return@post
                    // 用户是否停留在底部（允许 24px 误差）
                    val atBottom =
                        reasoningScroll.scrollY + reasoningScroll.height >= child.height - 24
                    if (!reasoningExpanded || forceScrollToEnd || atBottom) {
                        reasoningScroll.fullScroll(View.FOCUS_DOWN)
                    }
                }
                reasoningScroll.setOnClickListener { onReasoningClick(message.id) }
            } else {
                reasoningScroll.visibility = View.GONE
                reasoningScroll.setOnClickListener(null)
            }

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