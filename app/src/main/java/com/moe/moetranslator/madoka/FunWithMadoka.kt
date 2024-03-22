package com.moe.moetranslator.madoka

import android.graphics.Typeface
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.moe.moetranslator.utils.ConstDatas
import com.moe.moetranslator.FragmentTouchListener
import com.moe.moetranslator.MainActivity
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FunWithMadokaFragmentBinding
import live2dsdk.madoka.ClothPlay
import live2dsdk.madoka.GLRenderer
import live2dsdk.madoka.LAppDelegate

class FunWithMadoka : Fragment() {

    private val mLeftMenu = arrayOf(
        arrayOf("打招呼...", "学校生活...", "学校朋友...", "闲聊...", "关于发带..."),
        arrayOf("打招呼...", "关于魔法少女...", "大家...", "闲聊...", "长大以后..."),
        arrayOf("打招呼...", "家庭趣事...", "学校工作...", "闲聊...", "睡不着..."),
        arrayOf("打招呼...", "冬天...", "打雪仗...", "闲聊...", "关于自己..."),
        arrayOf("打招呼...", "新年快乐...", "新年美食...", "闲聊...", "新年祝福..."),
        arrayOf("打招呼...", "这套泳衣...", "游泳技巧...", "闲聊...", "说再见..."),
        arrayOf("打招呼...", "新的泳衣...", "关于大海...", "闲聊...", "盛夏的回忆..."),
        arrayOf("under construction..."),
        arrayOf("under construction...")
    )
    private val mRightMenu = arrayOf("见泷原校服", "魔法少女", "睡衣", "情人节2018", "晴着ver", "泳装2018", "泳装ver","便服","女仆服（Scene0）")
    private lateinit var binding:FunWithMadokaFragmentBinding
    private lateinit var glRenderer : GLRenderer //自定义渲染器，该渲染器实现GLSurfaceView.Renderer接口
    private lateinit var dialogs: TextView
    private lateinit var DisplayText: String

    private var thread: Thread? = null
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) { //handler接收到指令后分不同的情况对界面UI进行更新
            dialogs.text = DisplayText
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (activity as MainActivity?)!!.registerFragmentTouchListener(fragmentTouchListener) //注册监听器
        binding = FunWithMadokaFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart(){
        super.onStart()
        if (!LAppDelegate.getInstance().getIsStart()) {
            LAppDelegate.getInstance().setIsStart(true)
            dialogs = TextView(activity)
            dialogs.setBackgroundResource(R.drawable.textview_shape)
            val font = Typeface.createFromAsset(context!!.assets, "talk.ttf")
            dialogs.typeface = font
            binding.listviewleft.adapter = ArrayAdapter(
                this.context!!, R.layout.edge_menu,
                mLeftMenu[0]
            )
            binding.listviewright.adapter = ArrayAdapter(
                this.context!!,
                R.layout.edge_menu,
                mRightMenu
            )
            binding.MadokaView.setEGLContextClientVersion(2) // 启用OpenGL ES 2.0
            glRenderer = GLRenderer() //创建出自定义渲染器的对象
            binding.MadokaView.setRenderer(glRenderer) //setRenderer()将会创建一个渲染线程，指定GLSurfaceView.Renderer接口的实现者
            binding.MadokaView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY) //设置渲染模式为自动渲染模式（以一定的时间间隔自动的循环调用用户实现的onDrawFrame()方法进行一帧一帧的绘制）
            TalkSentences.clothes = 0
        }
        binding.buttonChat.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        binding.buttonChange.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.END) }
        binding.listviewleft.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                binding.drawerLayout.closeDrawer(GravityCompat.START)

                if ((thread == null || !(thread!!).isAlive()) && (ClothPlay.doEandM == null || !(ClothPlay.doEandM.isAlive()))) {
                    if (ConstDatas.isTalking) {
                        binding.mycontainer.removeView(dialogs)
                    }
                    TalkSentences.sent = position
                    val layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.topToBottom = R.id.button_change
                    layoutParams.topMargin = 1300
                    layoutParams.leftMargin = 20
                    layoutParams.rightMargin = 20
                    binding.mycontainer.addView(dialogs, layoutParams)
                    ConstDatas.isTalking = true
                    ClothPlay.PlayIt(TalkSentences.clothes, TalkSentences.sent,activity)

                    thread = Thread {

                        for (i in 0 until (TalkSentences.sentences[TalkSentences.clothes][TalkSentences.sent]!!.length)) {
                            DisplayText =
                                TalkSentences.sentences[TalkSentences.clothes][TalkSentences.sent]!!.substring(0, i + 1)
                            try {
                                Thread.sleep(50)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            handler.sendMessage(Message())
                        }
                    }
                    thread!!.start()

                } else {
                    Toast.makeText(context, "你问得太快啦，让小圆说完吧...", Toast.LENGTH_SHORT).show()
                }

            }
        binding.listviewright.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                binding.drawerLayout.closeDrawer(GravityCompat.END)

                if ((thread == null || !thread!!.isAlive) && (ClothPlay.doEandM == null || !ClothPlay.doEandM.isAlive)) {
                    binding.listviewleft.adapter =
                        ArrayAdapter<String>(
                            context!!, R.layout.edge_menu,
                            mLeftMenu[position]
                        )
                    LAppDelegate.getInstance().view.setChangedModel(true, position)
                    TalkSentences.clothes = position
                    if (ConstDatas.isTalking) {
                        DisplayText = ""
                        handler.sendMessage(Message())
                        binding.mycontainer.removeView(dialogs)
                        ConstDatas.isTalking = false
                    }
                } else {
                    Toast.makeText(context, "等等！让小圆说完吧...", Toast.LENGTH_SHORT).show()
                }

            }
        LAppDelegate.getInstance().onStart(activity) //递交Activity对象
    }
    override fun onResume() {     //重新开始
        super.onResume()
        binding.MadokaView.onResume() //重新开始
    }

    override fun onPause() {      //暂停函数
        super.onPause()
        binding.MadokaView.onPause()
        LAppDelegate.getInstance().onPause() //会得到当前对象
    }

    override fun onStop() {
        super.onStop()
        LAppDelegate.getInstance().onStop() //做停止工作
    }

    override fun onDestroy() {        //销毁工作
        super.onDestroy()
        (this.activity as MainActivity?)!!.unRegisterFragmentTouchListener(fragmentTouchListener)
        LAppDelegate.getInstance().onDestroy()
    }

    var fragmentTouchListener: FragmentTouchListener = object : FragmentTouchListener {
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val pointX: Float = event.getX() //x坐标，相对的坐标值，相对于消费这个事件的视图的左上点的坐标
            val pointY: Float = event.getY() //y坐标，相对的坐标值，相对于消费这个事件的视图的左上点的坐标
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> LAppDelegate.getInstance()
                    .onTouchBegan(pointX, pointY) //传递“开始触碰”事件坐标
                MotionEvent.ACTION_UP -> LAppDelegate.getInstance()
                    .onTouchEnd(pointX, pointY) //传递“结束触碰”事件坐标
                MotionEvent.ACTION_MOVE -> LAppDelegate.getInstance()
                    .onTouchMoved(pointX, pointY) //传递滑动坐标
            }
            return false
        }
    }
}