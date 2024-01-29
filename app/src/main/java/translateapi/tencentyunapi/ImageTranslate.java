package translateapi.tencentyunapi;

import android.util.Log;

import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

public class ImageTranslate {
    private static final OkHttpClient client = new OkHttpClient();
    private final StringBuilder tencentTranslateResult = new StringBuilder();
    private String ID;
    private String Key;
    private String ImgMD5;
    private String From;
    private String To;

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

    public ImageTranslate(String from,String to,String id,String key,String data){
        ID = id;
        Key = key;
        From = from;
        To = to;
        ImgMD5 = data;
    }

    public String StartTranslate(){
        try{
            StringBuilder sb = new StringBuilder();
            sb.append("{\"SessionUuid\":\"");
            sb.append("session-00001");
            sb.append("\",\"Scene\":\"");
            sb.append("doc");
            sb.append("\",\"Data\":\"");
            sb.append(ImgMD5);
            sb.append("\",\"Source\":\"");
            sb.append(From);
            sb.append("\",\"Target\":\"");
            sb.append(To);
            sb.append("\",\"ProjectId\":");
            sb.append(0);
            sb.append("}");
            String result = sb.toString();
            String token = "";
            String service = "tmt";
            String version = "2018-03-21";
            String action = "ImageTranslate";
            String body = result;
            String region = "ap-beijing";
            String resp = doRequest(ID, Key, service, version, action, body, region, token);
            tencentTranslateResult.append(resp);
        }catch (Exception e){
            StringBuilder sb = new StringBuilder();
            sb.append("程序发生错误，错误信息：").append(e.toString());
            tencentTranslateResult.append(sb);
        }
        return tencentTranslateResult.toString();
    }

    public static String doRequest(
            String secretId, String secretKey,
            String service, String version, String action,
            String body, String region, String token
    ) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        Request request = buildRequest(secretId, secretKey, service, version, action, body, region, token);
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static Request buildRequest(
            String secretId, String secretKey,
            String service, String version, String action,
            String body, String region, String token
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        String host = "tmt.tencentcloudapi.com";
        String endpoint = "https://" + host;
        String contentType = "application/json; charset=utf-8";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String auth = getAuth(secretId, secretKey, host, contentType, timestamp, body);
        return new Request.Builder()
                .header("Host", host)
                .header("X-TC-Timestamp", timestamp)
                .header("X-TC-Version", version)
                .header("X-TC-Action", action)
                .header("X-TC-Region", region)
                .header("X-TC-Token", token)
                .header("X-TC-RequestClient", "SDK_JAVA_BAREBONE")
                .header("Authorization", auth)
                .url(endpoint)
                .post(RequestBody.create(MediaType.parse(contentType), body))
                .build();
    }

    private static String getAuth(
            String secretId, String secretKey, String host, String contentType,
            String timestamp, String body
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:" + contentType + "\nhost:" + host + "\n";
        String signedHeaders = "content-type;host";

        String hashedRequestPayload = sha256Hex(body.getBytes(StandardCharsets.UTF_8));
        String canonicalRequest = "POST"
                + "\n"
                + canonicalUri
                + "\n"
                + canonicalQueryString
                + "\n"
                + canonicalHeaders
                + "\n"
                + signedHeaders
                + "\n"
                + hashedRequestPayload;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.valueOf(timestamp + "000")));
        String service = host.split("\\.")[0];
        String credentialScope = date + "/" + service + "/" + "tc3_request";
        String hashedCanonicalRequest =
                sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));
        String stringToSign =
                "TC3-HMAC-SHA256\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, service);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature =
                printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();
        return "TC3-HMAC-SHA256 "
                + "Credential="
                + secretId
                + "/"
                + credentialScope
                + ", "
                + "SignedHeaders="
                + signedHeaders
                + ", "
                + "Signature="
                + signature;
    }

    public static String sha256Hex(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(b);
        return printHexBinary(d).toLowerCase();
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    public static byte[] hmac256(byte[] key, String msg) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }
}