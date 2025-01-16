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

package com.moe.moetranslator.me

import android.content.Context
import com.moe.moetranslator.R

object UrlUtils {
    private const val HTTP_SCHEME = "http://"
    private const val HTTPS_SCHEME = "https://"

    /**
     * 标准化URL
     * 1. 添加scheme (如果缺少)
     * 2. 移除多余的斜杠
     * 3. 处理基本的URL格式问题
     */
    fun normalizeUrl(ctx: Context, url: String): String {
        var normalizedUrl = url.trim()

        // 如果URL为空，抛出异常
        if (normalizedUrl.isBlank()) {
            throw IllegalArgumentException(ctx.getString(R.string.url_blank))
        }

        // 添加scheme如果缺少
        if (!normalizedUrl.startsWith(HTTP_SCHEME) && !normalizedUrl.startsWith(HTTPS_SCHEME)) {
            normalizedUrl = HTTPS_SCHEME + normalizedUrl
        }

        // 移除结尾的斜杠
        while (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length - 1)
        }

        // 验证URL格式
        try {
            val uri = java.net.URI.create(normalizedUrl)
            if (uri.host == null) {
                throw IllegalArgumentException("Invalid URL: missing host")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL format: ${e.message}")
        }

        return normalizedUrl
    }

    /**
     * 验证URL是否有效
     */
    fun validateUrl(ctx: Context, url: String): Boolean {
        return try {
            normalizeUrl(ctx, url)
            true
        } catch (e: Exception) {
            false
        }
    }
}