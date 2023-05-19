package com.moe.moetranslator

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class Myadapter(ctx: Context, private var str: Array<String>, private var img: Array<Int>) : BaseAdapter() {
    private var lf: LayoutInflater = LayoutInflater.from(ctx)
    override fun getCount(): Int {
        return str.size
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return 0L
    }

    @SuppressLint("ViewHolder", "MissingInflatedId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val newView = lf.inflate(R.layout.my_listview,null)
        val txt: TextView = newView.findViewById(R.id.Introduce)
        val im:ImageView = newView.findViewById(R.id.smallIcon)
        txt.text = str[position]
        im.setImageResource(img[position])
        return newView
    }
}