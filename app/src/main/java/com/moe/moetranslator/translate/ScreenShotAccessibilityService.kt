package com.moe.moetranslator.translate

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.Display.DEFAULT_DISPLAY
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// 单例类管理SharedFlow
object ScreenshotManager {
    private val _screenshotFlow = MutableSharedFlow<Bitmap>()
    val screenshotFlow = _screenshotFlow.asSharedFlow()

    suspend fun emitScreenshot(screenshot: Bitmap) {
        _screenshotFlow.emit(screenshot)
    }
}

class ScreenShotAccessibilityService: AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 当用户点击悬浮球时调用此方法
    fun takeScreenshot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // Android 11及以上
            takeScreenshotImpl()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun takeScreenshotImpl() {
        try {
            takeScreenshot(
                DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        var bitmap: Bitmap? = null
                        try {
                            bitmap = Bitmap.wrapHardwareBuffer(
                                screenshot.hardwareBuffer,
                                screenshot.colorSpace
                            )?.copy(Bitmap.Config.ARGB_8888, true)
                            //使用sharedflow，发送截图完成信号以及bitmap
                            bitmap?.let { nonNullBitmap ->
                                // 在协程中发送截图事件
                                serviceScope.launch {
                                    try {
                                        ScreenshotManager.emitScreenshot(nonNullBitmap)
                                    } catch (e: Exception) {
                                        showToast("Error emitting screenshot：$e")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Screenshot", "Error processing screenshot", e)
                        } finally {
                            screenshot.hardwareBuffer.close()
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        val errorText = when (errorCode){
                            ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR -> "The status of taking screenshot is failure and the reason is internal error."
                            ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS -> "The status of taking screenshot is failure and the reason is no accessibility access."
                            ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT -> "The status of taking screenshot is failure and the reason is that too little time has elapsed since the last screenshot."
                            ERROR_TAKE_SCREENSHOT_INVALID_DISPLAY -> "The status of taking screenshot is failure and the reason is invalid display Id."
                            else -> "Unknown error: $errorCode"
                        }
                        showToast(errorText)
                    }
                }
            )
        } catch (e: Exception) {
            showToast("Failed to take screenshot：$e")
        }
    }


    fun showToast(message: String) {
        serviceScope.launch {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityServiceManager.setService(this)
        Log.d("CONNECT", "Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 处理无障碍事件
    }

    override fun onInterrupt() {
        // 处理中断
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消服务
        AccessibilityServiceManager.setService(null)
        // 取消所有协程
        serviceScope.cancel()
    }
}