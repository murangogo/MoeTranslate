package translationapi.customtranslation

import android.graphics.Bitmap
import com.moe.moetranslator.translate.TranslationPicAPI
import com.moe.moetranslator.translate.TranslationResult

class CustomTranslationImage: TranslationPicAPI {
    override fun getTranslation(
        pic: Bitmap,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelTranslation() {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }
}