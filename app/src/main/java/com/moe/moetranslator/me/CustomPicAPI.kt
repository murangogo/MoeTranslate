package com.moe.moetranslator.me

import android.app.AlertDialog
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
import com.moe.moetranslator.databinding.FragmentCustomPicApiBinding
import com.moe.moetranslator.me.ConfigurationStorage.loadPicConfig
import com.moe.moetranslator.me.ConfigurationStorage.savePicConfig
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.launch

class CustomPicAPI : Fragment() {
    private lateinit var binding: FragmentCustomPicApiBinding
    private var isPostMethod = false
    private var isJsonContentType = true  // 当POST时，是否使用JSON格式
    private var apiCode: Int? = null
    private var config: CustomPicAPIConfig? = null
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
        binding = FragmentCustomPicApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMethodSpinner()
        setupContentTypeSpinner()
        setupButtons()
        updateViewVisibility()

        config = loadPicConfig(prefs, apiCode!!)
        if (config != null) {
            loadConfig()
        }

        binding.introduce.setOnClickListener{
            showIntroduce()
        }

        if(!prefs.getBoolean("Read_Custom_Pic_Introduce", false)){
            showIntroduce()
        }
    }

    private fun showIntroduce(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.introduce_custom_pic_api_title)
            .setMessage(R.string.introduce_custom_pic_api_content)
            .setCancelable(false)
            .setPositiveButton(R.string.user_known, null)
            .setNeutralButton(R.string.introduce_not_show_again){
                _, _ ->
                prefs.setBoolean("Read_Custom_Pic_Introduce", true)
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

    private fun setupContentTypeSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.content_types,  // "application/json", "multipart/form-data"
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerContentType.adapter = adapter
        }

        binding.spinnerContentType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                isJsonContentType = parent?.getItemAtPosition(pos).toString().contains("json")
                updateViewVisibility()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateViewVisibility() {
        // GET/POST相关视图
        binding.layoutGetParams.visibility = if (isPostMethod) View.GONE else View.VISIBLE
        binding.layoutPostBody.visibility = if (isPostMethod) View.VISIBLE else View.GONE

        // Content-Type选择器
        binding.layoutContentType.visibility = if (isPostMethod) View.VISIBLE else View.GONE

        // 根据Content-Type更新提示文本
        if (isPostMethod) {
            binding.txtBodyHint.text = if (isJsonContentType) {
                getString(R.string.use_img_base64)
            } else {
                getString(R.string.use_img_file)
            }
        }
    }

    private fun setupButtons() {
        binding.btnAddGetParam.setOnClickListener { addKeyValuePair(binding.containerGetParams) }
        binding.btnAddHeader.setOnClickListener { addKeyValuePair(binding.containerHeaders) }
        binding.btnAddBodyField.setOnClickListener { addKeyValuePair(binding.containerBody) }
        binding.btnSave.setOnClickListener { saveConfiguration() }
    }

    private fun addKeyValuePair(container: LinearLayout): View {
        val pairView = layoutInflater.inflate(R.layout.item_key_value_pair, container, false)

        pairView.findViewById<ImageButton>(R.id.btnRemove).setOnClickListener {
            container.removeView(pairView)
        }

        container.addView(pairView)
        return pairView
    }

    private fun saveConfiguration() {
        try {
            val normalizedUrl = UrlUtils.normalizeUrl(requireContext(), binding.editBaseUrl.text.toString())

            if(binding.editJsonPath.text.toString().trim().isBlank()){
                throw Exception(getString(R.string.response_json_blank))
            }

            val contentType = if (isPostMethod) {
                if (isJsonContentType) "application/json" else "multipart/form-data"
            } else null

            val config = CustomPicAPIConfig(
                method = if (isPostMethod) "POST" else "GET",
                contentType = contentType,
                baseUrl = normalizedUrl,
                queryParams = if (isPostMethod) mutableListOf<KeyValuePair>() else collectKeyValuePairs(binding.containerGetParams),
                headers = collectKeyValuePairs(binding.containerHeaders),
                body = if (isPostMethod) collectKeyValuePairs(binding.containerBody) else mutableListOf<KeyValuePair>(),
                jsonResponsePath = binding.editJsonPath.text.toString().trim()
            )

            lifecycleScope.launch {
                savePicConfig(prefs, config, apiCode!!)
                showToast(getString(R.string.save_successfully))
                requireActivity().finish()
            }
        } catch (e: Exception) {
            showToast(getString(R.string.failed_save_config, e.message))
        }
    }

    private fun loadConfig() {
        config?.let { savedConfig ->
            try {
                // 设置基本字段
                binding.editBaseUrl.setText(savedConfig.baseUrl)
                binding.editJsonPath.setText(savedConfig.jsonResponsePath)

                // 设置请求方法
                isPostMethod = savedConfig.method == "POST"
                binding.spinnerMethod.setSelection(if (isPostMethod) 1 else 0)

                // 设置Content-Type
                if (isPostMethod && savedConfig.contentType != null) {
                    isJsonContentType = savedConfig.contentType.contains("json")
                    binding.spinnerContentType.setSelection(if (isJsonContentType) 0 else 1)
                }

                // 清除现有的键值对
                binding.containerGetParams.removeAllViews()
                binding.containerHeaders.removeAllViews()
                binding.containerBody.removeAllViews()

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

                // 加载请求体字段
                savedConfig.body.forEach { pair ->
                    addKeyValuePair(binding.containerBody).apply {
                        findViewById<TextInputEditText>(R.id.editKey).setText(pair.key)
                        findViewById<TextInputEditText>(R.id.editValue).setText(pair.value)
                    }
                }

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
            } else {
                throw Exception(getString(R.string.key_value_blank))
            }
        }

        return pairs
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}