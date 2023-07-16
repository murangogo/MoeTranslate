package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.moe.moetranslator.MainActivity
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.utils.Myadapter
import com.moe.moetranslator.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception

class Dialogs {
    private lateinit var repository: MySharedPreferenceData
    private var str = arrayOf("  选取翻译范围","  调整翻译结果位置","  字体大小设置","  关闭悬浮球","  回到萌译主界面")
    private var img = arrayOf(
        R.drawable.cut_screen,
        R.drawable.word_position,
        R.drawable.font_size,
        R.drawable.close_floatingball,
        R.drawable.back_moe
    )
    @SuppressLint("MissingInflatedId")
    fun FloatFirstDialog(context: Context){
        repository = MySharedPreferenceData(context)
        val customView:View = LayoutInflater.from(context).inflate(R.layout.floating_dialog_first, null,false)
        val lv = customView.findViewById<ListView>(R.id.firstList)
        val imgIcon = customView.findViewById<ImageView>(R.id.TitleIcon)
        val myWelcome = customView.findViewById<TextView>(R.id.welcome)

        var random = Math.random()
        if(random>0.8){
            myWelcome.text = " 哎呀，摸鱼被发现啦～"
            imgIcon.setImageResource(R.drawable.amazed_relax)
        }else if(random>0.4){
            myWelcome.text = " 你好鸭～"
            imgIcon.setImageResource(R.drawable.happy_hi)
        }else{
            myWelcome.text = " 要来杯果汁吗？"
            imgIcon.setImageResource(R.drawable.smile_cool)
        }

        val myAdapter = Myadapter(context,str,img)
        lv.adapter = myAdapter
        val dialogBuilder = AlertDialog.Builder(context).setView(customView).setCancelable(false).setNegativeButton("取消"){_,_->}
        val myDialog = dialogBuilder.create()

        myDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        myDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        myDialog.show()
        myDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lv.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(p2){
                    0->{
                        val intent1 = Intent(context, FloatingService::class.java)
                        val intent2 = Intent(context, FloatingService::class.java)
                        if(repository.IsCrop){
                            intent2.putExtra("Opera",5)
                            intent2.putExtra("Message","不可以重复打开选取框哦～")
                            context.startService(intent2)
                        }else if (repository.IsMovingText){
                            intent2.putExtra("Opera",5)
                            intent2.putExtra("Message","请先调整翻译结果位置")
                            context.startService(intent2)
                        }else{
                            intent1.putExtra("Opera",1)
                            context.startService(intent1)
                        }
                        myDialog.dismiss()
                    }
                    1->{
                        val intent1 = Intent(context, FloatingService::class.java)
                        val intent2 = Intent(context, FloatingService::class.java)
                        if(repository.IsCrop){
                            intent2.putExtra("Opera",5)
                            intent2.putExtra("Message","请先选取完截图位置")
                            context.startService(intent2)
                        }else if (repository.IsMovingText){
                            intent2.putExtra("Opera",5)
                            intent2.putExtra("Message","不可以重复打开翻译结果调整界面哦～")
                            context.startService(intent2)
                        }else{
                            intent1.putExtra("Opera",2)
                            context.startService(intent1)
                        }
                        myDialog.dismiss()
                    }
                    2->{
                        val intent = Intent(context, FloatingService::class.java)
                        intent.putExtra("Opera",3)
                        context.startService(intent)
                        myDialog.dismiss()
                    }
                    3->{
                        val intent = Intent(context, FloatingService::class.java)
                        intent.putExtra("Opera",4)
                        context.startService(intent)
                        myDialog.dismiss()
                    }
                    4->{
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        myDialog.dismiss()
                    }

                }
            }
        }
    }
    fun textSizeDialog(context: Context,view: TextView){
        val dialogBuilder = AlertDialog.Builder(context).setCancelable(false).setNegativeButton("取消"){_,_->}
        dialogBuilder.setTitle("请输入字体大小")
        val edit = EditText(context)
        edit.hint = "可以为整数或者小数"
        dialogBuilder.setView(edit)
        dialogBuilder.setPositiveButton("确认") {_,_->
            try{
                var mytextsize = (edit.text.toString()).toFloat()
                MainScope().launch{
                    view.textSize = mytextsize
                }
            }catch (e:Exception){
                Toast.makeText(context,"输入不合法", Toast.LENGTH_LONG).show()
            }
        }
        val myDialog = dialogBuilder.create()
        myDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        myDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        myDialog.show()
        myDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}