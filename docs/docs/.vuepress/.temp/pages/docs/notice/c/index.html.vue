<template><div><h2 id="vuepress-配置文件" tabindex="-1"><a class="header-anchor" href="#vuepress-配置文件"><span>VuePress 配置文件</span></a></h2>
<h3 id="概述" tabindex="-1"><a class="header-anchor" href="#概述"><span>概述</span></a></h3>
<p>VuePress 站点的基本配置文件是 <code v-pre>.vuepress/config.js</code> ，但也同样支持 TypeScript 配置文件。
你可以使用 <code v-pre>.vuepress/config.ts</code> 来得到更好的类型提示。</p>
<p>具体而言，VuePress 对于配置文件的路径有着约定（按照优先顺序）：</p>
<p>当前工作目录 <code v-pre>cwd</code> 下：</p>
<ul>
<li><code v-pre>vuepress.config.ts</code></li>
<li><code v-pre>vuepress.config.js</code></li>
<li><code v-pre>vuepress.config.mjs</code></li>
</ul>
<p>源文件目录 <code v-pre>sourceDir</code> 下：</p>
<ul>
<li><code v-pre>.vuepress/config.ts</code></li>
<li><code v-pre>.vuepress/config.js</code></li>
<li><code v-pre>.vuepress/config.mjs</code></li>
</ul>
<p>基础配置文件示例：</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span>import { viteBundler } from '@vuepress/bundler-vite'</span></span>
<span class="line"><span>import { defineUserConfig } from 'vuepress'</span></span>
<span class="line"><span>import { plumeTheme } from 'vuepress-theme-plume'</span></span>
<span class="line"><span></span></span>
<span class="line"><span>export default defineUserConfig({</span></span>
<span class="line"><span>  bundler: viteBundler(),</span></span>
<span class="line"><span></span></span>
<span class="line"><span>  theme: plumeTheme({</span></span>
<span class="line"><span>    // 在这里配置主题</span></span>
<span class="line"><span>  }),</span></span>
<span class="line"><span></span></span>
<span class="line"><span>  lang: 'zh-CN',</span></span>
<span class="line"><span>  title: '你好， VuePress ！',</span></span>
<span class="line"><span>  description: '这是我的第一个 VuePress 站点',</span></span>
<span class="line"><span>})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h3 id="类型" tabindex="-1"><a class="header-anchor" href="#类型"><span>类型</span></a></h3>
<p>在 VuePress 中，有三种配置类型:</p>
<ul>
<li>站点配置: 这是你在 配置文件 中直接导出的对象</li>
<li>主题配置: 传递给 <code v-pre>plumeTheme</code> 的对象参数</li>
<li>页面配置: 由在页面顶部基于 YAML 语法的 Frontmatter 提供</li>
</ul>
<h2 id="主题配置文件" tabindex="-1"><a class="header-anchor" href="#主题配置文件"><span>主题配置文件</span></a></h2>
<h3 id="概述-1" tabindex="-1"><a class="header-anchor" href="#概述-1"><span>概述</span></a></h3>
<p>一般我们使用 <code v-pre>.vuepress/config.js</code> 或者 <code v-pre>.vuepress/config.ts</code> 来配置主题。</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span>import { defineUserConfig } from 'vuepress'</span></span>
<span class="line"><span>import { plumeTheme } from 'vuepress-theme-plume'</span></span>
<span class="line"><span></span></span>
<span class="line"><span>export default defineUserConfig({</span></span>
<span class="line"><span>  theme: plumeTheme({</span></span>
<span class="line"><span>    // 在这里配置主题</span></span>
<span class="line"><span>  }),</span></span>
<span class="line"><span>})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>但是当我们已经启动了 VuePress 服务，对该文件的修改会导致 VuePres 服务重启，然后站点进行全量刷新，
这可能需要等待一段时间才能恢复， 如果你的站点内容不多还能够接受，
而对于一些较大的站点，可能需要等待漫长的时间。</p>
<p>特别是当我们频繁修改，或者修改的间隔较短时，很容易使 VuePress 服务 崩溃，我们不得不手动重启。</p>
<p><strong>这给我们在编写站点内容时带来的极大的不便。</strong></p>
<p>为了解决这一问题，主题支持在 单独的 主题配置文件中进行配置。</p>
<p><strong>对该文件的修改将通过热更新的方式实时生效。</strong></p>
<h3 id="配置" tabindex="-1"><a class="header-anchor" href="#配置"><span>配置</span></a></h3>
<p>你可以直接在 <a href="#vuepress-%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">VuePress 配置文件</a> 相同的路径下创建一个 <code v-pre>plume.config.js</code> 文件，这样就可以在该文件中进行主题配置。
你也可以使用 TypeScript 来创建一个 <code v-pre>plume.config.ts</code> 文件，以获得更好的类型提示。</p>
<div class="vp-file-tree"><ul>
<FileTreeItem type="folder" :expanded="true" :empty="false"><span class="tree-node folder"><VPIcon name="vscode-icons:folder-type-docs"></VPIcon><span class="name">docs</span></span>
<ul>
<FileTreeItem type="folder" :expanded="true" :empty="false"><span class="tree-node folder"><VPIcon name="vscode-icons:default-folder"></VPIcon><span class="name">.vuepress</span></span>
<ul>
<FileTreeItem type="file" :expanded="false" :empty="true"><span class="tree-node file"><VPIcon name="vscode-icons:file-type-typescript"></VPIcon><span class="name">config.ts</span></span></FileTreeItem>
<FileTreeItem type="file" :expanded="false" :empty="true"><span class="tree-node file"><VPIcon name="vscode-icons:file-type-typescript"></VPIcon><span class="name focus"><strong>plume.config.ts</strong></span></span></FileTreeItem>
</ul>
</FileTreeItem>
</ul>
</FileTreeItem>
</ul>
</div><CodeTabs id="138" :data='[{"id":"plume.config.ts"}]'><template #title0="{ value, isActive }"><VPIcon name="vscode-icons:file-type-typescript"/><span>plume.config.ts</span></template><template #tab0="{ value, isActive }"><div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span>import { defineThemeConfig } from 'vuepress-theme-plume'</span></span>
<span class="line"><span>import navbar from './navbar'</span></span>
<span class="line"><span></span></span>
<span class="line"><span>export default defineThemeConfig({</span></span>
<span class="line"><span>  // 在这里配置主题</span></span>
<span class="line"><span>  profile: {</span></span>
<span class="line"><span>    name: 'Your name',</span></span>
<span class="line"><span>  },</span></span>
<span class="line"><span>  navbar,</span></span>
<span class="line"><span>})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></template></CodeTabs><p>主题提供了 <code v-pre>defineThemeConfig(config)</code> 函数，为主题使用者提供主题配置的类型帮助。
你可以直接在这个文件中配置除了 <code v-pre>plugins</code> 字段外的其他配置。</p>
<h3 id="自定义配置文件路径" tabindex="-1"><a class="header-anchor" href="#自定义配置文件路径"><span>自定义配置文件路径</span></a></h3>
<p>如果你不希望按照 VuePress 默认的配置文件路径管理你的主题配置文件，
你也可以在 VuePress 配置文件中指定自己的主题配置文件路径。</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark has-diff vp-code" v-pre=""><code><span class="line"><span>import path from 'node:path'</span></span>
<span class="line"><span>import { defineUserConfig } from 'vuepress'</span></span>
<span class="line"><span>import { plumeTheme } from 'vuepress-theme-plume'</span></span>
<span class="line"><span></span></span>
<span class="line"><span>export default defineUserConfig({</span></span>
<span class="line"><span>  theme: plumeTheme({</span></span>
<span class="line"><span>    // 在这里定义自己的主题配置文件路径</span></span>
<span class="line diff add"><span>    configFile: path.join(__dirname, 'custom/config.ts'),</span></span>
<span class="line"><span>  }),</span></span>
<span class="line"><span>})</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><div class="hint-container tip">
<p class="hint-container-title">提示</p>
<p>更推荐 使用 主题配置文件 来单独管理 主题配置，你不必再为频繁修改配置而一直等待
VuePress 重启。</p>
</div>
</div></template>


