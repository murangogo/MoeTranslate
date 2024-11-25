package com.moe.moetranslator.geminiapi

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentChatwithgeminiBinding
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatwithGemini : Fragment() {

    private lateinit var binding: FragmentChatwithgeminiBinding
    private lateinit var prefs: CustomPreference
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter

    private var geminiApiKey = ""
    private var geminiModel: GenerativeModel? = null
    private var geminiChat: Chat? = null

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
        binding = FragmentChatwithgeminiBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeMessages()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        binding.messageList.apply {
            this.adapter = this@ChatwithGemini.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners(){
        binding.settingGemini.setOnClickListener {
            showGeminiAPIDialog()
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

    private fun showGeminiAPIDialog(){
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_message_edittext, null)
        val textView = customView.findViewById<TextView>(R.id.dialog_top_message)
        val apiEdit = customView.findViewById<EditText>(R.id.dialog_bottom_edittext)

        // 使用getText保证HTML标签有效
        textView.text = getText(R.string.gemini_intro)

        apiEdit.hint = if(prefs.getString("Gemini_EncryptedKey", "") != ""){
            getString(R.string.api_saved)
        }else{
            getString(R.string.gemini_key)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.gemini_title)
            .setView(customView)
            .setCancelable(false)
            .setPositiveButton(R.string.save) {_,_->
                if(apiEdit.text.isBlank()){
                    showToast(getString(R.string.fill_blank))
                } else {
                    KeystoreManager.storeKey(
                        requireContext(),
                        apiEdit.text.toString().trim(),
                        "Gemini"
                    )
                    showToast(getString(R.string.save_successfully))
                }
            }
            .setNeutralButton(R.string.view_tutorial){_,_->
                val url = "https://blog.csdn.net/qq_45487246/article/details/135536624"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showDeleteConfirmationDialog(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_history_title)
            .setMessage(R.string.delete_history_content)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm){ _,_ ->
                messageViewModel.deleteAll()
                showToast(getString(R.string.delete_finish))
            }
            .setNegativeButton(R.string.user_cancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun sendMessage(userContent: String){
        viewLifecycleOwner.lifecycleScope.launch {

            withContext(Dispatchers.Main){
                binding.buttonSend.isClickable = false
                binding.buttonSend.text = getString(R.string.please_wait)
            }

            // 保存用户消息
            val userMessage = ChatMessage(
                content = userContent,
                timestamp = System.currentTimeMillis(),
                sender = 2 // 用户
            )
            messageViewModel.insert(userMessage)

            var aiMessageId = 0L

            // 调用Gemini API
            try {
                if (geminiApiKey.isEmpty()) {
                    if (prefs.getString("Gemini_EncryptedKey", "") == "") {
                        showToast(getString(R.string.gemini_api_empty))
                        val emptyAPIMessage = ChatMessage(
                            content = getString(R.string.gemini_set_api),
                            timestamp = System.currentTimeMillis(),
                            sender = 1 // AI
                        )
                        messageViewModel.insert(emptyAPIMessage)
                        return@launch
                    } else {
                        geminiApiKey = KeystoreManager.retrieveKey(requireContext(), "Gemini")!!
                        Log.d("GEMINI",geminiApiKey)
                    }
                }

                // 创建AI回复消息
                val aiMessage = ChatMessage(
                    content = getString(R.string.gemini_thinking),
                    timestamp = System.currentTimeMillis(),
                    sender = 1 // AI
                )
                aiMessageId = messageViewModel.insert(aiMessage)
                Log.d("GEMINI","aimessageid:${aiMessageId}")

                if (geminiModel == null){
                    geminiModel = GeminiModelFactory.createGeminiModel("gemini-1.5-flash", geminiApiKey)
                }

                // 创建历史记录实现多轮聊天
                val messages = messageViewModel.getAllMessagesList()
                val chatHistory = mutableListOf<Content>()

                // 将历史消息转换为Gemini API格式
                messages.forEach { message ->
                    val role = if (message.sender == 1) "model" else "user"
                    chatHistory.add( content( role ){ text( message.content ) } )
                }

                geminiChat = geminiModel!!.startChat(history = chatHistory)

                var isFirstChunk = true
                geminiChat!!.sendMessageStream(userContent).collect { chunk ->
                    withContext(Dispatchers.IO) {
                        if (isFirstChunk) {
                            // 第一个chunk到达时，清空"思考中"的提示
                            messageViewModel.clearMessageById(aiMessageId)
                            isFirstChunk = false
                        }
                        // 追加新的内容
                        messageViewModel.appendContentById(aiMessageId, chunk.text!!)
                    }
                }

                // 处理消息，去除前后换行
                messageViewModel.getMessageById(aiMessageId)?.let { message ->
                    val trimmedContent = message.content.trim() // 去除前后的空白和换行
                    if (trimmedContent != message.content) {
                        // 只有当内容确实发生变化时才更新
                        messageViewModel.updateMessageContent(aiMessageId, trimmedContent)
                    }
                }

            } catch (e: Exception) {
                // 创建错误消息提醒
                messageViewModel.clearMessageById(aiMessageId)
                messageViewModel.appendContentById(aiMessageId, getString(R.string.error_occurred, e.toString()))
            } finally {
                withContext(Dispatchers.Main) {
                    binding.buttonSend.isClickable = true
                    binding.buttonSend.text = getString(R.string.send)
                }
            }
        }
    }

    private fun showToast(str: String, isShort: Boolean = false){
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }
}