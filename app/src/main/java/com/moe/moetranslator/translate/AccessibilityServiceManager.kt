package com.moe.moetranslator.translate

object AccessibilityServiceManager {
    private var screenShotService: ScreenShotAccessibilityService? = null

    fun setService(service: ScreenShotAccessibilityService?) {
        screenShotService = service
    }

    fun getService(): ScreenShotAccessibilityService? = screenShotService

    fun takeScreenshot() {
        screenShotService?.takeScreenshot()
    }
}