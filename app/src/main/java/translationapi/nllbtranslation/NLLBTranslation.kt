package translationapi.nllbtranslation

import android.content.Context
import android.widget.Toast
import com.moe.moetranslator.R
import com.moe.moetranslator.translate.CustomLocale
import com.moe.moetranslator.translate.TranslationAPI

class NLLBTranslation(val ctx: Context) : TranslationAPI {
    private var currentTask: Thread? = null
    private var isInitialized = false

    private var nllbTranslator: TranslationCore = TranslationCore(ctx, object :InitializationListener{
        override fun onInitializationComplete() {
            isInitialized = true
            Toast.makeText(ctx, ctx.getString(R.string.initialization_complete), Toast.LENGTH_LONG).show()
        }
        override fun onInitializationError(e: Exception) {
            e.printStackTrace()
            Toast.makeText(ctx, ctx.getString(R.string.initialization_failed), Toast.LENGTH_LONG).show()
        }
    })

    init {
        Toast.makeText(ctx, ctx.getString(R.string.initialization_start), Toast.LENGTH_LONG).show()
    }

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationAPI.TranslationResult) -> Unit
    ) {
        if (isInitialized){

            currentTask = Thread {
                try {
                    nllbTranslator.translate(text, CustomLocale(sourceLanguage), CustomLocale(targetLanguage), object: TranslationListener{
                        override fun onTranslationComplete(result: String) {
                            callback(TranslationAPI.TranslationResult.Success(result))
                        }

                        override fun onTranslationError(e: java.lang.Exception) {
                            callback(TranslationAPI.TranslationResult.Error(e))
                        }

                    })
                } catch (e: Exception) {
                    callback(TranslationAPI.TranslationResult.Error(e))
                }

            }.apply { start() }

        }else{
            Toast.makeText(ctx, ctx.getString(R.string.initialization_not_complete), Toast.LENGTH_LONG).show()
        }
    }

    override fun cancelTranslation() {
        currentTask?.interrupt()
        currentTask = null
    }

    override fun release() {
        cancelTranslation()
    }
}