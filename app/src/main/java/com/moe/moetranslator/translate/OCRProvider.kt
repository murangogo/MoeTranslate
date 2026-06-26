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

package com.moe.moetranslator.translate

import android.graphics.Bitmap

/**
 * OCR 引擎统一接口。
 *
 * 与翻译接口 [TranslationTextAPI] 同理：上层（[FloatingBallService]）只依赖本接口，
 * 新增 OCR 引擎时只需实现本接口并在服务里登记构造方式，无需改动调用链。
 * 现有实现：[MLKitOCR]（ML Kit 端侧 OCR）、[com.moe.moetranslator.ppocr.PaddleOCR]（PP-OCRv6 离线 OCR）。
 */
interface OCRProvider {

    /**
     * 识别整张图中的文字并按合并模式拼成一段文本。
     *
     * @param bitmap         待识别图（实现方不得回收它，生命周期由调用方管理）
     * @param sourceLanguage 源语言代码（如 "ja"/"zh"/"en"），用于选择识别语言与决定拼接分隔符
     * @param mergeMode      结果合并模式：0=不合并、1=分段合并、2=直接合并（见个性化设置）
     * @return 合并后的识别文本；无文字时返回空串
     */
    suspend fun recognize(bitmap: Bitmap, sourceLanguage: String, mergeMode: Int): String

    /** 释放引擎占用的资源（模型、识别器等）。服务停止时调用。 */
    fun release()
}
