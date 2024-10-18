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

class UpdateChecker(private val context: Context) {
    private val client = OkHttpClient()

    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.moetranslate.top/version.json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val jsonString = response.body?.string() ?: throw IOException("Empty response body")
            val jsonObject = JSONObject(jsonString)
            val latestVersionCode = jsonObject.getLong("versionCode")
            val latestVersionName = jsonObject.getString("versionName")
            val latestVersionDescription = jsonObject.getString("versionContent")

            val currentVersion = getCurrentVersion()

            if (latestVersionCode > currentVersion) {
                UpdateResult.UpdateAvailable(latestVersionCode, latestVersionName, latestVersionDescription)
            } else {
                UpdateResult.NoUpdate
            }
        } catch (e: Exception) {
            UpdateResult.Error
        }
    }

    private fun getCurrentVersion(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            Log.e("UpdateChecker", "Error getting current version: ${e.message}")
            0
        }
    }
}

sealed class UpdateResult {
    object NoUpdate : UpdateResult()
    data class UpdateAvailable(val versionCode: Long, val versionName: String, val versionDescription: String) : UpdateResult()
    object Error : UpdateResult()
}