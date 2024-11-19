package com.moe.moetranslator.me

object UrlUtils {
    private const val HTTP_SCHEME = "http://"
    private const val HTTPS_SCHEME = "https://"

    /**
     * 标准化URL
     * 1. 添加scheme (如果缺少)
     * 2. 移除多余的斜杠
     * 3. 处理基本的URL格式问题
     */
    fun normalizeUrl(url: String): String {
        var normalizedUrl = url.trim()

        // 如果URL为空，抛出异常
        if (normalizedUrl.isEmpty()) {
            throw IllegalArgumentException("URL cannot be empty")
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
    fun validateUrl(url: String): Boolean {
        return try {
            normalizeUrl(url)
            true
        } catch (e: Exception) {
            false
        }
    }
}