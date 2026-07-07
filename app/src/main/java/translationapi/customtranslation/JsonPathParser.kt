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

package translationapi.customtranslation

import org.json.JSONArray
import org.json.JSONObject

/**
 * 自定义翻译 API 的「JSON 响应路径」解析工具。
 *
 * 早期版本仅支持以 "." 分隔的对象属性访问（如 data.translation），
 * 现在同时支持「数组下标」访问，并可与对象属性自由组合。支持的写法示例：
 *   - result
 *   - data.translation
 *   - data.list[0].text
 *   - data.items[0][1].value
 *
 * 语法说明：
 *   - "."   分隔对象（JSONObject）的属性名；
 *   - "[n]" 访问数组（JSONArray）中第 n 个元素（下标从 0 开始）；
 *   - 二者可任意组合，如 a.b[0].c、a[0][1] 等。
 *
 * 注意：与旧版一致，属性名中不能包含 "."、"["、"]" 这三个用于分隔的字符。
 */
object JsonPathParser {

    // 路径中的单个访问单元：对象属性 或 数组下标
    private sealed class Token {
        data class Key(val name: String) : Token()   // 对象属性名
        data class Index(val index: Int) : Token()   // 数组下标
    }

    /**
     * 按路径从 JSON 对象中取出目标值，并转为字符串返回。
     *
     * @param root 已解析的根 JSON 对象
     * @param path 用户配置的响应路径，如 "data.list[0].text"
     * @return 目标节点的字符串表示
     * @throws IllegalArgumentException  路径语法非法（空路径、下标非法等）
     * @throws IllegalStateException     路径与实际 JSON 结构不匹配（类型不符或键不存在）
     * @throws IndexOutOfBoundsException 数组下标越界
     */
    fun parse(root: JSONObject, path: String): String {
        val tokens = tokenize(path)
        require(tokens.isNotEmpty()) { "JSON path is empty" }

        var current: Any = root
        val traversed = StringBuilder() // 记录已走过的路径，便于报错时定位
        // 当前已走过路径的可读表示（根节点显示为 <root>）
        fun here(): String = traversed.toString().ifEmpty { "<root>" }

        for (token in tokens) {
            current = when (token) {
                is Token.Key -> {
                    val obj = current as? JSONObject ?: throw IllegalStateException(
                        "Node at \"${here()}\" is not a JSON object, " +
                                "cannot read key \"${token.name}\""
                    )
                    if (!obj.has(token.name)) {
                        throw IllegalStateException(
                            "Key \"${token.name}\" not found at \"${here()}\""
                        )
                    }
                    traversed.append(if (traversed.isEmpty()) token.name else ".${token.name}")
                    obj.get(token.name)
                }

                is Token.Index -> {
                    val arr = current as? JSONArray ?: throw IllegalStateException(
                        "Node at \"${here()}\" is not a JSON array, " +
                                "cannot use index [${token.index}]"
                    )
                    if (token.index >= arr.length()) {
                        throw IndexOutOfBoundsException(
                            "Index [${token.index}] out of bounds at " +
                                    "\"${here()}\" (length ${arr.length()})"
                        )
                    }
                    traversed.append("[${token.index}]")
                    arr.get(token.index)
                }
            }
        }

        return current.toString()
    }

    /**
     * 将路径字符串解析为 Token 序列。
     *
     * 每个以 "." 分隔的段可以是：
     *   - 纯属性名：name
     *   - 属性名 + 一个或多个下标：name[0]、name[0][1]
     *   - 纯下标（紧跟在 "." 之后，较少见）：[0]
     */
    private fun tokenize(path: String): List<Token> {
        val tokens = mutableListOf<Token>()

        for (segment in path.split('.')) {
            val bracketStart = segment.indexOf('[')
            val key = if (bracketStart == -1) segment else segment.substring(0, bracketStart)

            // 段内既无属性名也无下标 —— 说明路径中出现了空段（如 a..b、.a、a. ）
            if (key.isEmpty() && bracketStart == -1) {
                throw IllegalArgumentException("Empty segment in path: \"$path\"")
            }

            if (key.isNotEmpty()) {
                if (key.contains(']')) {
                    throw IllegalArgumentException("Unexpected ']' in segment: \"$segment\"")
                }
                tokens.add(Token.Key(key))
            }

            // 解析属性名之后连续的 [index] 部分
            var i = if (bracketStart == -1) segment.length else bracketStart
            while (i < segment.length) {
                if (segment[i] != '[') {
                    throw IllegalArgumentException(
                        "Unexpected character '${segment[i]}' in segment: \"$segment\""
                    )
                }
                val close = segment.indexOf(']', i)
                if (close == -1) {
                    throw IllegalArgumentException("Missing ']' in segment: \"$segment\"")
                }
                val indexText = segment.substring(i + 1, close)
                if (indexText.isEmpty() || indexText.any { !it.isDigit() }) {
                    throw IllegalArgumentException(
                        "Invalid array index \"[$indexText]\" in segment: \"$segment\""
                    )
                }
                val index = indexText.toIntOrNull() ?: throw IllegalArgumentException(
                    "Array index \"[$indexText]\" is too large in segment: \"$segment\""
                )
                tokens.add(Token.Index(index))
                i = close + 1
            }
        }

        return tokens
    }
}
