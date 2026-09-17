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
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.moe.moetranslator.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 会话列表适配器：每行 = 勾选框 + 标题（点击行文本切换会话，点击勾选框切换选中）。
 */
class ChatSessionAdapter(
    val sessions: List<ChatSessionInfo>,
    private val checkedIds: MutableSet<Long>,
    private val activeSessionId: Long,
    private val onOpenSession: (Long) -> Unit,
    private val onToggleCheck: (Long, Boolean) -> Unit,
) : BaseAdapter() {

    override fun getCount(): Int = sessions.size

    override fun getItem(position: Int): ChatSessionInfo = sessions[position]

    override fun getItemId(position: Int): Long = sessions[position].sessionId

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_list, parent, false)

        val checkBox = view.findViewById<CheckBox>(R.id.session_check)
        val label = view.findViewById<TextView>(R.id.session_label)

        val session = sessions[position]
        val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val marker = if (session.sessionId == activeSessionId) "● " else ""
        label.text = "$marker${session.title}   (${dateFormat.format(Date(session.lastTimestamp))})"

        // 勾选框：点击仅切换选中，不触发行点击
        checkBox.setOnCheckedChangeListener(null)
        checkBox.isChecked = checkedIds.contains(session.sessionId)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            onToggleCheck(session.sessionId, isChecked)
        }

        // 行文本点击：切换会话
        label.setOnClickListener {
            if (checkedIds.isEmpty()) {
                onOpenSession(session.sessionId)
            } else {
                // 有勾选时点击行 = 切换该行勾选状态（避免误切换会话）
                checkBox.isChecked = !checkBox.isChecked
            }
        }
        view.setOnClickListener {
            if (checkedIds.isEmpty()) {
                onOpenSession(session.sessionId)
            } else {
                checkBox.isChecked = !checkBox.isChecked
            }
        }

        return view
    }
}
