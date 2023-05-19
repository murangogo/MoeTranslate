package com.moe.moetranslator

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moe.moetranslator.databinding.FragmentApiConfigBinding
import com.moe.moetranslator.databinding.FragmentApiselectBinding
import com.moe.moetranslator.databinding.FragmentDeveloperBinding


class Developer : Fragment() {
    private lateinit var binding: FragmentDeveloperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeveloperBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.officialwebsite.setOnClickListener {
            val url = "https://www.moetranslate.top/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.ideas.setOnClickListener {
            val url = "https://www.wjx.cn/vm/Oky2ycy.aspx/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.wechat.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("萌译官方公众号")
                .setMessage("您可以在微信搜索“MoeTranslator”，来关注官方公众号。")
                .setCancelable(false)
                .setPositiveButton("我知道了") { _, _ -> }
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
        binding.github.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("萌译的GitHub仓库")
                .setMessage("萌译是开源的，您可以访问GitHub仓库。但如果您在国内，可能会比较困难。")
                .setCancelable(false)
                .setPositiveButton("去GitHub看看") { _, _ ->
                    val url = "https://github.com/murangogo/MoeTranslate"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                .setNegativeButton("暂时不看了") { _, _ ->}
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
    }

}