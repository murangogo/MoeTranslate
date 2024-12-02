/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package live2dsdk.madoka;

import com.live2d.sdk.cubism.framework.math.CubismMatrix44;
import com.live2d.sdk.cubism.framework.motion.ACubismMotion;
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback;
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback;
import com.moe.moetranslator.utils.AppPathManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static live2dsdk.basic.LAppDefine.*;

import android.util.Log;

/**
 * サンプルアプリケーションにおいてCubismModelを管理するクラス。
 * モデル生成と破棄、タップイベントの処理、モデル切り替えを行う。
 */
public class LAppLive2DManager {
    private final List<LAppModel> models = new ArrayList<>();

    /**
     * 表示するシーンのインデックス値
     */
    private int currentModel;

    /**
     * モデルディレクトリ名
     */
    private final List<String> modelDir = new ArrayList<>();

    // onUpdateメソッドで使用されるキャッシュ変数
    private final CubismMatrix44 viewMatrix = CubismMatrix44.create();
    private final CubismMatrix44 projection = CubismMatrix44.create();
    public static LAppLive2DManager getInstance() {
        if (s_instance == null) {
            s_instance = new LAppLive2DManager();
        }
        return s_instance;
    }

    public static void releaseInstance() {
        s_instance = null;
    }

    /**
     * 現在のシーンで保持している全てのモデルを解放する
     */
    public void releaseAllModel() {     //释放所有模型
        for (LAppModel model : models) {    //遍历模型
            model.deleteModel();        //释放
        }
        models.clear();     //清除
    }

    // モデル更新処理及び描画処理を行う
    public void onUpdate() {        //描绘模型
        int width = LAppDelegate.getInstance().getWindowWidth();        //得到宽度
        int height = LAppDelegate.getInstance().getWindowHeight();      //得到高度

        for (int i = 0; i < models.size(); i++) {       //获得模型
            LAppModel model = models.get(i);

            if (model.getModel() == null) {     //得不到模型打印日志
                LAppPal.printLog("Failed to model.getModel().");
                continue;
            }

            projection.loadIdentity();

            if (model.getModel().getCanvasWidth() > 1.0f && width < height) {
                // 横に長いモデルを縦長ウィンドウに表示する際モデルの横サイズでscaleを算出する-在纵长窗口上显示横向长的模型时，用模型的横向尺寸计算scale
                model.getModelMatrix().setWidth(2.0f);
                projection.scale(1.0f, (float) width / (float) height);
            } else {
                projection.scale((float) height / (float) width, 1.0f);
            }

            // 必要があればここで乗算する-如果有必要，在这里相乘
            if (viewMatrix != null) {
                viewMatrix.multiplyByMatrix(projection);
            }

            // モデル1体描画前コール-模型1体描绘前调用
            LAppDelegate.getInstance().getView().preModelDraw(model);

            model.update();

            model.draw(projection);     // 参照渡しなのでprojectionは変質する-因为是引用传递，projection会变质

            // モデル1体描画後コール-一个模型绘制后调用
            LAppDelegate.getInstance().getView().postModelDraw(model);
        }
    }

    /**
     * 画面をドラッグした時の処理
     *
     * @param x 画面のx座標
     * @param y 画面のy座標
     */
    public void onDrag(float x, float y) {      //拖动函数
        for (int i = 0; i < models.size(); i++) {
            LAppModel model = getModel(i);
            model.setDragging(x, y);        //0，0
        }
    }

    /**
     * 画面をタップした時の処理
     *
     * @param x 画面のx座標
     * @param y 画面のy座標
     */
    public void onTap(float x, float y) {       //获取点击坐标值
        if (DEBUG_LOG_ENABLE) {     //日志
            LAppPal.printLog("tap point: {" + x + ", y: " + y);
        }

        for (int i = 0; i < models.size(); i++) {       //对于每一个模型
            LAppModel model = models.get(i);        //获取模型

            // 頭をタップした場合表情をランダムで再生する-当你点击头部时，随机播放表情
            if (model.hitTest(HitAreaName.HEAD.getId(), x, y)) {    //判断是否在头部：注：该函数似乎无效？？
                if (DEBUG_LOG_ENABLE) {     //打印日志
                    LAppPal.printLog("hit area: " + HitAreaName.HEAD.getId());
                }
                model.setRandomExpression();        //点到头部就做乱表情
            }
            // 体をタップした場合ランダムモーションを開始する-当你点击身体时，开始随机运动
            else if (model.hitTest(HitAreaName.BODY.getId(), x, y)) {   //判断是否为身体，但打印日志依然是头部？？？
                if (DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("hit area: " + HitAreaName.HEAD.getId());
                }

                model.startRandomMotion(MotionGroup.TAP_BODY.getId(), Priority.NORMAL.getPriority(), finishedMotion, beganMotion);   //开始随机运动
            }
        }
    }

