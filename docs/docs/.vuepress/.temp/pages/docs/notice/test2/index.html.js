import comp from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/docs/.vuepress/.temp/pages/docs/notice/test2/index.html.vue"
const data = JSON.parse("{\"path\":\"/docs/notice/test2/\",\"title\":\"test2\",\"lang\":\"zh-CN\",\"frontmatter\":{\"title\":\"test2\",\"author\":\"azuki\",\"createTime\":\"2024/03/02 10:48:14\",\"permalink\":\"/docs/notice/test2/\"},\"headers\":[],\"readingTime\":{\"minutes\":0.05,\"words\":15},\"filePathRelative\":\"notes/docs/notice/test2.md\",\"bulletin\":false}")
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
