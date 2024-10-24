package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.moe.moetranslator.R

class MenuDialogAdapter(ctx: Context, private var str: Array<String>, private var img: Array<Int>) : BaseAdapter() {
    private var lf: LayoutInflater = LayoutInflater.from(ctx)
    override fun getCount(): Int {
        return str.size
    }

    override fun getItem(position: Int): Any {
        return str[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "MissingInflatedId", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val newView = lf.inflate(R.layout.dialog_listview,null)
        val txt: TextView = newView.findViewById(R.id.Introduce)
        val im:ImageView = newView.findViewById(R.id.smallIcon)
        txt.text = str[position]
        im.setImageResource(img[position])
        return newView
    }
}