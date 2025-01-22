<template><div><h2 id="概述" tabindex="-1"><a class="header-anchor" href="#概述"><span>概述</span></a></h2>
<p>主题内置的代码高亮插件， 对代码块进行代码高亮。</p>
<p>主题 使用 <a href="https://github.com/shikijs/shiki" target="_blank" rel="noopener noreferrer">Shiki</a> 在 Markdown 代码块中使用彩色文本实现语法高亮。
Shiki 支持多种编程语言。</p>
<p>在 Shiki 的代码仓库中，可以找到 <a href="https://shiki.style/languages" target="_blank" rel="noopener noreferrer">合法的编程语言列表</a> 。</p>
<p>关联插件： <a href="https://github.com/pengzhanbo/vuepress-theme-plume/tree/main/plugins/plugin-shikiji" target="_blank" rel="noopener noreferrer">@vuepress-plume/plugin-shikiji</a></p>
<details class="hint-container details"><summary>为什么不用 官方的 @vuepress/plugin-shiki ？</summary>
<p>你可以认为本插件是 官方 <a href="https://ecosystem.vuejs.press/zh/plugins/shiki.html" target="_blank" rel="noopener noreferrer">@vuepress/plugin-shiki</a> 的
一个分支版本，但本插件更为激进，支持更多新的特性。</p>
<p>同时，我也是 <a href="https://ecosystem.vuejs.press/zh/plugins/shiki.html" target="_blank" rel="noopener noreferrer">@vuepress/plugin-shiki</a> 的主要维护者之一
，在 <code v-pre>@vuepress-plume/plugin-shikiji</code> 插件中新增的试验性的新特性，会在未来合适的时候合并到 官方插件中。</p>
</details>
<h2 id="特性" tabindex="-1"><a class="header-anchor" href="#特性"><span>特性</span></a></h2>
<ul>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/%E7%89%B9%E6%80%A7%E6%94%AF%E6%8C%81.html#%E5%9C%A8%E4%BB%A3%E7%A0%81%E5%9D%97%E4%B8%AD%E5%AE%9E%E7%8E%B0%E8%A1%8C%E9%AB%98%E4%BA%AE">代码行高亮</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/%E7%89%B9%E6%80%A7%E6%94%AF%E6%8C%81.html#%E4%BB%A3%E7%A0%81%E5%9D%97%E4%B8%AD%E8%81%9A%E7%84%A6">代码聚焦</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/%E7%89%B9%E6%80%A7%E6%94%AF%E6%8C%81.html#%E4%BB%A3%E7%A0%81%E5%9D%97%E4%B8%AD%E7%9A%84%E9%A2%9C%E8%89%B2%E5%B7%AE%E5%BC%82">代码对比差异</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/%E7%89%B9%E6%80%A7%E6%94%AF%E6%8C%81.html#%E9%AB%98%E4%BA%AE-%E9%94%99%E8%AF%AF-%E5%92%8C-%E8%AD%A6%E5%91%8A">代码高亮“错误”和“警告”</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/%E7%89%B9%E6%80%A7%E6%94%AF%E6%8C%81.html#%E4%BB%A3%E7%A0%81%E5%9D%97%E4%B8%AD-%E8%AF%8D%E9%AB%98%E4%BA%AE">代码词高亮</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/%E7%89%B9%E6%80%A7%E6%94%AF%E6%8C%81.html#%E6%8A%98%E5%8F%A0%E4%BB%A3%E7%A0%81%E5%9D%97">代码块折叠</RouteLink></li>
<li><RouteLink to="/notes/guide/%E4%BB%A3%E7%A0%81/twoslash.html#twoslash">twoslash</RouteLink> ：在代码块内提供内联类型提示。</li>
</ul>
<h2 id="配置" tabindex="-1"><a class="header-anchor" href="#配置"><span>配置</span></a></h2>
<p>默认配置：</p>
<CodeTabs id="69" :data='[{"id":".vuepress/config.ts"}]'><template #title0="{ value, isActive }"><VPIcon name="vscode-icons:file-type-typescript"/><span>.vuepress/config.ts</span></template><template #tab0="{ value, isActive }"><div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">import</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> from</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vuepress</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">import</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> from</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vuepress-theme-plume</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#59873A;--shiki-dark:#80A665"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#59873A;--shiki-dark:#80A665">plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    plugins</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      shiki</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: { </span><span style="--shiki-light:#998418;--shiki-dark:#B8A965">light</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vitesse-light</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#998418;--shiki-dark:#B8A965">dark</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vitesse-dark</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  }),</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></template></CodeTabs><h3 id="theme" tabindex="-1"><a class="header-anchor" href="#theme"><span>theme</span></a></h3>
<ul>
<li>类型: <code v-pre>string | { light: string, dark: string }</code></li>
<li>默认值: <code v-pre>{ light: 'vitesse-light', dark: 'vitesse-dark' }</code></li>
</ul>
<p>代码高亮主题，支持 浅色/暗色 双主题。</p>
<p>可在 支持的 <a href="https://shiki.style/themes" target="_blank" rel="noopener noreferrer">主题列表</a> 中选择你喜欢的主题。</p>
<h3 id="languages" tabindex="-1"><a class="header-anchor" href="#languages"><span>languages</span></a></h3>
<ul>
<li>类型: <code v-pre>string[]</code></li>
<li>默认值: <code v-pre>[]</code></li>
</ul>
<p>需要高亮的编程语言， 例如 <code v-pre>javascript</code>、<code v-pre>typescript</code>、<code v-pre>python</code>、<code v-pre>java</code>、<code v-pre>c++</code>、<code v-pre>c#</code>等。
默认会根据代码块的语言自动识别。</p>
<p>在 Shiki 的代码仓库中，可以找到 <a href="https://shiki.style/languages" target="_blank" rel="noopener noreferrer">合法的编程语言列表</a> 。</p>
<h3 id="defaulthighlightlang" tabindex="-1"><a class="header-anchor" href="#defaulthighlightlang"><span>defaultHighlightLang</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>text</code></li>
</ul>
<p>默认高亮的编程语言。当代码块未指定语言时使用。</p>
<h3 id="linenumbers" tabindex="-1"><a class="header-anchor" href="#linenumbers"><span>lineNumbers</span></a></h3>
<ul>
<li>类型：<code v-pre>boolean | number</code></li>
<li>默认值： <code v-pre>true</code></li>
</ul>
<p>是否显示行号。</p>
<p><code v-pre>true</code>： 显示行号<br>
<code v-pre>false</code>： 不显示行号<br>
<code v-pre>number</code>： 指定需要显式代码行号的最小行数。</p>
<h3 id="copycode" tabindex="-1"><a class="header-anchor" href="#copycode"><span>copyCode</span></a></h3>
<ul>
<li>类型: <code v-pre>boolean | CopyCodeOptions</code></li>
<li>默认值： <code v-pre>true</code></li>
</ul>
<p>是否允许复制代码。启用时，会在代码块右侧显示复制按钮。</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> CopyCodeOptions</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 复制成功后提示文本持续时间</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> 2000</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  duration</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 多语言配置</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  locales</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    [</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">localePath</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666">]: {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       * 复制按钮标题</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">Copy code</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">      title</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       * 复制成功提示</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">Copied</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">       */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">      copied</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h3 id="twoslash" tabindex="-1"><a class="header-anchor" href="#twoslash"><span>twoslash</span></a></h3>
<ul>
<li>类型: <code v-pre>boolean</code></li>
<li>默认值: <code v-pre>false</code></li>
</ul>
<p>实验性功能，是否启用 对 <code v-pre>typescript</code> 和 <code v-pre>vue</code> 语言的 类型提示 支持。</p>
<p>查看 <RouteLink to="/guide/markdown/experiment/#twoslash">twoslash</RouteLink> 了解更多信息。</p>
<h3 id="whitespace" tabindex="-1"><a class="header-anchor" href="#whitespace"><span>whitespace</span></a></h3>
<ul>
<li>类型: <code v-pre>boolean | 'all' | 'boundary' | 'trailing'</code></li>
<li>默认值: <code v-pre>false</code></li>
</ul>
<p>将空白字符（Tab 和空格）渲染为单独的标签（具有 tab 或 space 类名）。</p>
<p>效果：</p>
<!-- @include: ../../snippet/whitespace.snippet.md{18-24} -->
<h3 id="collapselines" tabindex="-1"><a class="header-anchor" href="#collapselines"><span>collapseLines</span></a></h3>
<ul>
<li>类型: <code v-pre>boolean | number</code></li>
<li>默认值： <code v-pre>false</code></li>
</ul>
<p>将代码块折叠到指定行数。</p>
<h3 id="codetransformers" tabindex="-1"><a class="header-anchor" href="#codetransformers"><span>codeTransformers</span></a></h3>
<ul>
<li>类型: <code v-pre>ShikiTransformer[]</code></li>
<li>默认值: <code v-pre>[]</code></li>
</ul>
<p>代码转换器， 查看 <a href="https://shiki.style/guide/transformers" target="_blank" rel="noopener noreferrer">shiki transformers</a> 了解更多信息。</p>
</div></template>


