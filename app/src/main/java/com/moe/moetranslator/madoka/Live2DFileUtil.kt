package com.moe.moetranslator.madoka

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class Live2DFileUtil(private val context: Context) {
    companion object {
        private const val LIVE2D_DIR = "live2d"
    }

    private fun getModelBaseDir(): File {
        return File(context.getExternalFilesDir(null), LIVE2D_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    fun generateNextModelId(): String {
        val existingDirs = getModelBaseDir().listFiles() ?: emptyArray()
        val existingNumbers = existingDirs
            .map { it.name }
            .filter { it.startsWith("model_") }
            .mapNotNull { it.removePrefix("model_").toIntOrNull() }
            .toSet()

        var counter = 1
        while (existingNumbers.contains(counter)) {
            counter++
        }
        return "model_$counter"
    }

    suspend fun copyModelFolder(folderUri: Uri, modelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 确保有持久的权限
            ensurePermission(folderUri)

            val sourceDir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext false
            val destinationDir = File(getModelBaseDir(), modelId)

            // 创建目标文件夹
            if (!destinationDir.exists()) destinationDir.mkdirs()

            // 确保exp和mtn文件夹存在
            File(destinationDir, "exp").mkdirs()
            File(destinationDir, "mtn").mkdirs()

            // 复制文件夹内容
            copyDocumentFolder(sourceDir, destinationDir)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun ensurePermission(uri: Uri) {
        val contentResolver = context.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION

        // 检查是否已经有权限
        if (!hasPermission(uri)) {
            // 请求持久权限
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        }
    }

    private fun hasPermission(uri: Uri): Boolean {
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }
    }

    private suspend fun copyDocumentFolder(sourceDir: DocumentFile, destinationDir: File) {
        sourceDir.listFiles().forEach { sourceFile ->
            if (sourceFile.isDirectory) {
                // 如果是文件夹，创建对应的目标文件夹并递归复制
                val newDir = File(destinationDir, sourceFile.name ?: return@forEach)
                newDir.mkdirs()
                copyDocumentFolder(sourceFile, newDir)
            } else {
                // 如果是文件，直接复制
                copyDocumentFile(sourceFile, destinationDir)
            }
        }
    }

    private suspend fun copyDocumentFile(sourceFile: DocumentFile, destinationDir: File) {
        val fileName = sourceFile.name ?: return
        val destFile = File(destinationDir, fileName)

        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(sourceFile.uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scanExpressions(modelId: String): List<Live2DExpression> {
        val expDir = File(File(getModelBaseDir(), modelId), "exp")
        return expDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map {
                Live2DExpression(
                    modelId = modelId,
                    fileName = it.name,
                    displayName = it.nameWithoutExtension
                )
            } ?: emptyList()
    }

    fun scanMotions(modelId: String): List<Live2DMotion> {
        val mtnDir = File(File(getModelBaseDir(), modelId), "mtn")
        return mtnDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map {
                Live2DMotion(
                    modelId = modelId,
                    fileName = it.name,
                    displayName = it.nameWithoutExtension
                )
            } ?: emptyList()
    }
}