package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs

// 悬浮球配置
// TODO :自定义反应时间
data class FloatingBallConfig(
    val floatingBallInitialX: Int = 80,
    val floatingBallInitialY: Int = 80,
    val CLICK_SLOP:Float = 5f,           // 点击判定的最大移动距离
    val LONG_PRESS_SLOP:Float = 10f,     // 长按判定的最大移动距离
    val LONG_PRESS_DELAY:Long = 500L   // 长按触发时间（毫秒）
)

data class FloatingTextViewConfig(
    val floatingBallInitialX: Int = 50,
    val floatingBallInitialY: Int = 50
)

// 手势类型
sealed class GestureType {
    object Click : GestureType()
    object LongPress : GestureType()
    object Drag : GestureType()
}

// 状态
sealed class BallStatus {
    object Normal : BallStatus()
    object Crop : BallStatus()
    object MovingText : BallStatus()
}

class FloatingBallService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingBallView: View
    private lateinit var floatingTextView: TextView
    private var floatingBallParams: WindowManager.LayoutParams? = null
    private var floatingTextViewParams: WindowManager.LayoutParams? = null
    private var cropViewParams: WindowManager.LayoutParams? = null

    private lateinit var prefs: CustomPreference

    // 配置
    private var floatingBallConfig = FloatingBallConfig()
    private var floatingTextViewConfig = FloatingTextViewConfig()

    // 悬浮球触摸相关变量
    private var floatingBallInitialX: Int = 0
    private var floatingBallInitialY: Int = 0
    private var floatingBallInitialTouchX: Float = 0f
    private var floatingBallInitialTouchY: Float = 0f

    // 翻译结果触摸相关变量
    private var floatingTextViewInitialX: Int = 0
    private var floatingTextViewInitialY: Int = 0
    private var floatingTextViewInitialTouchX: Float = 0f
    private var floatingTextViewInitialTouchY: Float = 0f

    // 长按处理器
    private val handler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable { handleLongPress() }

    // 当前手势类型
    private var currentGesture: GestureType? = null

    // 当前悬浮球状态
    private var currentBallStatus: BallStatus = BallStatus.Normal

    override fun onCreate() {
        super.onCreate()
        prefs = CustomPreference.getInstance(this)
        initialize()
        setupScreenshotCollector()
    }

    @SuppressLint("InflateParams")
    private fun initialize() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 创建悬浮窗参数
        floatingBallParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.START or Gravity.TOP
            x = floatingBallConfig.floatingBallInitialX
            y = floatingBallConfig.floatingBallInitialY
        }

        // 设置翻译结果视图参数
        floatingTextViewParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = floatingTextViewConfig.floatingBallInitialX
            y = floatingTextViewConfig.floatingBallInitialY
        }

        // 设置裁剪框视图参数
        cropViewParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }



        // 创建悬浮球视图
        floatingBallView = LayoutInflater.from(this).inflate(R.layout.floatball_layout, null)

        // 创建翻译结果视图
        floatingTextView = FloatingTextView.translateTextView(this)

        // 添加到窗口
        windowManager.addView(floatingBallView, floatingBallParams)

        // 设置点击接收器
        setupTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        floatingBallView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    floatingBallInitialX = floatingBallParams?.x ?: 0
                    floatingBallInitialY = floatingBallParams?.y ?: 0
                    floatingBallInitialTouchX = event.rawX
                    floatingBallInitialTouchY = event.rawY

                    // 开始长按检测
                    handler.postDelayed(longPressRunnable, floatingBallConfig.LONG_PRESS_DELAY)
                    currentGesture = null
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // 总移动距离
                    val totalMoveX = abs(event.rawX - floatingBallInitialTouchX)
                    val totalMoveY = abs(event.rawY - floatingBallInitialTouchY)

                    // 判断总移动距离是否超出长按移动阈值
                    if (totalMoveX > floatingBallConfig.LONG_PRESS_SLOP || totalMoveY > floatingBallConfig.LONG_PRESS_SLOP) {
                        handler.removeCallbacks(longPressRunnable)
                    }

                    // 如果移动距离足够大，判定为拖动
                    if (totalMoveX > floatingBallConfig.CLICK_SLOP || totalMoveY > floatingBallConfig.CLICK_SLOP) {
                        currentGesture = GestureType.Drag
                        // 更新悬浮球位置
                        floatingBallParams?.apply {
                            x = (floatingBallInitialX + (event.rawX - floatingBallInitialTouchX)).toInt()
                            y = (floatingBallInitialY + (event.rawY - floatingBallInitialTouchY)).toInt()
                            windowManager.updateViewLayout(floatingBallView, this)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 移除长按检测
                    handler.removeCallbacks(longPressRunnable)

                    // 处理点击事件
                    if (currentGesture == null) {
                        val totalMoveX = abs(event.rawX - floatingBallInitialTouchX)
                        val totalMoveY = abs(event.rawY - floatingBallInitialTouchY)
                        if (totalMoveX <= floatingBallConfig.CLICK_SLOP && totalMoveY <= floatingBallConfig.CLICK_SLOP) {
                            handleClick()
                        }
                    }

                    currentGesture = null
                    true
                }
                else -> false
            }
        }

        floatingTextView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(view.isClickable){
                        floatingTextViewInitialX = floatingTextViewParams?.x ?: 0
                        floatingTextViewInitialY = floatingTextViewParams?.y ?: 0
                        floatingTextViewInitialTouchX = event.rawX
                        floatingTextViewInitialTouchY = event.rawY
                        true
                    }else{
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    view.isClickable
                }
                MotionEvent.ACTION_MOVE -> {
                    if(view.isClickable){
                        floatingTextViewParams?.apply {
                            x = (floatingTextViewInitialX + (event.rawX - floatingTextViewInitialTouchX)).toInt()
                            y = (floatingTextViewInitialY + (event.rawY - floatingTextViewInitialTouchY)).toInt()
                            windowManager.updateViewLayout(floatingTextView, this)
                        }
                        true
                    }else{
                        false
                    }
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun handleLongPress() {
        currentGesture = GestureType.LongPress
        lifecycleScope.launch {
            // 处理长按事件，显示菜单
            showLongPressMenu()
        }
    }

    private fun showLongPressMenu() {
        val (dialog, listView) = Dialogs.menuDialog(applicationContext)
        listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        when (currentBallStatus) {
                            is BallStatus.Crop -> showToast(getString(R.string.repeat_crop))
                            is BallStatus.MovingText -> showToast(getString(R.string.textview_first))
                            is BallStatus.Normal -> setCropView()
                        }
                    }
                    1 -> {
                        when (currentBallStatus) {
                            is BallStatus.Crop -> showToast(getString(R.string.crop_first))
                            is BallStatus.MovingText -> showToast(getString(R.string.repeat_textview))
                            is BallStatus.Normal -> setMovingTextView()
                        }
                    }
                    2 -> {
                        when (currentBallStatus) {
                            is BallStatus.Crop -> showToast(getString(R.string.crop_first))
                            is BallStatus.MovingText -> showToast(getString(R.string.textview_first))
                            is BallStatus.Normal -> {
                                // 判断是否添加了翻译结果
                            }
                        }
                    }
                    3 -> {
                        // 设置字体大小
                    }
                    4 -> {
                        // 停止服务，关闭所有窗口
                    }
                    5 -> {
                        // 回到主界面
                    }
                }
                dialog.dismiss()
            }
        }
        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setCropView(){

    }

    private fun setMovingTextView(){
        if(isViewAdded(floatingTextView)){
            setFloatingTextViewTouchable(true)
        }else{
            windowManager.addView(floatingTextView, floatingTextViewParams)
            setFloatingTextViewTouchable(true)
        }
        currentBallStatus = BallStatus.MovingText
    }

    private fun handleClick() {
        when (currentBallStatus){
            is BallStatus.Normal -> {
                if (AccessibilityServiceManager.getService() == null) {
                    showToast(getString(R.string.accessibility_recycle))
                    return
                }else{
                    AccessibilityServiceManager.takeScreenshot()
                }
            }
            is BallStatus.Crop -> {

            }
            is BallStatus.MovingText -> {
                setFloatingTextViewTouchable(false)
                showToast(getString(R.string.finish_textview), true)
                currentBallStatus = BallStatus.Normal
            }
        }
    }

    private fun setupScreenshotCollector() {
        lifecycleScope.launch {
            ScreenshotManager.screenshotFlow.collect { bitmap ->
                try {
                    Log.d("SCREENSHOT", "getScreenShot")
                    processScreenshot(bitmap)
                } catch (e: Exception) {
                    showToast("OCR Failed：$e")
                }
            }
        }
    }

    private suspend fun processScreenshot(bitmap: Bitmap) {
        Log.d("SCREENSHOT", "processScreenShot")
        try{
            if(prefs.getInt("Translate_Mode", 0) == 0){
                // TODO OCR翻译
                val txt = OCRTextRecognizer.getPicText("zh", bitmap)
                floatingTextView.text = txt
                val str = ""
                translateByText(str)
            }else{
                // 上传图片翻译
                translateByPic(bitmap)
            }
        }catch (e: Exception){
            e.printStackTrace()
            showToast("Translate Failed：$e")
        }finally {
            bitmap.recycle()
        }
    }

    private fun translateByText(str: String){

    }

    private fun translateByPic(bitmap: Bitmap){

    }

    fun showToast(message: String, isShort: Boolean = false) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isShort) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isViewAdded(v: View): Boolean {
        return try {
            // 尝试更新View的LayoutParams
            // 如果View没有被添加，会抛出IllegalArgumentException
            windowManager.updateViewLayout(v, v.layoutParams)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun setFloatingTextViewTouchable(touchable: Boolean) {
        floatingTextView.isClickable = touchable
        // TODO 可穿透放入个性化设置
        floatingTextViewParams?.apply {
            if (touchable) {
                flags = flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            } else {
                flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
            windowManager.updateViewLayout(floatingTextView, this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        OCRTextRecognizer.cleanup()
        handler.removeCallbacks(longPressRunnable)
        windowManager.removeView(floatingBallView)
        lifecycleScope.cancel()
    }
}