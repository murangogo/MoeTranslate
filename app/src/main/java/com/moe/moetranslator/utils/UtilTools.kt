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

package com.moe.moetranslator.utils

import android.content.Context
import kotlin.math.roundToInt

object UtilTools {
    private var density: Float? = null
    private var screenWidth: Int? = null
    private var screenHeight: Int? = null

    /**
     * 初始化工具类
     * @param context Application 的 Context
     */
    fun init(context: Context) {
        if (density == null) {
            val metrics = context.resources.displayMetrics
            density = metrics.density
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }
    }

    /**
     * 将 dp 值转换为像素值
     * @param dpValue dp值
     * @return 对应的像素值
     */
    fun dp2px(dpValue: Float): Int {
        return (dpValue * (density ?: throw IllegalStateException("UtilTools is not initialized")) + 0.5f).roundToInt()
    }

    /**
     * 获取屏幕宽度（像素）
     * @return 屏幕宽度
     */
    fun getScreenWidth(): Int {
        return screenWidth ?: throw IllegalStateException("UtilTools is not initialized")
    }

    /**
     * 获取屏幕高度（像素）
     * @return 屏幕高度
     */
    fun getScreenHeight(): Int {
        return screenHeight ?: throw IllegalStateException("UtilTools is not initialized")
    }

    /**
     * 将像素值转换为 dp 值
     * @param pxValue 像素值
     * @return 对应的 dp 值
     */
    fun px2dp(pxValue: Float): Int {
        return (pxValue / (density ?: throw IllegalStateException("UtilTools is not initialized")) + 0.5f).roundToInt()
    }
}