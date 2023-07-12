package com.moe.moetranslator.me

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

        val drawable3: Drawable? = ResourcesCompat.getDrawable(resources,R.drawable.chatgpt,null)
        drawable3!!.setBounds(0,0,UtilTools.dip2px(context!!,50.0),UtilTools.dip2px(context!!,50.0));//第一0是距左右边距离，第二0是距上下边距离，第三长度,第四宽度
        binding.chatgptbtn.setCompoundDrawables(drawable3,null,null,null);//设置drawable

        binding.chatgptbtn.setOnClickListener {
            binding.chatgptbtn.isChecked = false;
            Toast.makeText(context,"将在后续版本中推出。", Toast.LENGTH_LONG).show()
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