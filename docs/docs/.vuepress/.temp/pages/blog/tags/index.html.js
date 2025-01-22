import comp from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/docs/.vuepress/.temp/pages/blog/tags/index.html.vue"
const data = JSON.parse("{\"path\":\"/blog/tags/\",\"title\":\"标签\",\"lang\":\"zh-CN\",\"frontmatter\":{\"lang\":\"zh-CN\",\"title\":\"标签\",\"draft\":true},\"headers\":[],\"readingTime\":{\"minutes\":0,\"words\":0},\"filePathRelative\":null,\"type\":\"blog-tags\",\"bulletin\":false}")
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
