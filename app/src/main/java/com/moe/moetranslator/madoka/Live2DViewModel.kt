package com.moe.moetranslator.madoka

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class Live2DViewModel(
    private val repository: ModelInfoRepository,
    private val fileUtil: Live2DFileUtil
) : ViewModel() {

    val allModels = repository.allModels

    private val _currentModelId = MutableStateFlow<String?>(null)
    val currentModelId: StateFlow<String?> = _currentModelId.asStateFlow()

    val currentExpressions = currentModelId.flatMapLatest { modelId ->
        modelId?.let { repository.getExpressionsForModel(it) } ?: flowOf(emptyList())
    }

    val currentMotions = currentModelId.flatMapLatest { modelId ->
        modelId?.let { repository.getMotionsForModel(it) } ?: flowOf(emptyList())
    }

    fun setCurrentModel(modelId: String) {
        _currentModelId.value = modelId
    }

    suspend fun importModel(folderUri: Uri, displayName: String): Boolean {
        val modelId = fileUtil.generateNextModelId()

        return try {
            // 复制文件夹
            Log.d("Live2DViewModel", "Importing model from $folderUri \n id:$modelId")
            val success = fileUtil.copyModelFolder(folderUri, modelId)
            Log.d("Live2DViewModel", "Model import result: $success")
            if (!success) return false

            // 保存模型信息
            repository.insertModel(Live2DModel(modelId, displayName))

            // 扫描并保存表情信息
            val expressions = fileUtil.scanExpressions(modelId)

            Log.d("Live2DViewModel", "Found ${expressions.size} expressions for model $modelId")
            expressions.forEach { expression ->
                Log.d("Live2DViewModel", "Expression: id=${expression.id}, " +
                        "modelId=${expression.modelId}, " +
                        "fileName=${expression.fileName}, " +
                        "displayName=${expression.displayName}")
            }

            if (expressions.isNotEmpty()) {
                repository.insertExpressions(expressions)
            }

            // 扫描并保存动作信息
            val motions = fileUtil.scanMotions(modelId)

            Log.d("Live2DViewModel", "Found ${motions.size} motions for model $modelId")
            motions.forEach { motion ->
                Log.d("Live2DViewModel", "Motion: id=${motion.id}, " +
                        "modelId=${motion.modelId}, " +
                        "fileName=${motion.fileName}, " +
                        "displayName=${motion.displayName}")
            }

            if (motions.isNotEmpty()) {
                repository.insertMotions(motions)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteModel(modelId: String): Boolean {
        return try {
            // 首先删除文件夹
            val fileDeleted = fileUtil.deleteModelFolder(modelId)
            if (fileDeleted) {
                // 然后删除数据库记录
                repository.deleteModel(modelId)
                // 如果当前选中的就是被删除的模型，清除当前选中状态
                if (_currentModelId.value == modelId) {
                    _currentModelId.value = null
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateModelName(modelId: String, newName: String) {
        repository.updateModelName(modelId, newName)
    }

    suspend fun updateExpressionName(expressionId: Long, newName: String) {
        repository.updateExpressionName(expressionId, newName)
    }

    suspend fun updateMotionName(motionId: Long, newName: String) {
        repository.updateMotionName(motionId, newName)
    }
}