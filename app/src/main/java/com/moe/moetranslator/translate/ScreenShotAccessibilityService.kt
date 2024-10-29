package com.moe.moetranslator.translate

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
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
import kotlinx.coroutines.withContext

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
    fun takeScreenshot(mRectF: RectF?, offset: Point) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // Android 11及以上
            takeScreenshotImpl(mRectF, offset)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun takeScreenshotImpl(mRectF: RectF?, offset: Point) {
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

                            Log.d("ASSOFFSET", "x:"+offset.x+"  y:"+offset.y)
                            if (mRectF != null){
                                bitmap = Bitmap.createBitmap(
                                    bitmap!!,
                                    mRectF.left.toInt() + offset.x,    // 起始X坐标
                                    mRectF.top.toInt() + offset.y,     // 起始Y坐标
                                    mRectF.width().toInt(), // 裁剪宽度
                                    mRectF.height().toInt() // 裁剪高度
                                )
                            }

                            //使用sharedflow，发送截图完成信号以及bitmap
                            bitmap?.let { nonNullBitmap ->
                                serviceScope.launch {

                                    // 在IO线程保存图片
                                    val savePath = withContext(Dispatchers.IO) {
                                        ImageFileManager.saveBitmapToCache(
                                            applicationContext,
                                            nonNullBitmap
                                        )
                                    }

                                    when(savePath){
                                        null -> {
                                            showToast("Failed to save image")
                                        }
                                        else -> {
                                            try {
                                                ScreenshotManager.emitScreenshot(nonNullBitmap)
                                            } catch (e: Exception) {
                                                showToast("Error emitting screenshot：$e")
                                            }
                                        }
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