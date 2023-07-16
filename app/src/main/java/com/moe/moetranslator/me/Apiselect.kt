package com.moe.moetranslator.me

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moe.moetranslator.databinding.FragmentApiselectBinding
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.R
import android.graphics.drawable.Drawable
import android.net.Uri

import android.widget.RadioGroup
import androidx.core.content.res.ResourcesCompat
import com.moe.moetranslator.utils.UtilTools


class Apiselect : Fragment() {
    private lateinit var binding: FragmentApiselectBinding
    private lateinit var repository: MySharedPreferenceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MySharedPreferenceData(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApiselectBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val drawable1: Drawable? = ResourcesCompat.getDrawable(resources,R.drawable.tencentyun,null)
        drawable1!!.setBounds(0,0,UtilTools.dip2px(context!!,50.0),UtilTools.dip2px(context!!,50.0));//第一0是距左右边距离，第二0是距上下边距离，第三长度,第四宽度
        binding.tencentyunbtn.setCompoundDrawables(drawable1,null,null,null);//设置drawable

        val drawable2: Drawable? = ResourcesCompat.getDrawable(resources,R.drawable.baiduyi,null)
        drawable2!!.setBounds(0,0,UtilTools.dip2px(context!!,50.0),UtilTools.dip2px(context!!,50.0));//第一0是距左右边距离，第二0是距上下边距离，第三长度,第四宽度
        binding.baiduyibtn.setCompoundDrawables(drawable2,null,null,null);//设置drawable

        val drawable3: Drawable? = ResourcesCompat.getDrawable(resources,R.drawable.claude,null)
        drawable3!!.setBounds(0,0,UtilTools.dip2px(context!!,50.0),UtilTools.dip2px(context!!,50.0));//第一0是距左右边距离，第二0是距上下边距离，第三长度,第四宽度
        binding.chatgptbtn.setCompoundDrawables(drawable3,null,null,null);//设置drawable

        binding.chatgptbtn.setOnClickListener {
            binding.chatgptbtn.isChecked = false;
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("Claude(Beta)")
                .setMessage("考虑到使用ChatGPT需要借助科学上网，而且目前ChatGPT的账号难以注册，再加上使用cookie来连接ChatGPT非常不稳定。因此改用了国内可用的Claude。Claude类似于ChatGPT，也是一个人工智能聊天机器人，但由于Claude也不支持识图，所以仅推出了聊天功能，并不支持翻译。您可在底部导航栏的“Claude”页面体验。")
                .setCancelable(false)
                .setNegativeButton("我知道了") { _, _ ->}
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
        when(repository.ApiChoose){
            0->binding.tencentyunbtn.isChecked = true
            1->binding.baiduyibtn.isChecked = true
        }
        binding.APIRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("ID","$checkedId")
            when (checkedId) {
                binding.tencentyunbtn.id->repository.saveApi(0)
                binding.baiduyibtn.id->repository.saveApi(1)
            }
        }
    }
}