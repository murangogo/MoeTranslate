<template><div><h2 id="基础配置" tabindex="-1"><a class="header-anchor" href="#基础配置"><span>基础配置</span></a></h2>
<h3 id="configfile" tabindex="-1"><a class="header-anchor" href="#configfile"><span>configFile</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>自定义主题配置文件的路径。</p>
<p>查看 <RouteLink to="/notes/docs/live2d/%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 了解更多。</p>
</li>
</ul>
<h3 id="plugins" tabindex="-1"><a class="header-anchor" href="#plugins"><span>plugins</span></a></h3>
<ul>
<li>
<p>类型：<code v-pre>PlumeThemePluginOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>{}</code></p>
</li>
<li>
<p>详情：</p>
<p>对主题内部使用的插件进行自定义配置。</p>
<p>主题使用的插件默认已进行了配置，大多数情况下您不需要进行修改，如果需要使用到细致的定制化，请查阅
<RouteLink to="/notes/docs/live2d/plugins/">此文档</RouteLink></p>
<div class="hint-container warning">
<p class="hint-container-title">该字段不支持在 <RouteLink to="%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
</div>
</li>
</ul>
<h3 id="hostname" tabindex="-1"><a class="header-anchor" href="#hostname"><span>hostname</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>部署站点域名。</p>
<p>当 <code v-pre>hostname</code> 配置为有效域名时，主题将会生成 <code v-pre>sitemap</code> 和 <code v-pre>seo</code> 相关的内容。</p>
<div class="hint-container warning">
<p class="hint-container-title">该字段不支持在 <RouteLink to="%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
</div>
</li>
</ul>
<h3 id="blog" tabindex="-1"><a class="header-anchor" href="#blog"><span>blog</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | BlogOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>{ link: '/blog/',  include: ['**/*.md'], exclude: [] }</code></p>
</li>
<li>
<p>详情：</p>
<p>博客配置。</p>
</li>
</ul>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> BlogOptions</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * blog list link</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">/blog/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  link</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 在 `{sourceDir}` 目录中，通过 glob string 配置包含文件</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> -</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD"> ['**\*.md']</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  include</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 在 `{sourceDir}` 目录中，通过 glob string 配置排除的文件</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> -</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD"> ['.vuepress/', 'node_modules/']</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  exclude</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 分页配置</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * - `false` - 不启用分页</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * - `number` - 每页显示的文章数量</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  pagination</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">false</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">    /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">     * 每页显示的文章数量</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">     * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> 10</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">     */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">    perPage</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  }</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否启用标签页</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> true</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  tags</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 自定义标签页链接</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">/blog/tags/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  tagsLink</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 标签颜色主题</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * - `colored`： 彩色标签，不同标签颜色不同</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * - `brand`: 使用主题颜色作为标签颜色</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * - `gray`: 使用 灰色 作为标签颜色</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">colored</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  tagsTheme</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">colored</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">gray</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">brand</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否启用归档页</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> true</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  archives</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 自定义归档页链接</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">/blog/archives/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  archivesLink</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否启用分类页</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> true</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  categories</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 自定义分类页链接</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">/blog/categories/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  categoriesLink</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 分类页展开深度</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">deep</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  categoriesExpand</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">deep</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 文章分类列表转换函数，比如排除不需要的一级分类</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">param</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> categories</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD"> 分类列表</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">returns</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD"> 返回一个新的分类列表</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#59873A;--shiki-dark:#80A665">  categoriesTransform</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: (</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">categories</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">PageCategoryData</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]) => </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">PageCategoryData</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 博客文章封面图</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 配置封面图的位置，支持 `'left'`、`'right'`、`'top'`、`'top-inside'`</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">right</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  postCover</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">BlogPostCoverLayout</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">Omit</span><span style="--shiki-light:#999999;--shiki-dark:#666666">&#x3C;</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">BlogPostCover</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">url</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">></span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">type</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> BlogPostCoverLayout</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> =</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">left</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">right</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">odd-left</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">odd-right</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">top</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> BlogPostCover</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 封面图链接地址，只能使用 绝对路径 以及 远程图片地址</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  url</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 博客文章封面图的位置</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  layout</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">BlogPostCoverLayout</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 博客文章封面图的比例</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">4:3</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  ratio</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">`</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">${</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">}</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">:</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">${</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">}</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">`</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 封面图的宽度, 仅在 layout 为 'left' 或 'right' 时生效</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> 240</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  width</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">number</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否使用紧凑模式，紧凑模式下，封面图紧贴容器边缘</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> false</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  compact</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h3 id="article" tabindex="-1"><a class="header-anchor" href="#article"><span>article</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>/article/</code></li>
<li>详情： 文章链接前缀</li>
</ul>
<h3 id="autofrontmatter" tabindex="-1"><a class="header-anchor" href="#autofrontmatter"><span>autoFrontmatter</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | AutoFrontmatter</code></p>
</li>
<li>
<p>详情：</p>
<p>是否为 markdown 文件自动添加 frontmatter 配置</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> AutoFrontmatter</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * glob 匹配，被匹配的文件将会自动生成 frontmatter</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> ['</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">**\/*.md']</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  include</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * glob 匹配，被匹配的文件将不会自动生成 frontmatter</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  exclude</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否自动生成 permalink</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> true</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  permalink</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否自动生成 createTime</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 默认读取 文件创建时间，`createTitme` 比 vuepress 默认的 `date` 时间更精准到秒</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  createTime</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否自动生成 title</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 默认读取文件名作为标题</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  title</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
</ul>
<h3 id="cache" tabindex="-1"><a class="header-anchor" href="#cache"><span>cache</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | 'memory' | 'filesystem'</code></p>
</li>
<li>
<p>默认值： <code v-pre>filesystem</code></p>
</li>
<li>
<p>详情：</p>
<p>是否启用 编译缓存，或配置缓存方式</p>
<p>此配置项用于解决 VuePress 启动速度慢的问题，在首次启动服务时，对编译结果进行缓存，二次启动时
直接读取缓存，跳过编译，从而加快启动速度。</p>
<ul>
<li><code v-pre>false</code>：禁用 缓存</li>
<li><code v-pre>'memory'</code>：使用内存缓存，此方式可获得更快的启动速度，但随着项目文件数量增加，内存占用会增加，
适合文章数量较少的项目使用</li>
<li><code v-pre>'filesystem'</code>：使用文件系统缓存，此方式可获得相对快且稳定的启动速度，更适合内容多的项目使用</li>
</ul>
<div class="hint-container warning">
<p class="hint-container-title">注意</p>
<p>该字段不支持在 <RouteLink to="/notes/docs/live2d/%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
<p>为了使缓存能够生效，您应该 <strong>删除</strong> <code v-pre>package.json</code> 中 <code v-pre>vuepress dev</code> 开发服务启动脚本中的 <code v-pre>--clean-cache</code> 参数。</p>
</div>
</li>
</ul>
<h3 id="docsrepo" tabindex="-1"><a class="header-anchor" href="#docsrepo"><span>docsRepo</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>''</code></li>
<li>详情： 文档仓库配置, 用于生成 <code v-pre>Edit this page</code> 链接。</li>
</ul>
<h3 id="docsbranch" tabindex="-1"><a class="header-anchor" href="#docsbranch"><span>docsBranch</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>''</code></li>
<li>详情： 文档仓库分支配置，用于生成 <code v-pre>Edit this page</code> 链接。</li>
</ul>
<h3 id="docsdir" tabindex="-1"><a class="header-anchor" href="#docsdir"><span>docsDir</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>''</code></li>
<li>详情： 文档仓库目录配置，用于生成 <code v-pre>Edit this page</code> 链接。</li>
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
<p>详情： 是否启用 编辑链接</p>
<div class="hint-container warning">
<p class="hint-container-title">该字段不支持在 <RouteLink to="%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
</div>
</li>
</ul>
<h3 id="lastupdated" tabindex="-1"><a class="header-anchor" href="#lastupdated"><span>lastUpdated</span></a></h3>
<ul>
<li>类型： <code v-pre>false | LastUpdatedOptions</code></li>
<li>默认值： <code v-pre>{ formatOptions: { dateStyle: 'short', timeStyle: 'short' } }</code></li>
<li>详情： 最后更新时间</li>
</ul>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> LastUpdatedOptions</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 设置最后更新时间格式的选项。</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">see</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat#using_options</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * { dateStyle: 'short', timeStyle: 'short' }</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  formatOptions</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">Intl</span><span style="--shiki-light:#999999;--shiki-dark:#666666">.</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">DateTimeFormatOptions</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> &#x26; { </span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">forceLocale</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><div class="hint-container warning">
<p class="hint-container-title">该字段不支持在 <RouteLink to="%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
</div>
<h3 id="contributors" tabindex="-1"><a class="header-anchor" href="#contributors"><span>contributors</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | ContributorsOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情： 是否显示贡献者</p>
<p>更多配置请参考 <RouteLink to="/notes/docs/guide/%E5%8A%9F%E8%83%BD/%E6%96%87%E7%AB%A0%E8%B4%A1%E7%8C%AE%E8%80%85.html">此文档</RouteLink></p>
<div class="hint-container warning">
<p class="hint-container-title">该字段不支持在 <RouteLink to="%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
</div>
</li>
</ul>
<h3 id="changelog" tabindex="-1"><a class="header-anchor" href="#changelog"><span>changelog</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | ChangelogOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>false</code></p>
</li>
<li>
<p>详情： 是否显示页面变更历史</p>
<p>更多配置请参考 <RouteLink to="/notes/docs/guide/%E5%8A%9F%E8%83%BD/%E6%96%87%E7%AB%A0%E5%8F%98%E6%9B%B4%E5%8E%86%E5%8F%B2.html">此文档</RouteLink></p>
<div class="hint-container warning">
<p class="hint-container-title">该字段不支持在 <RouteLink to="%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E.html#%E4%B8%BB%E9%A2%98%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6">主题配置文件 <code v-pre>plume.config.js</code></RouteLink> 中进行配置。</p>
</div>
</li>
</ul>
<h3 id="locales" tabindex="-1"><a class="header-anchor" href="#locales"><span>locales</span></a></h3>
<ul>
<li>类型： <code v-pre>Record&lt;string, PlumeThemeLocaleConfig&gt;</code></li>
<li>默认值： <code v-pre>{}</code></li>
<li>详情： 多语言配置</li>
</ul>
<p>多语言配置，参考 <RouteLink to="/notes/docs/live2d/%E5%A4%9A%E8%AF%AD%E8%A8%80%E9%85%8D%E7%BD%AE.html">此文档</RouteLink></p>
<p>多语言配置支持以下 <a href="#locale-%E9%85%8D%E7%BD%AE">Locale</a> 所有配置选项</p>
<h2 id="locale-配置" tabindex="-1"><a class="header-anchor" href="#locale-配置"><span>Locale 配置</span></a></h2>
<h3 id="home" tabindex="-1"><a class="header-anchor" href="#home"><span>home</span></a></h3>
<ul>
<li>类型： <code v-pre>false | string</code></li>
<li>默认值： <code v-pre>/</code></li>
<li>详情：
首页的路径， 它将被用于：
<ul>
<li>导航栏中 logo的链接；</li>
<li>404页面的 <em>返回首页</em> 的链接；</li>
</ul>
</li>
</ul>
<h3 id="logo" tabindex="-1"><a class="header-anchor" href="#logo"><span>logo</span></a></h3>
<ul>
<li>类型: <code v-pre>false | string</code></li>
<li>默认值： <code v-pre>false</code></li>
<li>详情： 导航栏中的logo。</li>
</ul>
<h3 id="logodark" tabindex="-1"><a class="header-anchor" href="#logodark"><span>logoDark</span></a></h3>
<ul>
<li>类型 <code v-pre>false | string</code></li>
<li>默认值： <code v-pre>false</code></li>
<li>详情： Dark模式下，导航栏中的logo。</li>
</ul>
<h3 id="appearance" tabindex="-1"><a class="header-anchor" href="#appearance"><span>appearance</span></a></h3>
<ul>
<li>类型： <code v-pre>boolean | 'dark' | 'force-dark</code></li>
<li>默认值： <code v-pre>true</code></li>
</ul>
<p>是否启用 深色模式。</p>
<ul>
<li>如果该选项设置为 <code v-pre>true</code>，则默认主题将由用户的首选配色方案决定。</li>
<li>如果该选项设置为 <code v-pre>dark</code>，则默认情况下主题将是深色的，除非用户手动切换它。</li>
<li>如果该选项设置为 <code v-pre>false</code>，用户将无法切换主题。</li>
<li>如果该选项设置为 <code v-pre>force-dark</code>，则用户将无法切换主题，但会强制将主题更改为深色。</li>
</ul>
<p>此选项注入一个内联脚本，从本地存储恢复用户设置。这确保在呈现页面之前应用 <code v-pre>[data-theme=&quot;dark&quot;]</code> 以避免闪烁。</p>
<h3 id="appearancetext" tabindex="-1"><a class="header-anchor" href="#appearancetext"><span>appearanceText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Appearance'</code></li>
<li>详情： 导航栏中的主题切换按钮的文本。</li>
</ul>
<h3 id="avatar" tabindex="-1"><a class="header-anchor" href="#avatar"><span>avatar <Badge type="danger" text="弃用" /></span></a></h3>
<p>弃用，请使用 <a href="#profile">profile</a>。</p>
<h3 id="profile" tabindex="-1"><a class="header-anchor" href="#profile"><span>profile</span></a></h3>
<ul>
<li>类型： <code v-pre>PlumeThemeProfile</code></li>
<li>默认值： <code v-pre>{}</code></li>
<li>详情：配置站点博主的个人信息
<ul>
<li><code v-pre>profile.avatar</code>: 头像地址，用于右侧博主信息展示</li>
<li><code v-pre>profile.name</code>: 名称， 用于右侧博主信息展示</li>
<li><code v-pre>profile.description</code>: 个人描述，用于右侧博主信息展示</li>
<li><code v-pre>profile.circle</code>: 是否为圆形头像</li>
<li><code v-pre>profile.location</code>: 用户地理位置</li>
<li><code v-pre>profile.organization</code>: 用户所在组织/公司</li>
<li><code v-pre>profile.layout</code>: 个人信息展示在左侧还是右侧，<code v-pre>'left' | 'right'</code></li>
</ul>
</li>
</ul>
<p>示例：</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#59873A;--shiki-dark:#80A665">plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    profile</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      avatar</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/avatar.jpg</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      name</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">张三</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      description</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">此处无银三百两，隔壁王二不曾偷</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      circle</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">true</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      location</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">杭州，中国</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      organization</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">xxx公司</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">      layout</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">right</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  })</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h3 id="social" tabindex="-1"><a class="header-anchor" href="#social"><span>social</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | SocialLink[]</code></p>
</li>
<li>
<p>默认值： <code v-pre>false</code></p>
</li>
<li>
<p>详情： 个人社交信息配置。</p>
<p>将作为 图标链接 展示在 导航栏最右侧。</p>
<p>图标可选值：</p>
<ul>
<li><code v-pre>'github'</code></li>
<li><code v-pre>'gitlab'</code></li>
<li><code v-pre>'npm'</code></li>
<li><code v-pre>'docker'</code></li>
<li><code v-pre>'discord'</code></li>
<li><code v-pre>'facebook'</code></li>
<li><code v-pre>'instagram'</code></li>
<li><code v-pre>'linkedin'</code></li>
<li><code v-pre>'mastodon'</code></li>
<li><code v-pre>'slack'</code></li>
<li><code v-pre>'twitter'</code></li>
<li><code v-pre>'x'</code></li>
<li><code v-pre>'youtube'</code></li>
<li><code v-pre>'juejin'</code></li>
<li><code v-pre>'stackoverflow'</code></li>
<li><code v-pre>'qq'</code></li>
<li><code v-pre>'weibo'</code></li>
<li><code v-pre>'bilibili'</code></li>
<li><code v-pre>'zhihu'</code></li>
<li><code v-pre>'douban'</code></li>
<li><code v-pre>'steam'</code></li>
<li><code v-pre>'xbox'</code></li>
<li><code v-pre>{ svg: string, name?: string }</code>: 自定义图标，传入 svg 源码字符串，可选 <code v-pre>name</code> 字段，用于配置 <a href="#navbarsocialinclude"><code v-pre>navbarSocialInclude</code></a></li>
</ul>
</li>
</ul>
<p>示例：</p>
<div class="language-ts" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#59873A;--shiki-dark:#80A665">plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    social</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: [</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      { </span><span style="--shiki-light:#998418;--shiki-dark:#B8A965">icon</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">github</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#998418;--shiki-dark:#B8A965">link</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">https://github.com/zhangsan</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        icon</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: { </span><span style="--shiki-light:#998418;--shiki-dark:#B8A965">svg</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">&#x3C;svg>xxxxx&#x3C;/svg></span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">, </span><span style="--shiki-light:#998418;--shiki-dark:#B8A965">name</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">xxx</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> },</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        link</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">https://xxx.com</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    ]</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  })</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

