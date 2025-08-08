---
title: 自定义文本API
createTime: 2025/01/22 20:22:31
permalink: /docs/translationapi/customtext/
---

- <Badge type="cimportant" text="是否需要网络：不能确定" />
- <Badge type="tip" text="是否需要申请API Key：不能确定" />
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
此功能允许您配置自己的<b>文本翻译API</b>，支持GET和POST两种请求方式（对于文本翻译，POST的Content-Type是且只能是“application/json; charset=utf-8”，<b>不需</b>显式填写在下面的请求头中，已预置好）。所有API响应必须为JSON格式。

## 二、使用场景
一般来说，由于各大翻译API提供商都有自己的签名算法，无法通过简单的GET/POST请求接入，因此实际上自定义文本翻译API可以接入的商用API接口数量有限，更贴合实际的使用场景为接入自己搭建的翻译API接口。

## 三、参数说明
1、请求方法：可以为GET或POST  
2、基础URL：可指定http或https，如不指定scheme，则默认为https  
3、查询参数（仅GET）：附加于基础URL之后的参数  
4、请求头：发送请求时的头部  
5、JSON请求体（仅POST）：发送请求时附带的JSON内容  
6、JSON响应路径：调用API后，从响应的JSON中获取翻译结果的路径  

注意：  
1、在“查询参数”和“JSON请求体”中，<b>使用“usesourcetext”代表待翻译的文本</b>；  
2、务必将“源语言”和“目标语言”的语言代码包含在“查询参数”或“JSON请求体”中；  
3、同时请注意将主页面指定“源语言”，因为萌译在使用OCR识别文字时，需要知道“源语言”是什么。  

##  四、请求示例
<b>GET请求</b>  
基础URL：  
https://api.example.com/translate  

查询参数：  
key：text，value：usesourcetext；  
key：source，value：ja；  
key：target，value：zh  

请求头：  
Authorization = my_custom_token  

<b>POST请求</b>  
基础URL：  
https://api.example.com/translate  

请求头：  
Authorization = my_custom_token  

JSON请求体:  
key：text，value：usesourcetext；  
key：source，value：ja；  
key：target，value：zh  


## 五、JSON响应路径
<b>举例1：</b>  
假设成功响应的JSON如下：  

```md
{
    "code": 0,
    "msg": "success",
    "data": {
        "translation": "你好世界"
    }
}
```

则JSON响应路径应填写：`data.translation`  

<b>举例2：</b>  
假设成功响应的JSON如下：  

```md
{
    "code": 0,
    "msg": "success",
    "result": "你好世界"
}
```

则JSON响应路径应填写：`result`  

## 六、在使用该功能之前
您可以先使用Postman、Reqable等工具测试API响应，待测试成功后再将相关参数填入该功能中。