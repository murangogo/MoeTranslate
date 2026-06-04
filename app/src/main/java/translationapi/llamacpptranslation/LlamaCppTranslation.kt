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

package translationapi.llamacpptranslation

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.moe.moetranslator.R
import com.moe.moetranslator.llama.LlamaAndroid
import com.moe.moetranslator.translate.CustomLocale
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI
import java.io.File

/**
 * 基于 llama.cpp 的本地 GGUF 模型推理翻译。
 *
 * 设计：
 *   - 单例 LlamaAndroid 持有 .so + 模型句柄。本类只是 TranslationTextAPI 适配层。
 *   - 构造时异步加载模型；加载完成 / 失败均通过 Toast 反馈。
 *   - 推理走独立 Thread（complete() 是阻塞 native 调用）。
 *   - cancelTranslation() 通过 LlamaAndroid.stop() 设置 native 端原子标志，让正在跑的
 *     complete() 在下一个 token 边界处返回。
 *   - release() 卸载模型，归还 ~1.5GB RAM；用户切换到其它翻译方式时立即生效。
 *
 * Prompt 模板（MVP）：硬编码 Qwen2.5 chat template；systemPrompt / userPrompt 的内容
 * 走 SharedPreferences（与 OpenAITranslation 一致），用户可配置。后续若要支持
 * Llama-3.2 / Gemma-2 等其它模型族，再加 PromptStyle 枚举切换。
 */
