package com.moe.moetranslator.madoka

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.moe.moetranslator.R

// DialogManager.kt
class DialogManager private constructor() {
    companion object {
        private var MainContext: Context? = null
        private var progressDialog: AlertDialog? = null

        fun init(context: Context) {
            MainContext = context
        }

        fun showDialog(dialogTitle: String? = null, dialogMessage: String? = null) {
            dismissDialog() // 确保之前的 dialog 已经消失

            val title = dialogTitle ?: getApplicationContext().getString(R.string.loading)
            val message = dialogMessage ?: getApplicationContext().getString(R.string.waiting)

            // 使用 Handler 确保在主线程创建 dialog
            Handler(Looper.getMainLooper()).post {
                progressDialog = AlertDialog.Builder(getApplicationContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .create()
                progressDialog?.show()
                progressDialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            }
        }

        fun dismissDialog() {
            Handler(Looper.getMainLooper()).post {
                progressDialog?.dismiss()
                progressDialog = null
            }
        }

        fun showToast(message: String, isShort: Boolean = false) {
            if (isShort) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun getApplicationContext(): Context {
            return MainContext ?: throw IllegalStateException(
                "DialogManager Not Initialized"
            )
        }
    }
}