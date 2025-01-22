import { defineNoteConfig } from 'vuepress-theme-plume'

export const downloadNotes = defineNoteConfig({
  dir: 'download',
  link: '/download/',
  sidebar: [
    {
      text: '下载',
      collapsed: false,
      items: [
        '下载链接',
        '更新公告',
        '注意事项',
      ],
    },
  ],
})
