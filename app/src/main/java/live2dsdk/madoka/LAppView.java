/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package live2dsdk.madoka;

import live2dsdk.basic.TouchManager;
import com.live2d.sdk.cubism.framework.math.CubismMatrix44;
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix;
import com.live2d.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid;
import com.moe.moetranslator.utils.AppPathManager;

import static live2dsdk.basic.LAppDefine.*;

public class LAppView implements AutoCloseable {
    private Live2DCallbackCustom modelChangeCallback;  // 回调变量

    private final CubismMatrix44 deviceToScreen = CubismMatrix44.create(); // デバイス座標からスクリーン座標に変換するための行列
    private final CubismViewMatrix viewMatrix = new CubismViewMatrix();   // 画面表示の拡縮や移動の変換を行う行列
    private int windowWidth;
    private int windowHeight;

    /**
     * レンダリング先の選択肢
     */
    private RenderingTarget renderingTarget = RenderingTarget.NONE;
    /**
     * レンダリングターゲットのクリアカラー
     */
    private final float[] clearColor = new float[4];

    private CubismOffscreenSurfaceAndroid renderingBuffer = new CubismOffscreenSurfaceAndroid();

    private LAppSprite backSprite;
    private LAppSprite gearSprite;
    private LAppSprite powerSprite;
    private LAppSprite renderingSprite;

    /**
     * モデルの切り替えフラグ
     */
    private boolean isChangedModel = false;
    private int changeModelId = 0;

    private final TouchManager touchManager = new TouchManager();

    /**
     * シェーダー作成委譲クラス
     */
    private LAppSpriteShader spriteShader;

    /**
     * LAppModelのレンダリング先
     */
    public enum RenderingTarget {
        NONE,   // デフォルトのフレームバッファにレンダリング-在默认帧缓冲器中呈现
        MODEL_FRAME_BUFFER,     // LAppModelForSmallDemoが各自持つフレームバッファにレンダリング-LAppModelForSmallDemo在各自拥有的帧缓冲器中呈现
        VIEW_FRAME_BUFFER  // LAppViewForSmallDemoが持つフレームバッファにレンダリング-LAppViewForSmallDemo所具有的帧缓冲器中渲染
    }

    public LAppView() {     //初始化所有color数组
        clearColor[0] = 1.0f;
        clearColor[1] = 1.0f;
        clearColor[2] = 1.0f;
        clearColor[3] = 0.0f;
    }

    public void setChangeModel(int n, Live2DCallbackCustom callback) {
        modelChangeCallback = callback;  // 保存回调
        isChangedModel = true;
        changeModelId = n;
    }

    @Override
    public void close() {
        spriteShader.close();
    }

    // ビューを初期化する
    public void initialize() {
        int width = LAppDelegate.getInstance().getWindowWidth();
        int height = LAppDelegate.getInstance().getWindowHeight();

        float ratio = (float) width / (float) height;
        float left = -ratio;
        float right = ratio;
        float bottom = LogicalView.LEFT.getValue();
        float top = LogicalView.RIGHT.getValue();

        // デバイスに対応する画面範囲。Xの左端、Xの右端、Yの下端、Yの上端
        viewMatrix.setScreenRect(left, right, bottom, top);
        viewMatrix.scale(Scale.DEFAULT.getValue(), Scale.DEFAULT.getValue());

        // 単位行列に初期化
        deviceToScreen.loadIdentity();

        if (width > height) {
            float screenW = Math.abs(right - left);
            deviceToScreen.scaleRelative(screenW / width, -screenW / width);
        } else {
            float screenH = Math.abs(top - bottom);
            deviceToScreen.scaleRelative(screenH / height, -screenH / height);
        }
        deviceToScreen.translateRelative(-width * 0.5f, -height * 0.5f);

        // 表示範囲の設定
        viewMatrix.setMaxScale(Scale.MAX.getValue());   // 限界拡大率
        viewMatrix.setMinScale(Scale.MIN.getValue());   // 限界縮小率

        // 表示できる最大範囲
        viewMatrix.setMaxScreenRect(
                MaxLogicalView.LEFT.getValue(),
                MaxLogicalView.RIGHT.getValue(),
                MaxLogicalView.BOTTOM.getValue(),
                MaxLogicalView.TOP.getValue()
        );

        spriteShader = new LAppSpriteShader();
    }

