package com.moe.moetranslator.geminiapi

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentChatwithgeminiBinding
import com.moe.moetranslator.utils.MySharedPreferenceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ChatwithGemini : Fragment() {

    private lateinit var binding: FragmentChatwithgeminiBinding
    private lateinit var repository: MySharedPreferenceData
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MessageAdapter
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var messageViewmodelfactory: MessageViewModelFactory
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var geminimodelfactory:GeminiModelFactory
    private lateinit var geminimodel: GenerativeModel
    private lateinit var geminichat: Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MySharedPreferenceData(context!!)
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

        geminimodelfactory = GeminiModelFactory()
        recyclerView = binding.messageList
        messageViewmodelfactory = MessageViewModelFactory(activity!!.application)
        messageViewModel = ViewModelProvider(this,messageViewmodelfactory).get(MessageViewModel::class.java)
        viewManager = LinearLayoutManager(context)
        viewAdapter = MessageAdapter(emptyList())

        recyclerView.apply {
            adapter = viewAdapter
            layoutManager = viewManager
        }

        messageViewModel.allMessages.observe(viewLifecycleOwner, Observer { messages ->
            // Update the cached copy of the messages in the adapter.
            messages?.let { viewAdapter.setMessages(it) }
        })

    }

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onStart() {
        super.onStart()

        binding.buttonSend.setOnClickListener {
            val messageText = binding.claudeInputBox.text.toString()
            if (messageText.isNotEmpty()) {
                val userMessage = ChatMessage(content = messageText, timestamp = System.currentTimeMillis(), sender = 2)
                messageViewModel.insert(userMessage)
                binding.claudeInputBox.setText("")
                if(repository.GeminiApi==""){
                    lifecycleScope.launch(Dispatchers.IO) {
                        val response = "您还未配置Gemini API，请点击左上角齿轮按钮进行配置，也可在其中查看教程，配置完成后请先清除聊天记录再发送消息。"
                        val botMessage = ChatMessage(content = response, timestamp = System.currentTimeMillis(), sender = 1)
                        messageViewModel.insert(botMessage)
                    }
                }else{
                    if(!(::geminimodel.isInitialized)){
                        try {
                            geminimodel = geminimodelfactory.createGeminiModel(repository.GeminiModel,repository.GeminiApi)
                            val messages = messageViewModel.allMessages.value
                            // 将每条消息转换为 Content 对象
                            val chatHistory = mutableListOf<Content>()
                            messages?.forEach { message ->
                                val role = if (message.sender == 1) "model" else "user"
                                chatHistory.add(content(role) { text(message.content) })
                            }
                            geminichat = geminimodel.startChat(
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
                            geminichat.sendMessageStream(messageText).collect { chunk ->
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
        binding.settingClaude.setOnClickListener {
            val customView:View = LayoutInflater.from(context).inflate(R.layout.gemini_setting, null,false)
            val textView = customView.findViewById<TextView>(R.id.gemini_intr)
            val fullText = "    Gemini是Google最新推出的生成式AI，其配置非常简单，但和其对话需要科学上网。\n    确保您知道上面在说什么，点击“查看教程”即可跳转到相关网页；若您并不熟悉相关的知识，您也可以使用萌译的其他功能。"
            val spannableString = SpannableString(fullText)
            val redColor = ForegroundColorSpan(Color.parseColor("#E05858"))
            val startIndex1 = fullText.indexOf("科学上网")
            val endIndex1 = startIndex1 + "科学上网".length
            val blueColor = ForegroundColorSpan(Color.parseColor("#1485EE"))
            val startIndex2 = fullText.indexOf("配置非常简单")
            val endIndex2 = startIndex2 + "配置非常简单".length
            spannableString.setSpan(redColor, startIndex1, endIndex1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(blueColor, startIndex2, endIndex2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannableString

            if(repository.GeminiApi!=""){
                customView.findViewById<EditText>(R.id.gemini_api).hint = repository.GeminiApi+"（已保存）"
            }
            val mygeminiapi = customView.findViewById<EditText>(R.id.gemini_api)
            val myintr = customView.findViewById<TextView>(R.id.gemini_intr)
            val dialogBuilder = AlertDialog.Builder(context).setView(customView).setCancelable(false).setNegativeButton("取消"){_,_->}.setPositiveButton("保存"){_,_->
                repository.saveGeminiApi(mygeminiapi.text.toString())
                Toast.makeText(context,"保存成功，请点击右上角清除聊天记录后再开始对话。", Toast.LENGTH_LONG).show()
            }.setNeutralButton("查看教程"){_,_->
                val url = "https://blog.csdn.net/qq_45487246/article/details/135536624"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            val myDialog = dialogBuilder.create()
            myDialog.setTitle("配置相关参数")
            myDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            myDialog.show()
            myDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        binding.cleanClaude.setOnClickListener {
            val customView:View = LayoutInflater.from(context).inflate(R.layout.gemini_clean, null,false)
            var textcon = customView.findViewById<TextView>(R.id.gemini_cleaninto)
            val dialogBuilder = AlertDialog.Builder(context).setView(customView).setCancelable(false).setNegativeButton("取消"){_,_->}
            dialogBuilder.setTitle("清除聊天记录")
            textcon.text = "    确认清除聊天记录吗？这也会清除AI的记忆。"
            dialogBuilder.setPositiveButton("确认") {_,_->
                messageViewModel.deleteAll()
                Toast.makeText(context,"清除成功", Toast.LENGTH_LONG).show()
                geminimodel = geminimodelfactory.createGeminiModel(repository.GeminiModel,repository.GeminiApi)
                geminichat = geminimodel.startChat(
                    history = listOf()
                )
            }
            val myDialog = dialogBuilder.create()
            myDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            myDialog.show()
            myDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}