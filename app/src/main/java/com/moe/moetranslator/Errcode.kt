package com.moe.moetranslator

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView


class Errcode : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_errcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        var img : ImageView = view.findViewById(R.id.errsheet)
//        var width:Float = Resources.getSystem().displayMetrics.widthPixels.toFloat()
//        var height:Float = width / 973*3065
//        var layoutParams:ViewGroup.LayoutParams = img.layoutParams
//        layoutParams.height = height.toInt()
//        img.layoutParams = layoutParams
    }

}