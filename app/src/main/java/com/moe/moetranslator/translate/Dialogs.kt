package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.moe.moetranslator.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception

data class DialogResult(
    val dialog: AlertDialog,
    val listView: ListView
)

object Dialogs {
    @SuppressLint("MissingInflatedId")
    fun menuDialog(ctx: Context): DialogResult {
        val strlist = ctx.resources.getStringArray(R.array.menu_item)
        val imglist = arrayOf(
            R.drawable.cut_screen,
            R.drawable.word_position,
            R.drawable.close_text,
            R.drawable.font_size,
            R.drawable.close_floatingball,
            R.drawable.back_moe
        )
        val view = LayoutInflater.from(ctx).inflate(R.layout.floating_menu_dialog, null,false)
        val img = view.findViewById<ImageView>(R.id.TitleIcon)
        val welcome = view.findViewById<TextView>(R.id.welcome)
        val lv = view.findViewById<ListView>(R.id.menu_list)
        lv.adapter = MenuDialogAdapter(ctx,strlist,imglist)
        val random = Math.random()
        if(random>0.8){
            welcome.text = ctx.getString(R.string.floating_ball_menu_1)
            img.setImageResource(R.drawable.amazed_relax)
        }else if(random>0.4){
            welcome.text = ctx.getString(R.string.floating_ball_menu_2)
            img.setImageResource(R.drawable.happy_hi)
        }else{
            welcome.text = ctx.getString(R.string.floating_ball_menu_3)
            img.setImageResource(R.drawable.smile_cool)
        }
        val dialog = AlertDialog.Builder(ctx)
            .setView(view)
            .setCancelable(false)
            .setNegativeButton(R.string.user_cancel,null)
            .create()
        return DialogResult(dialog, lv)
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