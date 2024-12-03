/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package live2dsdk.madoka;

import live2dsdk.basic.LAppDefine;
import com.live2d.sdk.cubism.framework.CubismDefaultParameterId.ParameterId;
import com.live2d.sdk.cubism.framework.CubismFramework;
import com.live2d.sdk.cubism.framework.CubismModelSettingJson;
import com.live2d.sdk.cubism.framework.ICubismModelSetting;
import com.live2d.sdk.cubism.framework.effect.CubismBreath;
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink;
import com.live2d.sdk.cubism.framework.id.CubismId;
import com.live2d.sdk.cubism.framework.id.CubismIdManager;
import com.live2d.sdk.cubism.framework.math.CubismMatrix44;
import com.live2d.sdk.cubism.framework.model.CubismMoc;
import com.live2d.sdk.cubism.framework.model.CubismUserModel;
import com.live2d.sdk.cubism.framework.motion.ACubismMotion;
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion;
import com.live2d.sdk.cubism.framework.motion.CubismMotion;
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback;
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback;
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer;
import com.live2d.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid;
import com.live2d.sdk.cubism.framework.rendering.android.CubismRendererAndroid;
import com.live2d.sdk.cubism.framework.utils.CubismDebug;

import java.util.*;

public class LAppModel extends CubismUserModel {
    public LAppModel() {
        if (LAppDefine.MOC_CONSISTENCY_VALIDATION_ENABLE) {
            mocConsistency = true;
        }

        if (LAppDefine.DEBUG_LOG_ENABLE) {      //日志打开or关闭
            debugMode = true;
        }

        CubismIdManager idManager = CubismFramework.getIdManager();

        idParamAngleX = idManager.getId(ParameterId.ANGLE_X.getId());
        idParamAngleY = idManager.getId(ParameterId.ANGLE_Y.getId());
        idParamAngleZ = idManager.getId(ParameterId.ANGLE_Z.getId());
        idParamBodyAngleX = idManager.getId(ParameterId.BODY_ANGLE_X.getId());
        idParamEyeBallX = idManager.getId(ParameterId.EYE_BALL_X.getId());
        idParamEyeBallY = idManager.getId(ParameterId.EYE_BALL_Y.getId());
    }

    public void loadModelFiles(final String dir, final String fileName) {       //加载模型
        if (LAppDefine.DEBUG_LOG_ENABLE) {     //打印日志
            LAppPal.printLog("load model setting: " + fileName);
        }

        modelHomeDirectory = dir;       //获取模型文件夹
        String filePath = modelHomeDirectory + fileName;

        // json読み込み
        byte[] buffer = createBuffer(filePath);

        ICubismModelSetting setting = new CubismModelSettingJson(buffer);

        // Setup model
        setupModel(setting);

        if (model == null) {
            LAppPal.printLog("Failed to loadAssets().");
            return;
        }

        // Setup renderer.
        CubismRenderer renderer = CubismRendererAndroid.create();
        setupRenderer(renderer);

        setupTextures();
    }

    /**
     * Delete the model which LAppModel has.
     */
    public void deleteModel() {
        delete();
    }

