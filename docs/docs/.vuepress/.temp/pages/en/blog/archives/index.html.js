import comp from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/docs/.vuepress/.temp/pages/en/blog/archives/index.html.vue"
const data = JSON.parse("{\"path\":\"/en/blog/archives/\",\"title\":\"Archives\",\"lang\":\"en-US\",\"frontmatter\":{\"lang\":\"en-US\",\"title\":\"Archives\",\"draft\":true},\"headers\":[],\"readingTime\":{\"minutes\":0,\"words\":0},\"filePathRelative\":null,\"type\":\"blog-archives\",\"bulletin\":false}")
export { comp, data }

if (import.meta.webpackHot) {
  import.meta.webpackHot.accept()
  if (__VUE_HMR_RUNTIME__.updatePageData) {
    __VUE_HMR_RUNTIME__.updatePageData(data)
  }
}

if (import.meta.hot) {
  import.meta.hot.accept(({ data }) => {
    __VUE_HMR_RUNTIME__.updatePageData(data)
  })
}
