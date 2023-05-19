package com.moe.moetranslator

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.widget.TextView

class FloatingTextView(private var context: Context) {
    private var textview = TextView(context)
    fun TranslateTextView():TextView{
        textview.setBackgroundResource(R.drawable.translate_result_shape)
        textview.movementMethod = ScrollingMovementMethod.getInstance()
        val font = Typeface.createFromAsset(context.assets, "translatefonts.ttf")
        textview.typeface = font
        textview.setTextColor(Color.rgb(227,218,214))
        textview.text = "这里将会显示翻译结果，请注意不要把翻译结果和游戏原文重合，以免影响截图翻译。"
        return textview
    }
}