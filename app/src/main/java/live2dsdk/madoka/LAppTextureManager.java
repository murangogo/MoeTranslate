/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package live2dsdk.madoka;

import static live2dsdk.basic.LAppDefine.DEBUG_LOG_ENABLE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import live2dsdk.basic.LAppDefine;
import com.live2d.sdk.cubism.framework.CubismFramework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// テクスチャの管理を行うクラス
public class LAppTextureManager {
    // 画像情報データクラス
    public static class TextureInfo {       //内部类，含有材质信息
        public int id;  // テクスチャID-纹理ID
        public int width;   // 横幅
        public int height;  // 高さ
        public String filePath; // ファイル名-文件名
    }

    // 画像読み込み-图像读取
    // imageFileOffset: glGenTexturesで作成したテクスチャの保存場所-image文件偏移量:glGen纹理，底层纹理的保存处
    public TextureInfo createTextureFromPngFile(String filePath) {
        // search loaded texture already-搜索已经加载的纹理
        for (TextureInfo textureInfo : textures) {      //遍历材质列表
            if (textureInfo.filePath.equals(filePath)) {        //如果找到传入的文件
                return textureInfo;     //返回材质详情
            }
        }

        if (DEBUG_LOG_ENABLE) {     //启用日志则打印日志
            LAppPal.printLog("createTextureFromPngFile: " + filePath );
        }

        InputStream stream = null;      // 清空输入流
        try {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                stream = new FileInputStream(file);
            } else {
                throw new FileNotFoundException("Cannot read file: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();        // 失败返回错误信息
        }

        // decodeStreamは乗算済みアルファとして画像を読み込むようである
        Bitmap bitmap = BitmapFactory.decodeStream(stream);     //处理位图

        // Texture0をアクティブにする-激活Texture0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // OpenGLにテクスチャを生成-在OpenGL中生成纹理
        int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        // メモリ上の2D画像をテクスチャに割り当てる-将内存上的2d图像分配给纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // ミップマップを生成する-生成图像
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        // 縮小時の補間設定-纹理的渲染设置
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        // 拡大時の補間設定-纹理的渲染设置
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        TextureInfo textureInfo = new TextureInfo();
        textureInfo.filePath = filePath;
        textureInfo.width = bitmap.getWidth();
        textureInfo.height = bitmap.getHeight();
        textureInfo.id = textureId[0];

        textures.add(textureInfo);      //将纹理加入列表

        // bitmap解放
        bitmap.recycle();       //回收位图内存
        bitmap = null;      //清空位图

        if (LAppDefine.DEBUG_LOG_ENABLE) {      //打印日志
            CubismFramework.coreLogFunction("Create texture: " + filePath);
        }

        return textureInfo;     //返回材质信息
    }

    private final List<TextureInfo> textures = new ArrayList<TextureInfo>();        // 画像情報のリスト-图像信息的列表
}