class LlamaCppTranslation(
    context: Context,
    modelFileName: String,
    private val systemPrompt: String,
    private val userPrompt: String,
    private val nCtx: Int = 2048,
    private val nThreads: Int = 4,
    private val maxTokens: Int = 512,
    private val temperature: Float = 0.2f,
    private val topP: Float = 0.9f,
    private val repeatPenalty: Float = 1.1f,
) : TranslationTextAPI {

    companion object {
        private const val TAG = "LlamaCppTranslation"
        // 与 LlamaModelStorage.MODELS_DIR_NAME 同步，但这里不强引用 manager 包以保持解耦
        private const val MODELS_SUBDIR = "llamacppmodels"
    }

    // 获取应用上下文，便于读取模型文件
    private val ctx = context.applicationContext
    // 读取模型文件（应用专属外部目录 getExternalFilesDir(null)/llamacppmodels，与 LlamaModelManagerFragment
    // 落盘位置一致；若外部存储不可用则回退到 filesDir，与 LlamaModelStorage 的回退规则一致）
    private val modelFile: File = run {
        val base = ctx.getExternalFilesDir(null) ?: ctx.filesDir
        File(File(base, MODELS_SUBDIR), modelFileName)
    }

    // 使用@Volatile保证一致性
    @Volatile private var ready = false
    // 记录当前任务线程
    private var currentTask: Thread? = null

    init {
        if (modelFileName.isBlank()) {
            // 用户开启了 LLAMA 但还没在管理页选模型；给个明确提示，别让用户面对模糊的 "初始化失败"
            showToast(R.string.llama_no_active_model)
        } else {
            // 创建此类时，即开始初始化
            showToast(R.string.initialization_start)
            // 新建线程进行初始化，防止阻塞UI
            Thread {
                try {
                    // 检查模型文件是否存在
                    if (!modelFile.exists()) {
                        throw IllegalStateException("Model file not found: ${modelFile.absolutePath}")
                    }
                    // backendInit 内部幂等，重复调用安全；保证首次使用时一定初始化过
                    LlamaAndroid.backendInit()
                    Log.i(TAG, LlamaAndroid.systemInfo())
                    LlamaAndroid.load(
                        modelPath = modelFile.absolutePath,
                        nCtx = nCtx,
                        nBatch = 512,
                        nThreads = nThreads,
                        nGpuLayers = 0,
                    )
                    ready = true
                    Log.i(TAG, "Model loaded: ${modelFile.absolutePath}")
                    showToast(R.string.initialization_complete)
                } catch (e: Exception) {
                    Log.e(TAG, "Model load failed", e)
                    showToast(R.string.initialization_failed)
                }
            }.apply { name = "Llama-Loader"; start() }
        }
    }

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        // 没有初始化，不允许开始翻译
        if (!ready) {
            showToast(R.string.initialization_not_complete)
            return
        }

        // 同一时刻只允许一个推理在跑：先停掉旧的
        cancelTranslation()

        currentTask = Thread {
            try {
                // 构建提示词
                val prompt = buildPrompt(text, sourceLanguage, targetLanguage)
                Log.d(TAG, "Prompt:\n$prompt")
                // 获取模型回答
                val raw = LlamaAndroid.complete(
                    prompt = prompt,
                    maxTokens = maxTokens,
                    temperature = temperature,
                    topP = topP,
                    repeatPenalty = repeatPenalty,
                )
                val result = raw.trim()
                Log.d(TAG, "Result: $result")
                if (result.isEmpty()) {
                    callback(TranslationResult.Error(Exception("Empty completion (possibly cancelled or prompt too long)")))
                } else {
                    callback(TranslationResult.Success(result))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Completion error", e)
                callback(TranslationResult.Error(e))
            }
        }.apply { name = "Llama-Infer"; start() }
    }

    override fun cancelTranslation() {
        // 1. 通知 native 端在下个 token 处退出
        LlamaAndroid.stop()
        // 2. 不需要 interrupt：complete() 是 native 调用，interrupt 没用；
        //    让线程自己跑完剩余的一两个 token 就会返回
        currentTask = null
    }

    override fun release() {
        cancelTranslation()
        // 后台线程里卸载模型，避免 UI 线程阻塞（unload 内部要等 native free 完）
        Thread {
            try {
                LlamaAndroid.unload()
                Log.i(TAG, "Model unloaded")
            } catch (e: Exception) {
                Log.w(TAG, "Unload failed", e)
            }
        }.apply { name = "Llama-Unloader"; start() }
        ready = false
    }

    /**
     * 用 Qwen2.5 chat template 包裹用户配置的 system / user 提示词。
     *
     * userPrompt 里的三个占位符（与 OpenAITranslation 保持一致）：
     *   usefromlang   → 源语言显示名（如 "Japanese"）
     *   usetolang     → 目标语言显示名（如 "Chinese"）
     *   usesourcetext → 待翻译文本
     */
    private fun buildPrompt(text: String, src: String, dst: String): String {
        val fromLang = CustomLocale.getInstance(src).getDisplayName()
        val toLang = CustomLocale.getInstance(dst).getDisplayName()
        val renderedUser = userPrompt
            .replace("usefromlang", fromLang)
            .replace("usetolang", toLang)
            .replace("usesourcetext", text)

        // Qwen 系 ChatML 模板；assistant 段末尾的换行很重要，模型从这里开始生成。
        //
        // 关闭思考(thinking)：预设的 Qwen3.5 等 Qwen3 系模型默认开启思考，会先吐一大段
        // <think>…</think> 推理再给译文，对翻译既慢又浪费 token。这里在 assistant 段开头
        // 预填一个【空的 think 块】，等价于官方 chat template 的 enable_thinking=false，
        // 模型看到思考块已闭合就直接输出译文。预填内容属于 prompt（输入），不会出现在
        // nativeCompletion 的返回结果里。对不带思考的模型（如 Hunyuan-MT）也无副作用。
        return buildString {
            append("<|im_start|>system\n")
            append(systemPrompt)
            append("<|im_end|>\n")
            append("<|im_start|>user\n")
            append(renderedUser)
            append("<|im_end|>\n")
            append("<|im_start|>assistant\n")
            append("<think>\n\n</think>\n\n")
        }
    }

    private fun showToast(@androidx.annotation.StringRes messageId: Int) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ctx, ctx.getString(messageId), Toast.LENGTH_LONG).show()
        }
    }
}
