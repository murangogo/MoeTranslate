/*
 * llama-bridge.cpp —— JNI bridge between Kotlin and llama.cpp
 *
 * 提供 :llama 模块的核心 API:
 *   - backendInit / backendFree / systemInfo —— 基础设施
 *   - loadModel / freeModel                  —— 模型生命周期
 *   - newContext / freeContext               —— 推理上下文生命周期
 *   - completion                             —— 同步一次性推理（不流式）
 *   - requestStop                            —— 取消正在进行的推理
 *
 * 设计原则：
 *   1. Kotlin 持有 jlong handle，C++ 不维护额外状态（除取消标志外）
 *   2. prompt 模板渲染由 Kotlin 端完成，C++ 只做 tokenize → decode → detokenize
 *   3. 每次 completion 都清空 KV cache，每次推理彼此独立
 *   4. 一次只允许一个 completion 进行（用全局 atomic 标志同步取消）
 */

#include <jni.h>
#include <android/log.h>
#include <atomic>
#include <string>
#include <vector>

#include "llama.h"
#include "common.h"
#include "sampling.h"
#include "chat.h"

#define TAG "llama-android"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace {
    // 全局取消标志。同一时刻只允许一次 completion 在跑，所以一个 flag 够用。
    std::atomic<bool> g_should_stop{false};
}

// ===========================================================================
//  基础设施
// ===========================================================================

extern "C" JNIEXPORT void JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeBackendInit(
        JNIEnv * /* env */, jobject /* thiz */) {
    llama_backend_init();
    LOGI("llama_backend_init done");
}

