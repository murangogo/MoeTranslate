package com.moe.moetranslator.translate

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageFileManager {
    private const val MAX_IMAGES = 200
    private const val FOLDER_NAME = "screenshots"

    // 保存图片到缓存
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): String? {
        try {
            // 获取外部缓存目录 (Android/data/包名/cache)
            val cacheDir = context.externalCacheDir
            val screenshotsDir = File(cacheDir, FOLDER_NAME).apply {
                if (!exists()) mkdirs()
            }

            // 检查并清理旧图片
            cleanOldImages(screenshotsDir)

            // 生成文件名（使用时间戳）
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val imageFile = File(screenshotsDir, "Screenshot_$timestamp.jpg")

            // 保存图片
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            return imageFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // 清理旧图片
    private fun cleanOldImages(directory: File) {
        try {
            val files = directory.listFiles()?.toList() ?: return

            // 如果图片数量超过限制
            if (files.size >= MAX_IMAGES) {
                // 按修改时间排序
                val sortedFiles = files.sortedBy { it.lastModified() }

                // 删除最旧的文件
                val filesToDelete = sortedFiles.take(files.size - MAX_IMAGES + 1)
                filesToDelete.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 获取缓存中的所有图片
    fun getAllImages(context: Context): List<File> {
        val cacheDir = context.externalCacheDir
        val screenshotsDir = File(cacheDir, FOLDER_NAME)

        return if (screenshotsDir.exists()) {
            screenshotsDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } else {
            emptyList()
        }
    }
}