package com.moe.moetranslator.utils

import android.content.Context
import java.io.File

object AppPathManager {
    private var baseExternalPath: String? = null

    fun init(context: Context) {
        if (baseExternalPath == null) {
            baseExternalPath = context.getExternalFilesDir(null)?.absolutePath
        }
    }

    fun getBaseExternalPath(): String {
        return baseExternalPath ?: throw IllegalStateException("AppPathManager is not initialized")
    }

    fun getLive2DPath(): String {
        return File(getBaseExternalPath(), "live2d").absolutePath + File.separator
    }

    fun getModelPath(modelId: String): String {
        return File(File(getBaseExternalPath(), "live2d"), modelId).absolutePath + File.separator
    }

    fun getModelJsonName(modelId: String): String {
        return "$modelId.model3.json"
    }
}