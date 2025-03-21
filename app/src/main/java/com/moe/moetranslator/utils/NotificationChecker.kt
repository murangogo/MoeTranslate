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