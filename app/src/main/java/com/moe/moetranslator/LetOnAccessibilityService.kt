package com.moe.moetranslator

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

class LetOnAccessibilityService: AccessibilityService() {
    companion object {
        const val ACTION_SERVICE_STARTED = "TakeScreenshotOver"
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!=null){
            getScreenshot(intent.getIntExtra("x1",0),intent.getIntExtra("y1",0),intent.getIntExtra("x2",0),intent.getIntExtra("y2",0))
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getScreenshot(CropScaleX1 : Int,CropScaleY1 : Int,CropScaleX2 : Int,CropScaleY2 : Int){
        Log.d("坐标","x1 = $CropScaleX1, y1 = $CropScaleY1, x2 = $CropScaleX2, y2 = $CropScaleY2")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val callback: TakeScreenshotCallback = object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    val hardwarebuffer = screenshotResult.hardwareBuffer
                    val colorSpace = screenshotResult.colorSpace
                    if (hardwarebuffer.width > 0 && hardwarebuffer.height > 0) {
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwarebuffer, colorSpace)
                        Log.d("大小","H = ${bitmap!!.height}, W = ${bitmap.width}")
                        var cropbitmap:Bitmap
                        cropbitmap = if(CropScaleX1==0&&CropScaleY1==0&&CropScaleX2==0&&CropScaleY2==0){
                            bitmap
                        }else{
                            Bitmap.createBitmap(bitmap!!,CropScaleX1,CropScaleY1,CropScaleX2 - CropScaleX1,CropScaleY2 - CropScaleY1)
                        }
                        if (ConstDatas.pictimes > 200) {
                            ConstDatas.pictimes = 0
                        }
                        ConstDatas.pictimes++
                        val screenshotfile = File(ConstDatas.FilePath, ConstDatas.pictimes.toString() + ".jpg")
                        try {
                            val fileOutputStream = FileOutputStream(screenshotfile)
                            cropbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                            fileOutputStream.flush()
                            fileOutputStream.close()
                            Log.d("截图提示","图像编码为"+ConstDatas.pictimes)
                            val broadcastIntent = Intent(ACTION_SERVICE_STARTED)
                            sendBroadcast(broadcastIntent)
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    }
                }
                override fun onFailure(i: Int) {
                    Log.d("Fail", "onFailure = $i")
                }
            }
            takeScreenshot(Display.DEFAULT_DISPLAY, this.mainExecutor, callback)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("connect","service connected successful");
    }
}