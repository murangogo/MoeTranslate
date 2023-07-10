package translateapi.baidufanyiapi.http;

import okhttp3.*;

import java.util.Map;

public class HttpClient {
    private final OkHttpClient client;

    public HttpClient() {
        client = new OkHttpClient.Builder().build();
    }

    public void post(String host, HttpParams params, HttpCallback<?> callback) {
        Call call = newPostCall(client, host, params);
        call.enqueue(callback);
    }

    private static Call newPostCall(OkHttpClient client, String host, HttpParams params) {
        if (params == null || params.isEmpty()) {
            Request.Builder builder = new Request.Builder().url(host);
            Request request = builder.build();
            return client.newCall(request);
        } else {
            Request.Builder builder = new Request.Builder().url(host);
            RequestBody body = addPart(params);
            builder.post(body);
            Request request = builder.build();
            return client.newCall(request);
        }
    }

    private static RequestBody addPart(HttpParams params) {
        Map<String, HttpParams.FileWrapper> files = params.getFileParams();
        if (files.isEmpty()) {
            FormBody.Builder body = new FormBody.Builder();
            addRequestBody(body, params);
            return body.build();
        } else {
            MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (String key : files.keySet()) {
                HttpParams.FileWrapper wrapper = files.get(key);
                body.addFormDataPart(key, wrapper.file.getName(),
                        RequestBody.create(MediaType.parse(wrapper.contentType), wrapper.file));
            }
            addRequestBody(body, params);
            return body.build();
        }
    }

    private static void addRequestBody(Object body, HttpParams params) {
        Map<String, String> strings = params.getStringParams();
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (body instanceof FormBody.Builder) {
                ((FormBody.Builder) body).add(key, value);
            } else if (body instanceof MultipartBody.Builder) {
                ((MultipartBody.Builder) body).addFormDataPart(key, value);
            }
        }
    }
}
