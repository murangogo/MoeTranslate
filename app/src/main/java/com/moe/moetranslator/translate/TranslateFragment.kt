package com.moe.moetranslator.translate

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.moe.moetranslator.launch.FirstLaunchPage
import com.moe.moetranslator.utils.UpdateChecker
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentTranslateBinding
import com.moe.moetranslator.utils.CustomPreference
import com.moe.moetranslator.utils.NotificationChecker
import com.moe.moetranslator.utils.NotificationResult
import com.moe.moetranslator.utils.UpdateResult
import kotlinx.coroutines.launch

val TAG = "TranslateFragment"

class TranslateFragment : Fragment() {
    private lateinit var binding: FragmentTranslateBinding
    private lateinit var updateChecker: UpdateChecker
    private lateinit var notificationChecker: NotificationChecker
    private lateinit var prefs: CustomPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = CustomPreference.getInstance(requireContext())
        updateChecker = UpdateChecker(requireContext())
        notificationChecker = NotificationChecker(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTranslateBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForUpdate()
        checkNotification()

        val helpIntent = Intent(requireContext(), FirstLaunchPage::class.java)
        binding.help.setOnClickListener {
            startActivity(helpIntent)
        }

        binding.notice.setOnClickListener {
                showToast(getString(R.string.getting_notification))
                checkNotification(true)
        }

        // 未生效
        binding.floatball.setBackgroundResource(R.drawable.translatebutton_shape)
//        binding.floatball.setOnClickListener {
//            if(checkAndroidSDK() && checkAccessibilityService() && checkFloatingBall() && checkNotify() && checkTranslateAPI()){
//                if((prefs.getInt("Translate_Mode",0) == 0) && (prefs.getInt("OCR_API",0) == 0) && (prefs.getInt("OCR_AI",0) == 1)){
//                    checkRAM()
//                }
//                launchFloatingBallService()
//            }
//        }
        binding.floatball.setOnClickListener {
            launchFloatingBallService()
        }

        if(((prefs.getInt("Translate_Mode",0) == 0) && (prefs.getInt("OCR_API",0) == 3)) || ((prefs.getInt("Translate_Mode",0) == 1) && (prefs.getInt("Pic_API",0) == 2))){
            binding.SourceLanguageName.text = getString(R.string.custom_api_select_language)
            binding.TargetLanguageName.text = getString(R.string.custom_api_select_language)
        }else{
            binding.SourceLanguageName.text = CustomLocale.getInstance(prefs.getString("Source_Language","ja")).getDisplayName()
            binding.TargetLanguageName.text = CustomLocale.getInstance(prefs.getString("Target_Language","zh")).getDisplayName()
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
                is UpdateResult.UpdateAvailable -> { if (prefs.getLong("Ignore_Version",0) != result.versionCode) showUpdateDialog(result)}
                else -> { Log.e(TAG, "No update or Internet error") }
            }
        }
    }

    private fun checkNotification(userGet: Boolean = false) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = notificationChecker.checkNotification()) {
                is NotificationResult.NotificationAvailable -> { if ((prefs.getLong("Read_Notice",0) != result.notificationCode) || userGet) showNotificationDialog(result)}
                else -> { if (userGet) showToast(getString(R.string.get_notification_error)) }
            }
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
        }else{
            return true
        }
    }

    private fun checkTranslateAPI(): Boolean{
        val translateMode = prefs.getInt("Translate_Mode", 0)
        val ocrApi = prefs.getInt("OCR_API", 0)
        val ocrAi = prefs.getInt("OCR_AI", 0)
        val picApi = prefs.getInt("Pic_API", 0)
        val customTextApi = prefs.getInt("Custom_Text_API", 0)
        val customPicApi = prefs.getInt("Custom_Pic_API", 0)

        Log.d(TAG, "translatemode$translateMode，ocrapi:$ocrApi，ocrAI:$ocrAi，picapi:$picApi，customtextapi:$customTextApi，custompicapi:$customPicApi")

        val ret: Boolean = when {
            translateMode == 0 -> when (ocrApi) {
                0 ->{
                    if((ocrAi == 0) && (!(prefs.getBoolean("Download_MLKit",false)))){
                        Log.d(TAG, "Download_MLKit"+prefs.getBoolean("Download_MLKit",false).toString())
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.mlkit_not_download_title)
                            .setMessage(R.string.mlkit_not_download_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_download) { _, _ ->
                                // TODO：跳转到下载页面
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    } else if ((ocrAi == 1) && (!(prefs.getBoolean("Download_NLLB",false)))) {
                        Log.d(TAG, "Download_NLLB"+prefs.getBoolean("Download_NLLB",false).toString())
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.nllb_not_download_title)
                            .setMessage(R.string.nllb_not_download_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_download) { _, _ ->
                                // TODO：跳转到下载页面
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    }else{
                        true
                    }
                }
                1 -> {
                    if(prefs.getString("Baidu_Translate_EncryptedKey","") == ""){
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(R.string.baidu_api_not_config_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                // TODO：跳转到API设置页面
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    }else{
                        true
                    }
                }
                2 -> {
                    if(prefs.getString("Tencent_Cloud_EncryptedKey","") == ""){
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(R.string.tencent_api_not_config_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                // TODO：跳转到API设置页面
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    }else{
                        true
                    }
                }
                else -> {
                    when (customTextApi) {
                        0 -> {
                            if((prefs.getString("Custom_Text_API_1","") == "") || (prefs.getString("Custom_Text_JSON_Parse_1","") == "")){
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        // TODO：跳转到API设置页面
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            }else{
                                true
                            }
                        }
                        1 -> {
                            if((prefs.getString("Custom_Text_API_2","") == "") || (prefs.getString("Custom_Text_JSON_Parse_2","") == "")){
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        // TODO：跳转到API设置页面
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            }else{
                                true
                            }
                        }
                        else -> {
                            if((prefs.getString("Custom_Text_API_3","") == "") || (prefs.getString("Custom_Text_JSON_Parse_3","") == "")){
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        // TODO：跳转到API设置页面
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            }else{
                                true
                            }
                        }
                    }
                }
            }
            else -> when (picApi) {
                0 -> {
                    if(prefs.getString("Baidu_Translate_EncryptedKey","") == ""){
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(R.string.baidu_api_not_config_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                // TODO：跳转到API设置页面
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    }else {
                        true
                    }
                }
                1 -> {
                    if(prefs.getString("Tencent_Cloud_EncryptedKey","") == ""){
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.api_not_config_title)
                            .setMessage(R.string.tencent_api_not_config_content)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_config) { _, _ ->
                                // TODO：跳转到API设置页面
                            }
                            .setNegativeButton(R.string.user_cancel, null)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        false
                    }else{
                        true
                    }
                }
                else -> {
                    when (customPicApi) {
                        0 -> {
                            if((prefs.getString("Custom_Pic_API_1","") == "") || (prefs.getString("Custom_Pic_JSON_Parse_1","") == "")){
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        // TODO：跳转到API设置页面
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            }else{
                                true
                            }
                        }
                        1 -> {
                            if((prefs.getString("Custom_Pic_API_2","") == "") || (prefs.getString("Custom_Pic_JSON_Parse_2","") == "")){
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        // TODO：跳转到API设置页面
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            }else{
                                true
                            }
                        }
                        else -> {
                            if((prefs.getString("Custom_Pic_API_3","") == "") || (prefs.getString("Custom_Pic_JSON_Parse_3","") == "")){
                                val dialog = AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.api_not_config_title)
                                    .setMessage(R.string.custom_api_not_config_content)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.go_to_config) { _, _ ->
                                        // TODO：跳转到API设置页面
                                    }
                                    .setNegativeButton(R.string.user_cancel, null)
                                    .create()
                                dialog.show()
                                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                false
                            }else{
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
        val accessibilityManager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

//        Log.d(TAG, accessibilityManager.isEnabled.toString())

        val expectedServiceId = "${requireContext().packageName}/.translate.ScreenShotAccessibilityService"
        val ret = enabledServices.any { it.id == expectedServiceId }
        if(!ret){
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
        if (!ret){
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
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    fun checkRAM(){
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemoryGB = memoryInfo.totalMem / (1024 * 1024 * 1024.0)

        if(totalMemoryGB < 8) {
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
            .setMessage(getString(R.string.version_name)+ update.versionName+"\n{${update.versionDescription}}\n"+getString(R.string.update_prompt))
            .setCancelable(false)
            .setNeutralButton(R.string.ignore_update){_,_->
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
            .setPositiveButton(R.string.user_known){_,_->
                prefs.setLong("Read_Notice", notification.notificationCode)
            }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showLanguageListDialog(type: Int) {
        if(((prefs.getInt("Translate_Mode",0) == 0) && (prefs.getInt("OCR_API",0) == 3)) || ((prefs.getInt("Translate_Mode",0) == 1) && (prefs.getInt("Pic_API",0) == 2))){
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.custom_api_select_language_title)
                .setMessage(R.string.custom_api_select_language_content)
                .setCancelable(false)
                .setPositiveButton(R.string.user_known, null)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        }else{
            Log.d(TAG, TranslateTools.getLanguagesList(requireContext(), type)!!.toString())
            LanguageSelectionDialog(requireContext(), type, TranslateTools.getLanguagesList(requireContext(), type)!!) { selectedLocale ->
                if(type == 1){
                    Log.d(TAG, "Source_Language："+selectedLocale.getCode())
                    prefs.setString("Source_Language", selectedLocale.getCode())
                    binding.SourceLanguageName.text = CustomLocale.getInstance(prefs.getString("Source_Language","ja")).getDisplayName()
                }else{
                    Log.d(TAG, "Target_Language："+selectedLocale.getCode())
                    prefs.setString("Target_Language", selectedLocale.getCode())
                    binding.TargetLanguageName.text = CustomLocale.getInstance(prefs.getString("Target_Language","zh")).getDisplayName()
                }
            }.show()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    // 实际启动服务的方法
    private fun launchFloatingBallService() {
        try {
            val context = requireContext()
            // 检查服务是否已经在运行
            if (!isServiceRunning(FloatingBallService::class.java)) {
                val serviceIntent = Intent(requireContext(), FloatingBallService::class.java)
                requireContext().startService(serviceIntent)
                showToast(getString(R.string.startup_success))
            } else {
                showToast("已在运行")
            }
        } catch (e: Exception) {
            showToast(getString(R.string.startup_failure)+e.toString())
        }
    }

    // 检查服务是否正在运行
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        Log.d("SERVICE",manager.getRunningServices(Int.MAX_VALUE).toString())
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    private fun showToast(str: String){
        Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
    }
}