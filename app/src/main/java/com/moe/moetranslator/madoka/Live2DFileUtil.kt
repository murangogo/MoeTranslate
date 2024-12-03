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
        // 获取model3.json文件
        val modelConfigFile = File(File(getModelBaseDir(), modelId), "model.model3.json")
        if (!modelConfigFile.exists()) {
            throw IllegalStateException("Model config file not found for model: $modelId")
        }

        // 读取并解析JSON
        val jsonString = try {
            modelConfigFile.readText()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read model config file for model: $modelId", e)
        }

        val jsonObject = try {
            org.json.JSONObject(jsonString)
        } catch (e: Exception) {
            throw IllegalStateException("Invalid JSON format in model config file: $modelId", e)
        }

        // 获取FileReferences对象
        val fileReferences = jsonObject.optJSONObject("FileReferences")
            ?: throw IllegalStateException("Missing FileReferences in model config: $modelId")

        // 获取Expressions数组
        val expressions = fileReferences.optJSONArray("Expressions")
            ?: throw IllegalStateException("Missing Expressions in FileReferences: $modelId")

        // 转换为Live2DExpression列表
        return (0 until expressions.length()).map { index ->
            val expression = expressions.getJSONObject(index)
            val name = expression.optString("Name")
                ?: throw IllegalStateException("Missing Name in Expression at index $index: $modelId")

            Live2DExpression(
                modelId = modelId,
                fileName = name,
                displayName = name
            )
        }
    }

    fun scanMotions(modelId: String): List<Live2DMotion> {
        // 获取model3.json文件
        val modelConfigFile = File(File(getModelBaseDir(), modelId), "model.model3.json")
        if (!modelConfigFile.exists()) {
            throw IllegalStateException("Model config file not found for model: $modelId")
        }

        // 读取并解析JSON
        val jsonString = try {
            modelConfigFile.readText()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read model config file for model: $modelId", e)
        }

        val jsonObject = try {
            org.json.JSONObject(jsonString)
        } catch (e: Exception) {
            throw IllegalStateException("Invalid JSON format in model config file: $modelId", e)
        }

        // 获取FileReferences对象
        val fileReferences = jsonObject.optJSONObject("FileReferences")
            ?: throw IllegalStateException("Missing FileReferences in model config: $modelId")

        // 获取Motions对象
        val motions = fileReferences.optJSONObject("Motions")
            ?: throw IllegalStateException("Missing Motions in FileReferences: $modelId")

        // 获取所有motion组并展平为单个列表
        return motions.keys().asSequence().flatMap { groupName ->
            val motionGroup = motions.getJSONArray(groupName)
            (0 until motionGroup.length()).map { index ->
                val motion = motionGroup.getJSONObject(index)
                val file = motion.optString("File")
                    ?: throw IllegalStateException("Missing File in Motion at index $index of group $groupName: $modelId")

                Live2DMotion(
                    modelId = modelId,
                    fileName = file,
                    displayName = file
                )
            }
        }.toList()
    }
}