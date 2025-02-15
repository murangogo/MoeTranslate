---
title: 切换UA
author: azuki
createTime: 2024/03/02 10:48:14
permalink: /docs/notice/ua/
---

手机端的不少浏览器都支持**切换UA**的功能，这里开发者使用的是[X浏览器](https://www.xbext.com/)，其他浏览器的切换过程也大同小异。

## 一、不切换UA展示

这里我们以访问**百度翻译开放平台**为例，先看看不切换UA是什么样子的。

打开X浏览器，在地址栏输入**百度翻译开放平台**的地址：

```md:no-line-numbers
https://api.fanyi.baidu.com/
```

访问后发现，如果想要进入API平台，会**提示去电脑端访问**。

<img src="https://img.moetranslate.top/ua_step_1.jpg"/>

## 二、切换UA

如果这时我们手头没有电脑，就可以使用**切换UA**的方法来访问桌面版网站。

::: tip 注意事项

在手机端通过切换UA来请求桌面版网站并不是万能的。即便切换后显示的是桌面版页面，有些网站的功能由于兼容性问题可能仍然无法正常使用，还是建议尽量使用电脑访问相应的网站。

:::

在X浏览器中，点击下方的<Icon name="mingcute:menu-fill" />，在弹出的菜单中**向左划到下一页**，点击**切换UA**，在弹出的选项中选择“桌面”即可。

<img src="https://img.moetranslate.top/ua_step_2.jpg"/>

## 三、访问网站

接下来我们再次在地址栏输入**百度翻译开放平台**的地址：

```md:no-line-numbers
https://api.fanyi.baidu.com/
```

访问后发现，网站已经变成桌面版的布局和样式了，接下来可以正常进入API平台了。

<img src="https://img.moetranslate.top/ua_step_3.jpg"/>