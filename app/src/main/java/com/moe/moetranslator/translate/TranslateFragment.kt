package com.moe.moetranslator.translate

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.moe.moetranslator.BuildConfig
import com.moe.moetranslator.utils.ConstDatas
import com.moe.moetranslator.launch.FirstLaunchPage
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.TranslateFragmentBinding
import com.moe.moetranslator.me.SettingPageActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import translateapi.baidufanyiapi.data.Config
import translateapi.baidufanyiapi.data.Language
import translateapi.tencentyunapi.ImageTranslate


class TranslateFragment : Fragment() {
    companion object{
        @JvmField
        val config: Config =
            Config("", "")
    }
    lateinit var starAdapter1:ArrayAdapter<Any>
    lateinit var starAdapter2:ArrayAdapter<Any>
    private val BoriLanguage = arrayOf("自动检测", "中文", "英语", "日语", "韩语", "法语", "西班牙语", "俄语", "葡萄牙语", "德语", "意大利语", "丹麦语", "荷兰语", "马来语", "瑞典语", "印尼语", "波兰语", "罗马尼亚语", "土耳其语", "希腊语", "匈牙利语")
    private val BtarLanguage = arrayOf("中文", "英语", "日语", "韩语", "法语", "西班牙语", "俄语", "葡萄牙语", "德语", "意大利语", "丹麦语", "荷兰语", "马来语", "瑞典语", "印尼语", "波兰语", "罗马尼亚语", "土耳其语", "希腊语", "匈牙利语")
    private val ToriLanguage = arrayOf("自动识别", "简体中文", "繁体中文", "英语", "日语", "韩语", "俄语", "法语", "德语", "意大利语", "西班牙语", "葡萄牙语", "马来西亚语", "泰语", "越南语")
    private val TtarLanguage_1 = arrayOf("英语", "日语", "韩语", "俄语", "法语", "德语", "意大利语", "西班牙语", "葡萄牙语", "马来西亚语", "泰语", "越南语")
    private val TtarLanguage_2 = arrayOf("中文", "日语", "韩语", "俄语", "法语", "德语", "意大利语", "西班牙语", "葡萄牙语", "马来西亚语", "泰语", "越南语")
    private val TtarLanguage_3 = arrayOf("中文", "英语", "韩语")
    private val TtarLanguage_4 = arrayOf("中文", "英语", "日语")
    private val TtarLanguage_5 = arrayOf("中文", "英语")
    private lateinit var binding: TranslateFragmentBinding
    private lateinit var repository: MySharedPreferenceData
    //检查更新使用的协程
    private val job1:Job = Job()
    private val job2:Job = Job()
    val scope1 = CoroutineScope(Dispatchers.Default + job1)
    val scope2 = CoroutineScope(Dispatchers.Default + job2)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("注意","FYonCreate")
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
        try {
            checkForUpdates(scope1)
        }catch (_:Exception){

        }
        try {
            getNewNotice(scope2,0)
        }catch (_:Exception){

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("注意","onCreateView")
        binding = TranslateFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(repository.ApiChoose==0){
            binding.selectAPI.text = "API：腾讯云"
        }else{
            binding.selectAPI.text = "API：百度翻译"
        }
        MainScope().launch {
            try {
                val noticecode = getNoticeCode()
                Log.d("注意","get: "+noticecode)
                if(repository.NoticeCode<noticecode){
                    binding.notice.setImageResource(R.drawable.red_notice)
                }
            }catch (_:Exception){

            }
        }
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
        if(repository.ApiChoose==0){
            binding.oriLanguage.setSelection(repository.TFromNum)
            binding.tarLanguage.setSelection(repository.TToNum)
        }else{
            binding.oriLanguage.setSelection(repository.BFromNum)
            binding.tarLanguage.setSelection(repository.BToNum)
            config.langfrom(repository.LanguageFrom_Baidu)
            config.langto(repository.LanguageTo_Baidu)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("注意", "onStart")
            config.erase(Config.ERASE_NONE)
            config.paste(Config.PASTE_NONE)
            ConstDatas.FilePath = context!!.externalCacheDir.toString()

        val intent1 = Intent(this.context, FirstLaunchPage::class.java)
        binding.floatball.setBackgroundResource(R.drawable.translatebutton_shape)
        binding.floatball.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                val dialogper0 = AlertDialog.Builder(activity)
                    .setTitle("安卓版本不支持")
                    .setMessage("检测到您的安卓版本小于Android 11（API 30）或Harmony OS版本低于3.0（如果您的版本大于Harmony 3.0，请查看Me页面的“常见问题”），这意味着您无法使用本软件的翻译功能，但您仍然可以使用除翻译功能外的其他功能。若您想体验本软件的全部功能，请升级系统或更换设备。")
                    .setCancelable(false)
                    .setPositiveButton("我知道了") { _, _ -> }
                    .create()
                    dialogper0.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                    dialogper0.show()
            }else{
                if(((repository.ApiChoose==0)&&((repository.TencentApiS=="")||(repository.TencentApiK=="")))||((repository.ApiChoose==1)&&((config.appId=="")||(config.secretKey=="")))){
                    val dialogperapi = AlertDialog.Builder(activity)
                        .setTitle("未配置${if(repository.ApiChoose==0)"腾讯云" else "百度翻译"}API")
                        .setMessage("您未配置${if(repository.ApiChoose==0)"腾讯云" else "百度翻译"}API，将无法使用翻译功能，请配置${if(repository.ApiChoose==0)"腾讯云" else "百度翻译"}API。")
                        .setCancelable(false)
                        .setPositiveButton("去配置") { _, _ ->
                            var intent2 = Intent(context, SettingPageActivity::class.java)
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

        binding.notice.setOnClickListener {
            try {
                Toast.makeText(context, "正在获取通知...", Toast.LENGTH_LONG).show()
                getNewNotice(scope2,1)
            }catch (_:Exception){

            }
        }
    }

    fun checkForUpdates(myscope: CoroutineScope): Job {
        return myscope.launch {
            var versionCode:Long = 0
            var versionName = ""
            var versionContent = ""
            val client = OkHttpClient()
            try{
                val request = Request.Builder().url("https://www.moetranslate.top/version.json").build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonData = response.body?.string()
                        val obj = JSONParser().parse(jsonData)
                        val jo = obj as JSONObject
                        versionCode = jo["versionCode"] as Long
                        versionName = jo["versionName"] as String
                        versionContent = jo["versionContent"] as String
                    }
                    yield()
                    MainScope().launch {
                        if ((BuildConfig.VERSION_CODE < versionCode)&&(repository.NotUpadateCode!= versionCode)) {
                            val dialogupdate = AlertDialog.Builder(activity)
                                .setTitle("检测到新版本")
                                .setMessage("检测到了新版本：$versionName，\n$versionContent\n是否现在更新？点击去更新即可跳转到萌译官网，在官网中点击下载即可获取最新版本。")
                                .setCancelable(false)
                                .setNeutralButton("此版本不再提醒"){_,_->
                                    repository.saveUpdateCode(versionCode)
                                }
                                .setPositiveButton("去更新") { _, _ ->
                                    val url = "https://www.moetranslate.top/"
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(url)
                                    startActivity(intent)
                                }
                                .setNegativeButton("暂不更新") { _, _ -> }
                                .create()
                            dialogupdate.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                            dialogupdate.show()
                        }
                    }
                }
            }catch (_:Exception){

            }
        }
    }
    fun getNewNotice(myscope: CoroutineScope,noticecase:Int): Job {
        return myscope.launch {
            var NoticeCode:Long = 0
            var NoticeName = ""
            var NoticeContent = ""
            val client = OkHttpClient()
            try{
                val request = Request.Builder().url("https://www.moetranslate.top/notice.json").build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonData = response.body?.string()
                        val obj = JSONParser().parse(jsonData)
                        val jo = obj as JSONObject
                        NoticeCode = jo["NoticeCode"] as Long
                        NoticeName = jo["NoticeName"] as String
                        NoticeContent = jo["NoticeContent"] as String
                    }
                    yield()
                    MainScope().launch {
                        if ((repository.NoticeCode < NoticeCode)||(noticecase==1)) {
                            val dialogupdate = AlertDialog.Builder(activity)
                                .setTitle(NoticeName)
                                .setMessage(NoticeContent)
                                .setCancelable(false)
                                .setPositiveButton("我知道了") { _, _ ->
                                    repository.saveNoticeCode(NoticeCode)
                                }
                                .create()
                            dialogupdate.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                            dialogupdate.show()
                        }
                        binding.notice.setImageResource(R.drawable.notice)
                    }
                }
            }catch (_:Exception){
                if(noticecase==1){
                    MainScope().launch{
                        Toast.makeText(context, "获取通知失败，可能是网络未连接。", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    suspend fun getNoticeCode(): Long = withContext(Dispatchers.IO) {
        var NoticeCode:Long = 0
        val client = OkHttpClient()
        try{
            val request = Request.Builder().url("https://www.moetranslate.top/notice.json").build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    val obj = JSONParser().parse(jsonData)
                    val jo = obj as JSONObject
                    NoticeCode = jo["NoticeCode"] as Long
                }
            }
        }catch (e:Exception){
            return@withContext -1L
        }
        return@withContext NoticeCode
    }

    fun startBall(){
        val intent = Intent(this.context, FloatingService::class.java)
        context!!.startService(intent)
    }

    private fun initSpinnerForDropdown() {
        Log.d("hhh","hhhhh")
        // 声明一个下拉列表的数组适配器
        if(repository.ApiChoose==0){
            starAdapter1 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, ToriLanguage)
            when(repository.TFromNum){
                0->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_5)
                1->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_1)
                2->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_1)
                3->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_2)
                4->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_3)
                5->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_4)
                else->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_5)
            }
        }else{
            starAdapter1 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, BoriLanguage)
            starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, BtarLanguage)
        }
        // 从布局文件中获取名叫sp_dropdown的下拉框
        binding.oriLanguage.adapter = starAdapter1 // 设置下拉框的数组适配器
        binding.tarLanguage.adapter = starAdapter2
        binding.oriLanguage.setSelection(0) // 设置下拉框默认显示第一项
        binding.tarLanguage.setSelection(0)
        // 给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        binding.oriLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(repository.ApiChoose==0){
                    if(repository.TFromNum!=i){
                        binding.tarLanguage.setSelection(0)
                        repository.saveTFrom(Language.LanguageMap_Tencent.get(ToriLanguage.get(i))!!,i)
                        when(repository.TFromNum){
                            0->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_5)
                            1->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_1)
                            2->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_1)
                            3->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_2)
                            4->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_3)
                            5->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_4)
                            else->starAdapter2 = ArrayAdapter(context!!, android.R.layout.simple_dropdown_item_1line, TtarLanguage_5)
                        }
                        binding.tarLanguage.adapter = starAdapter2
                            if((repository.TFromNum!=1)&&(repository.TFromNum!=2)){
                                repository.saveTTo("zh",0)
                                binding.tarLanguage.setSelection(0)
                            }else{
                                repository.saveTTo("en",0)
                                binding.tarLanguage.setSelection(0)
                            }
                    }
                    Log.d("ORICLICK", i.toString() + "即" + ToriLanguage.get(i) + "即" + Language.LanguageMap_Tencent.get(ToriLanguage.get(i)))
                }else{
                    config.langfrom(Language.LanguageMap_Baidu.get(BoriLanguage.get(i)))
                    repository.saveBFrom(Language.LanguageMap_Baidu.get(BoriLanguage.get(i))!!,i)
                    Log.d("ORICLICK", i.toString() + "即" + BoriLanguage.get(i) + "即" + Language.LanguageMap_Baidu.get(BoriLanguage.get(i)))
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        binding.tarLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(repository.ApiChoose==0){
                    when(repository.TFromNum){
                        0->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_5.get(i))!!,i)
                        1->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_1.get(i))!!,i)
                        2->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_1.get(i))!!,i)
                        3->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_2.get(i))!!,i)
                        4->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_3.get(i))!!,i)
                        5->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_4.get(i))!!,i)
                        else->repository.saveTTo(Language.LanguageMap_Tencent.get(TtarLanguage_5.get(i))!!,i)
                    }
                    Log.d("TARCLICK", i.toString() + "即" + repository.LanguageTo_Tencent)
                }else{
                    config.langto(Language.LanguageMap_Baidu.get(BtarLanguage.get(i)))
                    repository.saveBTo(Language.LanguageMap_Baidu.get(BtarLanguage.get(i))!!,i)
                    Log.d("TARCLICK", i.toString() + "即" + BtarLanguage.get(i) + "即" + Language.LanguageMap_Baidu.get(BtarLanguage.get(i)))
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }
}