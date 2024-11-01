package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.regex.Pattern

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

    fun fontSizeDialog(context: Context, view: TextView?, onSizeSet: ((Float) -> Unit)?): AlertDialog {
        val prefs = CustomPreference.getInstance(context)

        val editText = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            filters = arrayOf(DecimalDigitsInputFilter())
            hint = context.getString(R.string.font_size_hint, prefs.getFloat("Custom_Result_Font_Size", 16f).toString())

            // 设置输入框的布局参数
            val padding = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                resources.displayMetrics
            ).toInt()

            setPadding(padding, padding, padding, padding)
        }

        // 创建包含说明文字和输入框的布局
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            // 设置输入框的布局参数
            val padding = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                resources.displayMetrics
            ).toInt()

            setPadding(padding, padding, padding, 0)

            // 添加说明文字
            addView(TextView(context).apply {
                text = context.getString(R.string.font_size_float)
                setPadding(padding, 0, padding, padding)
            })

            // 添加输入框
            addView(editText)
        }

        val res = AlertDialog.Builder(context)
            .setTitle(R.string.font_size_setting)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val sizeText = editText.text.toString()
                try {
                    val size = sizeText.toFloat()
                    if (size > 0) {
                        // 保存字体大小
                        prefs.setFloat("Custom_Result_Font_Size", size)
                        if (view != null){
                            MainScope().launch{
                                view.textSize = size
                            }
                        }
                        // 回调通知设置完成
                        onSizeSet?.invoke(size)
                    } else {
                        Toast.makeText(context, context.getString(R.string.font_size_invalid), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.font_size_invalid), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        return res
    }
}

// 输入过滤器
class DecimalDigitsInputFilter : InputFilter {
    private val pattern = Pattern.compile("[0-9]*\\.?[0-9]*")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val builder = StringBuilder(dest)
        builder.replace(dstart, dend, source.subSequence(start, end).toString())
        val resultString = builder.toString()

        return if (!pattern.matcher(resultString).matches()) {
            ""
        } else null
    }
}