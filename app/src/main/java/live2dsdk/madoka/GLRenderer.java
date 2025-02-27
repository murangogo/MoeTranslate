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

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//该类只实现了GLSurfaceView的Render接口并实现了接口的3个方法
public class GLRenderer implements GLSurfaceView.Renderer {
    // Called at initialization (when the drawing context is lost and recreated).-在初始化时调用（当图形上下文丢失并重新创建时）。
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        try{
            LAppDelegate.getInstance().onSurfaceCreated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Mainly called when switching between landscape and portrait.-主要在横向和纵向之间切换时调用。
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        try{
            LAppDelegate.getInstance().onSurfaceChanged(width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called repeatedly for drawing.-反复调用绘图
    @Override
    public void onDrawFrame(GL10 unused) {
        try{
            LAppDelegate.getInstance().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