    /**
     * モデルの更新処理。モデルのパラメーターから描画状態を決定する
     */
    public void update() {
        final float deltaTimeSeconds = LAppPal.getDeltaTime();
        userTimeSeconds += deltaTimeSeconds;

        dragManager.update(deltaTimeSeconds);
        dragX = dragManager.getX();
        dragY = dragManager.getY();

        // モーションによるパラメーター更新の有無-是否根据运动更新参数
        boolean isMotionUpdated = false;

//         前回セーブされた状態をロード-加载上次被保存的状态
        model.loadParameters();

        // モーションの再生がない場合、待機モーションの中からランダムで再生する-没有动作再生时，从待机动作中随机再生
        if (!(motionManager.isFinished())) {
            // モーションを更新-更新动作
            isMotionUpdated = motionManager.updateMotion(model, deltaTimeSeconds);
        }

        // モデルの状態を保存-保存模型状态
        model.saveParameters();

        // 不透明度
        opacity = model.getModelOpacity();

        // eye blink
        // メインモーションの更新がないときだけまばたきする-只在没有更新主动作时眨眼
        if (!isMotionUpdated) {
            if (eyeBlink != null) {
                eyeBlink.updateParameters(model, deltaTimeSeconds);
            }
        }

        // expression
        if (expressionManager != null) {
            // 表情でパラメータ更新（相対変化）-通过表情更新参数（相对变化）
            expressionManager.updateMotion(model, deltaTimeSeconds);
        }

        // ドラッグ追従機能-拖动跟踪功能
        // ドラッグによる顔の向きの調整-通过拖动调整脸的方向
        model.addParameterValue(idParamAngleX, dragX * 30); // -30から30の値を加える
        model.addParameterValue(idParamAngleY, dragY * 30);
        model.addParameterValue(idParamAngleZ, dragX * dragY * (-30));

        // ドラッグによる体の向きの調整-通过拖动调整身体方向
        model.addParameterValue(idParamBodyAngleX, dragX * 10); // -10から10の値を加える

        // ドラッグによる目の向きの調整-通过拖动调整眼睛方向
        model.addParameterValue(idParamEyeBallX, dragX);  // -1から1の値を加える
        model.addParameterValue(idParamEyeBallY, dragY);

        // Breath Function
        if (breath != null) {
            breath.updateParameters(model, deltaTimeSeconds);
        }

        // Physics Setting
        if (physics != null) {
            physics.evaluate(model, deltaTimeSeconds);
        }

        // Lip Sync Setting
        if (lipSync) {
            // リアルタイムでリップシンクを行う場合、システムから音量を取得して0~1の範囲で値を入力します-当实时进行唇部同步时，从系统获取音量，并在0到1的范围内输入值
            float value = 0.0f;

            for (int i = 0; i < lipSyncIds.size(); i++) {
                CubismId lipSyncId = lipSyncIds.get(i);
                model.addParameterValue(lipSyncId, value, 0.8f);
            }
        }

        // Pose Setting
        if (pose != null) {
            pose.updateParameters(model, deltaTimeSeconds);
        }

        model.update();
    }

    /**
     * 引数で指定したモーションの再生を開始する。
     * コールバック関数が渡されなかった場合にそれをnullとして同メソッドを呼び出す。
     *
     * @param group モーショングループ名
     * @param number グループ内の番号
     * @param priority 優先度
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判別するisFinished()の引数で使用する。開始できない時は「-1」
     */
    public int startMotion(final String group, int number, int priority) {
        return startMotion(group, number, priority, null, null);
    }

