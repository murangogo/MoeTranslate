---
title: API配置
author: azuki
createTime: 2024/03/02 10:48:14
permalink: /docs/translation/apiconfig/
---

## 一、简介

API配置是萌译翻译功能中的核心步骤，一般来说选择完翻译模式，接下来就需要配置翻译API。针对不同的翻译模式，萌译分别支持不同的翻译API。

**本地OCR识别+文本翻译：** 支持的API有（共12个）：
- 2种本地AI翻译：ML Kit翻译、NLLB翻译；
- 7种预置云端API：必应翻译、小牛翻译、聚合 AI 翻译、火山引擎、Azure翻译、百度翻译、腾讯云；
- 3个自定义云端文本翻译API预留位。

**直接上传图片进行翻译：** 支持的API有（共5个）：
- 2种预置云端API：百度翻译、腾讯云；
- 3个自定义云端图片翻译API预留位。

下面分翻译模式介绍一下各种翻译API。

::: tip 哪个API翻译质量更好？

具体的API翻译质量评价可以在[项目介绍](https://www.moetranslate.top/docs/intro/#_2%E3%80%81%E9%A2%84%E7%BD%AEapi%E4%BB%8B%E7%BB%8D)中阅读，这里不再重复，重点介绍不同的API配置流程。

:::

## 二、“本地OCR识别+文本翻译”模式的翻译API介绍

当您选择“本地OCR识别+文本翻译”的翻译模式时，“API配置”页面会如下所示。

页面总的来说分为三个区域，分别是“本地AI翻译”、“预置云端API”、“自定义云端文本翻译API”；这三个区域加起来一共有11个API，这11个**API是互斥的，也就是说同时只能打开一个API来使用**。

下面将根据不同的翻译API类别做一下详细的介绍。

<img src="https://img.moetranslate.top/apiconfig_text_show.jpg"/>

### 1、本地AI翻译

本地AI翻译有2种，分别是ML Kit翻译、NLLB翻译。

这两种本地AI翻译无需配置API Key，下载模型后即可使用，具体流程如下图所示。

**ML Kit：**  
点击【管理ML Kit模型文件】，点击【开始下载】，等待下载完成后，在“API配置”页面中点击下图【绿色方框中的开关】，将其打开，即可使用ML Kit翻译。只需在初次使用时下载一次，后续无需再次下载，直接启用即可。

<img src="https://img.moetranslate.top/apiconfig_text_mlkit.jpg"/>

**NLLB：**  
和ML Kit的启用方法基本相同，点击【管理NLLB模型文件】，点击【开始下载】，等待下载完成后，在“API配置”页面中点击下图【绿色方框中的开关】，将其打开，即可使用NLLB翻译。只需在初次使用时下载一次，后续无需再次下载，直接启用即可。

<img src="https://img.moetranslate.top/apiconfig_text_nllb.jpg"/>

### 2、预置云端API

预置云端API有7种，分别是必应翻译、小牛翻译、聚合 AI 翻译、火山引擎、Azure翻译、百度翻译、腾讯云。其中只有必应翻译不需要配置API Key，其他的云端API均需配置API Key。

**必应翻译API：**

先来介绍一下必应翻译API的使用方法，非常简单，只需点击下图【绿色方框中的开关】，将其打开，即可使用必应翻译。这也是初次打开萌译时默认的翻译API，旨在让用户快速体验萌译的翻译功能。

::: tip 小建议

必应翻译不需要配置API Key，其实现原理是通过模拟浏览器访问网页的行为来实现翻译，依赖于网页结构和token提取，这是非常不规范的行为，稳定性较差。也正因为如此，萌译无法保证它随时可用，还是**推荐您使用其他翻译API，如小牛翻译**。

:::

<div style="text-align: center;">
  <img src="https://img.moetranslate.top/apiconfig_text_bing.jpg" width="35%" />
</div>

**其他预置云端API：**

除了上面介绍的必应翻译，其他的API均需配置API Key，其配置过程大同小异，下面以翻译效果比较好的小牛翻译为例，解释一下流程。

首先点击【管理小牛翻译API】，进入API Key配置页面，然后根据从小牛翻译官网获取的API Key填写相关信息，再点击【保存】即可，之后在“API配置”页面中点击下图【绿色方框中的开关】，将其打开，即可使用小牛翻译。后续无需再次配置API Key，直接启用即可。

若初次使用小牛翻译API，可点击【这是什么？】来查看提示和教程。

::: tip 各种API的申请教程

所有API的申请教程跳转链接在[项目介绍](https://www.moetranslate.top/docs/intro/#_2%E3%80%81%E9%A2%84%E7%BD%AEapi%E4%BB%8B%E7%BB%8D)中可以查看。

:::

<img src="https://img.moetranslate.top/apiconfig_text_niu.jpg"/>

### 3、自定义云端文本翻译API

该区域的配置项仅针对于高级用户，如果您已经需要配置自定义的API了，那么基本上也不需要过于啰嗦的指导了，在萌译App中已经有一些基本指导，[点击这里](https://www.moetranslate.top/docs/translationapi/customtext/)可以查看进一步的简易搭建教程。

基本流程如下图所示。

<img src="https://img.moetranslate.top/apiconfig_text_custom.jpg"/>

## 三、“直接上传图片进行翻译”模式的翻译API介绍

当您选择“直接上传图片进行翻译”的翻译模式时，“API配置”页面会如下所示。

页面总的来说分为两个区域，分别是“预置云端API”、“自定义云端图片翻译API”；这两个区域加起来一共有5个API，这5个**API是互斥的，也就是说同时只能打开一个API来使用**。

下面将根据不同的翻译API类别做一下详细的介绍。

<div style="text-align: center;">
  <img src="https://img.moetranslate.top/apiconfig_pic_show.jpg" width="35%" />
</div>

### 1、预置云端API

预置云端API有2种，分别是百度翻译、腾讯云，均需配置API Key。

这些API Key的配置过程大同小异，下面以百度翻译为例，解释一下流程。

首先点击【管理百度翻译API】，进入API Key配置页面，然后根据从百度翻译官网获取的API Key填写相关信息，再点击【保存】即可，之后在“API配置”页面中点击下图【绿色方框中的开关】，将其打开，即可使用百度翻译。后续无需再次配置API Key，直接启用即可。

若初次使用百度翻译API，可点击【这是什么？】来查看提示和教程。

::: tip 各种API的申请教程

所有API的申请教程跳转链接在[项目介绍](https://www.moetranslate.top/docs/intro/#_2%E3%80%81%E9%A2%84%E7%BD%AEapi%E4%BB%8B%E7%BB%8D)中可以查看。

:::

<img src="https://img.moetranslate.top/apiconfig_pic_baidu.jpg"/>

### 2、自定义云端文本翻译API

该区域的配置项仅针对于高级用户，如果您已经需要配置自定义的API了，那么基本上也不需要过于啰嗦的指导了，在萌译App中已经有一些基本指导，[点击这里](https://www.moetranslate.top/docs/translationapi/custompic/)可以查看进一步的简易搭建教程。

基本流程如下图所示。

<img src="https://img.moetranslate.top/apiconfig_pic_custom.jpg"/>