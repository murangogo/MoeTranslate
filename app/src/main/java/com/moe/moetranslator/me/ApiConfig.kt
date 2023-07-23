package com.moe.moetranslator.me

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.R
import com.moe.moetranslator.translate.TranslateFragment
import com.moe.moetranslator.databinding.FragmentApiConfigBinding


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
        binding.textViewAPIConfig.text = if (repository.ApiChoose==0) "API：腾讯云" else "API：百度翻译"
        if((repository.ApiChoose==0)&&(repository.TencentApiS=="")){
            binding.account.hint = "SecretId"
            binding.password.hint = "SecretKey"
        }else if ((repository.ApiChoose==1)&&(repository.BaiduApiA=="")){
            binding.account.hint = "APP ID"
            binding.password.hint = "密钥"
        }
        if((repository.ApiChoose==0)&&(repository.TencentApiS!="")){
            binding.account.hint = repository.TencentApiS+"（已保存）"
            binding.password.hint = repository.TencentApiK+"（已保存）"
        }else if ((repository.ApiChoose==1)&&(repository.BaiduApiA!="")){
            binding.account.hint = repository.BaiduApiA + "（已保存）"
            binding.password.hint = repository.BaiduApiP + "（已保存）"
        }
        binding.textView5.setOnClickListener {
            if(repository.ApiChoose==0){
                val dialogperapi = AlertDialog.Builder(activity)
                    .setTitle("什么是腾讯云API")
                    .setMessage("萌译可以使用腾讯云或百度翻译的API提供翻译服务，这意味着您需要从对应的平台申请API。检测到您现在选择的API平台为腾讯云，点击“现在就去”即可开始申请腾讯云API。申请腾讯云API的详细教程可点击“查看教程”。请注意，这些API的申请都是免费的，而且每月都有1万次的免费调用次数。")
                    .setCancelable(false)
                    .setNeutralButton("查看教程"){_,_->
                        val urlt = "https://blog.csdn.net/qq_45487246/article/details/131876975"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(urlt)
                        startActivity(intent)
                    }
                    .setPositiveButton("现在就去") { _, _ ->
                        val url = "https://console.cloud.tencent.com/cam/capi"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    .setNegativeButton("再说吧") { _, _ ->}
                    .create()
                dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                dialogperapi.show()
            }else{
                val dialogperapi = AlertDialog.Builder(activity)
                    .setTitle("什么是百度翻译API")
                    .setMessage("萌译可以使用腾讯云或百度翻译的API提供翻译服务，这意味着您需要从对应的平台申请API。检测到您现在选择的API平台为百度翻译，点击“现在就去”即可开始申请百度翻译API。申请百度翻译API的详细教程可点击“查看教程”。请注意，这些API的申请都是免费的，而且每月都有1万次的免费调用次数。")
                    .setCancelable(false)
                    .setNeutralButton("查看教程"){_,_->
                        val urlt = "https://blog.csdn.net/qq_45487246/article/details/131876712"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(urlt)
                        startActivity(intent)
                    }
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
        }
        binding.button.setOnClickListener {
            if(repository.ApiChoose==0){
                repository.saveTencentAPIS(binding.account.text.toString())
                repository.saveTencentAPIK(binding.password.text.toString())
            }else{
                repository.saveBaiduAPIA(binding.account.text.toString())
                repository.saveBaiduAPIP(binding.password.text.toString())
                TranslateFragment.config.appId = repository.BaiduApiA
                TranslateFragment.config.secretKey = repository.BaiduApiP
            }
            Toast.makeText(context,"保存成功", Toast.LENGTH_LONG).show()
            activity!!.finish()
        }
    }

}