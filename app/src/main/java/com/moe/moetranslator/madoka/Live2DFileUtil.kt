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

    suspend fun checkModelJson(folderUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // 确保有持久的权限
            ensurePermission(folderUri)

            // 获取文件夹 DocumentFile
            val sourceDir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext false

            // 检查文件夹中的文件
            sourceDir.listFiles().any { file ->
                file.name?.endsWith(".model3.json") == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun copyModelFolder(folderUri: Uri, modelId: String) = withContext(Dispatchers.IO) {

        // 确保有持久的权限
        ensurePermission(folderUri)

        val sourceDir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext false
        val destinationDir = File(getModelBaseDir(), modelId)

        // 创建目标文件夹
        if (!destinationDir.exists()) destinationDir.mkdirs()

        // 复制文件夹内容
        copyDocumentFolder(sourceDir, destinationDir)
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

        // 确定目标文件名
        val destFileName = if (fileName.endsWith(".model3.json")) {
            "model.model3.json"  // 统一命名为 model.model3.json
        } else {
            fileName
        }

        val destFile = File(destinationDir, destFileName)

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

    suspend fun deleteModelFolder(modelId: String) = withContext(Dispatchers.IO) {
        val modelDir = File(getModelBaseDir(), modelId)
        if (modelDir.exists()) {
            modelDir.deleteRecursively()
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
            ?: return emptyList()

        // 获取Expressions数组
        val expressions = fileReferences.optJSONArray("Expressions")
            ?: return emptyList()

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
            ?: return emptyList()

        // 获取Motions对象
        val motions = fileReferences.optJSONObject("Motions")
            ?: return emptyList()

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