package com.moe.moetranslator.claudeapi

import android.util.Log
import com.slack.api.Slack
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse
import com.slack.api.methods.response.conversations.ConversationsOpenResponse
import com.slack.api.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

class ChatFunction {
    suspend fun talk(tokenname: String, botid: String, prompt: String) : String {
        var SLACK_TOKEN = tokenname
        var BOT_USER_ID = botid
        Log.d("CLAUDE",SLACK_TOKEN+"\n"+BOT_USER_ID)
        val slack = Slack.getInstance()

        try {
            var botMessage = ""
            // Open conversation with the bot
            val openResponse: ConversationsOpenResponse = slack.methods(SLACK_TOKEN).conversationsOpen { it.users(listOf(BOT_USER_ID)) }
            val dmChannelId = openResponse.channel.id

            // Send a message
            val postResponse: ChatPostMessageResponse = slack.methods(SLACK_TOKEN)
                .chatPostMessage { it.channel(dmChannelId).text(prompt) }
            val lastMessageTimestamp = postResponse.ts

            // Wait for a new message
            withTimeoutOrNull(120000) {
                while (true) {
                    val historyResponse: ConversationsHistoryResponse = slack.methods(SLACK_TOKEN).conversationsHistory { it.channel(dmChannelId).oldest(lastMessageTimestamp) }
                    val messages: List<Message> = historyResponse.messages
                    val botMessages: List<Message> = messages.filter { it.user == BOT_USER_ID }
                    if (botMessages.isNotEmpty() && !botMessages.last().text.endsWith("Typing…_")) {
                        botMessage =  botMessages.last().text.trim()
                        break
                    }
                    delay(2000) // Use delay instead of Thread.sleep in coroutines
                }
            } ?: return "No response from bot within timeout."
            return botMessage
        } catch (e: Exception) {
            return when (e) {
                is NullPointerException -> {
                    "设置的Token或Member ID有误，请检查。"
                }

                else -> {
                    e.toString()
                }
            }
        }
    }
}