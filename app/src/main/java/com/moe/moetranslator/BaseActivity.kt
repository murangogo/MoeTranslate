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