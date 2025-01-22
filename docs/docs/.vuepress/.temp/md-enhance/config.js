import CodeDemo from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/node_modules/vuepress-plugin-md-enhance/lib/client/components/CodeDemo.js";
import MdDemo from "E:/CodeExercise/AAA_Exercise/Free_Exercise/Web/VuePressProj/moetranslate-doc-website/node_modules/vuepress-plugin-md-enhance/lib/client/components/MdDemo.js";

export default {
  enhance: ({ app }) => {
    app.component("CodeDemo", CodeDemo);
    app.component("MdDemo", MdDemo);
  },
};
