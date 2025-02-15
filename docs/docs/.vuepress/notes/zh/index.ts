import { defineNotesConfig } from 'vuepress-theme-plume'
// import { plugins } from './plugins'
import { docsNotes } from './docs'
import { downloadNotes } from './download'

export const zhNotes = defineNotesConfig({
  dir: 'notes',
  link: '/',
  notes: [
    docsNotes,
    downloadNotes,
  ],
})
