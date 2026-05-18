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

package com.moe.moetranslator.llamamanager

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.util.Locale

/**
 * 统一管理 llama.cpp GGUF 模型在磁盘上的位置。
 *
 * 路径：Context.filesDir/llamacppmodels/<fileName>
 * 选择内部存储而非 getExternalFilesDir，与用户的明确要求一致；私有目录，外部应用不可访问。
 */
object LlamaModelStorage {

    const val MODELS_DIR_NAME = "llamacppmodels"
    private const val BUFFER_SIZE = 8192

    fun modelsDir(context: Context): File {
        val dir = File(context.applicationContext.filesDir, MODELS_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun modelFile(context: Context, fileName: String): File =
        File(modelsDir(context), fileName)

    /**
     * 在不冲突的前提下，若 desired 已存在，则追加 "(2)", "(3)" 后缀直到可用。
     * 例如：foo.gguf -> foo (2).gguf
     */
    fun resolveCollisionFreeName(context: Context, desired: String): String {
        val dir = modelsDir(context)
        if (!File(dir, desired).exists()) return desired

        val dot = desired.lastIndexOf('.')
        val stem = if (dot >= 0) desired.substring(0, dot) else desired
        val ext = if (dot >= 0) desired.substring(dot) else ""
        var i = 2
        while (true) {
            val candidate = "$stem ($i)$ext"
            if (!File(dir, candidate).exists()) return candidate
            i++
        }
    }

    fun calculateMd5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buf = ByteArray(BUFFER_SIZE)
            var read: Int
            while (input.read(buf).also { read = it } != -1) {
                md.update(buf, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun formatSize(size: Long): String {
        val locale = Locale.getDefault()
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format(locale, "%.2f KB", size / 1024.0)
            size < 1024L * 1024 * 1024 -> String.format(locale, "%.2f MB", size / (1024.0 * 1024))
            else -> String.format(locale, "%.2f GB", size / (1024.0 * 1024 * 1024))
        }
    }
}
