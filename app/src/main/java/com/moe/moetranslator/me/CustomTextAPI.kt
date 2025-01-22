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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentCustomTextApiBinding
import com.moe.moetranslator.me.ConfigurationStorage.loadTextConfig
import com.moe.moetranslator.me.ConfigurationStorage.saveTextConfig
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.launch

class CustomTextAPI :Fragment() {
    private lateinit var binding: FragmentCustomTextApiBinding
    private var isPostMethod = false
    private var apiCode: Int? = null
    private var config: CustomTextAPIConfig? = null
    private lateinit var prefs: CustomPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
        arguments?.let {
            apiCode = it.getInt("custom_code")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomTextApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMethodSpinner()
        setupButtons()
        updateViewVisibility()

        config = loadTextConfig(prefs, apiCode!!)
        if ( config!= null ){
            loadConfig()
        }

        binding.introduce.setOnClickListener{
            showIntroduce()
        }

        if(!prefs.getBoolean("Read_Custom_Text_Introduce", false)){
            showIntroduce()
        }
    }

    private fun showIntroduce(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.introduce_custom_text_api_title)
            .setMessage(R.string.introduce_custom_text_api_content)
            .setCancelable(false)
            .setPositiveButton(R.string.user_known, null)
            .setNegativeButton(R.string.view_tutorial){_,_->
                val urlt = "https://www.moetranslate.top/docs/translationapi/customtext/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlt)
                startActivity(intent)
            }
            .setNeutralButton(R.string.introduce_not_show_again){
                    _, _ ->
                prefs.setBoolean("Read_Custom_Text_Introduce", true)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun setupMethodSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.request_methods,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerMethod.adapter = adapter
        }

        binding.spinnerMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                isPostMethod = parent?.getItemAtPosition(pos).toString() == "POST"
                updateViewVisibility()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateViewVisibility() {
        binding.layoutGetParams.visibility = if (isPostMethod) View.GONE else View.VISIBLE
        binding.layoutPostJson.visibility = if (isPostMethod) View.VISIBLE else View.GONE
    }

    private fun setupButtons() {
        binding.btnAddGetParam.setOnClickListener { addKeyValuePair(binding.containerGetParams) }
        binding.btnAddHeader.setOnClickListener { addKeyValuePair(binding.containerHeaders) }
        binding.btnAddJsonField.setOnClickListener { addKeyValuePair(binding.containerJsonBody) }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    private fun addKeyValuePair(container: LinearLayout): View {
        val pairView = layoutInflater.inflate(R.layout.item_key_value_pair, container, false)

        // 设置删除按钮点击事件
        pairView.findViewById<ImageButton>(R.id.btnRemove).setOnClickListener {
            container.removeView(pairView)
        }

        container.addView(pairView)
        return pairView
    }

    private fun saveConfiguration() {
        try{
            val normalizedUrl = UrlUtils.normalizeUrl(requireContext(), binding.editBaseUrl.text.toString())

            if(binding.editJsonPath.text.toString().trim().isBlank()){
                throw Exception(getString(R.string.response_json_blank))
            }

            val config = CustomTextAPIConfig(
                method = if (isPostMethod) "POST" else "GET",
                baseUrl = normalizedUrl,
                queryParams = if (isPostMethod) mutableListOf<KeyValuePair>() else collectKeyValuePairs(binding.containerGetParams),
                headers = collectKeyValuePairs(binding.containerHeaders),
                jsonBody = if (isPostMethod) collectKeyValuePairs(binding.containerJsonBody) else mutableListOf<KeyValuePair>(),
                jsonResponsePath = binding.editJsonPath.text.toString().trim()
            )

            lifecycleScope.launch {
                saveTextConfig(prefs, config, apiCode!!)
                showToast(getString(R.string.save_successfully))
                requireActivity().finish()
            }
        } catch (e: Exception){
            showToast(getString(R.string.failed_save_config, e.message))
        }
    }

    private fun loadConfig() {
        // 确保config不为空
        config?.let { savedConfig ->
            try {
                // 设置基本字段
                binding.editBaseUrl.setText(savedConfig.baseUrl)
                binding.editJsonPath.setText(savedConfig.jsonResponsePath)

                // 设置请求方法
                isPostMethod = savedConfig.method == "POST"
                binding.spinnerMethod.setSelection(if (isPostMethod) 1 else 0)

                // 清除现有的键值对
                binding.containerGetParams.removeAllViews()
                binding.containerHeaders.removeAllViews()
                binding.containerJsonBody.removeAllViews()

                // 加载GET参数
                savedConfig.queryParams.forEach { pair ->
                    addKeyValuePair(binding.containerGetParams).apply {
                        findViewById<TextInputEditText>(R.id.editKey).setText(pair.key)
                        findViewById<TextInputEditText>(R.id.editValue).setText(pair.value)
                    }
                }

                // 加载请求头
                savedConfig.headers.forEach { pair ->
                    addKeyValuePair(binding.containerHeaders).apply {
                        findViewById<TextInputEditText>(R.id.editKey).setText(pair.key)
                        findViewById<TextInputEditText>(R.id.editValue).setText(pair.value)
                    }
                }

                // 加载JSON请求体字段
                savedConfig.jsonBody.forEach { pair ->
                    addKeyValuePair(binding.containerJsonBody).apply {
                        findViewById<TextInputEditText>(R.id.editKey).setText(pair.key)
                        findViewById<TextInputEditText>(R.id.editValue).setText(pair.value)
                    }
                }

                // 更新视图可见性
                updateViewVisibility()

            } catch (e: Exception) {
                showToast("Error loading configuration: ${e.message}")
            }
        }
    }

    private fun collectKeyValuePairs(container: LinearLayout): List<KeyValuePair> {
        val pairs = mutableListOf<KeyValuePair>()

        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            val key = view.findViewById<TextInputEditText>(R.id.editKey).text.toString()
            val value = view.findViewById<TextInputEditText>(R.id.editValue).text.toString()

            if (key.isNotBlank() && value.isNotBlank()) {
                pairs.add(KeyValuePair(key, value))
            }else{
                throw Exception(getString(R.string.key_value_blank))
            }
        }

        return pairs
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}