    // 画像を初期化する
    public void initializeSprite() {
        int windowWidth = LAppDelegate.getInstance().getWindowWidth();
        int windowHeight = LAppDelegate.getInstance().getWindowHeight();

        LAppTextureManager textureManager = LAppDelegate.getInstance().getTextureManager();

//        // 背景画像の読み込み
        LAppTextureManager.TextureInfo backgroundTexture = textureManager.createTextureFromPngFile(AppPathManager.INSTANCE.getLive2DPath() + ResourcePath.BACK_IMAGE.getPath());


        // x,yは画像の中心座標
        float x = windowWidth * 0.5f;
        float y = windowHeight * 0.5f;
        float fWidth = backgroundTexture.width * 2.0f;
//        float fWidth = windowWidth * 1.0f;
        float fHeight = backgroundTexture.height * 2.0f;
//        float fHeight = windowHeight * 1.0f;
        int programId = spriteShader.getShaderId();

        if (backSprite == null) {
            backSprite = new LAppSprite(x, y, fWidth, fHeight, backgroundTexture.id, programId);
        } else {
            backSprite.resize(x, y, fWidth, fHeight);
        }
//        // 歯車画像の読み込み-齿轮图标
//        LAppTextureManager.TextureInfo gearTexture = textureManager.createTextureFromPngFile(ResourcePath.ROOT.getPath() + ResourcePath.GEAR_IMAGE.getPath());
//
//
//        x = windowWidth - gearTexture.width * 0.5f - 96.f;
//        y = windowHeight - gearTexture.height * 0.5f;
//        fWidth = (float) gearTexture.width;
//        fHeight = (float) gearTexture.height;
//
//        if (gearSprite == null) {
//            gearSprite = new LAppSprite(x, y, fWidth, fHeight, gearTexture.id, programId);
//        } else {
//            gearSprite.resize(x, y, fWidth, fHeight);
//        }
//
//        // 電源画像の読み込み-电源图标
//        LAppTextureManager.TextureInfo powerTexture = textureManager.createTextureFromPngFile(ResourcePath.ROOT.getPath() + ResourcePath.POWER_IMAGE.getPath());
//
//
//        x = windowWidth - powerTexture.width * 0.5f - 96.0f;
//        y = powerTexture.height * 0.5f;
//        fWidth = (float) powerTexture.width;
//        fHeight = (float) powerTexture.height;
//
//        if (powerSprite == null) {
//            powerSprite = new LAppSprite(x, y, fWidth, fHeight, powerTexture.id, programId);
//        } else {
//            powerSprite.resize(x, y, fWidth, fHeight);
//        }

        // 画面全体を覆うサイズ
        x = windowWidth * 0.5f;
        y = windowHeight * 0.5f;

        if (renderingSprite == null) {
            renderingSprite = new LAppSprite(x, y, windowWidth, windowHeight, 0, programId);
        } else {
            renderingSprite.resize(x, y, windowWidth, windowHeight);
        }
    }

