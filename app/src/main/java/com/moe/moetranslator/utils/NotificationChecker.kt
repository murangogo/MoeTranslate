package com.moe.moetranslator.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class NotificationChecker(private val context: Context) {
    private val client = OkHttpClient()

    suspend fun checkNotification(): NotificationResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.moetranslate.top/notice.json")
                .build()

            var jsonString = ""

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                jsonString = response.body?.string() ?: throw IOException("Empty response body")
            }

            val jsonObject = JSONObject(jsonString)
            val latestNotificationCode = jsonObject.getLong("NoticeCode")
            val latestNotificationName = jsonObject.getString("NoticeName")
            val latestNotificationContent = jsonObject.getString("NoticeContent")

            NotificationResult.NotificationAvailable(latestNotificationCode, latestNotificationName, latestNotificationContent)
        } catch (e: Exception) {
            NotificationResult.Error
        }
    }
}

sealed class NotificationResult {
    data class NotificationAvailable(val notificationCode: Long, val notificationName: String, val notificationContent: String) : NotificationResult()
    object Error : NotificationResult()
}