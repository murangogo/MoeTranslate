import { defineClientConfig } from 'vuepress/client'
// import RepoCard from 'vuepress-theme-plume/features/RepoCard.vue'
// import CustomComponent from './theme/components/Custom.vue'
import "./theme/styles/index.scss";
// import DownloadButtons from './components/DownloadButtons.vue'
// import DownloadButtons2 from './components/DownloadButtons2.vue'
import DownloadButtons3 from './components/DownloadButtons3.vue'

export default defineClientConfig({
  enhance({ app }) {
    // app.component('RepoCard', RepoCard)
    // app.component('CustomComponent', CustomComponent)
    // app.component('DownloadButtons', DownloadButtons)
    // app.component('DownloadButtons2', DownloadButtons2)
    app.component('DownloadButtons3', DownloadButtons3)
  },
})
