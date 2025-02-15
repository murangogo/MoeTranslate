/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

/*
 * Modified by murangogo in 2024
 * This file is derived from Live2D Cubism Components.
 * Modifications:
 * - Some functions have been simplified to suit the needs of the app
 * Original source: https://github.com/Live2D/CubismJavaSamples
 */

package live2dsdk.basic;

import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel;

/**
 * Constants used in this sample app.
 */
public class LAppDefine {       //该类主要定义一些常量
    /**
     * Scaling rate.
     */
    public enum Scale {     //枚举，包含大小规模
        /**
         * Default scaling rate
         */
        DEFAULT(1.0f),
        /**
         * Maximum scaling rate
         */
        MAX(2.0f),
        /**
         * Minimum scaling rate
         */
        MIN(0.8f);

        private final float value;

        Scale(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Logical view coordinate system.
     */
    public enum LogicalView {       //枚举，包含控件大小
        /**
         * Left end
         */
        LEFT(-1.0f),
        /**
         * Right end
         */
        RIGHT(1.0f),
        /**
         * Bottom end
         */
        BOTTOM(-1.0f),
        /**
         * Top end
         */
        TOP(1.0f);

        private final float value;

        LogicalView(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Maximum logical view coordinate system.
     */
    public enum MaxLogicalView {        //枚举，定义控件范围
        /**
         * Maximum left end
         */
        LEFT(-2.0f),
        /**
         * Maximum right end
         */
        RIGHT(2.0f),
        /**
         * Maximum bottom end
         */
        BOTTOM(-2.0f),
        /**
         * Maximum top end
         */
        TOP(2.0f);

        private final float value;

        MaxLogicalView(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Path of image materials.
     */
    public enum ResourcePath {      //枚举，定义图片路径
        /**
         * Relative path of the material directory
         */
        ROOT(""),
        /**
         * Relative path of shader directory
         */
        SHADER_ROOT("Shaders"),
        /**
         * Background image file
         */
        BACK_IMAGE("bg.png"),
        /**
         * Gear image file
         */
        GEAR_IMAGE("icon_gear.png"),
        /**
         * Power button image file
         */
        POWER_IMAGE("close.png"),
        /**
         * Vertex shader file
         */
        VERT_SHADER("VertSprite.vert"),
        /**
         * Fragment shader file
         */
        FRAG_SHADER("FragSprite.frag");

        private final String path;

        ResourcePath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * Model directory name.
     */
//    public enum ModelDir {      //枚举，定义模型位置路径
//        MADOKA_SCHOOL(0, "Madoka_School"),
//        MADOKA_MAGIC(1, "Madoka_Magic"),
//        MADOKA_PAJAMAS(2,"Madoka_Pajamas"),
//        MADOKA_VALENTINE(3, "Madoka_Valentine"),
//        MADOKA_NEWYEAR(4, "Madoka_NewYear"),
//        MADOKA_SWIMSUIT(5, "Madoka_Swimsuit"),
//        MADOKA_SWIMWEAR(6, "Madoka_Swimwear"),
//        MADOKA_DAYWEAR(6, "Madoka_Daywear"),
//        MADOKA_SCENE0(6, "Madoka_Scene0");
//
//
//        private final int order;
//        private final String dirName;
//
//        ModelDir(int order, String dirName) {
//            this.order = order;
//            this.dirName = dirName;
//        }
//
//        public int getOrder() {
//            return order;
//        }
//
//        public String getDirName() {
//            return dirName;
//        }
//
//    }

    /**
     * Motion group
     */
    public enum MotionGroup {       //枚举，定义动作
        /**
         * ID of the motion to be played at idling.
         */
        IDLE("Motion"),
        /**
         * ID of the motion to be played at tapping body.
         */
        TAP_BODY("TapBody");

        private final String id;

        MotionGroup(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * [Head] tag for hit detection.
     * (Match with external definition file(json))
     */
    public enum HitAreaName {       //枚举，定义身体部位
        HEAD("Head"),
        BODY("Body");

        private final String id;

        HitAreaName(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Motion priority
     */
    public enum Priority {      //枚举
        NONE(0),
        IDLE(1),
        NORMAL(2),
        FORCE(3);

        private final int priority;

        Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * MOC3の整合性を検証するかどうか。有効ならtrue。
     */
    public static final boolean MOC_CONSISTENCY_VALIDATION_ENABLE = true;

    /**
     * Enable/Disable debug logging.
     */
    public static final boolean DEBUG_LOG_ENABLE = true;
    /**
     * Enable/Disable debug logging for processing tapping information.
     */
    public static final boolean DEBUG_TOUCH_LOG_ENABLE = true;
    /**
     * Setting the level of the log output from the Framework.
     */
    public static final LogLevel cubismLoggingLevel = LogLevel.VERBOSE;
    /**
     * Enable/Disable premultiplied alpha.
     */
    public static final boolean PREMULTIPLIED_ALPHA_ENABLE = true;

    /**
     * Flag whether to draw to the target held by LAppView. (If both USE_RENDER_TARGET and USE_MODEL_RENDER_TARGET are true, this variable is given priority over USE_MODEL_RENDER_TARGET.)
     */
    public static final boolean USE_RENDER_TARGET = false;
    /**
     * Flag whether to draw to the target that each LAppModel has.
     */
    public static final boolean USE_MODEL_RENDER_TARGET = false;
}
