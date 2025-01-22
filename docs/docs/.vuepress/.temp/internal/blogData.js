export const blogPostData = [{"path":"/en/article/dcxswh1e/","title":"Markdown","categoryList":[{"id":"5ebeb6","sort":10000,"name":"preview"}],"tags":["markdown"],"createTime":"2024/11/21 15:10:12","lang":"en-US","excerpt":""},{"path":"/en/article/2s9dockl/","title":"Custom Component","categoryList":[{"id":"5ebeb6","sort":10000,"name":"preview"}],"tags":["preview","component"],"createTime":"2024/11/21 15:10:12","lang":"en-US","excerpt":""},{"path":"/article/12evxwe3/","title":"Markdown","categoryList":[{"id":"5ebeb6","sort":10000,"name":"preview"}],"tags":["markdown"],"createTime":"2024/11/21 15:10:12","lang":"zh-CN","excerpt":""},{"path":"/article/y3u9swl5/","title":"自定义组件","categoryList":[{"id":"5ebeb6","sort":10000,"name":"preview"}],"tags":["预览","组件"],"createTime":"2024/11/21 15:10:12","lang":"zh-CN","excerpt":""},{"path":"/en/support/","title":"支持项目","categoryList":[],"createTime":"2024/09/27 08:47:36","lang":"en-US","excerpt":""},{"path":"/en/socialmedia/","title":"社交媒体","categoryList":[],"createTime":"2024/09/27 08:47:36","lang":"en-US","excerpt":""},{"path":"/support/","title":"支持项目","categoryList":[],"createTime":"2024/09/27 08:47:36","lang":"zh-CN","excerpt":""},{"path":"/socialmedia/","title":"社交媒体","categoryList":[],"createTime":"2024/09/27 08:47:36","lang":"zh-CN","excerpt":""}]

if (import.meta.webpackHot) {
  import.meta.webpackHot.accept()
  if (__VUE_HMR_RUNTIME__.updateBlogPostData) {
    __VUE_HMR_RUNTIME__.updateBlogPostData(blogPostData)
  }
}

if (import.meta.hot) {
  import.meta.hot.accept(({ blogPostData }) => {
    __VUE_HMR_RUNTIME__.updateBlogPostData(blogPostData)
  })
}