    /**
     * 次のシーンに切り替える
     * サンプルアプリケーションではモデルセットの切り替えを行う
     */
    public void nextScene(int k) {
        changeScene(k);        //改变场景
    }

    /**
     * シーンを切り替える
     *
     * @param index 切り替えるシーンインデックス
     */
    public void changeScene(int index) {        //改变场景
        currentModel = index;        //得到当前模型
        if (DEBUG_LOG_ENABLE) {     //启用日志则打印日志
            LAppPal.printLog("model index: " + index);
        }

        String modelDirName = "model_" + index;        //获取模型文件夹
        String modelPath = AppPathManager.INSTANCE.getModelPath(modelDirName);        //获得模型路径
        String modelJsonName = "model.model3.json";      //获取模型JSON文件

        if (DEBUG_LOG_ENABLE) {     //启用日志则打印日志
            LAppPal.printLog("modelDirName: " + modelDirName);
            LAppPal.printLog("modelPath: " + modelPath);
            LAppPal.printLog("modelJsonName: " + modelJsonName);
        }


        releaseAllModel();      //释放所有模型

        models.add(new LAppModel());        //加入新模型
        models.get(0).loadModelFiles(modelPath, modelJsonName);

        /*
         * モデル半透明表示を行うサンプルを提示する。
         * ここでUSE_RENDER_TARGET、USE_MODEL_RENDER_TARGETが定義されている場合
         * 別のレンダリングターゲットにモデルを描画し、描画結果をテクスチャとして別のスプライトに張り付ける。
         */
        LAppView.RenderingTarget useRenderingTarget;
        if (USE_RENDER_TARGET) {
            // LAppViewの持つターゲットに描画を行う場合こちらを選択-选择此选项可在LAppView目标上绘制
            useRenderingTarget = LAppView.RenderingTarget.VIEW_FRAME_BUFFER;
        } else if (USE_MODEL_RENDER_TARGET) {
            // 各LAppModelの持つターゲットに描画を行う場合こちらを選択-选择此选项可在每个LAppModel的目标上绘制
            useRenderingTarget = LAppView.RenderingTarget.MODEL_FRAME_BUFFER;
        } else {
            // デフォルトのメインフレームバッファへレンダリングする(通常)-渲染到默认大型机缓冲区（正常）
            useRenderingTarget = LAppView.RenderingTarget.NONE;
        }

        if (USE_RENDER_TARGET || USE_MODEL_RENDER_TARGET) {
            // モデル個別にαを付けるサンプルとして、もう1体モデルを作成し少し位置をずらす。-每个模型α中描述的场景，使用以下步骤创建明细表，以便在概念设计中分析体量的体积。
            models.add(new LAppModel());
            models.get(1).loadModelFiles(modelPath, modelJsonName);
            models.get(1).getModelMatrix().translateX(0.2f);
        }

        // レンダリングターゲットを切り替える-切换渲染目标
        LAppDelegate.getInstance().getView().switchRenderingTarget(useRenderingTarget);

        // 別レンダリング先を選択した際の背景クリア色-选择其他渲染目标时的背景透明色
        float[] clearColor = {0.0f, 0.0f, 0.0f};
        LAppDelegate.getInstance().getView().setRenderingTargetClearColor(clearColor[0], clearColor[1], clearColor[2]);
    }

    /**
     * 現在のシーンで保持しているモデルを返す
     *
     * @param number モデルリストのインデックス値
     * @return モデルのインスタンスを返す。インデックス値が範囲外の場合はnullを返す
     */
    public LAppModel getModel(int number) {
//        Log.d("model","有"+models.size()+"个");
        if (number < models.size()) {
            return models.get(number);
        }
        return null;
    }

    /**
     * シーンインデックスを返す
     *
     * @return シーンインデックス
     */
    public int getCurrentModel() {
        return currentModel;
    }

    /**
     * Return the number of models in this LAppLive2DManager instance has.
     *
     * @return number fo models in this LAppLive2DManager instance has. If models list is null, return 0.
     */
    public int getModelNum() {
        if (models == null) {
            return 0;
        }
        return models.size();
    }

    /**
     * モーション再生時に実行されるコールバック関数
     */
    private static class BeganMotion implements IBeganMotionCallback {
        @Override
        public void execute(ACubismMotion motion) {
            LAppPal.printLog("Motion Began: " + motion);
        }
    }

    private static final BeganMotion beganMotion = new BeganMotion();

    /**
     * モーション終了時に実行されるコールバック関数
     */
    private static class FinishedMotion implements IFinishedMotionCallback {
        @Override
        public void execute(ACubismMotion motion) {     //接口实现
            LAppPal.printLog("Motion Finished: " + motion);     //打印日志
        }
    }

    private static final FinishedMotion finishedMotion = new FinishedMotion();

    /**
     * シングルトンインスタンス
     */
    private static LAppLive2DManager s_instance;

    private LAppLive2DManager() {
        currentModel = 1;        //默认为第一个对象
        changeScene(currentModel);       //返回模型编号
    }


}
