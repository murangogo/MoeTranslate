package translateapi.baidufanyiapi.pic;

import translateapi.baidufanyiapi.data.Config;
import translateapi.baidufanyiapi.http.HttpParams;
import translateapi.baidufanyiapi.utils.FileUtil;

import java.util.Map;

/**
 * PicTranslate signer
 */
class PicSigner {

    static String sign(Config config, HttpParams params) {
        if (config == null || params == null) {
            return "";
        }
        Map<String, String> stringParams = params.getStringParams();
        Map<String, HttpParams.FileWrapper> fileParams = params.getFileParams();

        String appId = config.getAppId();
        String md5Image = "";

        if (fileParams.containsKey("image")) {
            HttpParams.FileWrapper file = fileParams.get("image");
            md5Image = FileUtil.md5(file.file).toLowerCase();
        }
        String salt = stringParams.get("salt");
        String cuid = stringParams.get("cuid");
        String mac = stringParams.get("mac");
        String appKey = config.getSecretKey();

        return FileUtil.md5(appId + md5Image + salt + cuid + mac + appKey).toLowerCase();
    }
}
