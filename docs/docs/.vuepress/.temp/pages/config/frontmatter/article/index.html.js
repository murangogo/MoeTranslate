import comp from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/docs/.vuepress/.temp/pages/config/frontmatter/article/index.html.vue"
const data = JSON.parse("{\"path\":\"/config/frontmatter/article/\",\"title\":\"博客文章\",\"lang\":\"zh-CN\",\"frontmatter\":{\"title\":\"博客文章\",\"author\":\"pengzhanbo\",\"createTime\":\"2024/03/03 11:01:03\",\"permalink\":\"/config/frontmatter/article/\"},\"headers\":[],\"readingTime\":{\"minutes\":1.22,\"words\":367},\"filePathRelative\":\"notes/docs/translationapi/腾讯云.md\",\"categoryList\":[{\"id\":\"4358b5\",\"sort\":10001,\"name\":\"notes\"},{\"id\":\"4028d0\",\"sort\":10003,\"name\":\"docs\"},{\"id\":\"604795\",\"sort\":10008,\"name\":\"translationapi\"}],\"bulletin\":false}")
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

}
