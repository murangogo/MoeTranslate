package translateapi.data;

import java.util.HashMap;

/**
 * todo 注意这里仅列出常用的语种作为演示
 * 具体支持的语种详见 https://api.fanyi.baidu.com/doc/26， 可在本类中继续扩充
 */
public class Language {
    public static final HashMap<String,String> LanguageMap = new HashMap<String,String>();
    static{
        LanguageMap.put("自动检测", "auto");
        LanguageMap.put("中文", "zh");
        LanguageMap.put("英语", "en");
        LanguageMap.put("日语", "jp");
        LanguageMap.put("韩语", "kor");
        LanguageMap.put("法语", "fra");
        LanguageMap.put("西班牙语", "spa");
        LanguageMap.put("俄语", "ru");
        LanguageMap.put("葡萄牙语", "pt");
        LanguageMap.put("德语", "de");
        LanguageMap.put("意大利语", "it");
        LanguageMap.put("丹麦语", "dan");
        LanguageMap.put("荷兰语", "nl");
        LanguageMap.put("马来语", "may");
        LanguageMap.put("瑞典语", "swe");
        LanguageMap.put("印尼语", "id");
        LanguageMap.put("波兰语", "pl");
        LanguageMap.put("罗马尼亚语", "rom");
        LanguageMap.put("土耳其语", "tr");
        LanguageMap.put("希腊语", "el");
        LanguageMap.put("匈牙利语", "hu");
    }
}
