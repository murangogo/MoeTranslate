package translateapi.tencentyunapi;

import android.provider.ContactsContract;
import android.util.Log;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageTranslate
{
    private final StringBuffer tencentTranslateResult = new StringBuffer();
    private String ID;
    private String Key;
    private String ImgMD5;
    private String From;
    private String To;
    public ImageTranslate(String from,String to,String id,String key,String data){
        ID = id;
        Key = key;
        From = from;
        To = to;
        ImgMD5 = data;
    }
    public String StartTranslate() {
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(ID, Key);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("tmt.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            TmtClient client = new TmtClient(cred, "ap-beijing", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ImageTranslateRequest req = new ImageTranslateRequest();
            req.setSessionUuid("session-00001");
            req.setScene("doc");
            req.setData(ImgMD5);
            req.setSource(From);
            req.setTarget(To);
            req.setProjectId(0L);
            // 返回的resp是一个ImageTranslateResponse的实例，与请求对象对应
            ImageTranslateResponse resp = client.ImageTranslate(req);
            // 输出json格式的字符串回包
            for(int i=0;i<resp.getImageRecord().getValue().length;i++){
                tencentTranslateResult.append(resp.getImageRecord().getValue()[i].getTargetText());
                if(i!=(resp.getImageRecord().getValue().length-1)){
                    tencentTranslateResult.append("\n");
                }
            }
        } catch (TencentCloudSDKException e) {
            tencentTranslateResult.append(e.toString());
        }
        return tencentTranslateResult.toString();
    }

    public static String file2base64(String f){
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(f));
            return Base64.getEncoder().encodeToString(fileContent);
        }
        catch (Exception e) {
            Log.d("ERROR",e.toString());
            return "";
        }
    }
}