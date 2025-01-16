/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

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
        when (apiType){
            "niu" -> prepareNiu()
            "volc" -> prepareVolc()
            "azure" -> prepareAzure()
            "baidu" -> prepareBaidu()
            "tencent" -> prepareTencent()
            else -> Toast.makeText(context, "Unknow Error.", Toast.LENGTH_LONG).show()
        }
    }

    private fun prepareNiu(){
        binding.tvApiType.text = getString(R.string.api_name, getString(R.string.niuapi_name))
        binding.account.hint = getString(R.string.niu_noneed)
        binding.account.isFocusable = false
        binding.account.isClickable = false
        binding.account.isCursorVisible = false
        binding.account.isLongClickable = false
        if (prefs.getString("Niutrans_EncryptedKey","") != ""){
            binding.secretKey.hint = getString(R.string.api_saved)
        }else{
            binding.secretKey.hint = getString(R.string.niu_hint_secret_key)
        }
        binding.whatsThis.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.whats_api_title, getString(R.string.niuapi_name)))
                .setMessage(getString(R.string.whats_api_content, getString(R.string.niuapi_name)))
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
        binding.saveOnlineApiButton.setOnClickListener {
            if (binding.secretKey.text.isBlank()) {
                Toast.makeText(context, getString(R.string.fill_blank), Toast.LENGTH_LONG).show()
            } else {
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.secretKey.text.toString().trim(),
                    "Niutrans"
                )
                Toast.makeText(context, getString(R.string.save_successfully), Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }
    }

    private fun prepareVolc(){
        binding.tvApiType.text = getString(R.string.api_name, getString(R.string.volcapi_name))
        if (prefs.getString("Volc_ACCOUNT_EncryptedKey","") != ""){
            binding.account.hint = getString(R.string.api_saved)
            binding.secretKey.hint = getString(R.string.api_saved)
        }else{
            binding.account.hint = getString(R.string.volc_hint_account)
            binding.secretKey.hint = getString(R.string.volc_hint_secret_key)
        }
        binding.whatsThis.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.whats_api_title, getString(R.string.volcapi_name)))
                .setMessage(getString(R.string.whats_api_content, getString(R.string.volcapi_name)))
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
        binding.saveOnlineApiButton.setOnClickListener {
            if (binding.account.text.isBlank() || binding.secretKey.text.isBlank()) {
                Toast.makeText(context, getString(R.string.fill_blank), Toast.LENGTH_LONG).show()
            } else {
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.account.text.toString().trim(),
                    "Volc_ACCOUNT"
                )
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.secretKey.text.toString().trim(),
                    "Volc_SECRETKEY"
                )
                Toast.makeText(context, getString(R.string.save_successfully), Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }
    }

    private fun prepareAzure(){
        binding.tvApiType.text = getString(R.string.api_name, getString(R.string.azureapi_name))
        binding.account.hint = getString(R.string.niu_noneed)
        binding.account.isFocusable = false
        binding.account.isClickable = false
        binding.account.isCursorVisible = false
        binding.account.isLongClickable = false
        if (prefs.getString("Azure_EncryptedKey","") != ""){
            binding.secretKey.hint = getString(R.string.api_saved)
        }else{
            binding.secretKey.hint = getString(R.string.azure_hint_secret_key)
        }
        binding.whatsThis.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.whats_api_title, getString(R.string.azureapi_name)))
                .setMessage(getString(R.string.whats_api_content, getString(R.string.azureapi_name)))
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
        binding.saveOnlineApiButton.setOnClickListener {
            if (binding.secretKey.text.isBlank()) {
                Toast.makeText(context, getString(R.string.fill_blank), Toast.LENGTH_LONG).show()
            } else {
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.secretKey.text.toString().trim(),
                    "Azure"
                )
                Toast.makeText(context, getString(R.string.save_successfully), Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }
    }

    private fun prepareBaidu(){
        binding.tvApiType.text = getString(R.string.api_name, getString(R.string.baiduapi_name))
        if (prefs.getString("Baidu_Translate_ACCOUNT_EncryptedKey","") != ""){
            binding.account.hint = getString(R.string.api_saved)
            binding.secretKey.hint = getString(R.string.api_saved)
        }else{
            binding.account.hint = getString(R.string.baidu_hint_account)
            binding.secretKey.hint = getString(R.string.baidu_hint_secret_key)
        }
        binding.whatsThis.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.whats_api_title, getString(R.string.baiduapi_name)))
                .setMessage(getString(R.string.whats_api_content, getString(R.string.baiduapi_name)))
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
        binding.saveOnlineApiButton.setOnClickListener {
            if (binding.account.text.isBlank() || binding.secretKey.text.isBlank()) {
                Toast.makeText(context, getString(R.string.fill_blank), Toast.LENGTH_LONG).show()
            } else {
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.account.text.toString().trim(),
                    "Baidu_Translate_ACCOUNT"
                )
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.secretKey.text.toString().trim(),
                    "Baidu_Translate_SECRETKEY"
                )
                Toast.makeText(context, getString(R.string.save_successfully), Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }
    }

    private fun prepareTencent(){
        binding.tvApiType.text = getString(R.string.api_name, getString(R.string.tencentapi_name))
        if (prefs.getString("Tencent_Cloud_ACCOUNT_EncryptedKey","") != ""){
            binding.account.hint = getString(R.string.api_saved)
            binding.secretKey.hint = getString(R.string.api_saved)
        }else{
            binding.account.hint = getString(R.string.tencent_hint_account)
            binding.secretKey.hint = getString(R.string.tencent_hint_secret_key)
        }
        binding.whatsThis.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.whats_api_title, getString(R.string.tencentapi_name)))
                .setMessage(getString(R.string.whats_api_content, getString(R.string.tencentapi_name)))
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
        binding.saveOnlineApiButton.setOnClickListener {
            if (binding.account.text.isBlank() || binding.secretKey.text.isBlank()) {
                Toast.makeText(context, getString(R.string.fill_blank), Toast.LENGTH_LONG).show()
            } else {
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.account.text.toString().trim(),
                    "Tencent_Cloud_ACCOUNT"
                )
                KeystoreManager.storeKey(
                    requireContext(),
                    binding.secretKey.text.toString().trim(),
                    "Tencent_Cloud_SECRETKEY"
                )
                Toast.makeText(context, getString(R.string.save_successfully), Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }
    }
}