    /**
     * 引数で指定したモーションの再生を開始する。
     *
     * @param group モーショングループ名
     * @param number グループ内の番号
     * @param priority 優先度
     * @param onFinishedMotionHandler モーション再生終了時に呼び出されるコールバック関数。nullの場合は呼び出されない。
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数で使用する。開始できない時は「-1」
     */
    public int startMotion(final String group,
                           int number,
                           int priority,
                           IFinishedMotionCallback onFinishedMotionHandler,
                           IBeganMotionCallback onBeganMotionHandler
    ) {
        if (priority == LAppDefine.Priority.FORCE.getPriority()) {
            motionManager.setReservationPriority(priority);
        } else if (!motionManager.reserveMotion(priority)) {
            if (debugMode) {
                LAppPal.printLog("Cannot start motion.");
            }
            return -1;
        }

        // ex) idle_0
        String name = group + "_" + number;

        if (debugMode) {
            LAppPal.printLog("String name = " + name);
        }

        CubismMotion motion = (CubismMotion) motions.get(name);

        if (debugMode) {
            // 打印所有可用的动作
            LAppPal.printLog("Available motions:");
            for (Map.Entry<String, ACubismMotion> entry : motions.entrySet()) {
                LAppPal.printLog("  " + entry.getKey() + " -> " + entry.getValue());
            }
        }

        if (motion == null) {
            String fileName = modelSetting.getMotionFileName(group, number);
            if (!fileName.equals("")) {
                String path = modelHomeDirectory + fileName;

                byte[] buffer;
                buffer = createBuffer(path);

                motion = loadMotion(buffer, onFinishedMotionHandler, onBeganMotionHandler);
                if (motion != null) {
                    final float fadeInTime = modelSetting.getMotionFadeInTimeValue(group, number);

                    if (fadeInTime != -1.0f) {
                        motion.setFadeInTime(fadeInTime);
                    }

                    final float fadeOutTime = modelSetting.getMotionFadeOutTimeValue(group, number);
                    if (fadeOutTime != -1.0f) {
                        motion.setFadeOutTime(fadeOutTime);
                    }

                    motion.setEffectIds(eyeBlinkIds, lipSyncIds);
                }
            }
        } else {
            motion.setBeganMotionHandler(onBeganMotionHandler);
            motion.setFinishedMotionHandler(onFinishedMotionHandler);
        }

        // load sound files
        String voice = modelSetting.getMotionSoundFileName(group, number);
        if (!voice.equals("")) {
            String path = modelHomeDirectory + voice;

            // 別スレッドで音声再生
            LAppWavFileHandler voicePlayer = new LAppWavFileHandler(path);
            voicePlayer.start();
        }

        if (debugMode) {
            LAppPal.printLog("start motion: " + group + "_" + number);
        }
        return motionManager.startMotionPriority(motion, priority);
    }

    public int startMotionCustom(final String motionName,
                           IFinishedMotionCallback onFinishedMotionHandler,
                           IBeganMotionCallback onBeganMotionHandler
    ) throws Exception {
        // 优先级最高
        motionManager.setReservationPriority(LAppDefine.Priority.FORCE.getPriority());

        if (debugMode) {
            LAppPal.printLog("Motion name = " + motionName);
        }

        CubismMotion motion = (CubismMotion) motions.get(motionName);

        if (debugMode) {
            // 打印所有可用的动作
            LAppPal.printLog("Available motions:");
            for (Map.Entry<String, ACubismMotion> entry : motions.entrySet()) {
                LAppPal.printLog("  " + entry.getKey() + " -> " + entry.getValue());
            }
        }

        if (motion == null){
            throw new Exception("Null Motion File.");
        }
        motion.setBeganMotionHandler(onBeganMotionHandler);
        motion.setFinishedMotionHandler(onFinishedMotionHandler);


        if (debugMode) {
            LAppPal.printLog("start motion: " + motionName);
        }

        return motionManager.startMotionPriority(motion, LAppDefine.Priority.FORCE.getPriority());
    }

    /**
     * ランダムに選ばれたモーションの再生を開始する。
     * コールバック関数が渡されなかった場合にそれをnullとして同メソッドを呼び出す。
     *
     * @param group モーショングループ名
     * @param priority 優先度
     * @return 開始したモーションの識別番号。個別のモーションが終了したか否かを判定するisFinished()の引数で使用する。開始できない時は「-1」
     */
    public int startRandomMotion(final String group, int priority) {
        return startRandomMotion(group, priority, null, null);
    }

    /**
     * ランダムに選ばれたモーションの再生を開始する。
     *
     * @param group モーショングループ名
     * @param priority 優先度
     * @param onFinishedMotionHandler モーション再生終了時に呼び出されるコールバック関数。nullの場合は呼び出されない。
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数で使用する。開始できない時は-1
     */
    public int startRandomMotion(final String group, int priority, IFinishedMotionCallback onFinishedMotionHandler, IBeganMotionCallback onBeganMotionHandler) {
        if (modelSetting.getMotionCount(group) == 0) {  //没有表情
            return -1;
        }

        Random random = new Random();   //新建随机值对象
        int number = random.nextInt(Integer.MAX_VALUE) % modelSetting.getMotionCount(group);

        return startMotion(group, number, priority, onFinishedMotionHandler, onBeganMotionHandler);
    }

