package com.moe.moetranslator

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moe.moetranslator.databinding.FragmentApiConfigBinding
import com.moe.moetranslator.databinding.FragmentApiselectBinding


class ApiConfig : Fragment() {

    private lateinit var binding: FragmentApiConfigBinding
    private lateinit var repository: MySharedPreferenceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MySharedPreferenceData(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApiConfigBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(repository.BaiduApiA!=""){
            binding.baiduaccount.hint = repository.BaiduApiA + "（已保存）"
            binding.baidupassword.hint = repository.BaiduApiP + "（已保存）"
        }
        binding.textView5.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("什么是百度翻译API")
                .setMessage("萌译可以使用百度翻译的API提供翻译服务，这意味着您需要从百度翻译开放平台申请API，点击“现在就去”即可开始申请。请注意在申请时选择个人开发者，申请后开通图片翻译服务（每月可免费享受1万次的调用）。最后在开发者信息中获取APP ID和密钥，复制粘贴在上面即可。")
                .setCancelable(false)
                .setPositiveButton("现在就去") { _, _ ->
                    val url = "https://fanyi-api.baidu.com/"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                .setNegativeButton("再说吧") { _, _ ->}
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
        binding.button.setOnClickListener {
            repository.saveBaiduAPIA(binding.baiduaccount.text.toString())
            repository.saveBaiduAPIP(binding.baidupassword.text.toString())
            TranslateFragment.config.appId = repository.BaiduApiA
            TranslateFragment.config.secretKey = repository.BaiduApiP
            Toast.makeText(context,"保存成功", Toast.LENGTH_LONG).show()
            activity!!.finish()
        }
    }

}