---
title: Live2D文件结构
author: azuki
createTime: 2024/03/02 10:48:14
permalink: /docs/live2d/fileconstruction/
---

::: warning 版权声明

本节所使用的鹿目圆模型来自手游《魔法纪录》，其中：  
“**鹿目圆（晴着）**”模型会作为基础模型，预置在App中，    
版权归原作者所有，若认为侵权请通过官网的【更多】->【社交媒体】或App中的【关于】->【意见反馈】中联系开发者。

:::

## 一、简介

一个角色的Live 2D模型并不是由单个文件组成的，而是由若干文件共同构成，下面以App中预置的“鹿目圆（晴着）”为例，列出一个常见的Live 2D模型的文件树。

::: file-tree

- exp
  - mtn_ex_010.exp3.json
  - mtn_ex_011.exp3.json
  - ...
- model.1024
  - texture_00.png
  - texture_01.png
  - ...
- mtn
  - motion_000.motion3.json
  - motion_001.motion3.json
  - ...
- model.moc3
- model.model3.json
- model.physics3.json
- model.pose3.json
- params.json

:::

下面讲解一下每个文件/文件夹的作用。

## 二、文件夹及文件

### **文件夹**

一般来说，一个Live 2D模型至少要有1个文件夹，**不同模型之间起相同作用的文件夹的名称不一定相同**，文件夹的作用分别是：

- **贴图文件夹（必须）**：如上面文件树的model.1024文件夹，表示贴图的分辨率是1024x1024像素，一般里面存放的都是png文件；
- **表情文件夹**：如上面文件树的exp文件夹（expression），一般里面存放的都是json文件；
- **动作文件夹**：如上面文件树的mtn文件夹（motion），一般里面存放的都是json文件；
- **声音文件夹**：这个文件夹比较少见，一般命名为sounds，通常情况下其中存放的都是wav文件。

::: tip 进一步说明

当然，贴图文件也可以单独放置，不放进文件夹，但一般来说将贴图放在文件夹中是更规范的操作。

:::

### **文件**

一般来说，一个Live 2D模型至少要有2个独立文件（不在上面4个文件夹中的外层文件），文件的作用分别是：

- **.moc3（必须）**：核心模型文件，存储模型的网格、变形数据；
- **.model3.json（必须）**：模型配置文件，描述模型的基本信息，定义了moc3文件、贴图路径、动作、表情、物理效果等；
- **.physic3.json**：定义模型的物理效果，如头发和衣服的摆动；
- **.pose3.json**：定义模型的姿势或特定的姿态，防止模型姿势错乱；
- **.userdata3.json**：用户数据文件，存储开发者自定义的数据，如碰撞区域、热点检测（点击特定部位触发事件）等；
- **.cdi3.json**：很少出现，其为Cubism Display Information文件，存储模型在Live2D Viewer中的显示设置信息，更多用于Live2D Viewer预览，而不是出现在导出文件中。

上方文件树中的`params.json`并不是规范的Live 2D文件形式，事实上该文件是用于《魔法纪录》游戏识别模型信息的，和Live 2D的联系不大。