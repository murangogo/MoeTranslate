package com.moe.moetranslator.launch

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AssetsInitializer(private val context: Context) {

    fun initializeLive2DResources() {
        try {
            // 获取目标文件夹
            val baseDir = File(context.getExternalFilesDir(null), "live2d").apply {
                if (!exists()) mkdirs()
            }

            // 复制整个live2d文件夹
            copyAssetFolder("live2d", baseDir.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    private fun copyAssetFolder(sourcePath: String, targetPath: String) {
        val assetManager = context.assets

        try {
            // 获取源文件夹中的所有内容
            val files = assetManager.list(sourcePath)

            if (files.isNullOrEmpty()) {
                // 如果是文件，直接复制
                copyAssetFile(sourcePath, targetPath)
            } else {
                // 如果是文件夹，创建对应的目标文件夹
                File(targetPath).mkdirs()

                // 递归复制每个文件/文件夹
                files.forEach { filename ->
                    val sourceSubPath = if (sourcePath == "") filename else "$sourcePath/$filename"
                    val targetSubPath = "$targetPath/$filename"
                    copyAssetFolder(sourceSubPath, targetSubPath)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    private fun copyAssetFile(sourcePath: String, targetPath: String) {
        try {
            // 使用缓冲流来提升性能
            context.assets.open(sourcePath).use { input ->
                FileOutputStream(targetPath).use { output ->
                    input.copyTo(output, bufferSize = 8 * 1024) // 8KB buffer
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
}