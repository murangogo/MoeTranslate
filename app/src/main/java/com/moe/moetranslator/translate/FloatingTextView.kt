package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import com.moe.moetranslator.R

object FloatingTextView {
    fun translateTextView(ctx: Context):TextView{
        val textview = TextView(ctx)
        textview.setBackgroundResource(R.drawable.translate_result_shape)
        textview.movementMethod = ScrollingMovementMethod.getInstance()
//        val font = Typeface.createFromAsset(ctx.assets, "translatefonts.ttf")
//        textview.typeface = font
        textview.setTextColor(Color.rgb(232,220,209))
        textview.text = "这里将会显示翻译结果，请注意不要把翻译结果和游戏原文重合，以免影响截图翻译。"
        return textview
    }
}