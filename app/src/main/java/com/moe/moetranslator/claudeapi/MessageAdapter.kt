package com.moe.moetranslator.claudeapi

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.R

class MessageAdapter(private var messages: List<ChatMessage>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val messageView: View) : RecyclerView.ViewHolder(messageView){
        val textView: TextView = messageView.findViewById(R.id.respone_text)
        var imgview : ImageView = messageView.findViewById(R.id.userimg)
        var mylinearlayout = messageView.findViewById<LinearLayout>(R.id.recycleadapter)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.MessageViewHolder {
        // 创建新视图
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_adapter, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int) {
        val currentMessage = messages[position]
        holder.textView.text = currentMessage.content
        if (currentMessage.sender==2){
            holder.imgview.setImageResource(R.drawable.userimg)
            holder.mylinearlayout.setBackgroundColor(Color.argb(217,247,247,248))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setMessages(messages: List<ChatMessage>) {
        this.messages = messages
        notifyDataSetChanged()
    }
}