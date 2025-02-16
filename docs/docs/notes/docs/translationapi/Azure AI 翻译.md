---
title: Azure AI 翻译
createTime: 2025/01/22 20:22:03
permalink: /docs/translationapi/azure/
---

- <Badge type="cimportant" text="是否需要网络：是" />
- <Badge type="tip" text="是否需要申请API Key：是" />
- <Badge type="warning" text="支持的翻译模式：OCR" />
- <Badge type="danger" text="翻译质量：★★★★（4星）" />

::: important 操作须知

接下来的步骤默认在**电脑端**进行，建议您在电脑端操作，若不方便使用电脑，可[查看切换UA教程](https://www.moetranslate.top/docs/notice/ua/)做到在手机端访问电脑版网页。

:::

::: tip 需要国际银行卡提示

Azure AI 翻译的API申请需要使用**国际银行卡**，如Visa或Master Card（万事达），在您开始申请前请务必知悉。

:::

## 一、注册并登录火山引擎账号

打开[Azure登录界面](https://go.microsoft.com/fwlink/?linkid=2227353&clcid=0x804&l=zh-cn)。

```md:no-line-numbers
https://go.microsoft.com/fwlink/?linkid=2227353&clcid=0x804&l=zh-cn
```

点击**创建一个**按钮。

<img src="https://img.moetranslate.top/azure_step_1.png"/>

后续如实填写信息即可，直到下图步骤需要**填写国际信用卡**信息。

<img src="https://img.moetranslate.top/azure_step_2.png"/>

## 二、创建翻译资源获取API Key

在Azure主页上方搜索**翻译**，在弹出的结果中点击**翻译工具**，进入翻译工具页面。

<img src="https://img.moetranslate.top/azure_step_3.png"/>

点击**创建Translator**。

<img src="https://img.moetranslate.top/azure_step_4.png"/>

填写资源清单，注意区域选择**全球**，定价层选择**Free F0**。

<img src="https://img.moetranslate.top/azure_step_5.png"/>

一路下一步，最后点击**创建**。

<img src="https://img.moetranslate.top/azure_step_6.png"/>

部署完成后回到**主页**。

<img src="https://img.moetranslate.top/azure_step_7.png"/>

在主页点击**翻译工具**，查看我们刚才创建的翻译服务。

<img src="https://img.moetranslate.top/azure_step_8.png"/>

点击我们刚才创建的翻译服务。

<img src="https://img.moetranslate.top/azure_step_9.png"/>

点击**单击此处管理密钥**。

<img src="https://img.moetranslate.top/azure_step_10.png"/>

密钥1和密钥2作用是一样的，复制其中一个即可。

<img src="https://img.moetranslate.top/azure_step_11.png"/>

## 三、将翻译API粘贴到萌译

在萌译的【API配置】界面中，点击下图【绿色方框中的开关】，将其打开；

再点击【管理Azure API】，进入配置页面，将刚才复制的一条密钥粘贴到相应位置，并点击【保存】；

回到萌译主页面即可开始使用。后续从其他API切换至Azure AI 翻译时，直接打开开关即可，无需再次填写API Key。

<img src="https://img.moetranslate.top/azure_step_12.jpg"/>