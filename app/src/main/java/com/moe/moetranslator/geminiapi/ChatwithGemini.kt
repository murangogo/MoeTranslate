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

package com.moe.moetranslator.geminiapi

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.moe.moetranslator.R
import com.moe.moetranslator.chatapi.ChatProvider
import com.moe.moetranslator.chatapi.ChatTurn
import com.moe.moetranslator.chatapi.GeminiChatProvider
import com.moe.moetranslator.chatapi.OpenAIChatProvider
import com.moe.moetranslator.databinding.FragmentChatwithgeminiBinding
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.KeystoreManager
import kotlinx.coroutines.launch


class ChatwithGemini : Fragment() {

    companion object {
        // 聊天提供商类型
        const val PROVIDER_GEMINI = 0
        const val PROVIDER_OPENAI = 1

        // 默认配置
        const val DEFAULT_GEMINI_MODEL = "gemini-3.5-flash"
        const val DEFAULT_OPENAI_BASE_URL = "https://api.openai.com/v1"
        const val DEFAULT_OPENAI_MODEL = "gpt-4o-mini"

        // Keystore 别名：Gemini 沿用既有别名，OpenAI 聊天独立存储
        const val KEY_ALIAS_GEMINI = "Gemini"
        const val KEY_ALIAS_CHAT_OPENAI = "Chat_OpenAI"
    }

    private lateinit var binding: FragmentChatwithgeminiBinding
    private lateinit var prefs: CustomPreference
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = CustomPreference.getInstance(requireContext())
        val factory = MessageViewModelFactory(requireActivity().application)
        messageViewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatwithgeminiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeMessages()
        refreshProviderName()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        binding.messageList.apply {
            this.adapter = this@ChatwithGemini.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.settingGemini.setOnClickListener {
            showChatConfigDialog()
        }

        binding.cleanGemini.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.buttonSend.setOnClickListener {
            val content = binding.inputBox.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
                binding.inputBox.text.clear()
            }
        }
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageViewModel.allMessages.collect { messages ->
                    adapter.submitList(messages)
                    // 滚动到最新消息
                    if (messages.isNotEmpty()) {
                        binding.messageList.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    /** 顶部标题跟随当前提供商切换。 */
    private fun refreshProviderName() {
        binding.providerName.text =
            if (prefs.getInt("Chat_Provider", PROVIDER_GEMINI) == PROVIDER_OPENAI) {
                getString(R.string.chat_provider_openai)
            } else {
                getString(R.string.chat_provider_gemini)
            }
    }

    private fun currentProviderType(): Int = prefs.getInt("Chat_Provider", PROVIDER_GEMINI)

    /** 根据当前设置构建聊天提供商；未配置 Key 时提示并返回 null。 */
    private fun buildChatProvider(): ChatProvider? {
        return when (currentProviderType()) {
            PROVIDER_OPENAI -> {
                val key = KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI)
                if (key.isNullOrEmpty()) {
                    showToast(getString(R.string.chat_api_not_set))
                    return null
                }
                OpenAIChatProvider(
                    apiKey = key,
                    baseUrl = prefs.getString("Chat_OpenAI_Base_Url", DEFAULT_OPENAI_BASE_URL),
                    model = prefs.getString("Chat_OpenAI_Model", DEFAULT_OPENAI_MODEL),
                    systemPrompt = prefs.getString("Chat_OpenAI_System_Prompt", "").takeIf { it.isNotBlank() },
                    extraParams = OpenAIChatProvider.parseExtraParams(
                        prefs.getString("Chat_OpenAI_Extra_Params", "")
                    ),
                )
            }
            else -> {
                val key = KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_GEMINI)
                if (key.isNullOrEmpty()) {
                    showToast(getString(R.string.chat_api_not_set))
                    return null
                }
                GeminiChatProvider(
                    modelName = prefs.getString("Chat_Gemini_Model", DEFAULT_GEMINI_MODEL),
                    apiKey = key,
                    systemInstruction = prefs.getString("Chat_Gemini_System_Prompt", "").takeIf { it.isNotBlank() },
                )
            }
        }
    }

    private fun showChatConfigDialog() {
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_chat_provider, null)
        val introView = customView.findViewById<TextView>(R.id.chat_provider_intro)
        val providerGroup = customView.findViewById<RadioGroup>(R.id.chat_provider_group)
        val baseUrlEdit = customView.findViewById<EditText>(R.id.chat_base_url)
        val modelEdit = customView.findViewById<EditText>(R.id.chat_model)
        val apiKeyEdit = customView.findViewById<EditText>(R.id.chat_api_key)
        val systemPromptEdit = customView.findViewById<EditText>(R.id.chat_system_prompt)
        val extraParamsEdit = customView.findViewById<EditText>(R.id.chat_extra_params)

