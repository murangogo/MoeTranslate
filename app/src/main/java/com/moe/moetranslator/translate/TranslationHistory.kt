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

/**
 * 进程级「翻译历史」滚动缓冲。
 *
 * 仅对基于提示词的引擎（OCR 模式下的 LlamaCpp 引擎、聚合 AI 翻译）有意义：每次成功翻译后
 * 记录一条 (原文, 译文)，构建下一次提示词时按需取最近 N 条作为上下文，帮助模型在连续 OCR
 * 场景里保持术语 / 人名 / 语气一致。
 *
 * 设计：
 *   - 纯内存保存，进程结束即清空——与需求「刚启动时记录可能不足要求条数」的语义一致：
 *     刚启动缓冲为空，随翻译逐条累积，满 N 条后滚动丢弃最旧的。
 *   - 容量上限 [MAX_CAPACITY]，避免长时间 OCR 会话内无限增长；用户设置的条数超过上限时
 *     至多取上限条。
 *   - 全部方法 @Synchronized：记录发生在翻译 worker 线程，读取发生在构建提示词时，需互斥。
 */
object TranslationHistory {

    // 缓冲容量上限
    private const val MAX_CAPACITY = 100

    // 旧→新顺序保存，addLast 追加最新，超限时 removeFirst 丢弃最旧
    private val records = ArrayDeque<Pair<String, String>>()

    /** 记录一条成功的翻译；原文或译文为空则忽略。满 [MAX_CAPACITY] 时滚动丢弃最旧的一条。 */
    @Synchronized
    fun record(source: String, translated: String) {
        if (source.isBlank() || translated.isBlank()) return
        records.add(source to translated)
        while (records.size > MAX_CAPACITY) {
            records.removeAt(0)
        }
    }

    /** 取最近 [count] 条（不足则取全部），按时间顺序（旧→新）返回；count<=0 或无记录时返回空表。 */
    @Synchronized
    fun latest(count: Int): List<Pair<String, String>> {
        if (count <= 0 || records.isEmpty()) return emptyList()
        val n = minOf(count, records.size)
        // subList 视图可能随后续修改失效，toList() 拷贝一份返回，调用方可安全持有
        return records.toList().subList(records.size - n, records.size).toList()
    }

    /** 清空缓冲 */
    @Synchronized
    fun clear() {
        records.clear()
    }

    /**
     * 构建追加到系统提示词后的历史参考文本；无历史时返回空串。
     *
     * 格式：
     * ```
     * {prefix}
     *
     * 1.
     * {原文}
     * {译文}
     *
     * 2.
     * {原文}
     * {译文}
     * ```
     */
    fun buildHistoryBlock(prefix: String, count: Int): String {
        val items = latest(count)
        if (items.isEmpty()) return ""
        return buildString {
            append(prefix)
            items.forEachIndexed { index, (source, translated) ->
                append("\n\n")
                append(index + 1).append(".\n")
                append(source).append('\n')
                append(translated)
            }
        }
    }

    /**
     * 把历史参考文本追加到 [systemPrompt] 之后（中间空一行）。无历史时原样返回 [systemPrompt]。
     */
    fun appendHistory(systemPrompt: String, prefix: String, count: Int): String {
        val block = buildHistoryBlock(prefix, count)
        return if (block.isEmpty()) systemPrompt else "$systemPrompt\n\n$block"
    }
}
