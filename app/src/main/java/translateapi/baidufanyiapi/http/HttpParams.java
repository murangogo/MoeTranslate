package translateapi.baidufanyiapi.http;

import translateapi.baidufanyiapi.utils.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HttpParams {
    private static final String APPLICATION_STREAM = "application/bytes";
    private Map<String, String> params = new HashMap<>();
    private Map<String, FileWrapper> fileWrappers = new HashMap<>();

    public void put(String key, String value) {
        if (value == null) {
            params.remove(key);
        } else {
            params.put(key, value);
        }
    }

    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, File file) {
        put(key, file, "");
    }

    public void put(String key, File file, String contentType) {
        if (file != null && file.exists()) {
            fileWrappers.put(key, new FileWrapper(file, contentType));
        } else {
            fileWrappers.remove(key);
        }
    }

    public Map<String, String> getStringParams() {
        return params;
    }

    public Map<String, FileWrapper> getFileParams() {
        return fileWrappers;
    }

    boolean isEmpty() {
        return params.isEmpty() && fileWrappers.isEmpty();
    }

    public static class FileWrapper {
        public File file;
        String contentType;

        FileWrapper(File file) {
            this.file = file;
            this.contentType = APPLICATION_STREAM;
        }

        FileWrapper(File file, String contentType) {
            this.file = file;
            if (TextUtils.isEmpty(contentType)) {
                this.contentType = APPLICATION_STREAM;
            } else {
                this.contentType = contentType;
            }
        }
    }
}
