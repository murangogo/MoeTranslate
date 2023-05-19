package com.moe.moetranslator

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.moe.moetranslator.databinding.FunWithMadokaFragmentBinding
import com.moe.moetranslator.databinding.TranslateFragmentBinding
import translateapi.data.Config
import translateapi.data.Language
import translateapi.http.HttpStringCallback
import translateapi.pic.PicTranslate


class TranslateFragment : Fragment() {
    companion object{
        @JvmField
        val config: Config = Config("", "")
    }
    private val oriLanguage = arrayOf("自动检测", "中文", "英语", "日语", "韩语", "法语", "西班牙语", "俄语", "葡萄牙语", "德语", "意大利语", "丹麦语", "荷兰语", "马来语", "瑞典语", "印尼语", "波兰语", "罗马尼亚语", "土耳其语", "希腊语", "匈牙利语")
    private val tarLanguage = arrayOf("中文", "英语", "日语", "韩语", "法语", "西班牙语", "俄语", "葡萄牙语", "德语", "意大利语", "丹麦语", "荷兰语", "马来语", "瑞典语", "印尼语", "波兰语", "罗马尼亚语", "土耳其语", "希腊语", "匈牙利语")
    private lateinit var binding: TranslateFragmentBinding
    private lateinit var repository: MySharedPreferenceData

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("注意","onCreate")
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
            AlertDialog.Builder(activity)
                .setTitle("安卓版本不支持")
                .setMessage("检测到您的安卓版本小于Android 11（API 30）或Harmony OS版本低于3.0，这意味着您无法使用本软件的翻译功能，但您仍然可以使用除翻译功能外的其他功能。若您想体验本软件的全部功能，请升级系统或更换设备。")
                .setCancelable(false)
                .setPositiveButton("我知道了") { _, _ -> }
                .create()
                .show()
        }
        repository = MySharedPreferenceData(context!!)
        config.appId = repository.BaiduApiA
        config.secretKey = repository.BaiduApiP
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("注意","onCreateView")
        binding = TranslateFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("注意","onViewCreated-------")
        initSpinnerForDropdown()
    }

    override fun onPause() {
        Log.d("注意", "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("注意", "onStop")
        super.onStop()
    }

    override fun onResume() {
        Log.d("注意", "onResume")
        super.onResume()
        binding.oriLanguage.setSelection(repository.FromNum)
        binding.tarLanguage.setSelection(repository.ToNum)
        config.langfrom(repository.LanguageFrom)
        config.langto(repository.LanguageTo)
    }

    override fun onStart() {
        super.onStart()
        Log.d("注意", "onStart")
            config.erase(Config.ERASE_NONE)
            config.paste(Config.PASTE_NONE)
            ConstDatas.FilePath = context!!.externalCacheDir.toString()

        val intent1 = Intent(this.context,FirstLaunchPage::class.java)
        binding.floatball.setBackgroundResource(R.drawable.translatebutton_shape)
        binding.floatball.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                val dialogper0 = AlertDialog.Builder(activity)
                    .setTitle("安卓版本不支持")
                    .setMessage("检测到您的安卓版本小于Android 11（API 30）或Harmony OS版本低于3.0，这意味着您无法使用本软件的翻译功能，但您仍然可以使用除翻译功能外的其他功能。若您想体验本软件的全部功能，请升级系统或更换设备。")
                    .setCancelable(false)
                    .setPositiveButton("我知道了") { _, _ -> }
                    .create()
                    dialogper0.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                    dialogper0.show()
            }else{
                if((config.appId=="")||(config.secretKey=="")){
                    val dialogperapi = AlertDialog.Builder(activity)
                        .setTitle("未配置百度翻译API")
                        .setMessage("您未配置百度翻译API，将无法使用翻译功能，请配置百度翻译API。")
                        .setCancelable(false)
                        .setPositiveButton("去配置") { _, _ ->
                            var intent2 = Intent(context,SettingPageActivity::class.java)
                            intent2.putExtra("page",2)
                            startActivity(intent2)
                        }
                        .setNegativeButton("再说吧") { _, _ ->}
                        .create()
                    dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                    dialogperapi.show()
                }else{
                    val am = context!!.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
                    val isAccessibilityEnabled = am.isEnabled
                    if(!isAccessibilityEnabled){
                        val dialogper1 = AlertDialog.Builder(activity)
                            .setTitle("未开启无障碍权限")
                            .setMessage("您未开启无障碍权限，将无法使用翻译功能，请为软件授予无障碍权限。")
                            .setCancelable(false)
                            .setPositiveButton("去授权") { _, _ ->
                                startActivity(
                                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                )
                            }
                            .setNegativeButton("再说吧") { _, _ ->}
                            .create()
                        dialogper1.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        dialogper1.show()
                    }else{
                        if(!(Settings.canDrawOverlays(context))){
                            val dialogper2 = AlertDialog.Builder(activity)
                                .setTitle("未开启悬浮窗权限")
                                .setMessage("您未开启悬浮窗权限，将无法使用翻译功能，请为软件授予悬浮窗权限。")
                                .setCancelable(false)
                                .setPositiveButton("去授权") { _, _ ->
                                    val intenttemp = Intent()
                                    intenttemp.action = "android.settings.APPLICATION_DETAILS_SETTINGS";
                                    intenttemp.data = Uri.fromParts("package", context!!.packageName, null)
                                    context!!.startActivity(intenttemp)
                                }
                                .setNegativeButton("再说吧") { _, _ ->}
                                .create()
                            dialogper2.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                            dialogper2.show()
                        }else{
                            val areNotificationsEnabled = NotificationManagerCompat.from(context!!).areNotificationsEnabled()
                            if (!areNotificationsEnabled){
                                val dialogper3 = AlertDialog.Builder(activity)
                                    .setTitle("未开启通知权限")
                                    .setMessage("您未开启通知权限，将会降低翻译功能的体验，请为软件授予通知权限。")
                                    .setCancelable(false)
                                    .setPositiveButton("去授权") { _, _ ->
                                        val intenttemp = Intent()
                                        intenttemp.action = "android.settings.APPLICATION_DETAILS_SETTINGS";
                                        intenttemp.data = Uri.fromParts("package", context!!.packageName, null)
                                        context!!.startActivity(intenttemp)
                                    }
                                    .setNegativeButton("再说吧") { _, _ ->}
                                    .create()
                                dialogper3.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                                dialogper3.show()
                            }else{
                                startBall()
                            }
                        }
                    }
                }
            }
        }

        binding.help.setOnClickListener {
            context!!.startActivity(intent1)
        }
    }

    fun startBall(){
        val intent = Intent(this.context,FloatingService::class.java)
        context!!.startService(intent)
    }

    private fun initSpinnerForDropdown() {
        Log.d("hhh","hhhhh")
        // 声明一个下拉列表的数组适配器
        val starAdapter1 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, oriLanguage)
        val starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, tarLanguage)
        // 从布局文件中获取名叫sp_dropdown的下拉框
        binding.oriLanguage.adapter = starAdapter1 // 设置下拉框的数组适配器
        binding.tarLanguage.adapter = starAdapter2
        binding.oriLanguage.setSelection(0) // 设置下拉框默认显示第一项
        binding.tarLanguage.setSelection(0)
        // 给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        binding.oriLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                config.langfrom(Language.LanguageMap.get(oriLanguage.get(i)))
                repository.saveFrom(Language.LanguageMap.get(oriLanguage.get(i))!!,i)
                Log.d("ORICLICK", i.toString() + "即" + oriLanguage.get(i) + "即" + Language.LanguageMap.get(oriLanguage.get(i)))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        binding.tarLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                config.langto(Language.LanguageMap.get(tarLanguage.get(i)))
                repository.saveTo(Language.LanguageMap.get(tarLanguage.get(i))!!,i)
                Log.d("TARCLICK", i.toString() + "即" + tarLanguage.get(i) + "即" + Language.LanguageMap.get(tarLanguage.get(i)))
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }
}