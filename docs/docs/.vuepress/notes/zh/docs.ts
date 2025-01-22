import { defineNoteConfig } from 'vuepress-theme-plume'

export const docsNotes = defineNoteConfig({
  dir: 'docs',
  link: '/docs/',
  sidebar: [
    {
      text: '快速上手',
      collapsed: true,
      items: [
        '项目介绍',
        '翻译使用',
        'Gemini使用',
        'Live2D使用',
      ],
    },
    {
      text: '翻译功能',
      prefix: 'translation',
      collapsed: true,
      items: [
        '翻译模式',
        'API配置',
        '个性化设置',
        '常见问题',
        '错误代码',
      ],
    },
    {
      text: '翻译API',
      prefix: 'translationapi',
      collapsed: true,
      items: [
        'ML Kit',
        'NLLB',
        '必应翻译',
        '小牛翻译',
        '火山引擎',
        'Azure AI 翻译',
        '百度翻译',
        '腾讯云',
        '自定义文本API',
        '自定义图片API',
      ],
    },
    {
     text: 'Gemini',
     prefix: 'gemini',
     collapsed: true,
     items: [
        'Gemini API申请',
        '注意事项',
        ],
      },
      {
        text: 'Live2D功能',
        prefix: 'live2d',
        collapsed: true,
        items: [
          'Live2D文件结构',
          '导入模型',
        ],
      },
      {
        text: '一些事项',
        prefix: 'notice',
        collapsed: true,
        items: [
          'test1',
          'test2',
        ],
      },
  ],
})
