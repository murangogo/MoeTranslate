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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentOpenaiApiBinding
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.launch

class OpenAIText :Fragment() {
    private lateinit var binding: FragmentOpenaiApiBinding
    private lateinit var prefs: CustomPreference
    private val defaultSystemPrompt = "你是一名专业翻译。你的任务是准确、自然地翻译给定的文本。\n具体规则如下： \n1、根据用户的要求，将文本翻译成指定的目标语言；\n2、保持原意和语气；\n3、尽可能保持格式和结构；\n4、直接返回翻译后的文本，不要有任何解释或附加内容；\n5、如果文本已经是目标语言，请按原样返回。"
    private val defaultUserPrompt = "请将下面的文本从usefromlang翻译为usetolang：\n\nusesourcetext"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOpenaiApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        loadConfig()

        if(!prefs.getBoolean("Read_OpenAI_API_Introduce", false)){
            showIntroduce()
        }
    }

    private fun showIntroduce(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.introduce_openai_api_title)
            .setMessage(R.string.introduce_openai_api_content)
            .setCancelable(false)
            .setPositiveButton(R.string.user_known, null)
            .setNegativeButton(R.string.view_tutorial){_,_->
                val urlt = "https://www.moetranslate.top/docs/translationapi/uniaitrans/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlt)
                startActivity(intent)
            }
            .setNeutralButton(R.string.introduce_not_show_again){
                    _, _ ->
                prefs.setBoolean("Read_OpenAI_API_Introduce", true)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun setupButtons() {
        binding.introduce.setOnClickListener{ showIntroduce() }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    private fun saveConfiguration() {
        try{
            if(binding.editApiKey.text.toString().trim().isBlank()){
                throw Exception(getString(R.string.fill_blank))
            }

            val normalizedUrl = UrlUtils.normalizeUrl(requireContext(), binding.editBaseUrl.text.toString())

            if(binding.editModelName.text.toString().trim().isBlank()){
                throw Exception(getString(R.string.fill_blank))
            }

            lifecycleScope.launch {
                prefs.setString("OpenAI_Api_Key", binding.editApiKey.text.toString())
                prefs.setString("OpenAI_Base_Url", normalizedUrl)
                prefs.setString("OpenAI_Model_Name", binding.editModelName.text.toString())
                if ((binding.editSystemPrompt.text.toString()).isBlank()){
                    prefs.setString("OpenAI_System_Prompt", defaultSystemPrompt)
                }
                if ((binding.editUserPrompt.text.toString()).isBlank()){
                    prefs.setString("OpenAI_User_Prompt", defaultUserPrompt)
                }
                showToast(getString(R.string.save_successfully))
                requireActivity().finish()
            }
        } catch (e: Exception){
            showToast(getString(R.string.failed_save_config, e.message))
        }
    }

    private fun loadConfig() {
        try {
            binding.editApiKey.setText(prefs.getString("OpenAI_Api_Key", ""))
            binding.editBaseUrl.setText(prefs.getString("OpenAI_Base_Url", ""))
            binding.editModelName.setText(prefs.getString("OpenAI_Model_Name", ""))
            binding.editSystemPrompt.setText(prefs.getString("OpenAI_System_Prompt", defaultSystemPrompt))
            binding.editUserPrompt.setText(prefs.getString("OpenAI_User_Prompt", defaultUserPrompt))
        } catch (e: Exception) {
            showToast("Error loading configuration: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}