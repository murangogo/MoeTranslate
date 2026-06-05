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
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentOpenaiApiBinding
import com.moe.moetranslator.utils.Constants.defaultOpenAITemperature
import com.moe.moetranslator.utils.Constants.defaultSystemPrompt
import com.moe.moetranslator.utils.Constants.defaultUserPrompt
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.launch
import translationapi.openaitranslation.OpenAITranslation

class OpenAIText :Fragment() {
    private lateinit var binding: FragmentOpenaiApiBinding
    private lateinit var prefs: CustomPreference

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
        binding.btnAddExtraParam.setOnClickListener { addExtraParamRow() }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    /** 在「自定义请求参数」区域新增一行键值对（复用通用的 item_key_value_pair 布局）。 */
    private fun addExtraParamRow(key: String = "", value: String = "") {
        val row = layoutInflater.inflate(R.layout.item_key_value_pair, binding.containerExtraParams, false)
        row.findViewById<TextInputEditText>(R.id.editKey).setText(key)
        row.findViewById<TextInputEditText>(R.id.editValue).setText(value)
        row.findViewById<ImageButton>(R.id.btnRemove).setOnClickListener {
            binding.containerExtraParams.removeView(row)
        }
        binding.containerExtraParams.addView(row)
    }

    /** 收集当前所有非空键的自定义参数行。 */
    private fun collectExtraParams(): List<Pair<String, String>> {
        val container = binding.containerExtraParams
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            val key = view.findViewById<TextInputEditText>(R.id.editKey).text.toString().trim()
            val value = view.findViewById<TextInputEditText>(R.id.editValue).text.toString().trim()
            if (key.isNotBlank()) {
                list.add(key to value)
            }
        }
        return list
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
                } else {
                    prefs.setString("OpenAI_System_Prompt", binding.editSystemPrompt.text.toString())
                }
                if ((binding.editUserPrompt.text.toString()).isBlank()){
                    prefs.setString("OpenAI_User_Prompt", defaultUserPrompt)
                } else {
                    prefs.setString("OpenAI_User_Prompt", binding.editUserPrompt.text.toString())
                }
                // 温度留空（或非法）即存空串，表示请求时不发送 temperature
                val tempText = binding.editTemperature.text.toString().trim()
                prefs.setString("OpenAI_Temperature",
                    if (tempText.isBlank()) "" else (tempText.toFloatOrNull()?.toString() ?: ""))
                prefs.setString("OpenAI_Extra_Params",
                    OpenAITranslation.encodeExtraParams(collectExtraParams()))
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
            binding.editTemperature.setText(prefs.getString("OpenAI_Temperature", defaultOpenAITemperature.toString()))
            binding.containerExtraParams.removeAllViews()
            OpenAITranslation.decodeExtraParams(prefs.getString("OpenAI_Extra_Params", "")).forEach { (k, v) ->
                addExtraParamRow(k, v)
            }
        } catch (e: Exception) {
            showToast("Error loading configuration: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}