package live2dsdk.madoka;

import android.opengl.GLES20;

import live2dsdk.basic.LAppDefine;
import com.live2d.sdk.cubism.framework.utils.CubismDebug;

/**
 * スプライト用のシェーダー設定を保持するクラス
 */
public class LAppSpriteShader implements AutoCloseable {
    /**
     * コンストラクタ
     */
    public LAppSpriteShader() {
        programId = createShader();
    }

    @Override
    public void close() {
        GLES20.glDeleteShader(programId);
    }

    /**
     * シェーダーIDを取得する。
     *
     * @return シェーダーID
     */
    public int getShaderId() {
        return programId;
    }

    /**
     * シェーダーを作成する。
     *
     * @return シェーダーID。正常に作成できなかった場合は0を返す。
     */
    private int createShader() {
        // シェーダーのパスの作成
        String vertShaderFile = LAppDefine.ResourcePath.SHADER_ROOT.getPath();
        vertShaderFile += ("/" + LAppDefine.ResourcePath.VERT_SHADER.getPath());

        String fragShaderFile = LAppDefine.ResourcePath.SHADER_ROOT.getPath();
        fragShaderFile += ("/" + LAppDefine.ResourcePath.FRAG_SHADER.getPath());

        // シェーダーのコンパイル
        int vertexShaderId = compileShader(vertShaderFile, GLES20.GL_VERTEX_SHADER);
        int fragmentShaderId = compileShader(fragShaderFile, GLES20.GL_FRAGMENT_SHADER);

        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0;
        }

        // プログラムオブジェクトの作成
        int programId = GLES20.glCreateProgram();

        // Programのシェーダーを設定
        GLES20.glAttachShader(programId, vertexShaderId);
        GLES20.glAttachShader(programId, fragmentShaderId);

        GLES20.glLinkProgram(programId);
        GLES20.glUseProgram(programId);

        // 不要になったシェーダーオブジェクトの削除
        GLES20.glDeleteShader(vertexShaderId);
        GLES20.glDeleteShader(fragmentShaderId);

        return programId;
    }

    /**
     * CreateShader内部関数。エラーチェックを行う。
     *
     * @param shaderId シェーダーID
     * @return エラーチェック結果。trueの場合、エラーなし。
     */
    private boolean checkShader(int shaderId) {
        int[] logLength = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_INFO_LOG_LENGTH, logLength, 0);

        if (logLength[0] > 0) {
            String log = GLES20.glGetShaderInfoLog(shaderId);
            CubismDebug.cubismLogError("Shader compile log: %s", log);
        }

        int[] status = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] == GLES20.GL_FALSE) {
            GLES20.glDeleteShader(shaderId);
            return false;
        }

        return true;
    }


    /**
     * シェーダーをコンパイルする。
     * コンパイルに成功したら0を返す。
     *
     * @param fileName シェーダーファイル名
     * @param shaderType 作成するシェーダーの種類
     * @return シェーダーID。正常に作成できなかった場合は0を返す。
     */
    private int compileShader(String fileName, int shaderType) {
        // ファイル読み込み
        byte[] shaderBuffer = LAppPal.loadFileAsBytes(fileName);

        // コンパイル
        int shaderId = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shaderId, new String(shaderBuffer));
        GLES20.glCompileShader(shaderId);

        if (!checkShader(shaderId)) {
            return 0;
        }

        return shaderId;
    }

    private final int programId; // シェーダーID
}
