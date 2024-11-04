package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.RectF
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
import com.moe.moetranslator.utils.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import translationapi.baidutranslation.BaiduTranslationText
import translationapi.nllbtranslation.NLLBTranslation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

// 悬浮球配置
// TODO :自定义反应时间
data class FloatingBallConfig(
    val floatingBallInitialX: Int = 80,
    val floatingBallInitialY: Int = 200,
    val CLICK_SLOP:Float = 5f,           // 点击判定的最大移动距离
    val LONG_PRESS_SLOP:Float = 10f,     // 长按判定的最大移动距离
    val LONG_PRESS_DELAY:Long = 500L   // 长按触发时间（毫秒）
)

data class FloatingTextViewConfig(
    val floatingTextViewInitialX: Int = 0,
    val floatingTextViewInitialY: Int = 0
)

data class CropViewConfig(
    val cropViewInitialX: Int = 50,
    val cropViewInitialY: Int = 50
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
    private lateinit var cropView: CropView

    private var floatingBallParams: WindowManager.LayoutParams? = null
    private var floatingTextViewParams: WindowManager.LayoutParams? = null
    private var cropViewParams: WindowManager.LayoutParams? = null

    private lateinit var prefs: CustomPreference

    // 是否正在翻译，默认false
    private val isTranslating = AtomicBoolean(false)

    // 配置
    private var floatingBallConfig = FloatingBallConfig()
    private var floatingTextViewConfig = FloatingTextViewConfig()
    private var cropViewConfig = CropViewConfig()

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

    // 保存裁剪框状态
    private var mRectF: RectF? = null

    // 保存目前的横竖屏配置
    private var orientation = 1

    // 初始化的翻译对象
    private lateinit var translator: TranslationAPI

    override fun onCreate() {
        super.onCreate()
        prefs = CustomPreference.getInstance(this)
        initialize()
        setupScreenshotCollector()
    }

    @SuppressLint("InflateParams")
    private fun initialize() {
        // 初始化翻译API
        try {
            if (prefs.getInt("Translate_Mode", 0) == 0){
                when (prefs.getInt("OCR_API", 0)) {
                    0 -> when (prefs.getInt("OCR_AI", 0)){
                        0 -> {}
                        1 -> translator = NLLBTranslation(this)
                        else -> {}
                    }
                    1 -> translator = BaiduTranslationText(KeystoreManager.retrieveKey(this, "Baidu_Translate_ACCOUNT")!!, KeystoreManager.retrieveKey(this, "Baidu_Translate_SECRETKEY")!!)
                    else -> {}
                }
            }else{
                when (prefs.getInt("Pic_API", 0)){
                    0 -> {}
                    else -> {}
                }
            }
        } catch (e: Exception){
            showToast("Initialize Error: $e")
        }

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
            gravity = Gravity.CENTER
            x = floatingTextViewConfig.floatingTextViewInitialX
            y = floatingTextViewConfig.floatingTextViewInitialY
        }

        // 设置裁剪框视图参数
        cropViewParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.START or Gravity.TOP
            x = cropViewConfig.cropViewInitialX
            y = cropViewConfig.cropViewInitialY
        }



        // 创建悬浮球视图
        floatingBallView = LayoutInflater.from(this).inflate(R.layout.floatball_layout, null)

        // 创建翻译结果视图
        floatingTextView = FloatingTextView.translateTextView(this)

        // 创建裁剪框视图
        cropView = CropView(this)

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
                                if(isViewAdded(floatingTextView)){
                                    windowManager.removeView(floatingTextView)
                                }else{
                                    showToast(getString(R.string.not_added_remove), true)
                                }
                            }
                        }
                    }
                    3 -> {
                        // 设置字体大小
                    }
                    4 -> {
                        // 停止服务，移除所有窗口（悬浮球、翻译结果框、框选框等）
                        stopServiceAndRemoveViews()
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

        // 若有保存的裁剪框，则直接应用
        if ((orientation == this.resources.configuration.orientation) && (mRectF != null)){
            cropView.setRect(mRectF!!)
        }else{
            cropView.setRect(RectF(5f, 5f, 350f, 350f))
        }

        windowManager.addView(cropView, cropViewParams)

        // 存储屏幕方向
        orientation = this.resources.configuration.orientation

        // 保持悬浮球在最上层
        windowManager.removeView(floatingBallView)
        windowManager.addView(floatingBallView, floatingBallParams)
        currentBallStatus = BallStatus.Crop
    }

    private fun setMovingTextView(){
        if(isViewAdded(floatingTextView)){
            setFloatingTextViewTouchable(true)
        }else{
            windowManager.addView(floatingTextView, floatingTextViewParams)

            // 保持悬浮球在最上层
            windowManager.removeView(floatingBallView)
            windowManager.addView(floatingBallView, floatingBallParams)
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
                    if(!isViewAdded(floatingTextView)){
                        windowManager.addView(floatingTextView, floatingTextViewParams)

                        // 保持悬浮球在最上层
                        windowManager.removeView(floatingBallView)
                        windowManager.addView(floatingBallView, floatingBallParams)
                        setFloatingTextViewTouchable(false)
                    }
                    if (orientation == this.resources.configuration.orientation){
                        if(isTranslating.get()){
                            showToast(getString(R.string.is_translating), true)
                        }else{
                            AccessibilityServiceManager.takeScreenshot(mRectF, cropView.absolutePointOffset)
                        }
                    }else{
                        showToast(getString(R.string.orientation_changed))
                    }
                }
            }
            is BallStatus.Crop -> {
                mRectF = cropView.mRect
                windowManager.removeView(cropView)
                showToast(getString(R.string.finish_crop), true)
                currentBallStatus = BallStatus.Normal
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
                    isTranslating.set(true)
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
                // OCR后文本翻译
                val txt = OCRTextRecognizer.getPicText(prefs.getString("Source_Language", "ja"), bitmap, prefs.getInt("Custom_OCR_Merge_Mode", 2))
                translateByText(txt)
            }else{
                // 上传图片翻译
                translateByPic(bitmap)
            }
        }catch (e: Exception){
            e.printStackTrace()
            showToast("Translate Failed：$e")
        }finally {
            isTranslating.set(false)
            bitmap.recycle()
        }
    }

    // 文本翻译
    private fun translateByText(str: String){
        translator.getTranslation(str, prefs.getString("Source_Language", "ja"), prefs.getString("Target_Language", "zh")){
            result->
            lifecycleScope.launch(Dispatchers.Main) {
                when (result) {
                    is TranslationAPI.TranslationResult.Success -> {
                        if(prefs.getInt("Custom_Show_Source_Mode", 0) == 0){
                            floatingTextView.text = result.translatedText
                        }else if(prefs.getInt("Custom_Show_Source_Mode", 0) == 1){
                            floatingTextView.text = str+"\n\n"+result.translatedText
                        }else{
                            floatingTextView.text = result.translatedText+"\n\n"+str
                        }
                    }
                    is TranslationAPI.TranslationResult.Error -> {
                        floatingTextView.text = getString(R.string.translation_failed, result.error.message)
                    }
                }
            }
        }
    }

    private fun translateByPic(bitmap: Bitmap){
        // TODO 图片翻译
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

    private fun stopServiceAndRemoveViews() {
        try {
            // 移除所有窗口
            if (isViewAdded(floatingBallView)) {
                windowManager.removeView(floatingBallView)
            }
            if (isViewAdded(floatingTextView)) {
                windowManager.removeView(floatingTextView)
            }
            if (isViewAdded(cropView)) {
                windowManager.removeView(cropView)
            }

            // 清理资源
            OCRTextRecognizer.cleanup()
            handler.removeCallbacks(longPressRunnable)
            lifecycleScope.cancel()

            // 停止服务
            stopSelf()
        } catch (e: Exception) {
            showToast("Stop service failed: ${e.message}")
        }
    }

    private fun setFloatingTextViewTouchable(touchable: Boolean) {
        floatingTextView.isClickable = touchable
        if (prefs.getBoolean("Custom_Result_Penetrability", true)){
            floatingTextViewParams?.apply {
                if (touchable) {
                    flags = flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                } else {
                    flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                }
                windowManager.updateViewLayout(floatingTextView, this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除所有窗口
        if (isViewAdded(floatingBallView)) {
            windowManager.removeView(floatingBallView)
        }
        if (isViewAdded(floatingTextView)) {
            windowManager.removeView(floatingTextView)
        }
        if (isViewAdded(cropView)) {
            windowManager.removeView(cropView)
        }

        // 清理资源
        OCRTextRecognizer.cleanup()
        handler.removeCallbacks(longPressRunnable)
        lifecycleScope.cancel()
    }
}