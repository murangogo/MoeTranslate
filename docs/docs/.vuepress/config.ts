import { viteBundler } from '@vuepress/bundler-vite';
import { defineUserConfig } from 'vuepress';
import { plumeTheme } from 'vuepress-theme-plume';
import * as dotenv from 'dotenv';

// 加载 .env 文件
dotenv.config();


export default defineUserConfig({
  base: '/',
  lang: 'zh-CN',
  locales: {
    '/': {
      title: '萌译 - MoeTranslate',
      lang: 'zh-CN',
      description: '一键翻译图片文字内容，让您和非中文游戏的距离不再遥远；不仅如此，还支持Gemini聊天和Live2D。',
    },
    '/en/': {
      title: 'MoeTranslate',
      lang: 'en-US',
      description: 'One-click translation of picture text content, so that the distance between you and non-Chinese games is no longer far; Not only that, but Gemini chat and Live2D are also supported.',
    },
  },

  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['link', { rel: 'icon', type: 'image/png', sizes: '16x16', href: '/favicon-16x16.png' }],
    ['link', { rel: 'icon', type: 'image/png', sizes: '32x32', href: '/favicon-32x32.png' }],
  ],

  bundler: viteBundler(),

  theme: plumeTheme({
    // 添加您的部署域名
    // hostname: 'https://your_site_url',

    plugins: {
      /**
       * Shiki 代码高亮
       * @see https://theme-plume.vuejs.press/config/plugins/code-highlight/
       */
      //  强烈建议预设代码块高亮语言，插件默认加载所有语言会产生不必要的时间开销
      shiki: {
        languages: ['md'],
      },

      /**
       * markdown enhance
       * @see https://theme-plume.vuejs.press/config/plugins/markdown-enhance/
       */
      markdownEnhance: {
        demo: true,
      //   include: true,
      //   chart: true,
      //   echarts: true,
      //   mermaid: true,
      //   flowchart: true,
      },

      /**
       *  markdown power
       * @see https://theme-plume.vuejs.press/config/plugin/markdown-power/
       */
      // markdownPower: {
      //   pdf: true,
      //   caniuse: true,
      //   plot: true,
      //   bilibili: true,
      //   youtube: true,
      //   icons: true,
      //   codepen: true,
      //   replit: true,
      //   codeSandbox: true,
      //   jsfiddle: true,
      //   repl: {
      //     go: true,
      //     rust: true,
      //     kotlin: true,
      //   },
      // },

      /**
       * 评论 comments
       * @see https://theme-plume.vuejs.press/guide/features/comments/
       */
      // comment: {
      //   provider: '', // "Artalk" | "Giscus" | "Twikoo" | "Waline"
      //   comment: true,
      //   repo: '',
      //   repoId: '',
      //   categoryId: '',
      //   mapping: 'pathname',
      //   reactionsEnabled: true,
      //   inputPosition: 'top',
      // },
    },
  }),

    // 在 define 中注入环境变量
    define: {
      VITE_LEANCLOUD_APP_ID: process.env.VITE_LEANCLOUD_APP_ID,
      VITE_LEANCLOUD_APP_KEY: process.env.VITE_LEANCLOUD_APP_KEY,
      VITE_LEANCLOUD_SERVER_URL: process.env.VITE_LEANCLOUD_SERVER_URL,
    },
})
