<template><div><h2 id="概述" tabindex="-1"><a class="header-anchor" href="#概述"><span>概述</span></a></h2>
<p>为 主题 提供 Markdown 增强功能。</p>
<p>关联插件: <a href="https://github.com/pengzhanbo/vuepress-theme-plume/tree/main/plugins/plugin-md-power" target="_blank" rel="noopener noreferrer">@vuepress-plume/plugin-md-power</a></p>
<h2 id="配置" tabindex="-1"><a class="header-anchor" href="#配置"><span>配置</span></a></h2>
<p>默认配置：</p>
<CodeTabs id="15" :data='[{"id":".vuepress/config.ts"}]'><template #title0="{ value, isActive }"><VPIcon name="vscode-icons:file-type-typescript"/><span>.vuepress/config.ts</span></template><template #tab0="{ value, isActive }"><div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">import</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> from</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vuepress</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">import</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> from</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vuepress-theme-plume</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#59873A;--shiki-dark:#80A665"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#59873A;--shiki-dark:#80A665">plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    plugins</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      markdownPower</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        fileTree</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// :::file-tree  文件树容器</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        plot</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// !!plot!! 隐秘文本</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        icons</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// :[collect:name]:   内联 iconify 图标</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // 默认不启用以下功能，你需要手动开启它们</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // pdf: true, // @[pdf](url)  嵌入 PDF 文件</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // bilibili: true, // @[bilibili](bvid)  嵌入 bilibili 视频</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // youtube: true, // @[youtube](id)  嵌入 youtube 视频</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // codepen: true, // @[codepen](user/slash)  嵌入 codepen</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // replit: true, // @[replit](user/repl-name)  嵌入 Replit</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // codeSandbox: true, // @[codesandbox](id)  嵌入 CodeSandbox</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // jsfiddle: true, // @[jsfiddle](id)  嵌入 jsfiddle</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // caniuse: true, // @[caniuse](feature)  嵌入 caniuse</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // repl: true, // :::go-repl   :::kotlin-repl  :::rust-repl</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // imageSize: true, // 在构建阶段为 图片添加 width/height 属性</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  }),</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></template></CodeTabs><h2 id="功能" tabindex="-1"><a class="header-anchor" href="#功能"><span>功能</span></a></h2>
<h3 id="嵌入-pdf" tabindex="-1"><a class="header-anchor" href="#嵌入-pdf"><span>嵌入 PDF</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>pdf</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">pdf</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">url</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/%E5%B5%8C%E5%85%A5/pdf.html">完整使用文档</RouteLink></p>
<h3 id="iconify-图标" tabindex="-1"><a class="header-anchor" href="#iconify-图标"><span>iconify 图标</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>icons</code> 为 <code v-pre>true</code></p>
<p>得益于 <a href="https://iconify.design/" target="_blank" rel="noopener noreferrer">iconify</a>, 你可以在 Markdown 中使用 iconify <strong>200k+</strong> 的图标</p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">collect:name</span><span style="--shiki-light:#999999;--shiki-dark:#666666">]</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">:</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/markdown/%E5%9B%BE%E6%A0%87.html">完整使用文档</RouteLink></p>
<h3 id="bilibili-视频" tabindex="-1"><a class="header-anchor" href="#bilibili-视频"><span>bilibili 视频</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>bilibili</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">bilibili</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">bvid</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/%E5%B5%8C%E5%85%A5/bilibili.html">完整使用文档</RouteLink></p>
<h3 id="youtube-视频" tabindex="-1"><a class="header-anchor" href="#youtube-视频"><span>youtube 视频</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>youtube</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">youtube</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">id</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/%E5%B5%8C%E5%85%A5/youtube.html">完整使用文档</RouteLink></p>
<h3 id="codepen-代码演示" tabindex="-1"><a class="header-anchor" href="#codepen-代码演示"><span>codePen 代码演示</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>codepen</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">codepen</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">user/slash</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81%E6%BC%94%E7%A4%BA/codepen.html">完整使用文档</RouteLink></p>
<h3 id="codesandbox-代码演示" tabindex="-1"><a class="header-anchor" href="#codesandbox-代码演示"><span>codeSandbox 代码演示</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>codeSandbox</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">codesandbox</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">id</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81%E6%BC%94%E7%A4%BA/codeSandbox.html">完整使用文档</RouteLink></p>
<h3 id="jsfiddle-代码演示" tabindex="-1"><a class="header-anchor" href="#jsfiddle-代码演示"><span>jsfiddle 代码演示</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>jsfiddle</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">jsfiddle</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">id</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81%E6%BC%94%E7%A4%BA/jsFiddle.html">完整使用文档</RouteLink></p>
<h3 id="caniuse-浏览器支持" tabindex="-1"><a class="header-anchor" href="#caniuse-浏览器支持"><span>caniuse 浏览器支持</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>caniuse</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">@</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">caniuse</span><span style="--shiki-light:#999999;--shiki-dark:#666666">](</span><span style="--shiki-light:#393A3490;--shiki-light-text-decoration:underline;--shiki-dark:#DEDCD590;--shiki-dark-text-decoration:underline">feature</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/markdown/caniuse.html">完整使用文档</RouteLink></p>
<h3 id="repl-代码演示容器" tabindex="-1"><a class="header-anchor" href="#repl-代码演示容器"><span>Repl 代码演示容器</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>repl</code> 为 <code v-pre>true</code></p>
<p>支持在线运行 Rust、Golang、Kotlin 代码，还支持在线编辑。</p>
<p>或者开启部分功能，如下所示</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#59873A;--shiki-dark:#80A665"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#59873A;--shiki-dark:#80A665">plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    plugins</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      markdownPower</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        repl</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">          rust</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">          go</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">          kotlin</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">        },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  })</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">::: rust-repl</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">```</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">rust</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// rust code</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">```</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">:::</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">::: go-repl</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">```</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">go</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// go code</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">```</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">:::</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">::: kotlin-repl</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">```</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">kotlin</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">// kotlin code</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">```</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">:::</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>请查看完整使用文档:</p>
<ul>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81%E6%BC%94%E7%A4%BA/rust.html">代码演示 &gt; Rust</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81%E6%BC%94%E7%A4%BA/golang.html">代码演示 &gt; Golang</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81%E6%BC%94%E7%A4%BA/kotlin.html">代码演示 &gt; Kotlin</RouteLink></li>
</ul>
<h3 id="plot-隐秘文本" tabindex="-1"><a class="header-anchor" href="#plot-隐秘文本"><span>Plot 隐秘文本</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>plot</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">!!content!!</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/markdown/%E9%9A%90%E7%A7%98%E6%96%87%E6%9C%AC.html">完整使用文档</RouteLink></p>
<h3 id="文件树" tabindex="-1"><a class="header-anchor" href="#文件树"><span>文件树</span></a></h3>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>fileTree</code> 为 <code v-pre>true</code></p>
<p><strong>语法:</strong></p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">::: file-tree</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A65E2B;--shiki-dark:#D4976C">-</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE"> folder1</span></span>
<span class="line"><span style="--shiki-light:#A65E2B;--shiki-dark:#D4976C">  -</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE"> file1.md</span></span>
<span class="line"><span style="--shiki-light:#A65E2B;--shiki-dark:#D4976C">  -</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE"> file2.ts</span></span>
<span class="line"><span style="--shiki-light:#A65E2B;--shiki-dark:#D4976C">  -</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE"> folder2</span></span>
<span class="line"><span style="--shiki-light:#A65E2B;--shiki-dark:#D4976C">    -</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE"> file3.md</span></span>
<span class="line"><span style="--shiki-light:#A65E2B;--shiki-dark:#D4976C">-</span><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE"> folder3</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">:::</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>请查看 <RouteLink to="/notes/guide/markdown/%E6%96%87%E4%BB%B6%E6%A0%91.html">完整使用文档</RouteLink></p>
<h3 id="图片尺寸" tabindex="-1"><a class="header-anchor" href="#图片尺寸"><span>图片尺寸</span></a></h3>
<p>该功能会为 markdown 文件中的 图片引用 添加当前图片的 <code v-pre>width</code> 和 <code v-pre>height</code> 属性。
通过读取 图片的原始尺寸大小，为 图片设置默认的 图片尺寸 和 比例。
从而解决页面在图片加载未完成到完成时，布局闪烁的问题。</p>
<p>插件默认不启用该功能，你需要手动设置 <code v-pre>imageSize</code>：</p>
<ul>
<li>如果 <code v-pre>imageSize</code> 为 <code v-pre>true</code>，则插件仅处理本地图片，等同于 <code v-pre>local</code> 选项；</li>
<li>如果 <code v-pre>imageSize</code> 为 <code v-pre>'local'</code>，则插件仅处理本地图片；</li>
<li>如果 <code v-pre>imageSize</code> 为 <code v-pre>'all'</code>，则插件同时处理本地图片和远程图片。</li>
</ul>
<div class="hint-container important">
<p class="hint-container-title">重要</p>
<p><strong>此功能仅在构建生产包时生效。</strong></p>
<p>请谨慎 使用 <code v-pre>'all'</code> 选项，由于该选项会在 构建生产包时，请求远程图片资源，这会使得构建时间变长。
虽然主题做了优化仅会加载图片 <strong>几 KB</strong> 的数据包 用于分析尺寸，但还是会实际影响构建时间。</p>
</div>
</div></template>


