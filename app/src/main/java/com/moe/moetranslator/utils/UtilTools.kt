package com.moe.moetranslator.utils

import android.content.Context

class UtilTools {
    companion object{
        @JvmStatic
        fun dip2px(context: Context, dpValue: Double): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}