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

class ModelInfoRepository(private val modelInfoDAO: ModelInfoDAO) {
    val allModels = modelInfoDAO.getAllModels()

    fun getExpressionsForModel(modelId: String) = modelInfoDAO.getExpressionsForModel(modelId)

    fun getMotionsForModel(modelId: String) = modelInfoDAO.getMotionsForModel(modelId)

    suspend fun deleteModel(modelId: String) {
        modelInfoDAO.deleteModel(modelId)
    }

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