        introView.text = getText(R.string.chat_provider_intro)

        val isOpenAICurrent = currentProviderType() == PROVIDER_OPENAI
        if (isOpenAICurrent) {
            providerGroup.check(R.id.chat_provider_openai)
            baseUrlEdit.visibility = View.VISIBLE
            baseUrlEdit.setText(prefs.getString("Chat_OpenAI_Base_Url", DEFAULT_OPENAI_BASE_URL))
            modelEdit.setText(prefs.getString("Chat_OpenAI_Model", DEFAULT_OPENAI_MODEL))
            systemPromptEdit.setText(prefs.getString("Chat_OpenAI_System_Prompt", ""))
            extraParamsEdit.visibility = View.VISIBLE
            extraParamsEdit.setText(prefs.getString("Chat_OpenAI_Extra_Params", ""))
            apiKeyEdit.hint = if (KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI) != null) {
                getString(R.string.api_saved)
            } else {
                getString(R.string.chat_api_key)
            }
        } else {
            providerGroup.check(R.id.chat_provider_gemini)
            baseUrlEdit.visibility = View.GONE
            extraParamsEdit.visibility = View.GONE
            modelEdit.setText(prefs.getString("Chat_Gemini_Model", DEFAULT_GEMINI_MODEL))
            systemPromptEdit.setText(prefs.getString("Chat_Gemini_System_Prompt", ""))
            apiKeyEdit.hint = if (KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_GEMINI) != null) {
                getString(R.string.api_saved)
            } else {
                getString(R.string.chat_api_key)
            }
        }

        // 切换提供商：先把当前编辑框内容按旧提供商存回，再加载新提供商的配置
        providerGroup.setOnCheckedChangeListener { _, checkedId ->
            val nowOpenAI = checkedId == R.id.chat_provider_openai
            if (nowOpenAI) {
                // 旧的是 Gemini：保存 Gemini 模型名与系统提示词
                prefs.setString("Chat_Gemini_Model", modelEdit.text.toString().trim())
                prefs.setString("Chat_Gemini_System_Prompt", systemPromptEdit.text.toString().trim())
                baseUrlEdit.visibility = View.VISIBLE
                baseUrlEdit.setText(prefs.getString("Chat_OpenAI_Base_Url", DEFAULT_OPENAI_BASE_URL))
                modelEdit.setText(prefs.getString("Chat_OpenAI_Model", DEFAULT_OPENAI_MODEL))
                systemPromptEdit.setText(prefs.getString("Chat_OpenAI_System_Prompt", ""))
                extraParamsEdit.visibility = View.VISIBLE
                extraParamsEdit.setText(prefs.getString("Chat_OpenAI_Extra_Params", ""))
                apiKeyEdit.hint = if (KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI) != null) {
                    getString(R.string.api_saved)
                } else {
                    getString(R.string.chat_api_key)
                }
            } else {
                // 旧的是 OpenAI：保存 Base URL、模型名、系统提示词与自定义参数
                prefs.setString("Chat_OpenAI_Base_Url", baseUrlEdit.text.toString().trim())
                prefs.setString("Chat_OpenAI_Model", modelEdit.text.toString().trim())
                prefs.setString("Chat_OpenAI_System_Prompt", systemPromptEdit.text.toString().trim())
                prefs.setString("Chat_OpenAI_Extra_Params", extraParamsEdit.text.toString().trim())
                baseUrlEdit.visibility = View.GONE
                extraParamsEdit.visibility = View.GONE
                modelEdit.setText(prefs.getString("Chat_Gemini_Model", DEFAULT_GEMINI_MODEL))
                systemPromptEdit.setText(prefs.getString("Chat_Gemini_System_Prompt", ""))
                apiKeyEdit.hint = if (KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_GEMINI) != null) {
                    getString(R.string.api_saved)
                } else {
                    getString(R.string.chat_api_key)
                }
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.chat_config_title)
            .setView(customView)
            .setCancelable(false)
            .setPositiveButton(R.string.save) { _, _ ->
                val isOpenAI = providerGroup.checkedRadioButtonId == R.id.chat_provider_openai
                val alias = if (isOpenAI) KEY_ALIAS_CHAT_OPENAI else KEY_ALIAS_GEMINI
                val keyText = apiKeyEdit.text.toString().trim()

                if (keyText.isEmpty() && KeystoreManager.retrieveKey(requireContext(), alias) == null) {
                    showToast(getString(R.string.fill_blank))
                    return@setPositiveButton
                }

                // Key 非空时重新加密保存；Keystore 不允许重复生成同名密钥，先删后存保证幂等
                if (keyText.isNotEmpty()) {
                    KeystoreManager.removeKey(requireContext(), alias)
                    KeystoreManager.storeKey(requireContext(), keyText, alias)
                }

                if (isOpenAI) {
                    prefs.setString("Chat_OpenAI_Base_Url", baseUrlEdit.text.toString().trim())
                    prefs.setString("Chat_OpenAI_Model", modelEdit.text.toString().trim())
                    prefs.setString("Chat_OpenAI_System_Prompt", systemPromptEdit.text.toString().trim())
                    prefs.setString("Chat_OpenAI_Extra_Params", extraParamsEdit.text.toString().trim())
                } else {
                    prefs.setString("Chat_Gemini_Model", modelEdit.text.toString().trim())
                    prefs.setString("Chat_Gemini_System_Prompt", systemPromptEdit.text.toString().trim())
                }
                prefs.setInt("Chat_Provider", if (isOpenAI) PROVIDER_OPENAI else PROVIDER_GEMINI)

                refreshProviderName()
                showToast(getString(R.string.save_successfully))
            }
            .setNeutralButton(R.string.view_tutorial) { _, _ ->
                val url = if (providerGroup.checkedRadioButtonId == R.id.chat_provider_openai) {
                    "https://www.moetranslate.top/docs/translationapi/uniaitrans/"
                } else {
                    "https://www.moetranslate.top/docs/gemini/apiapplication/"
                }
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_history_title)
            .setMessage(R.string.delete_history_content)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm) { _, _ ->
                messageViewModel.deleteAll()
                showToast(getString(R.string.delete_finish))
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun sendMessage(userContent: String) {
        viewLifecycleOwner.lifecycleScope.launch {

            binding.buttonSend.isClickable = false
            binding.buttonSend.text = getString(R.string.please_wait)

            // 保存用户消息
            val userMessage = ChatMessage(
                content = userContent,
                timestamp = System.currentTimeMillis(),
                sender = 2 // 用户
            )
            val userMessageId = messageViewModel.insert(userMessage)

            var aiMessageId = 0L

            try {
                // 构建当前选择的提供商
                val provider = buildChatProvider()
                if (provider == null) {
                    val emptyAPIMessage = ChatMessage(
                        content = getString(R.string.chat_api_not_set),
                        timestamp = System.currentTimeMillis(),
                        sender = 1 // AI
                    )
                    messageViewModel.insert(emptyAPIMessage)
                    return@launch
                }

                // 创建AI回复消息
                val aiMessage = ChatMessage(
                    content = getString(R.string.gemini_thinking),
                    timestamp = System.currentTimeMillis(),
                    sender = 1 // AI
                )
                aiMessageId = messageViewModel.insert(aiMessage)

                // 历史消息：排除刚插入的用户消息与“思考中”占位（用户输入单独传递）
                val allMessages = messageViewModel.getAllMessagesList()
                val history = allMessages
                    .filter { it.id != userMessageId && it.id != aiMessageId }
                    .map { msg ->
                        ChatTurn(
                            role = if (msg.sender == 1) ChatTurn.ROLE_ASSISTANT else ChatTurn.ROLE_USER,
                            content = msg.content
                        )
                    }

                // 调用AI API
                val reply = provider.chat(history, userContent)
                messageViewModel.updateMessageContent(aiMessageId, reply)
            } catch (e: Exception) {
                if (aiMessageId != 0L) {
                    // 占位消息已插入：清空后写入错误信息
                    messageViewModel.clearMessageById(aiMessageId)
                    messageViewModel.appendContentById(aiMessageId, getString(R.string.error_occurred, e.toString()))
                } else {
                    // 占位消息还没插入：直接插入错误消息
                    val errorMessage = ChatMessage(
                        content = getString(R.string.error_occurred, e.toString()),
                        timestamp = System.currentTimeMillis(),
                        sender = 1 // AI
                    )
                    messageViewModel.insert(errorMessage)
                }
            } finally {
                binding.buttonSend.isClickable = true
                binding.buttonSend.text = getString(R.string.send)
            }
        }
    }

    private fun showToast(str: String, isShort: Boolean = false) {
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }
}
