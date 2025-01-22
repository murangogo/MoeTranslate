import comp from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/docs/.vuepress/.temp/pages/en/demo/index.html.vue"
const data = JSON.parse("{\"path\":\"/en/demo/\",\"title\":\"Demo\",\"lang\":\"en-US\",\"frontmatter\":{\"title\":\"Demo\",\"createTime\":\"2024/11/21 15:10:12\",\"permalink\":\"/en/demo/\"},\"headers\":[],\"readingTime\":{\"minutes\":0.04,\"words\":13},\"filePathRelative\":\"en/notes/demo/README.md\",\"categoryList\":[{\"id\":\"4358b5\",\"sort\":10001,\"name\":\"notes\"},{\"id\":\"c19f38\",\"sort\":10002,\"name\":\"demo\"}],\"bulletin\":false}")
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
