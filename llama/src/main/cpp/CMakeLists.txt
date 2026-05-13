# ============================================================================
#  :llama —— Android JNI bridge for llama.cpp
#
#  本文件做两件事：
#    1. 把 llama.cpp 整个工程作为子项目编进来（add_subdirectory）
#    2. 编出 libllama-android.so —— 这是 Kotlin 端通过 System.loadLibrary 加载的桥
#
#  llama.cpp 自身有非常多 CMake option。用 set(... CACHE ... FORCE) 提前
#  覆盖掉默认值，确保子项目的 option() 不会把它们改回去。
# ============================================================================

cmake_minimum_required(VERSION 3.22.1)
project("llama-android" LANGUAGES C CXX)

# ----------------------------------------------------------------------------
#  C++ 标准 & 通用编译选项
# ----------------------------------------------------------------------------
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Release 构建强制启用优化（debug 构建不动）
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release CACHE STRING "" FORCE)
endif()

# ----------------------------------------------------------------------------
#  llama.cpp / ggml 的关键开关（必须在 add_subdirectory 之前 set）
# ----------------------------------------------------------------------------

# --- 静态链接，最终只输出一个 libllama-android.so ---
set(BUILD_SHARED_LIBS  OFF CACHE BOOL "" FORCE)

# --- ggml 选项 ---
# Android 是 cross-compile，绝不能让 ggml 跑 -march=native 之类的本机优化
set(GGML_NATIVE        OFF CACHE BOOL "" FORCE)
# Bionic 的 OpenMP 支持不稳，关掉；llama.cpp 自带线程池就够用
set(GGML_OPENMP        OFF CACHE BOOL "" FORCE)
# llamafile 在 Android 上没意义且增加体积
set(GGML_LLAMAFILE     OFF CACHE BOOL "" FORCE)
# ccache 是 host 工具，在 AS 里没接进来，关掉避免 cmake 找不到时报警
set(GGML_CCACHE        OFF CACHE BOOL "" FORCE)
# LTO 在 Android 上链接极慢且体积收益小
set(GGML_LTO           OFF CACHE BOOL "" FORCE)
# CPU repack：Android ARM 关键优化（Q4_0 → Q4_X_X 运行时转换），保持开启
set(GGML_CPU_REPACK    ON  CACHE BOOL "" FORCE)
# backend 不做 dynamic load，全部静态链进 ggml
set(GGML_BACKEND_DL    OFF CACHE BOOL "" FORCE)

# --- llama 选项 ---
# add_subdirectory 时 LLAMA_STANDALONE 自动是 OFF，下面这些默认就是 OFF。
# 但显式 FORCE 一遍可以防御未来上游改默认值。
set(LLAMA_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER   OFF CACHE BOOL "" FORCE)
# common 库需要 —— chat template / sampler 都在 common 里
set(LLAMA_BUILD_COMMON   ON  CACHE BOOL "" FORCE)
# 关掉 -Werror，防止上游某些 warning 把 release 编译炸了
set(LLAMA_FATAL_WARNINGS OFF CACHE BOOL "" FORCE)
# common 不需要 LLGuidance（structured output）
set(LLAMA_LLGUIDANCE     OFF CACHE BOOL "" FORCE)
# common 不需要 OpenSSL（不在 native 端发 HTTPS）
set(LLAMA_OPENSSL        OFF CACHE BOOL "" FORCE)

# ----------------------------------------------------------------------------
#  拉入 llama.cpp 整个工程
# ----------------------------------------------------------------------------
add_subdirectory(llama.cpp build-llama)

# ----------------------------------------------------------------------------
#  自己的 JNI bridge
# ----------------------------------------------------------------------------
add_library(llama-android SHARED
    llama-bridge.cpp
)

target_include_directories(llama-android PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/common
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/ggml/include
)

target_link_libraries(llama-android PRIVATE
    llama          # libllama.a
    llama-common   # libllama-common.a —— 注意 target 名是 llama-common，不是 common
    ggml           # libggml.a (内部已经链了 ggml-base / ggml-cpu)
    android        # for AAssetManager 等（暂未用，预留）
    log            # __android_log_print
)

# ----------------------------------------------------------------------------
#  链接期优化：去掉死代码、strip 符号
# ----------------------------------------------------------------------------
target_link_options(llama-android PRIVATE
    "-Wl,--gc-sections"
    "-Wl,--strip-all"
    # 一些 ARM 工具链下能再省 100KB+
    "-Wl,--exclude-libs,ALL"
)

# 让 ggml 内部的 -fvisibility=hidden 也对 .so 生效，进一步减小符号表
set_target_properties(llama-android PROPERTIES
    C_VISIBILITY_PRESET   hidden
    CXX_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN ON
)

message(STATUS "[llama-android] Configured for ABI=${ANDROID_ABI}, build=${CMAKE_BUILD_TYPE}")
