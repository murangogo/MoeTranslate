package com.moe.moetranslator.translate

import android.graphics.Point
import android.graphics.RectF

object AccessibilityServiceManager {
    private var screenShotService: ScreenShotAccessibilityService? = null

    fun setService(service: ScreenShotAccessibilityService?) {
        screenShotService = service
    }

    fun getService(): ScreenShotAccessibilityService? = screenShotService

    fun takeScreenshot(mRectF: RectF?, offset: Point) {
        screenShotService?.takeScreenshot(mRectF, offset)
    }
}