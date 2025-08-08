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

object Constants {
    // 翻译相关常量
    enum class TranslateMode(val id: Int) {
        TEXT(0),
        PIC(1)
    }

    // API 相关常量
    enum class TextApi(val id: Int) {
        AI(0),
        BING(1),
        NIUTRANS(2),
        VOLC(3),
        AZURE(4),
        BAIDU(5),
        TENCENT(6),
        CUSTOM_TEXT(7),
        OPENAI(8)
    }

    enum class TextAI(val id: Int) {
        MLKIT(0),
        NLLB(1)
    }

    enum class PicApi(val id: Int) {
        BAIDU(0),
        TENCENT(1),
        CUSTOM_PIC(2)
    }
}