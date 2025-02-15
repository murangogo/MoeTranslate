/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.moe.moetranslator.translate

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import java.io.File

object FloatingTextView {
    fun translateTextView(ctx: Context):TextView{
        val textView = TextView(ctx)
        val prefs = CustomPreference.getInstance(ctx)

        // 使用drawable设置背景颜色、圆角
        val shape = GradientDrawable().apply {
            setColor(prefs.getInt("Custom_Result_Background_Color", -649384925))
            cornerRadius = 15f
        }
        textView.background = shape

        // 设置文字颜色
        textView.setTextColor(prefs.getInt("Custom_Result_Font_Color", -1516335))

        // 设置文字大小，使用sp单位
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getFloat("Custom_Result_Font_Size", 16f))

        textView.apply {
            // 设置内边距
            setPadding(20, 15, 15, 20)
            // 设置文字对齐方式
            gravity = Gravity.START

            // 设置字体
            val customFont = prefs.getString("Custom_Result_Font", "")
            if (customFont.isEmpty()) {
                typeface = Typeface.DEFAULT
            } else {
                try {
                    // 构建字体文件路径
                    val fontFile = File(ctx.getExternalFilesDir(null), "font/$customFont")
                    if (fontFile.exists()) {
                        // 从文件创建Typeface
                        typeface = Typeface.createFromFile(fontFile)
                    } else {
                        // 如果文件不存在，使用默认字体
                        typeface = Typeface.DEFAULT
                        Toast.makeText(ctx, "Not Found Font File.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // 如果加载字体出错，使用默认字体
                    typeface = Typeface.DEFAULT
                    e.printStackTrace()
                    Toast.makeText(ctx, "Load Font Failed:$e", Toast.LENGTH_LONG).show()
                }
            }

            // 设置阴影效果
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }

        textView.text = ctx.getString(R.string.textview_tip)

        return textView
    }
}