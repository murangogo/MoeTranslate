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