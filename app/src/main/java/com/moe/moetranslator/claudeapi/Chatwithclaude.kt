package com.moe.moetranslator.claudeapi

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentChatwithclaudeBinding
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.utils.UtilTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class Chatwithclaude : Fragment() {

    private lateinit var binding: FragmentChatwithclaudeBinding
    private lateinit var repository: MySharedPreferenceData
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MessageAdapter
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var messageViewmodelfactory: MessageViewModelFactory
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var chatfunction:ChatFunction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MySharedPreferenceData(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatwithclaudeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatfunction = ChatFunction()
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
                if((repository.ClaudeT=="")||(repository.ClaudeD=="")){
                    lifecycleScope.launch(Dispatchers.IO) {
                        val response = "您还未配置Claude的Token和Member ID，请点击左上角齿轮按钮进行配置，也可在左上角齿轮页面中查看教程。"
                        val botMessage = ChatMessage(content = response, timestamp = System.currentTimeMillis(), sender = 1)
                        messageViewModel.insert(botMessage)
                    }
                }else{
                    Toast.makeText(context,"请等待AI回复，时间长短视回复长短而定", Toast.LENGTH_SHORT).show()
                    binding.buttonSend.text = "请等待"
                    binding.buttonSend.isClickable = false
                    // launch a coroutine to send the message and receive the response
                    lifecycleScope.launch(Dispatchers.IO) {
                        val response = chatfunction.talk(repository.ClaudeT!!,repository.ClaudeD!!,messageText)
                        val botMessage = ChatMessage(content = response, timestamp = System.currentTimeMillis(), sender = 1)
                        messageViewModel.insert(botMessage)
                        MainScope().launch {
                            binding.buttonSend.text = "发送"
                            binding.buttonSend.isClickable = true
                        }
                    }
                }
            }
        }
        binding.settingClaude.setOnClickListener {
            val customView:View = LayoutInflater.from(context).inflate(R.layout.claude_setting, null,false)
            if(repository.ClaudeT!=""){
                customView.findViewById<EditText>(R.id.slack_token).hint = "（已保存）"
            }
            if(repository.ClaudeD!=""){
                customView.findViewById<EditText>(R.id.slack_memberid).hint = "（已保存）"
            }
            val mytoken = customView.findViewById<EditText>(R.id.slack_token)
            val mymember = customView.findViewById<EditText>(R.id.slack_memberid)
            val myintr = customView.findViewById<TextView>(R.id.slack_intr)
            val font = Typeface.createFromAsset(context!!.assets, "translatefonts.ttf")
            myintr.typeface = font
            val dialogBuilder = AlertDialog.Builder(context).setView(customView).setCancelable(false).setNegativeButton("取消"){_,_->}.setPositiveButton("保存"){_,_->
                repository.saveClaudeT(mytoken.text.toString())
                repository.saveClaudeD(mymember.text.toString())
                Toast.makeText(context,"保存成功", Toast.LENGTH_LONG).show()
            }.setNeutralButton("查看教程"){_,_->
                val url = "https://blog.csdn.net/qq_45487246/article/details/131750491"
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
            val customView:View = LayoutInflater.from(context).inflate(R.layout.claude_clean, null,false)
            var textcon = customView.findViewById<TextView>(R.id.slack_cleaninto)
            val dialogBuilder = AlertDialog.Builder(context).setView(customView).setCancelable(false).setNegativeButton("取消"){_,_->}
            dialogBuilder.setTitle("清除聊天记录")
            textcon.text = "确认清除聊天记录吗？请注意，这只会清除本机的聊天记录，但不会清除AI的聊天记忆；如果您想开始一个新的对话，可以在聊天框中输入：\n“/reset”。"
            val font = Typeface.createFromAsset(context!!.assets, "translatefonts.ttf")
            textcon.typeface = font
            dialogBuilder.setPositiveButton("确认") {_,_->
                messageViewModel.deleteAll()
                Toast.makeText(context,"清除成功", Toast.LENGTH_LONG).show()
            }
            val myDialog = dialogBuilder.create()
            myDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            myDialog.show()
            myDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}