    public void draw(CubismMatrix44 matrix) {
        if (model == null) {
            return;
        }

        // キャッシュ変数の定義を避けるために、multiplyByMatrix()ではなく、multiply()を使用する。
        CubismMatrix44.multiply(
                modelMatrix.getArray(),
                matrix.getArray(),
                matrix.getArray()
        );

        this.<CubismRendererAndroid>getRenderer().setMvpMatrix(matrix);
        this.<CubismRendererAndroid>getRenderer().drawModel();
    }

    /**
     * 当たり判定テスト
     * 指定IDの頂点リストから矩形を計算し、座標が矩形範囲内か判定する
     *
     * @param hitAreaName 当たり判定をテストする対象のID
     * @param x 判定を行うx座標
     * @param y 判定を行うy座標
     * @return 当たっているならtrue
     */
    public boolean hitTest(final String hitAreaName, float x, float y) {    //点击判定
        // 透明時は当たり判定なし-透明时没有准确判定
        if (opacity < 1) {  //透明？是则无效点击
            return false;
        }

        final int count = modelSetting.getHitAreasCount();  //这个主要判断有几个位置是“传进来的那个位置”
        for (int i = 0; i < count; i++) {
            if (modelSetting.getHitAreaName(i).equals(hitAreaName)) {       //判定点击的是哪个“传进来的那个位置”
                final CubismId drawID = modelSetting.getHitAreaId(i);

                return isHit(drawID, x, y);     //回应是否点到了头部
            }
        }
        // 存在しない場合はfalse
        return false;
    }

    /**
     * 引数で指定した表情モーションを設定する
     *
     * @param expressionID 表情モーションのID
     */
    public void setExpression(final String expressionID) {
        ACubismMotion motion = expressions.get(expressionID);

        if (debugMode) {        //打印日志
            LAppPal.printLog("expression: " + expressionID);

            // 打印所有可用的表情
            LAppPal.printLog("Available expressions:");
            for (Map.Entry<String, ACubismMotion> entry : expressions.entrySet()) {
                LAppPal.printLog("  " + entry.getKey() + " -> " + entry.getValue());
            }
        }

        if (motion != null) {       //有该表情，直接播放
            expressionManager.startMotionPriority(motion, LAppDefine.Priority.FORCE.getPriority());
        } else {
            if (debugMode) {    //无该表情，打印日志
                LAppPal.printLog("expression " + expressionID + "is null");
            }
        }
    }

    /**
     * ランダムに選ばれた表情モーションを設定する
     */
    public void setRandomExpression() {     //设置为随机表情
        if (expressions.size() == 0) {      //没有表情则直接return
            return;
        }

        Random random = new Random();       //新的random对象
        int number = random.nextInt(Integer.MAX_VALUE) % expressions.size();

        int i = 0;
        for (String key : expressions.keySet()) {
            if (i == number) {
                setExpression(key); //设置表情
                return;
            }
            i++;
        }
    }

    public CubismOffscreenSurfaceAndroid getRenderingBuffer() {
        return renderingBuffer;
    }

    /**
     * .moc3ファイルの整合性をチェックする。
     *
     * @param mocFileName MOC3ファイル名
     * @return MOC3に整合性があるかどうか。整合性があればtrue。
     */
    public boolean hasMocConsistencyFromFile(String mocFileName) {
        assert mocFileName != null && !mocFileName.isEmpty();

        String path = mocFileName;
        path = modelHomeDirectory + path;

        byte[] buffer = createBuffer(path);
        boolean consistency = CubismMoc.hasMocConsistency(buffer);

        if (!consistency) {
            CubismDebug.cubismLogInfo("Inconsistent MOC3.");
        } else {
            CubismDebug.cubismLogInfo("Consistent MOC3.");
        }

        return consistency;
    }

