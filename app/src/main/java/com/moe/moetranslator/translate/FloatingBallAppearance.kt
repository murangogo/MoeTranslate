package com.moe.moetranslator.translate

import android.content.SharedPreferences

/** 尺寸以 dp 持久化，只有创建悬浮窗时才按当前屏幕密度转换。 */
data class FloatingBallAppearance(val widthDp: Int, val heightDp: Int, val opacity: Int) {
    fun save(prefs: SharedPreferences) {
        prefs.edit()
            .putInt(WIDTH, widthDp)
            .putInt(HEIGHT, heightDp)
            .putInt(OPACITY, opacity)
            .apply()
    }

    companion object {
        val sizeRange = 24..160
        val opacityRange = 10..100
        private const val WIDTH = "Custom_Floating_Width_Dp"
        private const val HEIGHT = "Custom_Floating_Height_Dp"
        private const val OPACITY = "Custom_Floating_Opacity"

        fun load(prefs: SharedPreferences) = FloatingBallAppearance(
            prefs.getInt(WIDTH, 65).coerceIn(sizeRange),
            prefs.getInt(HEIGHT, 65).coerceIn(sizeRange),
            prefs.getInt(OPACITY, 100).coerceIn(opacityRange),
        )
    }
}
