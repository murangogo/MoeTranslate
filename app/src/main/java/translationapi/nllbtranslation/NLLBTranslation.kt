package translationapi.nllbtranslation

import android.content.Context
import android.widget.Toast
import com.moe.moetranslator.R
import com.moe.moetranslator.translate.CustomLocale
import com.moe.moetranslator.translate.TranslationResult
import com.moe.moetranslator.translate.TranslationTextAPI

class NLLBTranslation(context: Context) : TranslationTextAPI {
    private val ctx = context.applicationContext
    private var currentTask: Thread? = null
    private var isInitialized = false

    private var nllbTranslator: TranslationCore = TranslationCore(ctx, object :InitializationListener{
        override fun onInitializationComplete() {
            isInitialized = true
            showToast(R.string.initialization_complete)
        }
        override fun onInitializationError(e: Exception) {
            e.printStackTrace()
            showToast(R.string.initialization_failed)
        }
    })

    init {
        showToast(R.string.initialization_start)
    }

    override fun getTranslation(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        callback: (TranslationResult) -> Unit
    ) {
        if (isInitialized){

            currentTask = Thread {
                try {
                    nllbTranslator.translate(text, CustomLocale(sourceLanguage), CustomLocale(targetLanguage), object: TranslationListener{
                        override fun onTranslationComplete(result: String) {
                            callback(TranslationResult.Success(result))
                        }

                        override fun onTranslationError(e: java.lang.Exception) {
                            callback(TranslationResult.Error(e))
                        }

                    })
                } catch (e: Exception) {
                    callback(TranslationResult.Error(e))
                }

            }.apply { start() }

        }else{
            showToast(R.string.initialization_not_complete)
        }
    }

    override fun cancelTranslation() {
        currentTask?.let {
            if (it.isAlive) {
                it.interrupt()
            }
        }
        currentTask = null
    }

    override fun release() {
        cancelTranslation()
    }

    private fun showToast(@androidx.annotation.StringRes messageId: Int) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(ctx, ctx.getString(messageId), Toast.LENGTH_LONG).show()
        }
    }
}