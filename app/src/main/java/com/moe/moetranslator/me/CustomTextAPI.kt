package com.moe.moetranslator.me

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
import com.moe.moetranslator.databinding.CustomTextApiBinding
import com.moe.moetranslator.me.ConfigurationStorage.loadTextConfig
import com.moe.moetranslator.me.ConfigurationStorage.saveTextConfig
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.launch

class CustomTextAPI :Fragment() {
    private lateinit var binding: CustomTextApiBinding
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
        binding = CustomTextApiBinding.inflate(inflater, container, false)
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
        val pairView = layoutInflater.inflate(R.layout.layout_key_value_pair, container, false)

        // 设置删除按钮点击事件
        pairView.findViewById<ImageButton>(R.id.btnRemove).setOnClickListener {
            container.removeView(pairView)
        }

        container.addView(pairView)
        return pairView
    }

    private fun saveConfiguration() {
        try{
            val config = CustomTextAPIConfig(
                method = if (isPostMethod) "POST" else "GET",
                baseUrl = binding.editBaseUrl.text.toString(),
                queryParams = collectKeyValuePairs(binding.containerGetParams),
                headers = collectKeyValuePairs(binding.containerHeaders),
                jsonBody = collectKeyValuePairs(binding.containerJsonBody),
                jsonResponsePath = binding.editJsonPath.text.toString()
            )

            lifecycleScope.launch {
                saveTextConfig(prefs, config, apiCode!!)
                showToast("Configuration saved successfully")
            }
        } catch (e: Exception){
            showToast("Error saving configuration: ${e.message}")
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
                throw Exception("Key or value is blank")
            }
        }

        return pairs
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}