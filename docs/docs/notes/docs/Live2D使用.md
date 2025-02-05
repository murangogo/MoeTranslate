---
title: Live2D使用
author: azuki
createTime: 2024/03/02 10:48:14
permalink: /docs/uselive2d/
---

::: warning 版权声明

本节所使用的所有的鹿目圆模型均来自手游《魔法纪录》，其中：  
**“鹿目圆（晴着）”**模型会作为基础模型，预置在App中，    
**“鹿目圆（便服）”**模型在此仅作为展示软件功能所用到的模型，不会预置在App中，  
版权归原作者所有，若认为侵权请通过官网的【更多】->【社交媒体】或App中的【关于】->【意见反馈】中联系开发者。

:::

## 一、简介

Live 2D功能是萌译中比较有意思的一个功能，它支持导入模型、切换模型、播放表情、播放动作等操作。

萌译在Live 2D页面预置了一个鹿目圆的模型，您可以借助该模型来了解并使用一些相关的功能，下面的一些介绍也会以该模型为例进行展开。

## 二、导入模型

大部分用户可能只听说过Live 2D，或者见过由Live 2D制作出的各种作品和形象；但并不了解Live 2D的更深入的原理和细节。因此，建议用户在使用导入模型功能前，先阅读[Live 2D文件结构](https://www.moetranslate.top/docs/live2d/fileconstruction/)，在了解Live 2D的基础知识后，再阅读[导入模型](https://www.moetranslate.top/docs/live2d/importmodel/)的方法。

## 三、切换模型

假设我们导入了另一个模型“鹿目圆（便服）”，我们可点击右上角（偏下）的【模型】按钮展开已导入的模型列表。

点击列表中的模型名称，即可切换到目标模型。

<img src="https://img.moetranslate.top/uselive2d_change_model.jpg"/>

在已导入的模型列表中，长按模型名称可以对模型进行【重命名】和【删除】操作。

<img src="https://img.moetranslate.top/uselive2d_rename_model.jpg"/>

## 四、播放表情

在导入模型后，萌译会自动检测模型拥有的表情，并在点击左上角（偏上）的【表情】按钮展开读取到的表情列表。

点击列表中的表情项，即可让模型播放表情。

<img src="https://img.moetranslate.top/uselive2d_play_emotion.jpg"/>

列表中的表情项的默认名称可能不太直观，长按列表中的表情项，可以重命名。

<img src="https://img.moetranslate.top/uselive2d_rename_emotion.jpg"/>

## 五、播放动作

在导入模型后，萌译会自动检测模型拥有的动作，并在点击左上角（偏下）的【动作】按钮展开读取到的动作列表。

点击列表中的动作项，即可让模型播放动作。

::: tip 可能出现的问题

如果点击动作后，萌译出现了<span style="color:#E25A5C; font-weight:bold;">闪退</span>
，请[点击此处](https://www.moetranslate.top/docs/live2d/faq/)查看解决方法。

:::

<img src="https://img.moetranslate.top/uselive2d_play_motion.jpg"/>

列表中的动作项的默认名称可能不太直观，长按列表中的动作项，可以重命名。

<img src="https://img.moetranslate.top/uselive2d_rename_motion.jpg"/>