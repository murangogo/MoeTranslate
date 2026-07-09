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

    // 默认提示词
    val defaultSystemPrompt = "你是一名专业翻译。你的任务是准确、自然地翻译给定的文本。\n具体规则如下： \n1、根据用户的要求，将文本翻译成指定的目标语言；\n2、保持原意和语气；\n3、尽可能保持格式和结构；\n4、直接返回翻译后的文本，不要有任何解释或附加内容；\n5、如果文本已经是目标语言，请按原样返回。"
    val defaultUserPrompt = "请将下面的文本从usefromlang翻译为usetolang：\n\nusesourcetext"

    // OpenAI 兼容接口默认温度；设置页留空则不发送 temperature（兼容只接受默认温度的推理模型）
    const val defaultOpenAITemperature = 0.2f

    // 「使用历史翻译记录」默认追加的历史条数（仅 OCR 模式下的 LlamaCpp / 聚合AI 生效）
    const val defaultTranslationHistoryCount = 5

    // 截图保存到缓存目录时的 JPEG 质量（1–100），默认 100。仅影响缓存占用，不影响 OCR / 图片翻译（二者用保存前的 bitmap）
    const val defaultScreenshotQuality = 100

    // llama.cpp 本地推理的每模型默认参数（新建/导入模型时的初值，也是 active 镜像缺失时的回退值）
    // 思考默认关闭：翻译场景追求速度，Qwen3 等模型默认开思考会显著拖慢
    const val defaultLlamaEnableThinking = false
    const val defaultLlamaTemperature = 0.2f
    const val defaultLlamaMaxTokens = 512

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
        OPENAI(8),
        DEEPL(9)
    }

    enum class TextAI(val id: Int) {
        MLKIT(0),
        NLLB(1),
        LLAMA(2)
    }

    enum class PicApi(val id: Int) {
        BAIDU(0),
        TENCENT(1),
        CUSTOM_PIC(2)
    }

    // OCR 引擎相关常量（仅文本翻译模式下使用，决定截图取字用哪个 OCR）
    enum class OcrEngine(val id: Int) {
        PADDLEOCR(0),       // PaddleOCR PP-OCRv6_small：中/英/日/拉丁，离线，不支持韩语
        MLKIT(1)    // ML Kit：中/英/日/韩，需 Google 端侧模型
    }
}