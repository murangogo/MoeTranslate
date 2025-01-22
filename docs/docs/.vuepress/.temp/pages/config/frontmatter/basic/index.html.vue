<template><div><h2 id="概述" tabindex="-1"><a class="header-anchor" href="#概述"><span>概述</span></a></h2>
<p>通用 Frontmatter 配置，适用于 所有的文章。</p>
<p>示例：</p>
<div class="language-md line-numbers-mode" data-ext="md" data-title="md"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#005CC5;--shiki-light-font-weight:bold;--shiki-dark:#79B8FF;--shiki-dark-font-weight:bold">---</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">title: 标题</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">createTime: 2024/03/02 20:01:09</span></span>
<span class="line"><span style="--shiki-light:#393A34;--shiki-dark:#DBD7CAEE">permalink: /config/frontmatter/basic/</span></span>
<span class="line"><span style="--shiki-light:#1C6B48;--shiki-light-font-weight:bold;--shiki-dark:#4D9375;--shiki-dark-font-weight:bold">---</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h2 id="配置" tabindex="-1"><a class="header-anchor" href="#配置"><span>配置</span></a></h2>
<h3 id="pagelayout" tabindex="-1"><a class="header-anchor" href="#pagelayout"><span>pageLayout</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | 'home' | 'doc' | 'custom' | 'page' | 'friends' | string</code></p>
</li>
<li>
<p>默认值： <code v-pre>doc</code></p>
</li>
<li>
<p>详情：</p>
<p>页面布局方式。</p>
<ul>
<li><code v-pre>'home'</code>: 主页布局</li>
<li><code v-pre>'doc'</code>: 文章页布局，包括左侧侧边栏、文章内容、右侧侧边栏、导航栏、评论等</li>
<li><code v-pre>'page'</code>: 仅包含导航栏 和 MD 文件内容，可以使用此布局方式自定义页面内容</li>
<li><code v-pre>'friends'</code>: 友链页布局</li>
<li><code v-pre>'custom'</code>: 仅包含 MD 文件内容，可以使用此布局方式完全自定义页面内容</li>
<li><code v-pre>false</code>: 等同于 <code v-pre>custom</code></li>
<li><code v-pre>string</code>: 可以传入一个全局组件组件名，该组件将被应用为布局组件</li>
</ul>
</li>
</ul>
<h3 id="pageclass" tabindex="-1"><a class="header-anchor" href="#pageclass"><span>pageClass</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>自定义页面额外的 类名。</p>
</li>
</ul>
<h3 id="title" tabindex="-1"><a class="header-anchor" href="#title"><span>title</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>文章标题。</p>
<p>主题会在文件创建时，自动填充 当前文件名作为 文章标题。</p>
</li>
</ul>
<h3 id="badge" tabindex="-1"><a class="header-anchor" href="#badge"><span>badge</span></a></h3>
<ul>
<li>类型： <code v-pre>string | { text: string, type?: 'info' | 'tip' | 'warning' | 'danger' }</code></li>
</ul>
<p>在文章标题右侧显示 徽章。</p>
<h3 id="createtime" tabindex="-1"><a class="header-anchor" href="#createtime"><span>createTime</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>文章创建时间。</p>
<p>主题会在文件创建时，自动填充 当前时间作为 文章创建时间。</p>
</li>
</ul>
<h3 id="permalink" tabindex="-1"><a class="header-anchor" href="#permalink"><span>permalink</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>文章永久链接。</p>
<p>主题会在文件创建时:</p>
<ul>
<li>博客类型的文章，自动填充 <code v-pre>/article/</code> + <code v-pre>nanoid 生成的 8 位随机字符串</code> 作为 文章永久链接</li>
<li>notes 目录下的文章，会根据 notes 的配置，自动填充  文章永久链接</li>
</ul>
</li>
</ul>
<h3 id="externallinkicon" tabindex="-1"><a class="header-anchor" href="#externallinkicon"><span>externalLinkIcon</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章内的 外部链接是否显示 外部链接图标。</p>
</li>
</ul>
<h3 id="backtotop" tabindex="-1"><a class="header-anchor" href="#backtotop"><span>backToTop</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 回到顶部 的按钮。</p>
</li>
</ul>
<h3 id="comments" tabindex="-1"><a class="header-anchor" href="#comments"><span>comments</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 可评论。 仅在启用了评论功能的情况下生效。</p>
</li>
</ul>
<h3 id="aside" tabindex="-1"><a class="header-anchor" href="#aside"><span>aside</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | 'left'</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 右侧边栏。</p>
<p>当设置为 <code v-pre>'left'</code> 时，右侧边栏将显示在左侧。</p>
</li>
</ul>
<h3 id="navbar" tabindex="-1"><a class="header-anchor" href="#navbar"><span>navbar</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 导航栏。</p>
</li>
</ul>
<h3 id="outline" tabindex="-1"><a class="header-anchor" href="#outline"><span>outline</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | number | [number, number] | 'deep'</code></p>
</li>
<li>
<p>默认值： <code v-pre>[2, 3]</code></p>
</li>
<li>
<p>详情：</p>
<p>要显示的标题级别。</p>
<p>单个数字表示只显示该级别的标题。</p>
<p>如果传递的是一个元组，第一个数字是最小级别，第二个数字是最大级别。</p>
<p><code v-pre>'deep'</code> 与 <code v-pre>[2, 6]</code> 相同，将显示从 <code v-pre>&lt;h2&gt;</code> 到 <code v-pre>&lt;h6&gt;</code> 的所有标题。</p>
</li>
</ul>
<h3 id="prev" tabindex="-1"><a class="header-anchor" href="#prev"><span>prev</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string | { text: string, link: string, icon?: string }</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>自定义当前文章的上一篇文章。</p>
<ul>
<li>博客类型的文章，主题根据时间排序，自动填充上一篇文章的标题和链接。</li>
<li>notes 类型的文章，主题根据 note sidebar 配置，自动填充上一篇文章的标题和链接。</li>
</ul>
</li>
</ul>
<h3 id="next" tabindex="-1"><a class="header-anchor" href="#next"><span>next</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string | { text: string, link: string, icon?: string }</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>自定义当前文章的下一篇文章。</p>
<ul>
<li>博客类型的文章，主题根据时间排序，自动填充下一篇文章的标题和链接。</li>
<li>notes 类型的文章，主题根据 note sidebar 配置，自动填充下一篇文章的标题和链接。</li>
</ul>
</li>
</ul>
<h3 id="readingtime" tabindex="-1"><a class="header-anchor" href="#readingtime"><span>readingTime</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 阅读时长。</p>
</li>
</ul>
<h3 id="lastupdated" tabindex="-1"><a class="header-anchor" href="#lastupdated"><span>lastUpdated</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 最后更新时间。 最后更新时间 根据 git 提交时间自动填充。</p>
</li>
</ul>
<h3 id="contributors" tabindex="-1"><a class="header-anchor" href="#contributors"><span>contributors</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true | string | string[]</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 贡献者。 贡献者 根据 git 提交者自动填充。</p>
<p>如果您的文章来源于第三方， git 提交不能完整列出所有的作者，您可以在此处补充贡献者。</p>
<p>额外信息补充请查看 <RouteLink to="/notes/guide/%E5%8A%9F%E8%83%BD/%E6%96%87%E7%AB%A0%E8%B4%A1%E7%8C%AE%E8%80%85.html#info">contributors</RouteLink></p>
</li>
</ul>
<h3 id="changelog" tabindex="-1"><a class="header-anchor" href="#changelog"><span>changelog</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>false</code></p>
</li>
<li>
<p>详情：</p>
<p>当前页面是否显示页面变更历史。</p>
</li>
</ul>
<h3 id="editlink" tabindex="-1"><a class="header-anchor" href="#editlink"><span>editLink</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>当前文章是否 显示 文章编辑 按钮。</p>
</li>
</ul>
<h3 id="watermark" tabindex="-1"><a class="header-anchor" href="#watermark"><span>watermark</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | WatermarkOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>undefined</code> 主题不启用水印，或不启用全局水印时，默认值为 <code v-pre>false</code>，启用全局水印则为 <code v-pre>true</code>。</p>
</li>
<li>
<p>详情：
配置当前文章 水印。</p>
<p>当类型为 boolean 时，表示是否启用水印。</p>
<p>当类型为 WatermarkOptions 时，表示当前页面水印配置。</p>
<p>可以参考 <a href="https://zhensherlock.github.io/watermark-js-plus/zh/config/" target="_blank" rel="noopener noreferrer">watermark-js-plus</a> 。</p>
</li>
</ul>
</div></template>