</div><h3 id="navbarsocialinclude" tabindex="-1"><a class="header-anchor" href="#navbarsocialinclude"><span>navbarSocialInclude</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string[]</code></p>
</li>
<li>
<p>默认值： <code v-pre>['github', 'twitter', 'discord', 'facebook']</code></p>
</li>
<li>
<p>详情：</p>
<p>允许显示在导航栏的社交链接。
该配置仅在 PC 端下有效。</p>
<p>如果 <a href="#social"><code v-pre>social</code></a> 配置为 <code v-pre>{ svg: string, name: string}</code> 则可将 <code v-pre>name</code> 作为 <code v-pre>navbarSocialInclude</code> 的值。</p>
</li>
</ul>
<h3 id="navbar" tabindex="-1"><a class="header-anchor" href="#navbar"><span>navbar</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>NavItem[]</code></p>
</li>
<li>
<p>默认值： <code v-pre>[]</code></p>
</li>
<li>
<p>详情： 导航栏配置。</p>
<p>为了配置导航栏元素，你可以将其设置为 导航栏数组 ，其中的每个元素是 <code v-pre>string</code> 或 <code v-pre>NavItem</code> 对象、</p>
<ul>
<li><code v-pre>NavItem</code> 对象应该有一个 text 字段和一个 link 字段，还有一个可选的 <code v-pre>activeMatch</code> 字段。</li>
<li><code v-pre>string</code> 表示是一个页面文件路径，或者是一个页面的访问路径。</li>
</ul>
</li>
</ul>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">type</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> NavItem</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> =</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  link</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 当前分组的页面前缀</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  prefix</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 该分组下的导航项</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  items</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">NavItem</span><span style="--shiki-light:#999999;--shiki-dark:#666666">[]</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 支持 iconify 图标，直接使用 iconify name 即可自动加载</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">see</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> https://icon-sets.iconify.design/</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  icon</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 控制元素何时被激活</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  activeMatch</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><ul>
<li>
<p>示例1：</p>
<div class="language-js line-numbers-mode" data-ext="js" data-title="js"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#59873A;--shiki-dark:#80A665"> plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    navbar</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> [</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      // NavbarItem</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      {</span><span style="--shiki-light:#998418;--shiki-dark:#B8A965"> text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">Foo</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span><span style="--shiki-light:#998418;--shiki-dark:#B8A965"> link</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/foo/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> },</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      // NavbarGroup</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">Group</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        prefix</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/group/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        item</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> [</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">foo/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">bar/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">],</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      },</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      // 字符串 - 页面文件路径</span></span>
<span class="line"><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">      '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/bar</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD"> // 可以直接省略后缀 `.md`</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    ],</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  }),</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
<li>
<p>示例2：</p>
<div class="language-js line-numbers-mode" data-ext="js" data-title="js"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">export</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375"> default</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">  theme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#59873A;--shiki-dark:#80A665"> plumeTheme</span><span style="--shiki-light:#999999;--shiki-dark:#666666">({</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">    navbar</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> [</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      // 嵌套 Group - 最大深度为 2</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">Group</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        items</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> [</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">          {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">SubGroup</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            items</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> [</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/group/sub/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/group/sub/bar/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">],</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">          },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">        ],</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      },</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">      // 控制元素何时被激活</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">Group 2</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">        items</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> [</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">          {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">Always active</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            link</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">            // 该元素将一直处于激活状态</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            activeMatch</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">          },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">          {</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            text</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">Active on /foo/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            link</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">/not-foo/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">            // 该元素在当前路由路径是 /foo/ 开头时激活</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">            // 支持正则表达式</span></span>
<span class="line"><span style="--shiki-light:#998418;--shiki-dark:#B8A965">            activeMatch</span><span style="--shiki-light:#999999;--shiki-dark:#666666">:</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">^/foo/</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">          },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">        ],</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">      },</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">    ],</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  }),</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
</ul>
<h3 id="notes" tabindex="-1"><a class="header-anchor" href="#notes"><span>notes</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | NotesOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>{ link: '/note', dir: 'notes', notes: [] }</code></p>
</li>
<li>
<p>详情：</p>
<p>笔记配置， 笔记中的文章默认不会出现在首页文章列表</p>
<p>你可以将配置的notes 配置到 navbar中，以便浏览查看</p>
</li>
</ul>
<p>详细配置请查看 <RouteLink to="/notes/docs/live2d/notes%E9%85%8D%E7%BD%AE.html">此文档</RouteLink></p>
<h3 id="sidebar" tabindex="-1"><a class="header-anchor" href="#sidebar"><span>sidebar</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>false | SidebarMulti</code></p>
</li>
<li>
<p>详情：</p>
<p>侧边栏配置。<strong>主题更推荐在 <RouteLink to="/notes/docs/live2d/notes%E9%85%8D%E7%BD%AE.html">notes 配置</RouteLink> 中进行侧边栏配置。</strong></p>
<p>当你不希望使用 <code v-pre>notes</code> 功能，但又期望给文档增加侧边栏时，可以使用此配置。</p>
<p>配置对象的 <code v-pre>key</code> 为侧边栏公共访问路径前缀。</p>
<p>对于 <code v-pre>value</code>:</p>
<ul>
<li><code v-pre>'auto'</code> 表示自动根据目录结构生成侧边栏</li>
<li><code v-pre>string</code> 表示侧边栏对应的页面文件路径</li>
<li><code v-pre>SidebarItem</code> 表示侧边栏单项配置</li>
</ul>
</li>
</ul>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">type</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> ThemeIcon</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> =</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> svg</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">type</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> SidebarMulti</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> =</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> Record</span><span style="--shiki-light:#999999;--shiki-dark:#666666">&#x3C;</span></span>
<span class="line"><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">  string</span><span style="--shiki-light:#999999;--shiki-dark:#666666">,</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  |</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">auto</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  |</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> (</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> |</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> SidebarItem</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)[]</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">  |</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> items</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">auto</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | (</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">SidebarItem</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)[],</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> prefix</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> }</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">></span></span>
<span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> SidebarItem</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 侧边栏文本</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  text</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 侧边栏链接</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  link</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 侧边栏图标</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  icon</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">ThemeIcon</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 次级侧边栏分组</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  items</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">auto</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | (</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">SidebarItem</span><span style="--shiki-light:#999999;--shiki-dark:#666666">)[]</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 如果未指定，组不可折叠。</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 如果为`true`，组可折叠，并默认折叠。</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   *</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 如果为`false`，组可折叠，但默认展开。</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  collapsed</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 当前分组的链接前缀</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  prefix</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  rel</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  target</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h3 id="aside" tabindex="-1"><a class="header-anchor" href="#aside"><span>aside</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | 'left'</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>是否显示侧边栏</p>
<ul>
<li><code v-pre>false</code> 表示禁用 右侧边栏</li>
<li><code v-pre>true</code> 表示启用 右侧边栏</li>
<li><code v-pre>'left</code> 表示将有侧边栏移动到文章内容左侧，sidebar 右侧</li>
</ul>
<p>每个页面可以通过 <RouteLink to="/notes/docs/live2d/frontmatter/basic.html#aside">frontmatter aside</RouteLink> 覆盖层级配置。</p>
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
<p>当 <a href="#aside">aside</a> 被禁用时，<code v-pre>outline</code> 也会被禁用</p>
<p>每个页面可以通过 <RouteLink to="/notes/docs/live2d/frontmatter/basic.html#outline">frontmatter outline</RouteLink> 覆盖层级配置。</p>
</li>
</ul>
<h3 id="transition" tabindex="-1"><a class="header-anchor" href="#transition"><span>transition</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | ThemeTransition</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情：</p>
<p>是否启用过渡动画。</p>
<p>传入 <code v-pre>boolean</code> 类型时，<code v-pre>true</code> 代表启用，<code v-pre>false</code> 代表禁用。</p>
<p>也可以传入一个对象，具体配置见下</p>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> ThemeTransition</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否启用 页面间跳转过渡动画</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> true</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  page</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否启用 博客文章列表过渡动画</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A"> true</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  postList</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">  /**</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 是否启用 深色/浅色 模式切换过渡动画，</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * 或配置过渡动画类型</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   * </span><span style="--shiki-light:#999999;--shiki-dark:#666666">@</span><span style="--shiki-light:#1E754F;--shiki-dark:#4D9375">default</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77"> '</span><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">fade</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#A0ADA0;--shiki-dark:#758575DD">   */</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  appearance</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">boolean</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">fade</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">circle-clip</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">horizontal-clip</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">vertical-clip</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> | </span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span><span style="--shiki-light:#B56959;--shiki-dark:#C98A7D">skew-clip</span><span style="--shiki-light:#B5695977;--shiki-dark:#C98A7D77">'</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
</ul>
<h3 id="footer" tabindex="-1"><a class="header-anchor" href="#footer"><span>footer</span></a></h3>
<ul>
<li>类型： <code v-pre>false | { message: string; copyright: string }</code></li>
<li>默认值： <code v-pre>false</code></li>
<li>详情：页脚配置。</li>
</ul>
<h3 id="bulletin" tabindex="-1"><a class="header-anchor" href="#bulletin"><span>bulletin</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | BulletinOptions</code></p>
</li>
<li>
<p>默认值： <code v-pre>false</code></p>
</li>
<li>
<p>详情： 公告板配置</p>
<p>详情请参考 <RouteLink to="/notes/docs/guide/%E5%8A%9F%E8%83%BD/%E5%85%AC%E5%91%8A%E6%9D%BF.html">公告板</RouteLink></p>
</li>
</ul>
<h3 id="selectlanguagename" tabindex="-1"><a class="header-anchor" href="#selectlanguagename"><span>selectLanguageName</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p>Locale 的语言名称。</p>
<p>该配置项 <strong>仅能在主题配置的 <a href="#locales">locales</a> 的内部生效</strong> 。它将被用作 locale 的语言名称，展示在 <em>选择语言菜单</em> 内。</p>
</li>
</ul>
<h3 id="selectlanguagetext" tabindex="-1"><a class="header-anchor" href="#selectlanguagetext"><span>selectLanguageText</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p><em>选择语言菜单</em> 的文本。</p>
<p>如果你在站点配置中设置了多个 <a href="#locales">locales</a> ，那么 <em>选择语言菜单</em> 就会显示在导航栏中仓库按钮的旁边。</p>
</li>
</ul>
<h3 id="selectlanguagearialabel" tabindex="-1"><a class="header-anchor" href="#selectlanguagearialabel"><span>selectLanguageAriaLabel</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情：</p>
<p><em>选择语言菜单 的 <code v-pre>aria-label</code> 属性。</em></p>
<p>它主要是为了站点的可访问性 (a11y) 。</p>
</li>
</ul>
<h3 id="sidebarmenulabel" tabindex="-1"><a class="header-anchor" href="#sidebarmenulabel"><span>sidebarMenuLabel</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>'Menu'</code></p>
</li>
<li>
<p>详情：</p>
<p>移动设备下的导航栏中 菜单选项的文本。</p>
</li>
</ul>
<h3 id="returntotoplabel" tabindex="-1"><a class="header-anchor" href="#returntotoplabel"><span>returnToTopLabel</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>'return to top'</code></p>
</li>
<li>
<p>详情：</p>
<p>移动设备下的导航栏中返回顶部的文本。</p>
</li>
</ul>
<h3 id="outlinelabel" tabindex="-1"><a class="header-anchor" href="#outlinelabel"><span>outlineLabel</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>'On this page'</code></p>
</li>
<li>
<p>详情：</p>
<p>移动设备下的导航栏中大纲标题的文本</p>
</li>
</ul>
<h3 id="editlinktext" tabindex="-1"><a class="header-anchor" href="#editlinktext"><span>editLinkText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Edit this page'</code></li>
<li>详情： 编辑链接文本</li>
</ul>
<h3 id="editlinkpattern" tabindex="-1"><a class="header-anchor" href="#editlinkpattern"><span>editLinkPattern</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>string</code></p>
</li>
<li>
<p>默认值： <code v-pre>''</code></p>
</li>
<li>
<p>详情： 编辑链接的正则表达式</p>
<p>示例： <code v-pre>':repo/edit/:branch/:path'</code></p>
</li>
</ul>
<h3 id="latestupdatedtext" tabindex="-1"><a class="header-anchor" href="#latestupdatedtext"><span>latestUpdatedText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Latest Updated'</code></li>
<li>详情： 最近更新时间 的文本</li>
</ul>
<h3 id="contributorstext" tabindex="-1"><a class="header-anchor" href="#contributorstext"><span>contributorsText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Contributors'</code></li>
<li>详情： 贡献者的文本</li>
</ul>
<h3 id="changelogtext" tabindex="-1"><a class="header-anchor" href="#changelogtext"><span>changelogText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Changelog'</code></li>
<li>详情： 变更记录的文本</li>
</ul>
<h3 id="changelogontext" tabindex="-1"><a class="header-anchor" href="#changelogontext"><span>changelogOnText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'On'</code></li>
<li>详情： 单次变更记录的时间文本</li>
</ul>
<h3 id="changelogbuttontext" tabindex="-1"><a class="header-anchor" href="#changelogbuttontext"><span>changelogButtonText</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'View All Changelog'</code></li>
<li>详情： 变更记录的按钮文本</li>
</ul>
<h3 id="prevpage" tabindex="-1"><a class="header-anchor" href="#prevpage"><span>prevPage</span></a></h3>
<ul>
<li>类型： <code v-pre>boolean</code></li>
<li>默认值： <code v-pre>true</code></li>
<li>详情： 是否显示上一页</li>
</ul>
<h3 id="prevpagelabel" tabindex="-1"><a class="header-anchor" href="#prevpagelabel"><span>prevPageLabel</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Previous Page'</code></li>
<li>详情： 上一页的文本</li>
</ul>
<h3 id="nextpage" tabindex="-1"><a class="header-anchor" href="#nextpage"><span>nextPage</span></a></h3>
<ul>
<li>类型： <code v-pre>boolean</code></li>
<li>默认值： <code v-pre>true</code></li>
<li>详情： 是否显示下一页</li>
</ul>
<h3 id="nextpagelabel" tabindex="-1"><a class="header-anchor" href="#nextpagelabel"><span>nextPageLabel</span></a></h3>
<ul>
<li>类型： <code v-pre>string</code></li>
<li>默认值： <code v-pre>'Next Page'</code></li>
<li>详情： 下一页的文本</li>
</ul>
<h3 id="createtime" tabindex="-1"><a class="header-anchor" href="#createtime"><span>createTime</span></a></h3>
<ul>
<li>
<p>类型： <code v-pre>boolean | 'only-blog'</code></p>
</li>
<li>
<p>默认值： <code v-pre>true</code></p>
</li>
<li>
<p>详情： 是否显示创建时间</p>
<ul>
<li><code v-pre>false</code> - 不显示</li>
<li><code v-pre>'only-blog'</code> - 只显示在博客文章页面</li>
<li><code v-pre>true</code> - 显示在所有文章页面</li>
</ul>
</li>
</ul>
<h3 id="notfound" tabindex="-1"><a class="header-anchor" href="#notfound"><span>notFound</span></a></h3>
<ul>
<li>类型： <code v-pre>NotFound | undefined</code></li>
<li>默认值： <code v-pre>undefined</code></li>
<li>详情： 404 页面配置</li>
</ul>
<div class="language-ts line-numbers-mode" data-ext="ts" data-title="ts"><button class="copy" title="复制代码" data-copied="已复制"></button><pre class="shiki shiki-themes vitesse-light vitesse-dark vp-code" v-pre=""><code><span class="line"><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">interface</span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994"> NotFound</span><span style="--shiki-light:#999999;--shiki-dark:#666666"> {</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  code</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  title</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  quote</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  linkLabel</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#B07D48;--shiki-dark:#BD976A">  linkText</span><span style="--shiki-light:#AB5959;--shiki-dark:#CB7676">?</span><span style="--shiki-light:#999999;--shiki-dark:#666666">: </span><span style="--shiki-light:#2E8F82;--shiki-dark:#5DA994">string</span></span>
<span class="line"><span style="--shiki-light:#999999;--shiki-dark:#666666">}</span></span></code></pre>

<div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></div></template>


