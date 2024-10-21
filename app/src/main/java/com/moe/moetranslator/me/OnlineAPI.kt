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
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentOnlineApiBinding
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.KeystoreManager

class OnlineAPI : Fragment() {

    private var apiType: String? = null
    private lateinit var binding: FragmentOnlineApiBinding
    private lateinit var prefs: CustomPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
        arguments?.let {
            apiType = it.getString("api_type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlineApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvApiType.text =
            if (apiType == "baidu") getString(R.string.api_name) + getString(R.string.baiduapi_name) else getString(
                R.string.api_name
            ) + getString(R.string.tencentapi_name)
        if (apiType == "baidu"){
            if (prefs.getString("Baidu_Translate_ACCOUNT_EncryptedKey","") != ""){
                binding.account.hint = getString(R.string.api_saved)
                binding.secretKey.hint = getString(R.string.api_saved)
            }else{
                binding.account.hint = getString(R.string.baidu_hint_account)
                binding.secretKey.hint = getString(R.string.baidu_hint_secret_key)

            }
        }else{
            if (prefs.getString("Tencent_Cloud_ACCOUNT_EncryptedKey","") != ""){
                binding.account.hint = getString(R.string.api_saved)
                binding.secretKey.hint = getString(R.string.api_saved)
            }else{
                binding.account.hint = getString(R.string.tencent_hint_account)
                binding.secretKey.hint = getString(R.string.tencent_hint_secret_key)
            }
        }

        binding.whatsThis.setOnClickListener {

            if(apiType == "baidu"){
                val dialog = AlertDialog.Builder(activity)
                    .setTitle(R.string.whats_baidu_translate_title)
                    .setMessage(R.string.whats_baidu_translate_content)
                    .setCancelable(false)
                    .setNeutralButton(R.string.view_tutorial){_,_->
                        val urlt = "https://blog.csdn.net/qq_45487246/article/details/131876712"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(urlt)
                        startActivity(intent)
                    }
                    .setPositiveButton(R.string.go_now) { _, _ ->
                        val url = "https://fanyi-api.baidu.com/"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    .setNegativeButton(R.string.user_cancel) { _, _ ->}
                    .create()
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            } else {
                val dialog = AlertDialog.Builder(activity)
                    .setTitle(R.string.whats_tencent_cloud_title)
                    .setMessage(R.string.whats_tencent_cloud_content)
                    .setCancelable(false)
                    .setNeutralButton(R.string.view_tutorial){_,_->
                        val urlt = "https://blog.csdn.net/qq_45487246/article/details/131876712"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(urlt)
                        startActivity(intent)
                    }
                    .setPositiveButton(R.string.go_now) { _, _ ->
                        val url = "https://fanyi-api.baidu.com/"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                    .setNegativeButton(R.string.user_cancel) { _, _ ->}
                    .create()
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            }
        }

        binding.saveOnlineApiButton.setOnClickListener {
            if (binding.account.text.isBlank() || binding.secretKey.text.isBlank()) {
                Toast.makeText(context, getString(R.string.fill_blank), Toast.LENGTH_LONG).show()
            } else {
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.account.text.toString().trim(),
                    if (apiType == "baidu") "Baidu_Translate_ACCOUNT" else "Tencent_Cloud_ACCOUNT"
                )
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.secretKey.text.toString().trim(),
                    if (apiType == "baidu") "Baidu_Translate_SECRETKEY" else "Tencent_Cloud_SECRETKEY"
                )
                Toast.makeText(context, getString(R.string.save_successfully), Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }
    }
}