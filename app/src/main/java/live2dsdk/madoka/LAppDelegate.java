/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package live2dsdk.madoka;

import android.app.Activity;
import android.opengl.GLES20;
import live2dsdk.basic.LAppDefine;
import com.live2d.sdk.cubism.framework.CubismFramework;

import static android.opengl.GLES20.*;

public class LAppDelegate {     //代码的核心类
    private static LAppDelegate s_instance;     //创建静态变量LAppDelegate s_instance

    private LAppDelegate() {        //构造函数，私有的，防止创建实例
        currentModel = 1;    //通过枚举，获取当前模型

        // Set up Cubism SDK framework.开启框架
        cubismOption.logFunction = new LAppPal.PrintLogFunction();      //打印日志
        cubismOption.loggingLevel = LAppDefine.cubismLoggingLevel;      //日志等级

        CubismFramework.cleanUp();      //清除可能残留的信息
        CubismFramework.startUp(cubismOption);      //按上述配置启动框架
    }

    private Activity activity;

    private final CubismFramework.Option cubismOption = new CubismFramework.Option();

    private LAppTextureManager textureManager;
    private LAppView view;
    private int windowWidth;
    private int windowHeight;
    private boolean isActive = true;

    /**
     * モデルシーンインデックス
     */
    private int currentModel;

    /**
     * クリックしているか
     */
    private boolean isCaptured;
    /**
     * マウスのX座標
     */
    private float mouseX;
    /**
     * マウスのY座標
     */
    private float mouseY;

    public static LAppDelegate getInstance() {  //判断s_instance是否为空，是则创建一个LAppDelegate对象给s_instance
        if (s_instance == null) {
            s_instance = new LAppDelegate();
        }
        return s_instance;      //返回一个LAppDelegate对象
    }

    /**
     * クラスのインスタンス（シングルトン）を解放する。
     */
    public static void releaseInstance() {      //清空s_instance
        if (s_instance != null) {
            s_instance = null;
        }
    }

    /**
     * アプリケーションを非アクティブにする
     */
    public void deactivateApp() {
        isActive = false;
    }       //禁用app，将isactive改为false

    public void onStart(Activity activity) {        //onStart函数，需要一个Activity类
        textureManager = new LAppTextureManager();      //创建一个纹理材质管理对象给textureManager变量
        view = new LAppView();      //创建一个LAppView对象

        this.activity = activity;   //把接收到的activity赋值

        LAppPal.updateTime();   //更新LAppPal的时间
    }

    public void onPause() {
        currentModel = LAppLive2DManager.getInstance().getCurrentModel();
    }   //得到当前模型

    public void onStop() {      //停止
        if (view != null) {
            view.close();   //放空
        }
        textureManager = null;  //放空
        LAppLive2DManager.releaseInstance();    //删除模型
        CubismFramework.dispose();  //销毁框架
    }

    public void onDestroy() {
        releaseInstance();
    }   //清空s_instance

    public void onSurfaceCreated() {
        // テクスチャサンプリング設定-纹理采样设定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        // 透過設定-透射设定
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Initialize Cubism SDK framework
        CubismFramework.initialize();
    }

    public void onSurfaceChanged(int width, int height) {
        // 描画範囲指定-绘图范围指定
        GLES20.glViewport(0, 0, width, height);
        windowWidth = width;
        windowHeight = height;

        // AppViewの初期化
        view.initialize();
        view.initializeSprite();

        // load models
        if (LAppLive2DManager.getInstance().getCurrentModel() != currentModel) {
            LAppLive2DManager.getInstance().changeScene(currentModel);
        }

        isActive = true;
    }

    public void run() {
        // 時間更新-时间更新
        LAppPal.updateTime();

        // 画面初期化-画面初始化
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepthf(1.0f);

        if (view != null) {
            view.render();      //描绘画面
        }

        // アプリケーションを非アクティブにする-取消激活应用程序
        if (!isActive) {
            activity.finishAndRemoveTask();
        }
    }


    public void onTouchBegan(float x, float y) {        //开始触碰，获得坐标值
        mouseX = x;
        mouseY = y;

        if (view != null) {     //视图不空
            isCaptured = true;  //捕捉
            view.onTouchesBegan(mouseX, mouseY);    //传递坐标值
        }
    }

    public void onTouchEnd(float x, float y) {
        mouseX = x;
        mouseY = y;

        if (view != null) {
            isCaptured = false;     //没有捕获
            view.onTouchesEnded(mouseX, mouseY);    //传递坐标值
        }
    }

    public void onTouchMoved(float x, float y) {    //滑动事件
        mouseX = x;
        mouseY = y;

        if (isCaptured && view != null) {       //没有松开且有视图
            view.onTouchesMoved(mouseX, mouseY);
        }
    }

    // getter, setter群
    public Activity getActivity() {
        return activity;
    }       //返回主活动

    public LAppTextureManager getTextureManager() {
        return textureManager;
    }

    public LAppView getView() {
        return view;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }
}
