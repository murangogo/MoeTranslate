package translationapi.volctranslation;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CryptoUtils {

    // 将字节数组转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    // 计算 HMAC-SHA256
    public static byte[] hmacSHA256(byte[] key, String data) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new Exception(
                    "Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }

    // 计算 HMAC-SHA256 并返回十六进制字符串
    public static String hmacSHA256AsHex(byte[] key, String data) throws Exception {
        return bytesToHex(hmacSHA256(key, data));
    }

    public static String readInputStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return response.toString();
    }
}