    // 描画する
    public void render() {
        // 画面サイズを取得する。
        int maxWidth = LAppDelegate.getInstance().getWindowWidth();
        int maxHeight = LAppDelegate.getInstance().getWindowHeight();

        backSprite.setWindowSize(maxWidth, maxHeight);
//        gearSprite.setWindowSize(maxWidth, maxHeight);
//        powerSprite.setWindowSize(maxWidth, maxHeight);


        // UIと背景の描画-UI背景的呈现
        backSprite.render();        //背景
//        gearSprite.render();        //齿轮
//        powerSprite.render();       //电源

        if (isChangedModel) {       //替换模型
            isChangedModel = false;
            // 哪一个模型
            LAppLive2DManager.getInstance().nextScene(changeModelId, modelChangeCallback);
        }

        // モデルの描画-模型的绘制
        LAppLive2DManager live2dManager = LAppLive2DManager.getInstance();
        live2dManager.onUpdate();

        // 各モデルが持つ描画ターゲットをテクスチャとする場合-将每个模型的绘制目标作为纹理
        if (renderingTarget == RenderingTarget.MODEL_FRAME_BUFFER && renderingSprite != null) {
            final float[] uvVertex = {
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
            };

            for (int i = 0; i < live2dManager.getModelNum(); i++) {
                LAppModel model = live2dManager.getModel(i);
                float alpha = i < 1 ? 1.0f : model.getOpacity();    // 片方のみ不透明度を取得できるようにする。

                renderingSprite.setColor(1.0f, 1.0f, 1.0f, alpha);

                if (model != null) {
                    renderingSprite.setWindowSize(maxWidth, maxHeight);
                    renderingSprite.renderImmediate(model.getRenderingBuffer().getColorBuffer()[0], uvVertex);
                }
            }
        }
    }

    /**
     * モデル1体を描画する直前にコールされる
     *
     * @param refModel モデルデータ
     */
    public void preModelDraw(LAppModel refModel) {
        // 別のレンダリングターゲットへ向けて描画する場合の使用するフレームバッファ-向另一渲染目标渲染时使用的帧缓冲器
        CubismOffscreenSurfaceAndroid useTarget;

        // 別のレンダリングターゲットへ向けて描画する場合-当你在另一个渲染目标上绘图时
        if (renderingTarget != RenderingTarget.NONE) {

            // 使用するターゲット-使用的目标
            useTarget = (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER)
                    ? renderingBuffer
                    : refModel.getRenderingBuffer();

            // 描画ターゲット内部未作成の場合はここで作成-绘制目标内部未创建时在此处创建
            if (!useTarget.isValid()) {
                int width = LAppDelegate.getInstance().getWindowWidth();
                int height = LAppDelegate.getInstance().getWindowHeight();

                // モデル描画キャンバス
                useTarget.createOffscreenSurface((int) width, (int) height, null);
            }
            // レンダリング開始-渲染开始
            useTarget.beginDraw(null);
            useTarget.clear(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);   // 背景クリアカラー-背景清色
        }
    }

    /**
     * モデル1体を描画した直後にコールされる
     *
     * @param refModel モデルデータ
     */
    public void postModelDraw(LAppModel refModel) {
        CubismOffscreenSurfaceAndroid useTarget = null;

        // 別のレンダリングターゲットへ向けて描画する場合-绘制到其他渲染目标时
        if (renderingTarget != RenderingTarget.NONE) {
            // 使用するターゲット-要使用的目标
            useTarget = (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER)
                    ? renderingBuffer
                    : refModel.getRenderingBuffer();

            // レンダリング終了-渲染结束
            useTarget.endDraw();

            // LAppViewの持つフレームバッファを使うなら、スプライトへの描画はこことなる-如果使用LAppView所具有的帧缓冲区，则在精灵中的描绘为这里
            if (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER && renderingSprite != null) {
                final float[] uvVertex = {
                        1.0f, 1.0f,
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                        1.0f, 0.0f
                };
                renderingSprite.setColor(1.0f, 1.0f, 1.0f, getSpriteAlpha(0));
                // 画面サイズを取得する。
                int maxWidth = LAppDelegate.getInstance().getWindowWidth();
                int maxHeight = LAppDelegate.getInstance().getWindowHeight();

                renderingSprite.setWindowSize(maxWidth, maxHeight);
                renderingSprite.renderImmediate(useTarget.getColorBuffer()[0], uvVertex);
            }
        }
    }

    /**
     * レンダリング先を切り替える
     *
     * @param targetType レンダリング先
     */
    public void switchRenderingTarget(RenderingTarget targetType) {
        renderingTarget = targetType;
    }

    /**
     * タッチされたときに呼ばれる
     *
     * @param pointX スクリーンX座標
     * @param pointY スクリーンY座標
     */
    public void onTouchesBegan(float pointX, float pointY) {    //传递坐标
        touchManager.touchesBegan(pointX, pointY);
    }

