package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.RectF
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import android.widget.Toast
import com.moe.moetranslator.utils.ConstDatas
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.R
import com.moe.moetranslator.translate.TranslateFragment.Companion.config
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import translateapi.http.HttpStringCallback
import translateapi.pic.PicTranslate
import kotlin.math.abs


class FloatingService : Service() {
    private lateinit var mWindowManager: WindowManager  //窗口管理器
    private lateinit var FloatingBallView: View    //悬浮球视图
    private lateinit var FloatingBallParams: WindowManager.LayoutParams //悬浮球视图参数
    private lateinit var dialogs: Dialogs    //对话框视图
    private lateinit var cropview: CropView //裁剪框视图
    private lateinit var CropViewParams: WindowManager.LayoutParams //裁剪框视图参数
    private lateinit var TextViewParams: WindowManager.LayoutParams //裁剪框视图参数
    private lateinit var repository: MySharedPreferenceData
    private lateinit var textview:TextView
    private var TouchPoint = Point()
    private var OriPoint = Point()
    private var TextTouchPoint = Point()
    private var TextOriPoint = Point()
    private var mtime:Long = 0L
    private val picTranslate = PicTranslate()
    private lateinit var intent : Intent
    private var isadded : Boolean = false
    private var translateFinish : Boolean = true

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LetOnAccessibilityService.ACTION_SERVICE_STARTED) {
                config.pic(ConstDatas.FilePath + "/" + ConstDatas.pictimes + ".jpg")
                Log.d("Path", ConstDatas.FilePath + "/" + ConstDatas.pictimes + ".jpg")
                picTranslate.setConfig(config)
                picTranslate.trans(object : HttpStringCallback() {
                    override fun onSuccess(response: String?) {
                        super.onSuccess(response)
                        Log.d("resp是","$response")
                        AnalysisJson.jsonParse(response!!)
                        Log.d("结果","${Result.ResultWords}")
                        MainScope().launch {
                            if(Result.ErrorCode=="0"){
                                textview.text = Result.ResultWords
                            }else{
                                textview.text = "发生错误，错误码为：${Result.ErrorCode}，您可在萌译的“Me”页面查找有关此错误码的信息。"
                            }
                            translateFinish = true
                        }
                    }
                    override fun onFailure(e: Throwable?) {
                        super.onFailure(e)
                        MainScope().launch {
                            textview.text = "发生了未知错误，可能是由于网络未连接。"
                            Log.e("ERR",e.toString())
                            translateFinish = true
                        }
                    }
                })
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreate() {
        super.onCreate()

        // 注册广播接收器
        Log.d("注意","正在注册接收器")
        val filter = IntentFilter(LetOnAccessibilityService.ACTION_SERVICE_STARTED)
        registerReceiver(receiver, filter)
        Log.d("注意","接收器已注册")

        intent = Intent(this, LetOnAccessibilityService::class.java)
        repository = MySharedPreferenceData(this)
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager  //初始化视窗管理器
        dialogs = Dialogs() //创建Dialogs对象
        textview = FloatingTextView(this).TranslateTextView()
        FloatingBallView = LayoutInflater.from(this).inflate(R.layout.floatball_layout, null)   //加载悬浮球视图
        cropview = CropView(this)   //加载裁剪框视图


        FloatingBallParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )   //设置悬浮球视图参数

        FloatingBallParams.x = 0
        FloatingBallParams.y = 200
        FloatingBallParams.gravity = Gravity.TOP or Gravity.START   //悬浮球参数

        CropViewParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )   //设置裁剪框视图参数

        TextViewParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )   //设置翻译结果视图参数

        mWindowManager.addView(FloatingBallView, FloatingBallParams)    //添加悬浮球视图

        FloatingBallView.findViewById<View>(R.id.root_container).setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            mtime = System.currentTimeMillis()
                            OriPoint.x = FloatingBallParams.x
                            OriPoint.y = FloatingBallParams.y
                            TouchPoint.x = event.rawX.toInt()
                            TouchPoint.y = event.rawY.toInt()
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val diffx = abs(event.rawX.toInt() - TouchPoint.x)
                            val diffy = abs(event.rawY.toInt() - TouchPoint.y)
                            val difft = abs(System.currentTimeMillis()-mtime)
                            if((diffx>10)||(diffy>10)){
                                Log.d("移动","移动")
                            }else if(difft<500){
                                if(repository.IsCrop){
                                    makeToast("选取完成")
                                    repository.saveCrop(false)
                                    mWindowManager.removeView(cropview)
                                    intent.putExtra("x1",repository.CropScaleX1)
                                    intent.putExtra("y1",repository.CropScaleY1)
                                    intent.putExtra("x2",repository.CropScaleX2)
                                    intent.putExtra("y2",repository.CropScaleY2)
                                }else if(repository.IsMovingText){
                                    makeToast("调整完成")
                                    repository.saveMovingText(false)
                                    textview.isClickable = false
                                }else{
                                    if(applicationContext.resources.configuration.orientation != repository.ScreenConfiguration){
                                        makeToast("检测到您改变了屏幕方向，请重新选取范围。")
                                    }else {
                                        if (!isadded) {
                                            mWindowManager.addView(textview, TextViewParams)
                                            textview.isClickable = false
                                            repository.saveMovingText(false)
                                            mWindowManager.removeView(FloatingBallView)
                                            mWindowManager.addView(
                                                FloatingBallView,
                                                FloatingBallParams
                                            )
                                            isadded = true
                                        }
                                        if (translateFinish) {
                                            TranslateFunction()
                                        } else {
                                            makeToast("上一个翻译任务还在进行中，请稍等...")
                                        }
                                    }
                                }
                            }else{
                                Log.d("长按","长按")
                                showMyDialog(1)
                            }
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            FloatingBallParams.x = OriPoint.x + (event.rawX.toInt() - TouchPoint.x)
                            FloatingBallParams.y = OriPoint.y + (event.rawY.toInt() - TouchPoint.y)
                            mWindowManager.updateViewLayout(FloatingBallView, FloatingBallParams)
                            return true
                        }
                    }
                    return false
                }
            })

        textview.setOnTouchListener(object : OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if(textview.isClickable){
                            TextOriPoint.x = TextViewParams.x
                            TextOriPoint.y = TextViewParams.y
                            TextTouchPoint.x = event.rawX.toInt()
                            TextTouchPoint.y = event.rawY.toInt()
                            return true
                        }else{
                            return false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        return textview.isClickable
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if(textview.isClickable){
                            TextViewParams.x = TextOriPoint.x + (event.rawX.toInt() - TextTouchPoint.x)
                            TextViewParams.y = TextOriPoint.y + (event.rawY.toInt() - TextTouchPoint.y)
                            mWindowManager.updateViewLayout(textview, TextViewParams)
                            return true
                        }else{
                            return false
                        }
                    }
                }
                return false
            }
        })
    }

        fun TranslateFunction(){
            Log.d("重要提示","进入了函数")
            translateFinish = false
            applicationContext.startService(intent)
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when (intent!!.getIntExtra("Opera",0)){
                1->{
                        if(applicationContext.resources.configuration.orientation==1){
                            repository.saveScreenConfiguration(1)
                        }else{
                            repository.saveScreenConfiguration(2)
                        }
                        cropview.setRect(RectF(5f,5f,350f,350f))
                        mWindowManager.addView(cropview,CropViewParams)
                        mWindowManager.removeView(FloatingBallView)
                        mWindowManager.addView(FloatingBallView,FloatingBallParams)
                        repository.saveCrop(true)
                }
                2->{
                    if(isadded==false){
                        isadded = true
                        repository.saveMovingText(true)
                        mWindowManager.addView(textview,TextViewParams)
                        mWindowManager.removeView(FloatingBallView)
                        mWindowManager.addView(FloatingBallView,FloatingBallParams)
                        textview.isClickable = true
                    }else{
                        textview.isClickable = true
                        repository.saveMovingText(true)
                        mWindowManager.removeView(FloatingBallView)
                        mWindowManager.addView(FloatingBallView,FloatingBallParams)
                    }
                }
                3->{
                    showMyDialog(2)
                }
                4->{
                        stopSelf()
                }
                5->{
                    makeToast(intent.getStringExtra("Message"))
                }
                else->{
                    repository.saveCrop(false)
                    repository.saveMovingText(false)
                    Log.d("value", intent.getIntExtra("Opera",0).toString())
                }
            }
            return super.onStartCommand(intent, flags, startId)
        }

        fun makeToast(s:String?){
            Toast.makeText(applicationContext,s,Toast.LENGTH_SHORT).show()
        }

        fun showMyDialog(i: Int){
            when(i){
                1->dialogs.FloatFirstDialog(this)
                2->dialogs.textSizeDialog(this,textview)
            }
        }

        override fun onBind(p0: Intent?): IBinder? {
            return null
        }


        override fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(receiver)
            val intent = Intent(this, FloatingService::class.java)
            stopService(intent)
            if(repository.IsCrop){
                mWindowManager.removeView(cropview)
            }
            if(isadded){
                mWindowManager.removeView(textview)
            }
            mWindowManager.removeView(FloatingBallView)
        }

}