package com.moe.moetranslator.madoka

class ModelInfoRepository(private val modelInfoDAO: ModelInfoDAO) {
    val allModels = modelInfoDAO.getAllModels()

    fun getExpressionsForModel(modelId: String) = modelInfoDAO.getExpressionsForModel(modelId)

    fun getMotionsForModel(modelId: String) = modelInfoDAO.getMotionsForModel(modelId)

    suspend fun insertModel(model: Live2DModel) {
        modelInfoDAO.insertModel(model)
    }

    suspend fun insertExpressions(expressions: List<Live2DExpression>) {
        modelInfoDAO.insertExpressions(expressions)
    }

    suspend fun insertMotions(motions: List<Live2DMotion>) {
        modelInfoDAO.insertMotions(motions)
    }

    suspend fun updateModelName(modelId: String, newName: String) {
        modelInfoDAO.updateModelName(modelId, newName)
    }

    suspend fun updateExpressionName(expressionId: Long, newName: String) {
        modelInfoDAO.updateExpressionName(expressionId, newName)
    }

    suspend fun updateMotionName(motionId: Long, newName: String) {
        modelInfoDAO.updateMotionName(motionId, newName)
    }
}