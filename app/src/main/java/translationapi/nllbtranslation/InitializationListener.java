package translationapi.nllbtranslation;

public interface InitializationListener {
    void onInitializationComplete();
    void onInitializationError(Exception e);
}