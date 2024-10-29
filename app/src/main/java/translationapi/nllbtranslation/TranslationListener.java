package translationapi.nllbtranslation;

// 需要在TranslationCore中添加的接口
public interface TranslationListener {
    void onTranslationComplete(String result);
    void onTranslationError(Exception e);
}
