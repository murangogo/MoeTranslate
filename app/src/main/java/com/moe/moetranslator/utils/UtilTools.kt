package com.moe.moetranslator.utils

import android.content.Context
import android.util.DisplayMetrics

class UtilTools {
    companion object{
        @JvmStatic
        fun dip2px(context: Context, dpValue: Double): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        fun getwidth(context: Context):Int{
            return context.resources.displayMetrics.widthPixels
        }
    }
}