package live2dsdk.madoka;


import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import live2dsdk.basic.LAppDefine;

public class ClothPlay {
    public static String[][] motion1 = new String[9][5];
    public static String[][] motion2 = new String[9][5];
    public static boolean[][] isContinue = new boolean[9][5];
    public static String[][] emotion1 = new String[9][5];
    public static String[][] emotion2 = new String[9][5];
    public static Thread doEandM = null;
    static {
        motion1[0][0] = "2";emotion1[0][0] = "011";
        motion1[0][1] = "1";emotion1[0][1] = "010";
        motion1[0][2] = "2";emotion1[0][2] = "010";
        motion1[0][3] = "4";emotion1[0][3] = "020";
        motion1[0][4] = "1";emotion1[0][4] = "010";
        motion1[1][0] = "7";emotion1[1][0] = "011";
        motion1[1][1] = "5";emotion1[1][1] = "040";
        motion1[1][2] = "1";emotion1[1][2] = "040";
        motion1[1][3] = "7";emotion1[1][3] = "030";
        motion1[1][4] = "2";emotion1[1][4] = "040";
        motion1[2][0] = "1";emotion1[2][0] = "011";
        motion1[2][1] = "2";emotion1[2][1] = "010";
        motion1[2][2] = "1";emotion1[2][2] = "011";
        motion1[2][3] = "1";emotion1[2][3] = "010";
        motion1[2][4] = "6";emotion1[2][4] = "040";
        motion1[3][0] = "2";emotion1[3][0] = "011";
        motion1[3][1] = "3";emotion1[3][1] = "010";
        motion1[3][2] = "2";emotion1[3][2] = "010";
        motion1[3][3] = "1";emotion1[3][3] = "030";
        motion1[3][4] = "2";emotion1[3][4] = "010";
        motion1[4][0] = "4";emotion1[4][0] = "011";
        motion1[4][1] = "2";emotion1[4][1] = "011";
        motion1[4][2] = "2";emotion1[4][2] = "010";
        motion1[4][3] = "1";emotion1[4][3] = "010";
        motion1[4][4] = "5";emotion1[4][4] = "011";
        motion1[5][0] = "4";emotion1[5][0] = "010";
        motion1[5][1] = "1";emotion1[5][1] = "040";
        motion1[5][2] = "0";emotion1[5][2] = "010";
        motion1[5][3] = "2";emotion1[5][3] = "011";
        motion1[5][4] = "0";emotion1[5][4] = "010";
        motion1[6][0] = "5";emotion1[6][0] = "011";
        motion1[6][1] = "2";emotion1[6][1] = "011";
        motion1[6][2] = "4";emotion1[6][2] = "040";
        motion1[6][3] = "5";emotion1[6][3] = "051";
        motion1[6][4] = "0";emotion1[6][4] = "011";
        motion1[7][0] = "1";emotion1[7][0] = "010";
        motion1[7][1] = "0";emotion1[7][1] = "010";
        motion1[7][2] = "2";emotion1[7][2] = "011";
        motion1[7][3] = "0";emotion1[7][3] = "010";
        motion1[7][4] = "5";emotion1[7][4] = "011";
        motion1[8][0] = "2";emotion1[8][0] = "011";
        motion1[8][1] = "4";emotion1[8][1] = "040";
        motion1[8][2] = "5";emotion1[8][2] = "051";
        motion1[8][3] = "0";emotion1[8][3] = "011";
        motion1[8][4] = "0";emotion1[8][4] = "011";
    }
    static {
        isContinue[0][1] = true;
        isContinue[0][2] = true;
        isContinue[0][3] = true;
        isContinue[1][2] = true;
        isContinue[1][3] = true;
        isContinue[1][4] = true;
        isContinue[2][2] = true;
        isContinue[2][3] = true;
        isContinue[2][4] = true;
        isContinue[3][0] = true;
        isContinue[3][1] = true;
        isContinue[3][2] = true;
        isContinue[4][2] = true;
        isContinue[4][3] = true;
        isContinue[4][4] = true;
        isContinue[5][0] = true;
        isContinue[5][1] = true;
        isContinue[5][2] = true;
        isContinue[5][3] = true;
        isContinue[5][4] = true;
        isContinue[6][0] = true;
        isContinue[6][1] = true;
        isContinue[6][2] = true;
        isContinue[6][3] = true;
        isContinue[6][4] = true;
}
    static {
        motion2[0][1] = "2";emotion2[0][1] = "011";
        motion2[0][2] = "5";emotion2[0][2] = "011";
        motion2[0][3] = "1";emotion2[0][3] = "030";
        motion2[1][2] = "2";emotion2[1][2] = "040";
        motion2[1][3] = "1";emotion2[1][3] = "041";
        motion2[1][4] = "1";emotion2[1][4] = "041";
        motion2[2][2] = "0";emotion2[2][2] = "040";
        motion2[2][3] = "5";emotion2[2][3] = "011";
        motion2[2][4] = "2";emotion2[2][4] = "011";
        motion2[3][0] = "1";emotion2[3][0] = "010";
        motion2[3][1] = "1";emotion2[3][1] = "011";
        motion2[3][2] = "0";emotion2[3][2] = "020";
        motion2[4][2] = "1";emotion2[4][2] = "011";
        motion2[4][3] = "1";emotion2[4][3] = "010";
        motion2[4][4] = "4";emotion2[4][4] = "010";
        motion2[5][0] = "1";emotion2[5][0] = "011";
        motion2[5][1] = "0";emotion2[5][1] = "011";
        motion2[5][2] = "1";emotion2[5][2] = "011";
        motion2[5][3] = "0";emotion2[5][3] = "010";
        motion2[5][4] = "2";emotion2[5][4] = "011";
        motion2[6][0] = "2";emotion2[6][0] = "010";
        motion2[6][1] = "4";emotion2[6][1] = "010";
        motion2[6][2] = "2";emotion2[6][2] = "011";
        motion2[6][3] = "0";emotion2[6][3] = "060";
        motion2[6][4] = "1";emotion2[6][4] = "011";
    }
    public static void PlayIt(int i, int j,Activity activity){
//        Log.d("衣服"+i,"句子"+j);
        try {
           doEandM = new Thread(new Runnable() {
               @Override
               public void run() {
                   Log.d("播放第一个","");
                   LAppLive2DManager.getInstance().getModel(0).setExpression("mtn_ex_" + emotion1[i][j] + ".exp3.json");
                   LAppLive2DManager.getInstance().getModel(0).startMotion("Motion", Integer.parseInt(motion1[i][j]), LAppDefine.Priority.FORCE.getPriority(),null);
                   if(isContinue[i][j]){
                       Log.d("有第二个","");
                       try {
                           Thread.sleep(2500);
                       } catch (Throwable e) {
                           Log.d("a","b");
                       }
                       try {
                       Log.d("播放第二个","");
                       LAppLive2DManager.getInstance().getModel(0).setExpression("mtn_ex_" + emotion2[i][j] + ".exp3.json");
                       LAppLive2DManager.getInstance().getModel(0).startMotion("Motion", Integer.parseInt(motion2[i][j]), LAppDefine.Priority.FORCE.getPriority(),null);
                       Thread.sleep(1500);
                       } catch (Throwable e) {
                           makeToast(activity);
                       }
                   }
               }
           });
           doEandM.start();
        }catch (Throwable e){
            Log.d("a","b");
        }
    }

    public static void makeToast(Activity activity){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "没听她说完就走了吗😭😭😭", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
