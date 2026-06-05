/*
 * LlamaAndroid —— Kotlin facade for libllama-android.so
 *
 *  使用流程：
 *      LlamaAndroid.backendInit()                    // App 启动时一次
 *      LlamaAndroid.load("/path/to/model.gguf")      // 加载模型
 *      val out = LlamaAndroid.complete("...")        // 同步推理
 *      LlamaAndroid.unload()                         // 切换模型或退出时
 *      LlamaAndroid.backendFree()                    // App 退出时一次（可省略）
 *
 *  线程模型：
 *      complete() 是同步阻塞调用，会卡住调用线程几秒到几分钟，不要在 UI 线程上调。
 *      推荐放在 Dispatchers.IO 或独立线程里。
 *      同一时刻只允许一个 complete() 在跑——这里用 @Synchronized 保证。
 *      stop() 可以从任意线程调（包括 UI 线程），它会让正在进行的 complete() 提前返回。
 */
package com.moe.moetranslator.llama

object LlamaAndroid {

    init {
        System.loadLibrary("llama-android")
    }

    // ------------------ native 声明 ------------------
    private external fun nativeBackendInit()
    private external fun nativeBackendFree()
    private external fun nativeSystemInfo(): String

    private external fun nativeLoadModel(path: String, nGpuLayers: Int): Long
    private external fun nativeFreeModel(modelHandle: Long)

    private external fun nativeNewContext(
        modelHandle: Long, nCtx: Int, nBatch: Int, nThreads: Int
    ): Long
    private external fun nativeFreeContext(ctxHandle: Long)

    private external fun nativeRequestStop()

    private external fun nativeCompletion(
        ctxHandle: Long, modelHandle: Long,
        prompt: String, maxTokens: Int,
        temperature: Float, topP: Float, repeatPenalty: Float
    ): String

    private external fun nativeFormatChat(
        modelHandle: Long, system: String, user: String, enableThinking: Boolean
    ): String

    // ------------------ 内部状态 ------------------
    @Volatile private var modelHandle: Long = 0L
    @Volatile private var ctxHandle:   Long = 0L

    /** 当前是否已加载模型并准备好推理。 */
    val isLoaded: Boolean
        get() = modelHandle != 0L && ctxHandle != 0L

    // ------------------ Backend ------------------

    /** 初始化 llama backend。整个 App 进程只需要调一次。 */
    fun backendInit() = nativeBackendInit()

    /** 释放 llama backend 全局资源。一般在 App 退出前调，省略也行（进程退出会自然回收）。 */
    fun backendFree() = nativeBackendFree()

    /** 返回 ggml 检测到的 CPU/GPU 特性字符串。 */
    fun systemInfo(): String = nativeSystemInfo()

    // ------------------ 模型加载 / 卸载 ------------------

    /**
     * 加载 GGUF 模型并创建推理上下文。
     * 阻塞调用，可能要几秒到几十秒（取决于模型大小和磁盘速度）。
     *
     * 如果当前已经有模型加载，会先 unload 旧模型再 load 新的。
     *
     * @throws IllegalStateException 加载失败时抛出
     */
    @Synchronized
    fun load(
        modelPath: String,
        nCtx: Int = 2048,
        nBatch: Int = 512,
        nThreads: Int = 4,
        nGpuLayers: Int = 0,
    ) {
        if (isLoaded) {
            unloadInternal()
        }

        val mh = nativeLoadModel(modelPath, nGpuLayers)
        if (mh == 0L) {
            throw IllegalStateException("Failed to load model: $modelPath")
        }

        val ch = nativeNewContext(mh, nCtx, nBatch, nThreads)
        if (ch == 0L) {
            nativeFreeModel(mh)
            throw IllegalStateException("Failed to create context for $modelPath")
        }

        modelHandle = mh
        ctxHandle = ch
    }

    /** 卸载当前模型并释放所有内存。可重复调用（已卸载时为 no-op）。 */
    @Synchronized
    fun unload() {
        unloadInternal()
    }

    private fun unloadInternal() {
        // 释放顺序：先 context 后 model
        if (ctxHandle != 0L) {
            nativeFreeContext(ctxHandle)
            ctxHandle = 0L
        }
        if (modelHandle != 0L) {
            nativeFreeModel(modelHandle)
            modelHandle = 0L
        }
    }

    // ------------------ 推理 ------------------

    /**
     * 用模型 GGUF 自带的 Jinja chat 模板，把 system / user 渲染成最终 prompt。
     * enableThinking=false 时按模型模板关闭思考（如 Qwen3 注入空 <think></think>）。
     *
     * @return 渲染好的 prompt；若模型无模板或渲染失败返回空串（调用方应回退到自带兜底模板）。
     * @throws IllegalStateException 模型未加载时抛出
     */
    @Synchronized
    fun formatChat(system: String, user: String, enableThinking: Boolean): String {
        check(isLoaded) { "Model not loaded. Call load() first." }
        return nativeFormatChat(modelHandle, system, user, enableThinking)
    }

    /**
     * 同步推理。会一直阻塞直到模型输出 EOS / 达到 maxTokens / 被 stop() 取消。
     *
     * 翻译场景推荐参数: temperature=0.2, topP=0.9, repeatPenalty=1.1
     *
     * @throws IllegalStateException 模型未加载时抛出
     */
    @Synchronized
    fun complete(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.2f,
        topP: Float = 0.9f,
        repeatPenalty: Float = 1.1f,
    ): String {
        check(isLoaded) { "Model not loaded. Call load() first." }
        return nativeCompletion(
            ctxHandle, modelHandle,
            prompt, maxTokens,
            temperature, topP, repeatPenalty,
        )
    }

    /**
     * 请求停止正在进行的 complete()。从任意线程调用都安全，包括 UI 线程。
     * 调用后 complete() 会在下一个 token 边界处返回已生成的部分文本。
     */
    fun stop() = nativeRequestStop()
}
