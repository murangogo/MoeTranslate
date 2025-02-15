---
title: ML Kit
author: azuki
createTime: 2024/03/02 10:48:14
permalink: /docs/translationapi/mlkit/
badge: 本地AI翻译（OCR）
---

- <Badge type="cimportant" text="是否需要网络：否" />
- <Badge type="tip" text="是否需要申请API Key：否" />
- <Badge type="warning" text="支持的翻译模式：OCR" />
- <Badge type="danger" text="翻译质量：★（1星）" />

## 启用方法

在【翻译模式】选择**本地OCR识别+文本翻译**的情况下，打开【API配置】。

打开图中【绿色方框中的开关】，点击【管理ML Kit模型文件】。

在下载页面可以看到【ML Kit模型下载状态：未下载】，点击【开始下载】按钮即可开始下载模型，总大小约100MB，可在下拉通知栏中查看下载进度。

::: important 重要

由于模型在谷歌服务器上，可能会出现下载速度慢或无法下载的情况，**请灵活切换WLAN与数据流量，以及开启或关闭VPN**。  
经开发者亲测，**WLAN+开启VPN**或**数据流量+关闭VPN**这两种情况下载速度较快。

:::

<img src="https://img.moetranslate.top/mlkit_step_1.jpg"/>

下载完成后，回到萌译首页即可使用该API。

<img src="https://img.moetranslate.top/mlkit_step_2.jpg"/>