    private static byte[] createBuffer(final String path) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("create buffer: " + path);
        }
        return LAppPal.loadFileAsBytes(path);
    }

    // model3.jsonからモデルを生成する
    private void setupModel(ICubismModelSetting setting) {
        modelSetting = setting;

        isUpdated = true;
        isInitialized = false;

        // Load Cubism Model
        {
            String fileName = modelSetting.getModelFileName();
            if (!fileName.equals("")) {
                String path = modelHomeDirectory + fileName;

                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("create model: " + modelSetting.getModelFileName());
                }

                byte[] buffer = createBuffer(path);
                loadModel(buffer, mocConsistency);
            }
        }

        // load expression files(.exp3.json)
        {
            if (modelSetting.getExpressionCount() > 0) {
                final int count = modelSetting.getExpressionCount();

                for (int i = 0; i < count; i++) {
                    String name = modelSetting.getExpressionName(i);
                    String path = modelSetting.getExpressionFileName(i);
                    path = modelHomeDirectory + path;

                    byte[] buffer = createBuffer(path);
                    CubismExpressionMotion motion = loadExpression(buffer);

                    if (debugMode) {
                        LAppPal.printLog("load expression: " + path + "==>[" + name + "]");
                    }

                    if (motion != null) {
                        expressions.put(name, motion);
                    }
                }
            }
        }

        // Physics
        {
            String path = modelSetting.getPhysicsFileName();
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;
                byte[] buffer = createBuffer(modelPath);

                loadPhysics(buffer);
            }
        }

        // Pose
        {
            String path = modelSetting.getPoseFileName();
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;
                byte[] buffer = createBuffer(modelPath);
                loadPose(buffer);
            }
        }

        // Load eye blink data
        if (modelSetting.getEyeBlinkParameterCount() > 0) {
            eyeBlink = CubismEyeBlink.create(modelSetting);
        }

        // Load Breath Data
        breath = CubismBreath.create();
        List<CubismBreath.BreathParameterData> breathParameters = new ArrayList<CubismBreath.BreathParameterData>();

        breathParameters.add(new CubismBreath.BreathParameterData(idParamAngleX, 0.0f, 15.0f, 6.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamAngleY, 0.0f, 8.0f, 3.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamAngleZ, 0.0f, 10.0f, 5.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamBodyAngleX, 0.0f, 4.0f, 15.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(CubismFramework.getIdManager().getId(ParameterId.BREATH.getId()), 0.5f, 0.5f, 3.2345f, 0.5f));

        breath.setParameters(breathParameters);

        // Load UserData
        {
            String path = modelSetting.getUserDataFile();
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;
                byte[] buffer = createBuffer(modelPath);
                loadUserData(buffer);
            }
        }


        // EyeBlinkIds
        int eyeBlinkIdCount = modelSetting.getEyeBlinkParameterCount();
        for (int i = 0; i < eyeBlinkIdCount; i++) {
            eyeBlinkIds.add(modelSetting.getEyeBlinkParameterId(i));
        }

        // LipSyncIds
        int lipSyncIdCount = modelSetting.getLipSyncParameterCount();
        for (int i = 0; i < lipSyncIdCount; i++) {
            lipSyncIds.add(modelSetting.getLipSyncParameterId(i));
        }

        if (modelSetting == null || modelMatrix == null) {
            LAppPal.printLog("Failed to setupModel().");
            return;
        }

        // Set layout
        Map<String, Float> layout = new HashMap<String, Float>();

        // レイアウト情報が存在すればその情報からモデル行列をセットアップする
        if (modelSetting.getLayoutMap(layout)) {
            modelMatrix.setupFromLayout(layout);
        }

        model.saveParameters();

        // Load motions
        for (int i = 0; i < modelSetting.getMotionGroupCount(); i++) {
            String group = modelSetting.getMotionGroupName(i);
            preLoadMotionGroup(group);
        }

        motionManager.stopAllMotions();

        isUpdated = false;
        isInitialized = true;
    }

    /**
     * モーションデータをグループ名から一括でロードする。
     * モーションデータの名前はModelSettingから取得する。
     *
     * @param group モーションデータのグループ名
     **/
    private void preLoadMotionGroup(final String group) {
        final int count = modelSetting.getMotionCount(group);

        for (int i = 0; i < count; i++) {
            // ex) idle_0
            // 修改name为motion的File
//            String name = group + "_" + i;
            String name = modelSetting.getMotionFileName(group, i);

            String path = modelSetting.getMotionFileName(group, i);
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;

                if (debugMode) {
                    LAppPal.printLog("load motion: " + path + "==>[" + group + "_" + i + "]");
                }

                byte[] buffer;
                buffer = createBuffer(modelPath);

                // If a motion cannot be loaded, a process is skipped.
                CubismMotion tmp = loadMotion(buffer);
                if (tmp == null) {
                    continue;
                }

                final float fadeInTime = modelSetting.getMotionFadeInTimeValue(group, i);

                if (fadeInTime != -1.0f) {
                    tmp.setFadeInTime(fadeInTime);
                }

                final float fadeOutTime = modelSetting.getMotionFadeOutTimeValue(group, i);

                if (fadeOutTime != -1.0f) {
                    tmp.setFadeOutTime(fadeOutTime);
                }

                tmp.setEffectIds(eyeBlinkIds, lipSyncIds);
                motions.put(name, tmp);
            }
        }
    }

    /**
     * OpenGLのテクスチャユニットにテクスチャをロードする
     */
    private void setupTextures() {
        for (int modelTextureNumber = 0; modelTextureNumber < modelSetting.getTextureCount(); modelTextureNumber++) {
            // テクスチャ名が空文字だった場合はロード・バインド処理をスキップ
            if (modelSetting.getTextureFileName(modelTextureNumber).equals("")) {
                continue;
            }

            // OpenGL ESのテクスチャユニットにテクスチャをロードする
            String texturePath = modelSetting.getTextureFileName(modelTextureNumber);
            texturePath = modelHomeDirectory + texturePath;

            LAppTextureManager.TextureInfo texture =
                    LAppDelegate.getInstance()
                            .getTextureManager()
                            .createTextureFromPngFile(texturePath);
            final int glTextureNumber = texture.id;

            this.<CubismRendererAndroid>getRenderer().bindTexture(modelTextureNumber, glTextureNumber);

            if (LAppDefine.PREMULTIPLIED_ALPHA_ENABLE) {
                this.<CubismRendererAndroid>getRenderer().isPremultipliedAlpha(true);
            } else {
                this.<CubismRendererAndroid>getRenderer().isPremultipliedAlpha(false);
            }
        }
    }


    private ICubismModelSetting modelSetting;
    /**
     * モデルのホームディレクトリ
     */
    private String modelHomeDirectory;
    /**
     * デルタ時間の積算値[秒]
     */
    private float userTimeSeconds;

    /**
     * モデルに設定されたまばたき機能用パラメーターID
     */
    private final List<CubismId> eyeBlinkIds = new ArrayList<CubismId>();
    /**
     * モデルに設定されたリップシンク機能用パラメーターID
     */
    private final List<CubismId> lipSyncIds = new ArrayList<CubismId>();
    /**
     * 読み込まれているモーションのマップ
     */
    private final Map<String, ACubismMotion> motions = new HashMap<String, ACubismMotion>();
    /**
     * 読み込まれている表情のマップ
     */
    private final Map<String, ACubismMotion> expressions = new HashMap<String, ACubismMotion>();

    /**
     * パラメーターID: ParamAngleX
     */
    private final CubismId idParamAngleX;
    /**
     * パラメーターID: ParamAngleY
     */
    private final CubismId idParamAngleY;
    /**
     * パラメーターID: ParamAngleZ
     */
    private final CubismId idParamAngleZ;
    /**
     * パラメーターID: ParamBodyAngleX
     */
    private final CubismId idParamBodyAngleX;
    /**
     * パラメーターID: ParamEyeBallX
     */
    private final CubismId idParamEyeBallX;
    /**
     * パラメーターID: ParamEyeBallY
     */
    private final CubismId idParamEyeBallY;

    /**
     * フレームバッファ以外の描画先
     */
    private final CubismOffscreenSurfaceAndroid renderingBuffer = new CubismOffscreenSurfaceAndroid();
}
