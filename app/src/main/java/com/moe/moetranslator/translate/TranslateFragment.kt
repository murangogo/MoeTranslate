package com.moe.moetranslator.translate

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.moe.moetranslator.launch.FirstLaunchPage
import com.moe.moetranslator.utils.UpdateChecker
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentTranslateBinding
import com.moe.moetranslator.me.ManageActivity
import com.moe.moetranslator.utils.Constants
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.NotificationChecker
import com.moe.moetranslator.utils.NotificationResult
import com.moe.moetranslator.utils.UpdateResult
import kotlinx.coroutines.launch
import java.io.File

val TAG = "TranslateFragment"

class TranslateFragment : Fragment() {
    private lateinit var binding: FragmentTranslateBinding
    private lateinit var updateChecker: UpdateChecker
    private lateinit var notificationChecker: NotificationChecker
    private lateinit var prefs: CustomPreference
    private lateinit var serviceStopReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化广播接收器
        serviceStopReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BroadcastAction.ACTION_FLOATING_BALL_SERVICE_STOPPED) {
                    setTitleAndButton(false)
                }
            }
        }

        // 创建文件夹
        val modelDir = File(requireContext().getExternalFilesDir(null), "models")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        prefs = CustomPreference.getInstance(requireContext())
        updateChecker = UpdateChecker(requireContext())
        notificationChecker = NotificationChecker(requireContext())
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart")

        // 注册广播接收器
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            serviceStopReceiver,
            IntentFilter(BroadcastAction.ACTION_FLOATING_BALL_SERVICE_STOPPED)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTranslateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForUpdate()
        checkNotification()
        showAPIName()
        setTitleAndButton(isServiceRunning(FloatingBallService::class.java))

        val helpIntent = Intent(requireContext(), FirstLaunchPage::class.java)
        binding.help.setOnClickListener {
            startActivity(helpIntent)
        }

        binding.notice.setOnClickListener {
            showToast(getString(R.string.getting_notification))
            checkNotification(true)
        }

        binding.startButton.setOnClickListener {
            if (!isServiceRunning(FloatingBallService::class.java)) {
                if (checkAndroidSDK() && checkAccessibilityService() && checkFloatingBall() && checkNotify() && checkTranslateAPI() && checkCombination()) {
                    if ((prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id) == Constants.TranslateMode.TEXT.id) && (prefs.getInt(
                            "Text_API",
                            Constants.TextApi.BING.id
                        ) == Constants.TextApi.AI.id) && (prefs.getInt("Text_AI", Constants.TextAI.MLKIT.id) == Constants.TextAI.NLLB.id)
                    ) {
                        checkRAM()
                    }
                    launchFloatingBallService()
                }
            } else {
                stopFloatingBallService()
            }
        }

        if ((prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id) == Constants.TranslateMode.TEXT.id) && (prefs.getInt("Text_API", Constants.TextApi.BING.id) == Constants.TextApi.CUSTOM_TEXT.id)) {
            binding.SourceLanguageName.text =
                CustomLocale.getInstance(prefs.getString("Source_Language", "ja")).getDisplayName()
            binding.TargetLanguageName.text = getString(R.string.custom_api_select_language)
        } else if ((prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id) == Constants.TranslateMode.PIC.id) && (prefs.getInt("Pic_API", Constants.PicApi.BAIDU.id) == Constants.PicApi.CUSTOM_PIC.id)) {
            binding.SourceLanguageName.text = getString(R.string.custom_api_select_language)
            binding.TargetLanguageName.text = getString(R.string.custom_api_select_language)
        } else {
            binding.SourceLanguageName.text =
                CustomLocale.getInstance(prefs.getString("Source_Language", "ja")).getDisplayName()
            binding.TargetLanguageName.text =
                CustomLocale.getInstance(prefs.getString("Target_Language", "zh")).getDisplayName()
        }

        binding.oriLanguage.setOnClickListener {
            showLanguageListDialog(1)
        }

        binding.tarLanguage.setOnClickListener {
            showLanguageListDialog(2)
        }
    }

    private fun checkForUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = updateChecker.checkForUpdate()) {
                is UpdateResult.UpdateAvailable -> {
                    if (prefs.getLong("Ignore_Version", 0) != result.versionCode) showUpdateDialog(
                        result
                    )
                }

                else -> {
                    Log.e(TAG, "No update or Internet error")
                }
            }
        }
    }

    private fun checkNotification(userGet: Boolean = false) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = notificationChecker.checkNotification()) {
                is NotificationResult.NotificationAvailable -> {
                    if ((prefs.getLong(
                            "Read_Notice",
                            0
                        ) != result.notificationCode) || userGet
                    ) showNotificationDialog(result)
                }

                else -> {
                    if (userGet) showToast(getString(R.string.get_notification_error))
                }
            }
        }
    }

    private fun checkCombination(): Boolean =
        if (prefs.getString("Source_Language", "ja") == prefs.getString("Target_Language", "zh")) {
            showToast(getString(R.string.invalid_combination))
            false
        } else {
            true
        }

    private fun showAPIName() {
        val translateMode = prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id)
        val textApi = prefs.getInt("Text_API", Constants.TextApi.BING.id)
        val textAi = prefs.getInt("Text_AI", Constants.TextAI.MLKIT.id)
        val picApi = prefs.getInt("Pic_API", Constants.PicApi.BAIDU.id)
        val customTextApi = prefs.getInt("Custom_Text_API", 0)
        val customPicApi = prefs.getInt("Custom_Pic_API", 0)

        Log.d(
            TAG,
            "translatemode$translateMode，textapi:$textApi，textAI:$textAi，picapi:$picApi，customtextapi:$customTextApi，custompicapi:$customPicApi"
        )

        when {
            translateMode == Constants.TranslateMode.TEXT.id -> when (textApi) {
                Constants.TextApi.AI.id -> {
                    if (textAi == Constants.TextAI.MLKIT.id) {
                        binding.selectedAPI.text = getString(
                            R.string.api_name,
                            getString(R.string.mlkit_name)
                        ) + "（${getString(R.string.ocr)}）"
                    } else {
                        binding.selectedAPI.text = getString(
                            R.string.api_name,
                            getString(R.string.nllb_name)
                        ) + "（${getString(R.string.ocr)}）"
                    }
                }

                Constants.TextApi.BING.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.bingapi_name)
                    ) + "（${getString(R.string.ocr)}）"
                }

                Constants.TextApi.NIUTRANS.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.niuapi_name)
                    ) + "（${getString(R.string.ocr)}）"
                }

                Constants.TextApi.VOLC.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.volcapi_name)
                    ) + "（${getString(R.string.ocr)}）"
                }

                Constants.TextApi.AZURE.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.azureapi_name)
                    ) + "（${getString(R.string.ocr)}）"
                }

                Constants.TextApi.BAIDU.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.baiduapi_name)
                    ) + "（${getString(R.string.ocr)}）"
                }

                Constants.TextApi.TENCENT.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.tencentapi_name)
                    ) + "（${getString(R.string.ocr)}）"
                }

                else -> {
                    when (customTextApi) {
                        0 -> {
                            binding.selectedAPI.text = getString(
                                R.string.api_name,
                                getString(R.string.custom)
                            ) + "1（${getString(R.string.ocr)}）"
                        }

                        1 -> {
                            binding.selectedAPI.text = getString(
                                R.string.api_name,
                                getString(R.string.custom)
                            ) + "2（${getString(R.string.ocr)}）"
                        }

                        else -> {
                            binding.selectedAPI.text = getString(
                                R.string.api_name,
                                getString(R.string.custom)
                            ) + "3（${getString(R.string.ocr)}）"
                        }
                    }
                }
            }

            else -> when (picApi) {
                Constants.PicApi.BAIDU.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.baiduapi_name)
                    ) + "（${getString(R.string.pic)}）"
                }

                Constants.PicApi.TENCENT.id -> {
                    binding.selectedAPI.text = getString(
                        R.string.api_name,
                        getString(R.string.tencentapi_name)
                    ) + "（${getString(R.string.pic)}）"
                }

                else -> {
                    when (customPicApi) {
                        0 -> {
                            binding.selectedAPI.text = getString(
                                R.string.api_name,
                                getString(R.string.custom)
                            ) + "1（${getString(R.string.pic)}）"
                        }

                        1 -> {
                            binding.selectedAPI.text = getString(
                                R.string.api_name,
                                getString(R.string.custom)
                            ) + "2（${getString(R.string.pic)}）"
                        }

                        else -> {
                            binding.selectedAPI.text = getString(
                                R.string.api_name,
                                getString(R.string.custom)
                            ) + "3（${getString(R.string.pic)}）"
                        }
                    }
                }
            }
        }
    }

    private fun setTitleAndButton(isRunning: Boolean) {
        if (!isRunning) {
            binding.welcomeTitle.text = getString(R.string.welcome_home_title)
            binding.welcomeSubtitle.text = getString(R.string.welcome_home_subtitle)
            binding.startButton.text = getString(R.string.start_ball)
            binding.startButton.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
        } else {
            binding.welcomeTitle.text = getString(R.string.welcome_home_title_2)
            binding.welcomeSubtitle.text = getString(R.string.welcome_home_subtitle_2)
            binding.startButton.text = getString(R.string.stop_ball)
            binding.startButton.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
        }
    }

    private fun checkAndroidSDK(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.android_sdk_old_title)
                .setMessage(R.string.android_sdk_old_content)
                .setCancelable(false)
                .setPositiveButton(R.string.user_known, null)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            return false
        } else {
            return true
        }
    }

    private fun checkTranslateAPI(): Boolean {
        val translateMode = prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id)
        val textApi = prefs.getInt("Text_API", Constants.TextApi.BING.id)
        val textAi = prefs.getInt("Text_AI", Constants.TextAI.MLKIT.id)
        val picApi = prefs.getInt("Pic_API", Constants.PicApi.BAIDU.id)
        val customTextApi = prefs.getInt("Custom_Text_API", 0)
        val customPicApi = prefs.getInt("Custom_Pic_API", 0)

        val ret: Boolean = when {
            translateMode == Constants.TranslateMode.TEXT.id -> when (textApi) {
                Constants.TextApi.AI.id -> {
                    if ((textAi == Constants.TextAI.MLKIT.id) && (!(prefs.getBoolean("Download_MLKit", false)))) {
                        Log.d(
                            TAG,
                            "Download_MLKit" + prefs.getBoolean("Download_MLKit", false).toString()
                        )
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.mlkit_not_download_title)
                            .setMessage(R.string.mlkit_not_download_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_download) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_MLKIT
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else if ((textAi == Constants.TextAI.NLLB.id) && (!(prefs.getBoolean("Download_NLLB", false)))) {
                        Log.d(
                            TAG,
                            "Download_NLLB" + prefs.getBoolean("Download_NLLB", false).toString()
                        )
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.nllb_not_download_title)
                            .setMessage(R.string.nllb_not_download_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_download) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_NLLB
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                Constants.TextApi.BING.id -> {
                    true
                }

                Constants.TextApi.NIUTRANS.id -> {
                    if (prefs.getString("Niutrans_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.niuapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_NIU_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                Constants.TextApi.VOLC.id -> {
                    if (prefs.getString("Volc_ACCOUNT_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.volcapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_VOLC_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                Constants.TextApi.AZURE.id -> {
                    if (prefs.getString("Azure_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.azureapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_AZURE_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                Constants.TextApi.BAIDU.id -> {
                    if (prefs.getString("Baidu_Translate_ACCOUNT_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.baiduapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_BAIDU_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                Constants.TextApi.TENCENT.id -> {
                    if (prefs.getString("Tencent_Cloud_ACCOUNT_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.tencentapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_TENCENT_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                else -> {
                    when (customTextApi) {
                        0 -> {
                            if ((prefs.getString("Custom_Text_API_0", "") == "")) {
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        val intent = Intent(
                                            requireContext(),
                                            ManageActivity::class.java
                                        ).apply {
                                            putExtra(
                                                ManageActivity.EXTRA_FRAGMENT_TYPE,
                                                ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API
                                            )
                                            putExtra(
                                                ManageActivity.EXTRA_CUSTOM_CODE,
                                                ManageActivity.CODE_CUSTOM_0
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            } else {
                                true
                            }
                        }

                        1 -> {
                            if ((prefs.getString("Custom_Text_API_1", "") == "")) {
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        val intent = Intent(
                                            requireContext(),
                                            ManageActivity::class.java
                                        ).apply {
                                            putExtra(
                                                ManageActivity.EXTRA_FRAGMENT_TYPE,
                                                ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API
                                            )
                                            putExtra(
                                                ManageActivity.EXTRA_CUSTOM_CODE,
                                                ManageActivity.CODE_CUSTOM_1
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            } else {
                                true
                            }
                        }

                        else -> {
                            if ((prefs.getString("Custom_Text_API_2", "") == "")) {
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        val intent = Intent(
                                            requireContext(),
                                            ManageActivity::class.java
                                        ).apply {
                                            putExtra(
                                                ManageActivity.EXTRA_FRAGMENT_TYPE,
                                                ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API
                                            )
                                            putExtra(
                                                ManageActivity.EXTRA_CUSTOM_CODE,
                                                ManageActivity.CODE_CUSTOM_2
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            } else {
                                true
                            }
                        }
                    }
                }
            }

            else -> when (picApi) {
                Constants.PicApi.BAIDU.id -> {
                    if (prefs.getString("Baidu_Translate_ACCOUNT_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.baiduapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_BAIDU_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                Constants.PicApi.TENCENT.id -> {
                    if (prefs.getString("Tencent_Cloud_ACCOUNT_EncryptedKey", "") == "") {
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(
                                getString(
                                    R.string.api_not_config_content,
                                    getString(R.string.tencentapi_name)
                                )
                            )
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                val intent =
                                    Intent(requireContext(), ManageActivity::class.java).apply {
                                        putExtra(
                                            ManageActivity.EXTRA_FRAGMENT_TYPE,
                                            ManageActivity.TYPE_FRAGMENT_MANAGE_TENCENT_API
                                        )
                                    }
                                startActivity(intent)
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else {
                        true
                    }
                }

                else -> {
                    when (customPicApi) {
                        0 -> {
                            if ((prefs.getString("Custom_Pic_API_0", "") == "")) {
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        val intent = Intent(
                                            requireContext(),
                                            ManageActivity::class.java
                                        ).apply {
                                            putExtra(
                                                ManageActivity.EXTRA_FRAGMENT_TYPE,
                                                ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API
                                            )
                                            putExtra(
                                                ManageActivity.EXTRA_CUSTOM_CODE,
                                                ManageActivity.CODE_CUSTOM_0
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            } else {
                                true
                            }
                        }

                        1 -> {
                            if ((prefs.getString("Custom_Pic_API_1", "") == "")) {
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        val intent = Intent(
                                            requireContext(),
                                            ManageActivity::class.java
                                        ).apply {
                                            putExtra(
                                                ManageActivity.EXTRA_FRAGMENT_TYPE,
                                                ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API
                                            )
                                            putExtra(
                                                ManageActivity.EXTRA_CUSTOM_CODE,
                                                ManageActivity.CODE_CUSTOM_1
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            } else {
                                true
                            }
                        }

                        else -> {
                            if ((prefs.getString("Custom_Pic_API_2", "") == "")) {
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        val intent = Intent(
                                            requireContext(),
                                            ManageActivity::class.java
                                        ).apply {
                                            putExtra(
                                                ManageActivity.EXTRA_FRAGMENT_TYPE,
                                                ManageActivity.TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API
                                            )
                                            putExtra(
                                                ManageActivity.EXTRA_CUSTOM_CODE,
                                                ManageActivity.CODE_CUSTOM_2
                                            )
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            } else {
                                true
                            }
                        }
                    }
                }
            }
        }
        return ret
    }

    private fun checkAccessibilityService(): Boolean {
        val accessibilityManager =
            requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

//        Log.d(TAG, accessibilityManager.isEnabled.toString())

        val expectedServiceId =
            "${requireContext().packageName}/.translate.ScreenShotAccessibilityService"
        val ret = enabledServices.any { it.id == expectedServiceId }
        if (!ret) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.not_accessibility_service_title)
                .setMessage(R.string.not_accessibility_service_content)
                .setPositiveButton(R.string.go_to_grant) { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .setNegativeButton(R.string.user_cancel, null)
                .setCancelable(false)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        }
        return ret
    }

    private fun checkFloatingBall(): Boolean {
        // 检测是否有悬浮窗权限
        val ret = Settings.canDrawOverlays(requireContext())
        if (!ret) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.not_floating_title)
                .setMessage(R.string.not_floating_content)
                .setPositiveButton(R.string.go_to_grant) { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)
                }
                .setNegativeButton(R.string.user_cancel, null)
                .setCancelable(false)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        }
        return ret
    }

    private fun checkNotify(): Boolean {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ret = notificationManager.areNotificationsEnabled()
        if (!ret) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.not_notify_title)
                .setMessage(R.string.not_notify_content)
                .setPositiveButton(R.string.go_to_grant) { _, _ ->
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }
                .setNegativeButton(R.string.user_cancel, null)
                .setCancelable(false)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        }
        return ret
    }

    private fun checkRAM() {
        val activityManager =
            requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemoryGB = memoryInfo.totalMem / (1024 * 1024 * 1024.0)

        if (totalMemoryGB < 6) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.small_ram_title)
                .setMessage(R.string.small_ram_content)
                .setCancelable(false)
                .setPositiveButton(R.string.user_known, null)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        }
    }

    private fun showUpdateDialog(update: UpdateResult.UpdateAvailable) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.find_new_version)
            .setMessage(
                getString(R.string.version_name) + update.versionName + "\n{${update.versionDescription}}\n" + getString(
                    R.string.update_prompt
                )
            )
            .setCancelable(false)
            .setNeutralButton(R.string.ignore_update) { _, _ ->
                prefs.setLong("Ignore_Version", update.versionCode)
            }
            .setPositiveButton(R.string.go_to_update) { _, _ ->
                val url = "https://www.moetranslate.top/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            .setNegativeButton(R.string.not_update, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showNotificationDialog(notification: NotificationResult.NotificationAvailable) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(notification.notificationName)
            .setMessage(notification.notificationContent)
            .setCancelable(false)
            .setPositiveButton(R.string.user_known) { _, _ ->
                prefs.setLong("Read_Notice", notification.notificationCode)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showLanguageListDialog(type: Int) {
        if (((prefs.getInt("Translate_Mode", Constants.TranslateMode.TEXT.id) == Constants.TranslateMode.TEXT.id) && (prefs.getInt(
                "Text_API",
                Constants.TextApi.BING.id
            ) == Constants.TextApi.CUSTOM_TEXT.id) && (type == 2)) || ((prefs.getInt(
                "Translate_Mode",
                Constants.TranslateMode.TEXT.id
            ) == Constants.TranslateMode.PIC.id) && (prefs.getInt("Pic_API", Constants.PicApi.BAIDU.id) == Constants.PicApi.CUSTOM_PIC.id))
        ) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.custom_api_select_language_title)
                .setMessage(R.string.custom_api_select_language_content)
                .setCancelable(false)
                .setPositiveButton(R.string.user_known, null)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        } else {
            Log.d(TAG, TranslateTools.getLanguagesList(requireContext(), type)!!.toString())
            LanguageSelectionDialog(
                requireContext(),
                type,
                TranslateTools.getLanguagesList(requireContext(), type)!!
            ) { selectedLocale ->
                if (type == 1) {
                    Log.d(TAG, "Source_Language：" + selectedLocale.getCode())
                    prefs.setString("Source_Language", selectedLocale.getCode())
                    binding.SourceLanguageName.text =
                        CustomLocale.getInstance(prefs.getString("Source_Language", "ja"))
                            .getDisplayName()
                } else {
                    Log.d(TAG, "Target_Language：" + selectedLocale.getCode())
                    prefs.setString("Target_Language", selectedLocale.getCode())
                    binding.TargetLanguageName.text =
                        CustomLocale.getInstance(prefs.getString("Target_Language", "zh"))
                            .getDisplayName()
                }
            }.show()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()

        Log.d(TAG, "onStop")

        // 注销广播接收器
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(serviceStopReceiver)
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume")

        showAPIName()
        setTitleAndButton(isServiceRunning(FloatingBallService::class.java))
    }

    // 实际启动服务的方法
    private fun launchFloatingBallService() {
        try {
            // 检查服务是否已经在运行
            if (!isServiceRunning(FloatingBallService::class.java)) {
                val serviceIntent = Intent(requireContext(), FloatingBallService::class.java)
                requireContext().startService(serviceIntent)
                showToast(getString(R.string.startup_success), true)
                setTitleAndButton(true)
            } else {
                setTitleAndButton(true)
                showToast("already running")
            }
        } catch (e: Exception) {
            showToast(getString(R.string.startup_failure, e.toString()))
        }
    }

    private fun stopFloatingBallService() {
        try {
            // 检查服务是否已经停止
            if (isServiceRunning(FloatingBallService::class.java)) {
                val intent = Intent(requireContext(), FloatingBallService::class.java)
                requireContext().stopService(intent)
                showToast(getString(R.string.stop_success), true)
                setTitleAndButton(false)
            } else {
                setTitleAndButton(false)
                showToast("already stopped")
            }
        } catch (e: Exception) {
            showToast(getString(R.string.stop_failed, e.toString()))
        }
    }

    // 检查服务是否正在运行
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        Log.d("SERVICE", manager.getRunningServices(Int.MAX_VALUE).toString())
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    private fun showToast(str: String, isShort: Boolean = false) {
        if (isShort) {
            Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
        }
    }
}