    /**
     * タッチしているときにポインターが動いたら呼ばれる
     *
     * @param pointX スクリーンX座標
     * @param pointY スクリーンY座標
     */
    public void onTouchesMoved(float pointX, float pointY) {        //滑动事件
        float viewX = transformViewX(touchManager.getLastX());      //获得最新坐标
        float viewY = transformViewY(touchManager.getLastY());

        touchManager.touchesMoved(pointX, pointY);

        LAppLive2DManager.getInstance().onDrag(viewX, viewY);       //调用拖动函数
    }

    /**
     * タッチが終了したら呼ばれる
     *
     * @param pointX スクリーンX座標
     * @param pointY スクリーンY座標
     */
    public void onTouchesEnded(float pointX, float pointY) {        //结束坐标值
        // タッチ終了
        LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
        live2DManager.onDrag(0.0f, 0.0f);       //发生拖拽事件（0，0）

        // シングルタップ-单击
        // 論理座標変換した座標を取得-获得逻辑坐标变换后的坐标
        float x = deviceToScreen.transformX(touchManager.getLastX());
        // 論理座標変換した座標を取得-获得逻辑坐标变换后的坐标
        float y = deviceToScreen.transformY(touchManager.getLastY());

        if (DEBUG_TOUCH_LOG_ENABLE) {       //打印日志
            LAppPal.printLog("Touches ended x: " + x + ", y:" + y);
        }

        live2DManager.onTap(x, y);      //传递点击事件

        // 歯車ボタンにタップしたか-判定是否点击齿轮按钮
//        if (gearSprite.isHit(pointX, pointY)) {
//            isChangedModel = true;
//        }

        // 電源ボタンにタップしたか-是否点击了电源键
        //       if (powerSprite.isHit(pointX, pointY)) {
        //           // アプリを終了する-关闭应用程序
        //           LAppDelegate.getInstance().deactivateApp();
        //       }

    }

    /**
     * X座標をView座標に変換する
     *
     * @param deviceX デバイスX座標
     * @return ViewX座標
     */
    public float transformViewX(float deviceX) {
        // 論理座標変換した座標を取得
        float screenX = deviceToScreen.transformX(deviceX);
        // 拡大、縮小、移動後の値
        return viewMatrix.invertTransformX(screenX);
    }

    /**
     * Y座標をView座標に変換する
     *
     * @param deviceY デバイスY座標
     * @return ViewY座標
     */
    public float transformViewY(float deviceY) {
        // 論理座標変換した座標を取得
        float screenY = deviceToScreen.transformY(deviceY);
        // 拡大、縮小、移動後の値
        return viewMatrix.invertTransformX(screenY);
    }

    /**
     * X座標をScreen座標に変換する
     *
     * @param deviceX デバイスX座標
     * @return ScreenX座標
     */
    public float transformScreenX(float deviceX) {
        return deviceToScreen.transformX(deviceX);
    }

    /**
     * Y座標をScreen座標に変換する
     *
     * @param deviceY デバイスY座標
     * @return ScreenY座標
     */
    public float transformScreenY(float deviceY) {
        return deviceToScreen.transformX(deviceY);
    }

    /**
     * レンダリング先をデフォルト以外に切り替えた際の背景クリア色設定
     *
     * @param r 赤(0.0~1.0)
     * @param g 緑(0.0~1.0)
     * @param b 青(0.0~1.0)
     */
    public void setRenderingTargetClearColor(float r, float g, float b) {
        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
    }

    /**
     * 別レンダリングターゲットにモデルを描画するサンプルで描画時のαを決定する
     *
     * @param assign
     * @return
     */
    public float getSpriteAlpha(int assign) {
        // assignの数値に応じて適当な差をつける
        float alpha = 0.25f + (float) assign * 0.5f;

        // サンプルとしてαに適当な差をつける
        if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        if (alpha < 0.1f) {
            alpha = 0.1f;
        }
        return alpha;
    }

    /**
     * Return rendering target enum instance.
     *
     * @return rendering target
     */
    public RenderingTarget getRenderingTarget() {
        return renderingTarget;
    }
}
