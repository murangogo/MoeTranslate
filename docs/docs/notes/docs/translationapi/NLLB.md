---
title: NLLB
author: azuki
permalink: /docs/translationapi/nllb/
createTime: 2024/11/21 15:32:23
badge: 本地AI翻译（OCR）
---

- <Badge type="cimportant" text="是否需要网络：否" />
- <Badge type="tip" text="是否需要申请API Key：否" />
- <Badge type="warning" text="支持的翻译模式：OCR" />
- <Badge type="danger" text="翻译质量：★★（2星）" />

## 应用内下载

在【翻译模式】选择**本地OCR识别+文本翻译**的情况下，打开【API配置】。

打开图中【绿色方框中的开关】，点击【管理NLLB模型文件】。

在下载页面可以看到【NLLB模型下载状态：未下载】，点击【开始下载】按钮即可开始下载模型，总大小约1GB，可在页面内查看下载速度及进度。

::: important 重要

由于模型在Cloudflare R2对象存储桶内，可能会出现下载速度慢或无法下载的情况，**请灵活切换WLAN与数据流量，以及开启或关闭VPN**。  
经开发者亲测，无论怎么样下载得都挺快的。

:::

<img src="https://img.moetranslate.top/nllb_app_step_1.jpg"/>

下载完成后，回到萌译首页即可使用该API。

<img src="https://img.moetranslate.top/nllb_app_step_2.jpg"/>

## 手动下载

一般来说是不会使用此方法进行手动下载的，但如果真的发生了特殊情况，在应用内怎么样也下载不了，请[点击此处](https://www.moetranslate.top/download/others/)来查看手动下载的链接。

当您手动下载完上面的5个模型文件后，请按如下步骤操作。

::: tip 该方法的本质

本质就是下载完成5个模型文件后，将模型文件移动到萌译的应用目录内。

:::

:::: steps

1. 打开文件管理器
   
   这里开发者使用的是[MT管理器](https://mt2.cn/)。

2. 在**左半边**找到下载的5个模型文件
   
   如下图所示，如果使用123云盘下载，那么一般就在【123云盘】文件夹中。

   <img src="https://img.moetranslate.top/nllb_hand_step_1.jpg"/>

3. 在**右半边**进入萌译的应用目录
   
   如图所示，在右半边根据路径**Android/data/com.moe.moetranslator/files/models**，进入models文件夹（如果没有models文件夹可以手动创建）。

   <img src="https://img.moetranslate.top/nllb_hand_step_2_1.jpg"/>

   <img src="https://img.moetranslate.top/nllb_hand_step_2_2.jpg"/>

4. 移动模型文件

   如图所示，将左边的5个模型文件移动到右边即可。

   <img src="https://img.moetranslate.top/nllb_hand_step_3.jpg"/>

5. 让萌译检测到
   
   在萌译中的下载页面点击开始下载，萌译会跳过下载步骤直接进行校验，校验通过即可变成【NLLB模型下载状态：已下载】状态。

   <img src="https://img.moetranslate.top/nllb_hand_step_4.jpg"/>