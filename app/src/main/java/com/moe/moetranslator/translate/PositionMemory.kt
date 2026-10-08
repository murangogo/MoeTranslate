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

import android.graphics.Point
import android.graphics.RectF
import com.moe.moetranslator.utils.CustomPreference

/**
 * 悬浮窗位置记忆：自动记住上一次的悬浮球位置、译文窗口位置与裁剪框区域，
 * 并在下次服务启动时恢复。横竖屏分别存储（orientation 后缀：1=竖屏 2=横屏），
 * 避免方向切换后坐标体系不一致导致窗口跑到屏幕外。
 */
object PositionMemory {

    // ---- 悬浮球（gravity = START|TOP，坐标即相对屏幕左上角的偏移）----
    private const val BALL_X = "Position_Ball_X_o"
    private const val BALL_Y = "Position_Ball_Y_o"

    // ---- 译文窗口（gravity = CENTER，坐标即相对屏幕中心的偏移）----
    private const val TEXT_X = "Position_Text_X_o"
    private const val TEXT_Y = "Position_Text_Y_o"

    // ---- 裁剪框（RectF 的 left/top/right/bottom，CropView 内部像素坐标）----
    private const val CROP_L = "Position_Crop_L_o"
    private const val CROP_T = "Position_Crop_T_o"
    private const val CROP_R = "Position_Crop_R_o"
    private const val CROP_B = "Position_Crop_B_o"

    // ==================== 悬浮球 ====================

    fun saveBallPosition(prefs: CustomPreference, orientation: Int, x: Int, y: Int) {
        prefs.setInt("$BALL_X$orientation", x)
        prefs.setInt("$BALL_Y$orientation", y)
    }

    /** 返回保存的悬浮球位置；没有保存记录时返回 null。 */
    fun loadBallPosition(prefs: CustomPreference, orientation: Int): Point? {
        if (!prefs.contains("$BALL_X$orientation") || !prefs.contains("$BALL_Y$orientation")) return null
        return Point(
            prefs.getInt("$BALL_X$orientation", 0),
            prefs.getInt("$BALL_Y$orientation", 0)
        )
    }

    // ==================== 译文窗口 ====================

    fun saveTextPosition(prefs: CustomPreference, orientation: Int, x: Int, y: Int) {
        prefs.setInt("$TEXT_X$orientation", x)
        prefs.setInt("$TEXT_Y$orientation", y)
    }

    /** 返回保存的译文窗口位置；没有保存记录时返回 null。 */
    fun loadTextPosition(prefs: CustomPreference, orientation: Int): Point? {
        if (!prefs.contains("$TEXT_X$orientation") || !prefs.contains("$TEXT_Y$orientation")) return null
        return Point(
            prefs.getInt("$TEXT_X$orientation", 0),
            prefs.getInt("$TEXT_Y$orientation", 0)
        )
    }

    // ==================== 裁剪框 ====================

    fun saveCropRect(prefs: CustomPreference, orientation: Int, rect: RectF) {
        prefs.setFloat("$CROP_L$orientation", rect.left)
        prefs.setFloat("$CROP_T$orientation", rect.top)
        prefs.setFloat("$CROP_R$orientation", rect.right)
        prefs.setFloat("$CROP_B$orientation", rect.bottom)
    }

    /**
     * 返回保存的裁剪框；没有保存记录或记录非法（宽高不足）时返回 null，
     * 调用方应回退到默认裁剪框。
     */
    fun loadCropRect(prefs: CustomPreference, orientation: Int): RectF? {
        if (!prefs.contains("$CROP_L$orientation") || !prefs.contains("$CROP_T$orientation") ||
            !prefs.contains("$CROP_R$orientation") || !prefs.contains("$CROP_B$orientation")
        ) {
            return null
        }

        val left = prefs.getFloat("$CROP_L$orientation", 0f)
        val top = prefs.getFloat("$CROP_T$orientation", 0f)
        val right = prefs.getFloat("$CROP_R$orientation", 0f)
        val bottom = prefs.getFloat("$CROP_B$orientation", 0f)

        // 合法性校验：方向正确且不小于最小宽高，坐标不小于 0
        if (left >= right || top >= bottom ||
            left < 0f || top < 0f ||
            right - left < RECT_MIN_WIDTH || bottom - top < RECT_MIN_HEIGHT
        ) {
            return null
        }

        return RectF(left, top, right, bottom)
    }
}
