package translateapi.baidufanyiapi.data;

import java.util.HashMap;

/**
 * todo 注意这里仅列出常用的语种作为演示
 * 具体支持的语种详见 https://api.fanyi.baidu.com/doc/26， 可在本类中继续扩充
 */
public class Language {
    public static final HashMap<String,String> LanguageMap_Baidu = new HashMap<String,String>();
    public static final HashMap<String,String> LanguageMap_Tencent = new HashMap<String,String>();
    static{
        LanguageMap_Baidu.put("自动检测", "auto");
        LanguageMap_Baidu.put("中文", "zh");
        LanguageMap_Baidu.put("英语", "en");
        LanguageMap_Baidu.put("日语", "jp");
        LanguageMap_Baidu.put("韩语", "kor");
        LanguageMap_Baidu.put("法语", "fra");
        LanguageMap_Baidu.put("西班牙语", "spa");
        LanguageMap_Baidu.put("俄语", "ru");
        LanguageMap_Baidu.put("葡萄牙语", "pt");
        LanguageMap_Baidu.put("德语", "de");
        LanguageMap_Baidu.put("意大利语", "it");
        LanguageMap_Baidu.put("丹麦语", "dan");
        LanguageMap_Baidu.put("荷兰语", "nl");
        LanguageMap_Baidu.put("马来语", "may");
        LanguageMap_Baidu.put("瑞典语", "swe");
        LanguageMap_Baidu.put("印尼语", "id");
        LanguageMap_Baidu.put("波兰语", "pl");
        LanguageMap_Baidu.put("罗马尼亚语", "rom");
        LanguageMap_Baidu.put("土耳其语", "tr");
        LanguageMap_Baidu.put("希腊语", "el");
        LanguageMap_Baidu.put("匈牙利语", "hu");

        LanguageMap_Tencent.put("中文","zh");
        LanguageMap_Tencent.put("自动识别","auto");
        LanguageMap_Tencent.put("简体中文","zh");
        LanguageMap_Tencent.put("繁体中文","zh-TW");
        LanguageMap_Tencent.put("英语","en");
        LanguageMap_Tencent.put("日语","ja");
        LanguageMap_Tencent.put("韩语","ko");
        LanguageMap_Tencent.put("俄语","ru");
        LanguageMap_Tencent.put("法语","fr");
        LanguageMap_Tencent.put("德语","de");
        LanguageMap_Tencent.put("意大利语","it");
        LanguageMap_Tencent.put("西班牙语","es");
        LanguageMap_Tencent.put("葡萄牙语","pt");
        LanguageMap_Tencent.put("马来西亚语","ms");
        LanguageMap_Tencent.put("泰语","th");
        LanguageMap_Tencent.put("越南语","vi");
    }
}
