package com.moe.moetranslator.me

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.R

class CardAdapter(private val cards: List<CustomCard>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val COLLAPSED_VIEW_TYPE = 0
    private val EXPANDED_VIEW_TYPE = 1

    inner class CollapsedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    }

    inner class ExpandedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val expandedtitle: LinearLayout = view.findViewById(R.id.expanded_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == COLLAPSED_VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_collapsed, parent, false)
            CollapsedViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_expanded, parent, false)
            ExpandedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val card = cards[position]
        if (holder is CollapsedViewHolder) {
            holder.tvTitle.text = card.title
            holder.itemView.setOnClickListener {
                card.isExpanded = true
                notifyItemChanged(position)
            }
        } else if (holder is ExpandedViewHolder) {
            holder.tvTitle.text = card.title
            holder.tvContent.text = card.content
            holder.expandedtitle.setOnClickListener {
                card.isExpanded = false
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (cards[position].isExpanded) EXPANDED_VIEW_TYPE else COLLAPSED_VIEW_TYPE
    }

    override fun getItemCount() = cards.size
}

data class CustomCard(val title: String, val content: String, var isExpanded: Boolean = false)