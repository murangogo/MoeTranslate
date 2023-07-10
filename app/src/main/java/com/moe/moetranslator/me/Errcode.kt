package com.moe.moetranslator.me

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moe.moetranslator.R


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
        val dialogper3 = AlertDialog.Builder(activity)
            .setTitle("还未完善")
            .setMessage("您现在看到的是百度翻译的错误代码，有关腾讯云的错误代码还在整理中，将在后续版本中加入。")
            .setCancelable(false)
            .setNegativeButton("我知道了") { _, _ ->}
            .create()
        dialogper3.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialogper3.show()
    }

}