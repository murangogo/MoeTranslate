package translateapi.baidufanyiapi.data;

public class Config {
    /**
     * 不擦除
     */
    public static final int ERASE_NONE = 0;
    /**
     * 全屏擦除
     */
    public static final int ERASE_FULL = 1;
    /**
     * 区块擦除
     */
    public static final int ERASE_BLOCK = 2;

    /**
     * 不贴合
     */
    public static final int PASTE_NONE = 0;
    /**
     * 全屏贴合
     */
    public static final int PASTE_FULL = 1;
    /**
     * 区块贴合
     */
    public static final int PASTE_BLOCK = 2;


    private String appId;
    private String secretKey;
    private String from;
    private String to;
    private String picPath;
    private int erase = ERASE_FULL;
    private int paste = PASTE_FULL;

    /**
     * 构建Config对象实例。具体请在 { http://api.fanyi.baidu.com/api/trans/product/desktop }上申请查看
     *
     * @param appId     APP ID
     * @param secretKey 密钥
     */
    public Config(String appId, String secretKey) {
        this.appId = appId;
        this.secretKey = secretKey;
    }

    /**
     * 设置翻译语种方向
     *
     * @see Language
     */
    public void langfrom(String from) {
        this.from = from;
    }

    public void langto(String to) {
        this.to = to;
    }

    /**
     * 设置翻译文件路径。
     *
     * @param filePath 文件路径
     */
    public void pic(String filePath) {
        this.picPath = filePath;
    }

    /**
     * 设置擦除模式.默认为{@link #ERASE_FULL}
     *
     * @see #ERASE_NONE
     * @see #ERASE_FULL
     * @see #ERASE_BLOCK
     */
    public void erase(int erase) {
        this.erase = erase;
    }

    /**
     * 设置贴合模式.默认为{@link #PASTE_FULL}
     *
     * @see #PASTE_NONE
     * @see #PASTE_FULL
     * @see #PASTE_BLOCK
     */
    public void paste(int paste) {
        this.paste = paste;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String s){
        appId = s;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String s){
        secretKey = s;
    }
    public String getPicPath() {
        return picPath;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getErase() {
        return erase;
    }

    public int getPaste() {
        return paste;
    }
}
