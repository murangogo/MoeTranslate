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

package live2dsdk.madoka;

import static live2dsdk.basic.LAppDefine.DEBUG_LOG_ENABLE;

import android.util.Log;
import live2dsdk.basic.LAppDefine;
import com.live2d.sdk.cubism.core.ICubismLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LAppPal {
    /**
     * Logging Function class to be registered in the CubismFramework's logging function.
     */
    public static class PrintLogFunction implements ICubismLogger {     //打印日志
        @Override
        public void print(String message) {
            Log.d(TAG, message);
        }
    }

    // アプリケーションを中断状態にする。実行されるとonPause()イベントが発生する-让应用程序处于中断状态。执行时发生onPause()事件
    public static void moveTaskToBack() {
        LAppDelegate.getInstance().getActivity().moveTaskToBack(true);
    }

    // デルタタイムの更新
    public static void updateTime() {   //更新时间
        s_currentFrame = getSystemNanoTime();   //获取当前时间
        _deltaNanoTime = s_currentFrame - _lastNanoTime;        //计算时间差
        _lastNanoTime = s_currentFrame;     //更新时间
    }

    // ファイルをバイト列として読み込む-将文件导入为字节列
    public static byte[] loadFileAsBytes(final String path) {       //按位加载文件
        InputStream fileData = null;        //清空输入流
        try {
            if (DEBUG_LOG_ENABLE) {     //启用日志则打印日志
                LAppPal.printLog("loadFileAsBytes(final String path): " + path);
            }

            fileData = new FileInputStream(new File(path));     //主活动打开路径

            int fileSize = fileData.available();        //读取文件大小
            byte[] fileBuffer = new byte[fileSize];     //创建缓冲区
            fileData.read(fileBuffer, 0, fileSize);     //把文件读进内存

            return fileBuffer;      //返回文件数组
        } catch (IOException e) {       //失败则打印错误信息
            e.printStackTrace();

            if (DEBUG_LOG_ENABLE) {
                printLog("File open error.");
            }

            return new byte[0];
        } finally {
            try {
                if (fileData != null) {     //关闭文件流
                    fileData.close();
                }
            } catch (IOException e) {       //失败则打印错误信息
                e.printStackTrace();

                if (DEBUG_LOG_ENABLE) {
                    printLog("File open error.");
                }
            }
        }
    }

    // デルタタイム(前回フレームとの差分)を取得する-获取delta时间(与前一帧的差)
    public static float getDeltaTime() {    //时间差，转换为秒
        // ナノ秒を秒に変換
        return (float) (_deltaNanoTime / 1000000000.0f);
    }

    /**
     * Logging function
     *
     * @param message log message
     */
    public static void printLog(String message) {
        Log.d(TAG, message);
    }       //打印日志

    private static long getSystemNanoTime() {
        return System.nanoTime();
    }       //获取cpu时间

    private static double s_currentFrame;
    private static double _lastNanoTime;
    private static double _deltaNanoTime;

    private static final String TAG = "[Madoka Live2D]";

    private LAppPal() {}        //防止实例化
}