extern "C" JNIEXPORT void JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeBackendFree(
        JNIEnv * /* env */, jobject /* thiz */) {
    llama_backend_free();
    LOGI("llama_backend_free done");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeSystemInfo(
        JNIEnv *env, jobject /* thiz */) {
    return env->NewStringUTF(llama_print_system_info());
}

// ===========================================================================
//  模型生命周期
// ===========================================================================

extern "C" JNIEXPORT jlong JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeLoadModel(
        JNIEnv *env, jobject /* thiz */, jstring jPath, jint nGpuLayers) {

    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string path_str(path);
    env->ReleaseStringUTFChars(jPath, path);

    auto params = llama_model_default_params();
    params.n_gpu_layers = nGpuLayers;

    llama_model *model = llama_model_load_from_file(path_str.c_str(), params);
    if (!model) {
        LOGE("nativeLoadModel: failed to load %s", path_str.c_str());
        return 0L;
    }
    LOGI("nativeLoadModel: loaded %s (n_gpu_layers=%d)", path_str.c_str(), nGpuLayers);
    return reinterpret_cast<jlong>(model);
}

extern "C" JNIEXPORT void JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeFreeModel(
        JNIEnv * /* env */, jobject /* thiz */, jlong modelHandle) {
    if (modelHandle != 0L) {
        llama_model_free(reinterpret_cast<llama_model *>(modelHandle));
        LOGI("nativeFreeModel done");
    }
}

// ===========================================================================
//  Context 生命周期
// ===========================================================================

extern "C" JNIEXPORT jlong JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeNewContext(
        JNIEnv * /* env */, jobject /* thiz */,
        jlong modelHandle, jint nCtx, jint nBatch, jint nThreads) {

    auto *model = reinterpret_cast<llama_model *>(modelHandle);
    if (!model) {
        LOGE("nativeNewContext: model handle is null");
        return 0L;
    }

    auto cparams = llama_context_default_params();
    cparams.n_ctx           = static_cast<uint32_t>(nCtx);
    cparams.n_batch         = static_cast<uint32_t>(nBatch);
    cparams.n_threads       = nThreads;
    cparams.n_threads_batch = nThreads;
    cparams.no_perf         = true;   // 关掉性能 metrics，省一点开销

    llama_context *ctx = llama_init_from_model(model, cparams);
    if (!ctx) {
        LOGE("nativeNewContext: failed");
        return 0L;
    }
    LOGI("nativeNewContext: n_ctx=%d n_batch=%d n_threads=%d", nCtx, nBatch, nThreads);
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeFreeContext(
        JNIEnv * /* env */, jobject /* thiz */, jlong ctxHandle) {
    if (ctxHandle != 0L) {
        llama_free(reinterpret_cast<llama_context *>(ctxHandle));
        LOGI("nativeFreeContext done");
    }
}

// ===========================================================================
//  Chat 模板渲染
//
//  用模型 GGUF 自带的 Jinja chat_template，把 system / user 两条消息渲染成最终
//  prompt（含 generation prompt，可直接喂给 nativeCompletion）。这样不同模型族
//  （Qwen / Gemma / Llama / Hunyuan-MT …）各自走正确模板；并通过 enableThinking
//  精确控制 Qwen3 这类模型的"思考"段。
//
//  返回:
//    - 成功: 渲染好的 prompt 字符串
//    - 失败（模型无 chat_template / 模板解析异常）: 空字符串
//      —— Kotlin 端据此回退到硬编码 ChatML。
//
//  注意: 这里用的是 common 层的 common_chat_templates_*（minja Jinja 引擎），
//        而不是 llama.h 里的 llama_chat_apply_template —— 后者不解析 Jinja，
//        也无法处理 enable_thinking。
// ===========================================================================

extern "C" JNIEXPORT jstring JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeFormatChat(
        JNIEnv *env, jobject /* thiz */,
        jlong modelHandle,
        jstring jSystem, jstring jUser,
        jboolean enableThinking) {

    auto *model = reinterpret_cast<llama_model *>(modelHandle);
    if (!model) {
        LOGE("nativeFormatChat: model handle is null");
        return env->NewStringUTF("");
    }

    const char *sys_c = env->GetStringUTFChars(jSystem, nullptr);
    std::string system_text(sys_c ? sys_c : "");
    env->ReleaseStringUTFChars(jSystem, sys_c);

    const char *usr_c = env->GetStringUTFChars(jUser, nullptr);
    std::string user_text(usr_c ? usr_c : "");
    env->ReleaseStringUTFChars(jUser, usr_c);

    try {
        // 第二个参数为空 = 不覆盖，直接用 GGUF metadata 里的 chat_template
        common_chat_templates_ptr tmpls = common_chat_templates_init(model, "");

        common_chat_templates_inputs inputs;
        if (!system_text.empty()) {
            common_chat_msg sys_msg;
            sys_msg.role    = "system";
            sys_msg.content = system_text;
            inputs.messages.push_back(std::move(sys_msg));
        }
        common_chat_msg user_msg;
        user_msg.role    = "user";
        user_msg.content = user_text;
        inputs.messages.push_back(std::move(user_msg));

        inputs.add_generation_prompt = true;
        inputs.use_jinja             = true;
        inputs.enable_thinking       = (enableThinking == JNI_TRUE);

        common_chat_params params = common_chat_templates_apply(tmpls.get(), inputs);
        LOGI("nativeFormatChat: %zu chars, thinking=%d", params.prompt.size(),
             inputs.enable_thinking ? 1 : 0);
        return env->NewStringUTF(params.prompt.c_str());
    } catch (const std::exception &e) {
        LOGE("nativeFormatChat: exception: %s", e.what());
        return env->NewStringUTF("");
    } catch (...) {
        LOGE("nativeFormatChat: unknown exception");
        return env->NewStringUTF("");
    }
}

// ===========================================================================
//  取消
// ===========================================================================

extern "C" JNIEXPORT void JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeRequestStop(
        JNIEnv * /* env */, jobject /* thiz */) {
    g_should_stop.store(true, std::memory_order_release);
    LOGI("nativeRequestStop");
}

// ===========================================================================
//  同步一次性推理
//
//  返回:
//    - 成功: 生成的文本（不含 prompt 本身）
//    - 失败 / prompt 过长: 空字符串（错误信息已通过 LOGE 打到 logcat）
//
//  阻塞当前线程直到 EOS / maxTokens / requestStop 为止。
// ===========================================================================

extern "C" JNIEXPORT jstring JNICALL
Java_com_moe_moetranslator_llama_LlamaAndroid_nativeCompletion(
        JNIEnv *env, jobject /* thiz */,
        jlong ctxHandle, jlong modelHandle,
        jstring jPrompt, jint maxTokens,
        jfloat temperature, jfloat topP, jfloat repeatPenalty) {

    g_should_stop.store(false, std::memory_order_release);

    auto *ctx   = reinterpret_cast<llama_context *>(ctxHandle);
    auto *model = reinterpret_cast<llama_model *>(modelHandle);
    if (!ctx || !model) {
        LOGE("nativeCompletion: invalid handles (ctx=%p model=%p)", ctx, model);
        return env->NewStringUTF("");
    }

    // ---- prompt 字符串 ----
    const char *prompt_c = env->GetStringUTFChars(jPrompt, nullptr);
    std::string prompt(prompt_c);
    env->ReleaseStringUTFChars(jPrompt, prompt_c);

    const llama_vocab *vocab = llama_model_get_vocab(model);

    // ---- tokenize ----
    // add_special=true: 让模型自己决定要不要加 BOS；很多 chat 模板里 BOS 已在 prompt 内
    // parse_special=true: 识别 <|im_start|> 这类特殊 token（chat 模板必备）
    std::vector<llama_token> tokens = common_tokenize(vocab, prompt, true, true);
    if (tokens.empty()) {
        LOGE("nativeCompletion: tokenize returned empty");
        return env->NewStringUTF("");
    }
    LOGI("nativeCompletion: prompt has %zu tokens, will generate up to %d", tokens.size(), maxTokens);

    const int n_ctx_used = (int) llama_n_ctx(ctx);
    if ((int) tokens.size() >= n_ctx_used) {
        LOGE("nativeCompletion: prompt (%zu tokens) >= n_ctx (%d). Increase n_ctx or shorten prompt.",
             tokens.size(), n_ctx_used);
        return env->NewStringUTF("");
    }

    // ---- sampler ----
    common_params_sampling sparams;
    sparams.temp           = temperature;
    sparams.top_p          = topP;
    sparams.penalty_repeat = repeatPenalty;
    sparams.no_perf        = true;

    common_sampler *smpl = common_sampler_init(model, sparams);
    if (!smpl) {
        LOGE("nativeCompletion: sampler init failed");
        return env->NewStringUTF("");
    }

    // ---- 清空 KV cache（保证每次推理彼此独立） ----
    llama_memory_clear(llama_get_memory(ctx), true);

    // ---- decode prompt ----
    llama_batch batch = llama_batch_get_one(tokens.data(), (int32_t) tokens.size());
    if (llama_decode(ctx, batch) != 0) {
        LOGE("nativeCompletion: prompt decode failed");
        common_sampler_free(smpl);
        return env->NewStringUTF("");
    }

    // ---- generate ----
    std::string result;
    result.reserve(2048);
    int generated = 0;

    for (int i = 0; i < maxTokens; ++i) {
        if (g_should_stop.load(std::memory_order_acquire)) {
            LOGI("nativeCompletion: cancelled after %d tokens", generated);
            break;
        }

        llama_token new_token_id = common_sampler_sample(smpl, ctx, -1);
        common_sampler_accept(smpl, new_token_id, /* accept_grammar */ true);

        if (llama_vocab_is_eog(vocab, new_token_id)) {
            LOGI("nativeCompletion: EOS at %d tokens", generated);
            break;
        }

        // special=false: 不要把 <|im_end|> 这种渲染成可见文本
        std::string piece = common_token_to_piece(ctx, new_token_id, /* special */ false);
        result += piece;
        ++generated;

        // decode the new token, then loop
        llama_batch one = llama_batch_get_one(&new_token_id, 1);
        if (llama_decode(ctx, one) != 0) {
            LOGE("nativeCompletion: decode failed at token %d", i);
            break;
        }
    }

    common_sampler_free(smpl);
    LOGI("nativeCompletion: done, %d tokens, %zu chars", generated, result.size());
    return env->NewStringUTF(result.c_str());
}
