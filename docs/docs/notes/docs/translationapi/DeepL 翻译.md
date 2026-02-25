---
title: DeepL 翻译
author: azuki
createTime: 2026/02/25 21:48:14
permalink: /docs/translationapi/deepl/
---

- <Badge type="cimportant" text="是否需要网络：是" />
- <Badge type="tip" text="是否需要申请API Key：是" />
- <Badge type="warning" text="支持的翻译模式：OCR" />
- <Badge type="danger" text="翻译质量：★★★★（4星）" />

::: important 操作须知

接下来的步骤默认在**电脑端**进行，建议您在电脑端操作，若不方便使用电脑，可[查看切换UA教程](https://www.moetranslate.top/docs/notice/ua/)做到在手机端访问电脑版网页。

:::

## 一、注册并登录DeepL翻译账号

打开[DeepL翻译开发者平台](https://developers.deepl.com/docs/getting-started/intro/)。

```md:no-line-numbers
https://developers.deepl.com/docs/getting-started/intro/
```

点击右上角的**Create free API account**按钮。

<img src="https://img.moetranslate.top/deepl_step_1.png"/>

注册完成并登录后，选择图中的“API”，并点击“API Free”等级下的“Sign up for free”。

<img src="https://img.moetranslate.top/deepl_step_2.png"/>

填写个人信息和信用卡信息（注意这里需要使用国外信用卡），完成后点击“Begin subscription”，开始订阅。

<img src="https://img.moetranslate.top/deepl_step_3.png"/>

## 二、在控制台获取翻译API

打开[DeepL翻译控制台](https://www.deepl.com/your-account/keys)。

```md:no-line-numbers
https://www.deepl.com/your-account/keys
```

点击“Create Key”创建密钥，创建完成后，复制API Key。

<img src="https://img.moetranslate.top/deepl_step_4.png"/>

## 三、将翻译API粘贴到萌译

在萌译的【API配置】界面中，点击下图【绿色方框中的开关】，将其打开；

再点击【管理DeepL 翻译API】，进入API Key配置页面。

这里要注意，填写第一行的Host时，如果你使用的是**免费版，请<font color="#E25A5C">严格</font>填写`api-free.deepl.com`**；如果你使用的是**免费版，请<font color="#E25A5C">严格</font>填写`api.deepl.com`**。

然后，将刚才复制的**API Key**粘贴到第二行，并点击【保存】；

回到萌译主页面即可开始使用。后续从其他API切换至DeepL 翻译时，直接打开开关即可，无需再次填写API Key。

<img src="https://img.moetranslate.top/deepl_step_5.jpg"/>