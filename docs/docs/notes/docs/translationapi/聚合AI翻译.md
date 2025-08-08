---
title: 聚合AI翻译
author: azuki
createTime: 2025/08/08 10:48:14
permalink: /docs/translationapi/uniaitrans/
---

- <Badge type="cimportant" text="是否需要网络：是" />
- <Badge type="tip" text="是否需要申请API Key：是" />
- <Badge type="warning" text="支持的翻译模式：OCR" />
- <Badge type="danger" text="翻译质量：不能确定" />

::: important 未完善

下面的文档只是草案，还没有进行进一步的撰写。

<img src="https://img.moetranslate.top/under_construction.png" width="35%" />

:::


::: important 重要

这是一个高级功能，需要您了解基本的API调用知识。

:::


## 一、基本说明
此功能允许您使用市面上主流的AI来进行翻译（<b>只要兼容OpenAI接口规范即可接入</b>），如ChatGPT、Qwen、DeepSeek等，接入方式为调用官方API接口。

## 二、参数说明
1、API_KEY：对应官网上给出的API密钥  
2、基础URL：对应官网上给出的接入路径  
3、model：即使用的模型名称  
4、系统提示词：给模型设定身份，保持默认即可  
5、用户提示词：发送给模型的翻译指令，保持默认即可  

## 三、填写示例
以接入阿里的“通义千问-Plus”模型为例：  
对于API_KEY，正常填写即可；  
对于基础URL，阿里旗下的千问模型一般都是`https://dashscope.aliyuncs.com/compatible-mode/v1`；  
对于model，“通义千问-Plus”模型对应的代号是qwen-plus；  
对于系统提示词和用户提示词，保持默认即可。  

## 四、在使用该功能之前
您可以先使用Postman、Reqable等工具测试API响应，待测试成功后再将相关参数填入该功能中。




