<template><div><h2 id="概述" tabindex="-1"><a class="header-anchor" href="#概述"><span>概述</span></a></h2>
<p>主题内置的 水印插件，为 全站，或者 单个页面 添加水印。</p>
<p>关联插件：<a href="https://ecosystem.vuejs.press/zh/plugins/features/watermark.html" target="_blank" rel="noopener noreferrer">@vuepress/plugin-watermark</a></p>
<h2 id="使用" tabindex="-1"><a class="header-anchor" href="#使用"><span>使用</span></a></h2>
<CodeTabs id="12" :data='[{"id":".vuepress/config.ts"}]'><template #title0="{ value, isActive }"><VPIcon name="vscode-icons:file-type-typescript"/><span>.vuepress/config.ts</span></template><template #tab0="{ value, isActive }"><div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">import</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> from</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vuepress</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">import</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> from</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vuepress-theme-plume</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#59873A;--shiki-dark:#80A665"> defineUserConfig</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#59873A;--shiki-dark:#80A665">plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    plugins</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      // watermark: true,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      watermark</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        // enabled: false,  // boolean 类型控制是否全局启用</span></span>
<span class="line"><span style="--shiki-light:#59873A;--shiki-dark:#80A665">        enabled</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">page</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> => </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// function 类型 过滤哪些页面启用水印</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        delay</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2F798A;--shiki-dark:#4C9A91">500</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">// 添加水印的延时。以毫秒为单位。</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">         * 是否全屏水印，默认为 `true`，</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">         * 设置为 `false` 时，水印仅在 内容区域中显示。</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">         */</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        fullPage</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">        /** </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">see</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> https://zhensherlock.github.io/watermark-js-plus/zh/config/</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD"> */</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        watermarkOptions</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">          content</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">your watermark</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">          // ...</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">        }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  })</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></template></CodeTabs><h2 id="配置项" tabindex="-1"><a class="header-anchor" href="#配置项"><span>配置项</span></a></h2>
<h3 id="enabled" tabindex="-1"><a class="header-anchor" href="#enabled"><span>enabled</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | ((page: Page) =&gt; boolean)</code></p>
</li>
<li>
<p>默认值： <code v-pre>false</code></p>
</li>
<li>
<p>详情：</p>
<p>指定哪些页面需要添加水印。</p>
<p>拥有 <code v-pre>true</code> 值的页面将会被添加水印。</p>
</li>
</ul>
<h3 id="watermarkoptions" tabindex="-1"><a class="header-anchor" href="#watermarkoptions"><span>watermarkOptions</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>WatermarkOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>undefined</code></p>
</li>
<li>
<p>详情： 配置项请参考 <a href="https://zhensherlock.github.io/watermark-js-plus/zh/config/" target="_blank" rel="noopener noreferrer">watermark-js-plus</a>。</p>
</li>
</ul>
<h4 id="watermarkoptions-parent" tabindex="-1"><a class="header-anchor" href="#watermarkoptions-parent"><span>watermarkOptions.parent</span></a></h4>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>body</code></p>
</li>
<li>
<p>详情：添加水印的父元素选择器。</p>
<p>默认插入到 body 中，可以指定插入到页面的某个元素中。</p>
</li>
</ul>
<h3 id="delay" tabindex="-1"><a class="header-anchor" href="#delay"><span>delay</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>number</code></p>
</li>
<li>
<p>默认值： <code v-pre>500</code></p>
</li>
<li>
<p>详情：添加水印的延时。以毫秒为单位。</p>
<p>延迟时间取决于页面过渡动画时间，可以根据实际情况调整。</p>
<p>如果水印的父元素在切换页面时被重新渲染，那么延迟时间应该比页面过渡时间长一些。</p>
</li>
</ul>
<h2 id="frontmatter" tabindex="-1"><a class="header-anchor" href="#frontmatter"><span>Frontmatter</span></a></h2>
<h3 id="watermark" tabindex="-1"><a class="header-anchor" href="#watermark"><span>watermark</span></a></h3>
<ul>
<li>
<p>类型: <code v-pre>boolean | WatermarkOptions</code></p>
</li>
<li>
<p>详情：</p>
<p>当类型为 <code v-pre>boolean</code> 时，表示是否启用水印。</p>
<p>当类型为 <code v-pre>WatermarkOptions</code> 时，表示当前页面水印配置。</p>
<p>可以参考 <a href="https://zhensherlock.github.io/watermark-js-plus/zh/config/" target="_blank" rel="noopener noreferrer">watermark-js-plus</a> 。</p>
</li>
</ul>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#005CC5;--shiki-light-font-weight:bold;--shiki-dark:#79B8FF;--shiki-dark-font-weight:bold">---</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">watermark:</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">  width: 200</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">  height: 200</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">  content: Your content</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">  opacity: 0.5</span></span>
<span class="line"><span style="--shiki-light:#1C6B48;--shiki-light-font-weight:bold;--shiki-dark:#4D9375;--shiki-dark-font-weight:bold">---</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></div></template>


