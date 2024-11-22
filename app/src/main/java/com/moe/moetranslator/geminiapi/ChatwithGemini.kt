package com.moe.moetranslator.geminiapi

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentChatwithgeminiBinding
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ChatwithGemini : Fragment() {

    private lateinit var binding: FragmentChatwithgeminiBinding
    private lateinit var prefs: CustomPreference
    private lateinit var messageViewModel: MessageViewModel

    private lateinit var geminiModelFactory:GeminiModelFactory
    private lateinit var geminiModel: GenerativeModel
    private lateinit var geminiChat: Chat



    private lateinit var viewAdapter: MessageAdapter




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

//        messageViewModel.allMessages.observe(viewLifecycleOwner, Observer { messages ->
//            // Update the cached copy of the messages in the adapter.
//            messages?.let { viewAdapter.setMessages(it) }
//        })

    }

    private fun setupRecyclerView() {
        binding.messageList.apply {
            adapter = MessageAdapter(emptyList())
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners(){
        binding.settingGemini.setOnClickListener {
            showGeminiAPIDialog()
        }
    }

    private fun showGeminiAPIDialog(){
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_message_edittext, null)
        val textView = customView.findViewById<TextView>(R.id.dialog_top_message)
        val apiEdit = customView.findViewById<EditText>(R.id.dialog_bottom_edittext)

        textView.text =  getString(R.string.gemini_intro)
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
                KeystoreManager.storeKey(
                    requireContext(),
                    apiEdit.text.toString().trim(),
                    "Gemini"
                )
                showToast(getString(R.string.gemini_save))
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

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onStart() {
        super.onStart()

        binding.buttonSend.setOnClickListener {
            val messageText = binding.inputBox.text.toString()
            if (messageText.isNotEmpty()) {
                val userMessage = ChatMessage(content = messageText, timestamp = System.currentTimeMillis(), sender = 2)
                messageViewModel.insert(userMessage)
                binding.inputBox.setText("")
                if(prefs.getString("Gemini_EncryptedKey", "") == ""){
                    lifecycleScope.launch(Dispatchers.IO) {
                        val response = "您还未配置Gemini API，请点击左上角齿轮按钮进行配置，也可在其中查看教程，配置完成后请先清除聊天记录再发送消息。"
                        val botMessage = ChatMessage(content = response, timestamp = System.currentTimeMillis(), sender = 1)
                        messageViewModel.insert(botMessage)
                    }
                }else{
                    if(!(::geminiModel.isInitialized)){
                        try {
                            // TODO: MODEL
                            geminiModel = geminiModelFactory.createGeminiModel("","")
                            val messages = messageViewModel.allMessages.value
                            // 将每条消息转换为 Content 对象
                            val chatHistory = mutableListOf<Content>()
                            messages?.forEach { message ->
                                val role = if (message.sender == 1) "model" else "user"
                                chatHistory.add(content(role) { text(message.content) })
                            }
                            geminiChat = geminiModel.startChat(
                                history = chatHistory
                            )
                        }catch (e: Exception){
                            lifecycleScope.launch(Dispatchers.IO) {
                                val response = "模型初始化失败，请检查Gemini API是否配置正确后重试。"
                                val botMessage = ChatMessage(content = response, timestamp = System.currentTimeMillis(), sender = 1)
                                messageViewModel.insert(botMessage)
                            }
                        }
                    }
                    binding.buttonSend.text = "请等待"
                    binding.buttonSend.isClickable = false
                    // launch a coroutine to send the message and receive the response
                    lifecycleScope.launch(Dispatchers.IO) {
                        val response = ""
                        val currenttime = System.currentTimeMillis()
                        val botMessage = ChatMessage(content = response, timestamp = currenttime, sender = 1)
                        messageViewModel.insert(botMessage)
                        try {
                            geminiChat.sendMessageStream(messageText).collect { chunk ->
                                run {
                                    try {
                                        messageViewModel.appendContentByTimestamp(
                                            currenttime,
                                            chunk.text!!
                                        )
                                    } catch (e: Exception) {
                                        messageViewModel.appendContentByTimestamp(
                                            currenttime,
                                            e.toString()
                                        )
                                    }
                                }
                            }
                        } catch (e: ServerException){
                            messageViewModel.appendContentByTimestamp(
                                currenttime,
                                "服务器返回信息："+e.message.toString()+"\n开发者备注：若提示地区不可用，请更换科学上网地区为国外节点。"
                            )
                        } catch (e: Exception){
                            messageViewModel.appendContentByTimestamp(
                                currenttime,
                                "其他类型错误："+e.message.toString()
                            )
                        }
                        MainScope().launch {
                            binding.buttonSend.text = "发送"
                            binding.buttonSend.isClickable = true
                        }
                    }
                }
            }
        }

        binding.cleanGemini.setOnClickListener {
//            val customView:View = LayoutInflater.from(context).inflate(R.layout.gemini_clean, null,false)
//            var textcon = customView.findViewById<TextView>(R.id.gemini_cleaninto)
//            val dialogBuilder = AlertDialog.Builder(context).setView(customView).setCancelable(false).setNegativeButton("取消"){_,_->}
//            dialogBuilder.setTitle("清除聊天记录")
//            textcon.text = "    确认清除聊天记录吗？这也会清除AI的记忆。"
//            dialogBuilder.setPositiveButton("确认") {_,_->
//                messageViewModel.deleteAll()
//                Toast.makeText(context,"清除成功", Toast.LENGTH_LONG).show()
//                geminimodel = geminimodelfactory.createGeminiModel(repository.GeminiModel,repository.GeminiApi)
//                geminichat = geminimodel.startChat(
//                    history = listOf()
//                )
//            }
//            val myDialog = dialogBuilder.create()
//            myDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
//            myDialog.show()
//            myDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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