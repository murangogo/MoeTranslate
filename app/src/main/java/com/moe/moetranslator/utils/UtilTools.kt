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
import kotlin.math.max
import kotlin.math.min
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

    /**
     * 计算两个字符串的相似度百分比
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 相似度 (0-1)
     */
    fun calculateSimilarity(str1: String, str2: String): Double {
        if (str1.isEmpty() && str2.isEmpty()) return 100.0
        if (str1.isEmpty() || str2.isEmpty()) return 0.0

        val distance = levenshteinDistance(str1, str2)
        val maxLength = max(str1.length, str2.length)

        return ((maxLength - distance) * 1.0) / maxLength
    }

    /**
     * 计算Levenshtein距离
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 编辑距离
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val len1 = str1.length
        val len2 = str2.length

        // 创建距离矩阵
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        // 初始化第一行和第一列
        for (i in 0..len1) {
            dp[i][0] = i
        }
        for (j in 0..len2) {
            dp[0][j] = j
        }

        // 填充矩阵
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[len1][len2]
    }
}