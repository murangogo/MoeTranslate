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

package com.moe.moetranslator

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 启用 Edge-to-Edge，一定要在super.onCreate之前调用
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false
    }

    /**
     * 为指定 View 应用系统栏 padding
     * @param view 需要应用 padding 的 View
     * @param applyTop 是否应用顶部 padding（状态栏）
     * @param applyBottom 是否应用底部 padding（导航栏）
     */
    protected fun applySystemBarsPadding(
        view: View,
        applyTop: Boolean = true,
        applyBottom: Boolean = true
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                if (applyTop) insets.top else v.paddingTop,
                v.paddingRight,
                if (applyBottom) insets.bottom else v.paddingBottom
            )
            windowInsets
        }
    }
}