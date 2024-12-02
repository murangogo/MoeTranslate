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
            if (expressions.isNotEmpty()) {
                repository.insertExpressions(expressions)
            }

            // 扫描并保存动作信息
            val motions = fileUtil.scanMotions(modelId)
            if (motions.isNotEmpty()) {
                repository.insertMotions(motions)
            }

            true
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