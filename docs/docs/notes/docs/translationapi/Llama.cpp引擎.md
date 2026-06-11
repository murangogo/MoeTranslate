---
title: Llama.cpp引擎
author: azuki
createTime: 2026/06/09 17:48:14
permalink: /docs/translationapi/llamacpp/
badge: 离线质量最佳
---

- <Badge type="cimportant" text="是否需要网络：否" />
- <Badge type="tip" text="是否需要申请API Key：否" />
- <Badge type="warning" text="支持的翻译模式：OCR" />
- <Badge type="danger" text="翻译质量：不能确定" />

::: important 重要

这是一个高级功能，需要您了解基本的大语言模型推理知识。

:::

## 启用方法

在【翻译模式】选择**本地OCR识别+文本翻译**的情况下，打开【API配置】。

打开图中【绿色方框中的开关】，点击【管理LlamaCpp模型】。

在下载页面点击【添加模型】，然后点击【从预设列表下载】按钮，再点击“HY-MT1.5-1.8B-Q4_K_M.gguf”旁边的蓝色下载图标即可开始下载模型，可在页面内查看下载速度及进度。

::: important 重要

由于模型在Cloudflare R2对象存储桶内，可能会出现下载速度慢或无法下载的情况，**请灵活切换WLAN与数据流量，以及开启或关闭VPN**。

:::

<img src="https://img.moetranslate.top/llamacpp_step_1.jpg"/>

下载完成后，点击模型列表中的【HY-MT1.5-1.8B-Q4_K_M】激活模型，回到萌译首页即可使用该API。

<img src="https://img.moetranslate.top/llamacpp_step_2.jpg"/>




