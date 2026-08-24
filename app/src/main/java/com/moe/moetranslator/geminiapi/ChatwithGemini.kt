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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
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
import com.moe.moetranslator.openaimanager.OpenAIPresetRepository
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.KeystoreManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import translationapi.openaitranslation.OpenAITranslation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

    // 当前正在进行的聊天任务（用于“停止”按钮）
    private var currentChatJob: Job? = null

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
            showDeleteCurrentSessionDialog()
        }

        // 点击顶部标题：打开会话列表
        binding.providerName.setOnClickListener {
            showSessionListDialog()
        }

        // 新建会话
        binding.newSession.setOnClickListener {
            if (currentChatJob?.isActive == true) {
                showToast(getString(R.string.chat_stop_first))
                return@setOnClickListener
            }
            messageViewModel.newSession()
            showToast(getString(R.string.chat_new_session))
        }

        binding.buttonSend.setOnClickListener {
            // 生成中：按钮变成“停止”
            if (currentChatJob?.isActive == true) {
                currentChatJob?.cancel()
                return@setOnClickListener
            }

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
    private suspend fun buildChatProvider(): ChatProvider? {
        return when (currentProviderType()) {
            PROVIDER_OPENAI -> {
                val chatSystemPrompt = prefs.getString("Chat_OpenAI_System_Prompt", "")
                    .takeIf { it.isNotBlank() }

                // 引用翻译侧聚合 AI 的激活预设：Base URL / Key / 模型 / 自定义参数都取自预设
                if (prefs.getBoolean("Chat_OpenAI_Use_UniAI_Preset", false)) {
                    val preset = OpenAIPresetRepository.getInstance(requireContext()).getActive()
                    if (preset != null && preset.apiKey.isNotBlank()) {
                        OpenAIChatProvider(
                            apiKey = preset.apiKey,
                            baseUrl = preset.baseUrl.ifBlank { DEFAULT_OPENAI_BASE_URL },
                            model = preset.modelName.ifBlank { DEFAULT_OPENAI_MODEL },
                            systemPrompt = chatSystemPrompt,
                            extraParams = OpenAITranslation.decodeExtraParams(preset.extraParams),
                        )
                    } else {
                        // 翻译侧没有激活预设：回退到聊天自己的手动配置
                        showToast(getString(R.string.chat_uniai_fallback))
                        val key = KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI)
                        if (key.isNullOrEmpty()) {
                            showToast(getString(R.string.chat_api_not_set))
                            return null
                        }
                        OpenAIChatProvider(
                            apiKey = key,
                            baseUrl = prefs.getString("Chat_OpenAI_Base_Url", DEFAULT_OPENAI_BASE_URL),
                            model = prefs.getString("Chat_OpenAI_Model", DEFAULT_OPENAI_MODEL),
                            systemPrompt = chatSystemPrompt,
                            extraParams = OpenAIChatProvider.parseExtraParams(
                                prefs.getString("Chat_OpenAI_Extra_Params", "")
                            ),
                        )
                    }
                } else {
                    val key = KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI)
                    if (key.isNullOrEmpty()) {
                        showToast(getString(R.string.chat_api_not_set))
                        return null
                    }
                    OpenAIChatProvider(
                        apiKey = key,
                        baseUrl = prefs.getString("Chat_OpenAI_Base_Url", DEFAULT_OPENAI_BASE_URL),
                        model = prefs.getString("Chat_OpenAI_Model", DEFAULT_OPENAI_MODEL),
                        systemPrompt = chatSystemPrompt,
                        extraParams = OpenAIChatProvider.parseExtraParams(
                            prefs.getString("Chat_OpenAI_Extra_Params", "")
                        ),
                    )
                }
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
        val useUniAICheck = customView.findViewById<CheckBox>(R.id.chat_use_uniai)
        val uniAIStatus = customView.findViewById<TextView>(R.id.chat_uniai_status)
        val testButton = customView.findViewById<Button>(R.id.chat_test_button)
        val testResult = customView.findViewById<TextView>(R.id.chat_test_result)

        // 用当前编辑框内容构建测试用提供商（不落盘保存）
        suspend fun buildTestProvider(): ChatProvider? {
            val isOpenAI = providerGroup.checkedRadioButtonId == R.id.chat_provider_openai
            val keyText = apiKeyEdit.text.toString().trim()
            return if (isOpenAI) {
                if (useUniAICheck.isChecked) {
                    val preset = OpenAIPresetRepository.getInstance(requireContext()).getActive()
                    if (preset != null && preset.apiKey.isNotBlank()) {
                        OpenAIChatProvider(
                            apiKey = preset.apiKey,
                            baseUrl = preset.baseUrl.ifBlank { DEFAULT_OPENAI_BASE_URL },
                            model = preset.modelName.ifBlank { DEFAULT_OPENAI_MODEL },
                            systemPrompt = systemPromptEdit.text.toString().trim().takeIf { it.isNotBlank() },
                            extraParams = OpenAITranslation.decodeExtraParams(preset.extraParams),
                        )
                    } else null
                } else {
                    val key = keyText.ifEmpty {
                        KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI)
                    }
                    if (key.isNullOrEmpty()) return null
                    OpenAIChatProvider(
                        apiKey = key,
                        baseUrl = baseUrlEdit.text.toString().trim().ifBlank { DEFAULT_OPENAI_BASE_URL },
                        model = modelEdit.text.toString().trim(),
                        systemPrompt = systemPromptEdit.text.toString().trim().takeIf { it.isNotBlank() },
                        extraParams = OpenAIChatProvider.parseExtraParams(extraParamsEdit.text.toString()),
                    )
                }
            } else {
                val key = keyText.ifEmpty {
                    KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_GEMINI)
                }
                if (key.isNullOrEmpty()) return null
                GeminiChatProvider(
                    modelName = modelEdit.text.toString().trim(),
                    apiKey = key,
                    systemInstruction = systemPromptEdit.text.toString().trim().takeIf { it.isNotBlank() },
                )
            }
        }

        // 测试连通性：向当前配置发送最小请求
        testButton.setOnClickListener {
            testButton.isEnabled = false
            testButton.text = getString(R.string.chat_testing)
            testResult.text = ""
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val provider = buildTestProvider()
                    if (provider == null) {
                        testResult.text = getString(R.string.chat_api_not_set)
                    } else {
                        val msg = provider.testConnection()
                        testResult.text = getString(R.string.chat_test_success, msg)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    testResult.text = getString(R.string.chat_test_failed, e.message ?: e.toString())
                } finally {
                    testButton.isEnabled = true
                    testButton.text = getString(R.string.chat_test)
                }
            }
        }

        introView.text = getText(R.string.chat_provider_intro)

        val isOpenAICurrent = currentProviderType() == PROVIDER_OPENAI

        // 读取翻译侧激活预设名并展示状态
        fun refreshUniAIStatus() {
            uniAIStatus.text = getString(R.string.chat_uniai_status_loading)
            viewLifecycleOwner.lifecycleScope.launch {
                val preset = OpenAIPresetRepository.getInstance(requireContext()).getActive()
                uniAIStatus.text = if (preset == null) {
                    getString(R.string.chat_uniai_status_none)
                } else {
                    getString(R.string.chat_uniai_status_active, preset.displayName.ifBlank { preset.modelName })
                }
            }
        }

        // 按当前选择刷新各字段可见性
        fun applyFieldVisibility(isOpenAI: Boolean, useUniAI: Boolean) {
            useUniAICheck.visibility = if (isOpenAI) View.VISIBLE else View.GONE
            uniAIStatus.visibility = if (isOpenAI && useUniAI) View.VISIBLE else View.GONE
            baseUrlEdit.visibility = if (isOpenAI && !useUniAI) View.VISIBLE else View.GONE
            modelEdit.visibility = if (!isOpenAI || !useUniAI) View.VISIBLE else View.GONE
            apiKeyEdit.visibility = if (!isOpenAI || !useUniAI) View.VISIBLE else View.GONE
            extraParamsEdit.visibility = if (isOpenAI && !useUniAI) View.VISIBLE else View.GONE
            if (isOpenAI && useUniAI) refreshUniAIStatus()
        }

        // 加载某提供商的配置到编辑框
        fun loadFields(isOpenAI: Boolean) {
            val useUniAI = prefs.getBoolean("Chat_OpenAI_Use_UniAI_Preset", false)
            if (isOpenAI) {
                useUniAICheck.isChecked = useUniAI
                baseUrlEdit.setText(prefs.getString("Chat_OpenAI_Base_Url", DEFAULT_OPENAI_BASE_URL))
                modelEdit.setText(prefs.getString("Chat_OpenAI_Model", DEFAULT_OPENAI_MODEL))
                systemPromptEdit.setText(prefs.getString("Chat_OpenAI_System_Prompt", ""))
                extraParamsEdit.setText(prefs.getString("Chat_OpenAI_Extra_Params", ""))
                apiKeyEdit.hint = if (KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_CHAT_OPENAI) != null) {
                    getString(R.string.api_saved)
                } else {
                    getString(R.string.chat_api_key)
                }
                applyFieldVisibility(true, useUniAI)
            } else {
                modelEdit.setText(prefs.getString("Chat_Gemini_Model", DEFAULT_GEMINI_MODEL))
                systemPromptEdit.setText(prefs.getString("Chat_Gemini_System_Prompt", ""))
                apiKeyEdit.hint = if (KeystoreManager.retrieveKey(requireContext(), KEY_ALIAS_GEMINI) != null) {
                    getString(R.string.api_saved)
                } else {
                    getString(R.string.chat_api_key)
                }
                applyFieldVisibility(false, false)
            }
        }

        // 把编辑框内容按某提供商存回
        fun stashFields(isOpenAI: Boolean) {
            if (isOpenAI) {
                prefs.setString("Chat_OpenAI_Base_Url", baseUrlEdit.text.toString().trim())
                prefs.setString("Chat_OpenAI_Model", modelEdit.text.toString().trim())
                prefs.setString("Chat_OpenAI_System_Prompt", systemPromptEdit.text.toString().trim())
                prefs.setString("Chat_OpenAI_Extra_Params", extraParamsEdit.text.toString().trim())
                prefs.setBoolean("Chat_OpenAI_Use_UniAI_Preset", useUniAICheck.isChecked)
            } else {
                prefs.setString("Chat_Gemini_Model", modelEdit.text.toString().trim())
                prefs.setString("Chat_Gemini_System_Prompt", systemPromptEdit.text.toString().trim())
            }
        }

        // 初始化
        providerGroup.check(if (isOpenAICurrent) R.id.chat_provider_openai else R.id.chat_provider_gemini)
        loadFields(isOpenAICurrent)

        // 切换提供商：先把当前编辑框内容按旧提供商存回，再加载新提供商的配置
        providerGroup.setOnCheckedChangeListener { _, checkedId ->
            val nowOpenAI = checkedId == R.id.chat_provider_openai
            stashFields(!nowOpenAI)
            loadFields(nowOpenAI)
        }

        // 勾选/取消“使用聚合AI预设”时切换字段可见性
        useUniAICheck.setOnCheckedChangeListener { _, checked ->
            applyFieldVisibility(true, checked)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.chat_config_title)
            .setView(customView)
            .setCancelable(false)
            .setPositiveButton(R.string.save) { _, _ ->
                val isOpenAI = providerGroup.checkedRadioButtonId == R.id.chat_provider_openai
                val useUniAI = useUniAICheck.isChecked
                val alias = if (isOpenAI) KEY_ALIAS_CHAT_OPENAI else KEY_ALIAS_GEMINI

                // 引用聚合AI翻译预设时不需要自己的 Key
                if (!(isOpenAI && useUniAI)) {
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
                }

                if (isOpenAI) {
                    prefs.setString("Chat_OpenAI_Base_Url", baseUrlEdit.text.toString().trim())
                    prefs.setString("Chat_OpenAI_Model", modelEdit.text.toString().trim())
                    prefs.setString("Chat_OpenAI_System_Prompt", systemPromptEdit.text.toString().trim())
                    prefs.setString("Chat_OpenAI_Extra_Params", extraParamsEdit.text.toString().trim())
                    prefs.setBoolean("Chat_OpenAI_Use_UniAI_Preset", useUniAI)
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

    private fun showDeleteCurrentSessionDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_history_title)
            .setMessage(R.string.delete_history_content)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm) { _, _ ->
                messageViewModel.deleteCurrentSession()
                showToast(getString(R.string.delete_finish))
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    /** 会话列表对话框：点击切换、长按删除。 */
    private fun showSessionListDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_session_list, null)
        val listView = dialogView.findViewById<ListView>(R.id.session_list)
        val emptyView = dialogView.findViewById<TextView>(R.id.session_empty)

        var sessionDialog: AlertDialog? = null

        lifecycleScope.launch {
            val sessions = messageViewModel.getSessions()
            if (sessions.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                listView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                listView.visibility = View.VISIBLE
                val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                val labels = sessions.map { s ->
                    val time = dateFormat.format(Date(s.lastTimestamp))
                    val marker = if (s.sessionId == messageViewModel.activeSessionId.value) "● " else ""
                    "$marker${s.title}   ($time)"
                }
                listView.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    labels
                )
                listView.setOnItemClickListener { _, _, position, _ ->
                    messageViewModel.switchSession(sessions[position].sessionId)
                    sessionDialog?.dismiss()
                }
                listView.setOnItemLongClickListener { _, _, position, _ ->
                    confirmDeleteSession(sessions[position])
                    true
                }
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.chat_sessions_title)
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton(R.string.chat_new_session) { _, _ ->
                messageViewModel.newSession()
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        sessionDialog = dialog
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    /** 长按会话：确认删除。 */
    private fun confirmDeleteSession(session: ChatSessionInfo) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.chat_session_delete_title)
            .setMessage(getString(R.string.chat_session_delete_message, session.title))
            .setCancelable(false)
            .setPositiveButton(R.string.confirm) { _, _ ->
                messageViewModel.deleteSession(session.sessionId)
                showToast(getString(R.string.delete_finish))
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun sendMessage(userContent: String) {
        val job = viewLifecycleOwner.lifecycleScope.launch {

            // 发送中：按钮变成“停止”
            binding.buttonSend.text = getString(R.string.chat_stop)

            val sessionId = messageViewModel.activeSessionId.value

            // 保存用户消息
            val userMessage = ChatMessage(
                content = userContent,
                timestamp = System.currentTimeMillis(),
                sender = 2, // 用户
                sessionId = sessionId,
            )
            val userMessageId = messageViewModel.insert(userMessage)

            var aiMessageId = 0L
            // 是否已收到首个流式增量（用于占位清理与停止标记）
            var streamStarted = false

            try {
                // 构建当前选择的提供商
                val provider = buildChatProvider()
                if (provider == null) {
                    val emptyAPIMessage = ChatMessage(
                        content = getString(R.string.chat_api_not_set),
                        timestamp = System.currentTimeMillis(),
                        sender = 1, // AI
                        sessionId = sessionId,
                    )
                    messageViewModel.insert(emptyAPIMessage)
                    return@launch
                }

                // 创建AI回复消息
                val aiMessage = ChatMessage(
                    content = getString(R.string.gemini_thinking),
                    timestamp = System.currentTimeMillis(),
                    sender = 1, // AI
                    sessionId = sessionId,
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

                // 调用AI API：思考与正文均流式增量实时写入
                fun ensureStreamStarted() {
                    if (!streamStarted) {
                        // 第一个增量到达时清空“思考中”占位
                        messageViewModel.clearMessageById(aiMessageId)
                        streamStarted = true
                    }
                }
                val reply = provider.chat(
                    history,
                    userContent,
                    onReasoning = { chunk ->
                        ensureStreamStarted()
                        messageViewModel.appendReasoningById(aiMessageId, chunk)
                    },
                    onContent = { chunk ->
                        ensureStreamStarted()
                        messageViewModel.appendContentById(aiMessageId, chunk)
                    },
                )
                // 以完整文本为准最终写入（trim 首尾空白）
                messageViewModel.updateMessageWithReasoning(aiMessageId, reply.content, reply.reasoning)
            } catch (e: CancellationException) {
                // 用户点了“停止”
                if (aiMessageId != 0L) {
                    if (streamStarted) {
                        // 已有部分内容：追加停止标记
                        messageViewModel.appendContentById(aiMessageId, getString(R.string.chat_stopped_suffix))
                    } else {
                        messageViewModel.updateMessageContent(aiMessageId, getString(R.string.chat_stopped))
                    }
                }
                throw e
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
                        sender = 1, // AI
                        sessionId = sessionId,
                    )
                    messageViewModel.insert(errorMessage)
                }
            } finally {
                // 只有当前任务仍是自己时才清空引用（避免覆盖新一轮任务）
                if (currentChatJob == coroutineContext[Job]) {
                    currentChatJob = null
                }
                binding.buttonSend.text = getString(R.string.send)
            }
        }
        currentChatJob = job
    }

    private fun showToast(str: String, isShort: Boolean = false) {
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }
}
