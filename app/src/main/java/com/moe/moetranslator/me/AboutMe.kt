package com.moe.moetranslator.me

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moe.moetranslator.BuildConfig
import com.moe.moetranslator.utils.ConstDatas
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.AboutMeFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File


class AboutMe : Fragment() {
    private lateinit var binding: AboutMeFragmentBinding
    private val job:Job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AboutMeFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    fun getFolderSize(dir: File): Long {
        var size: Long = 0
        for (file in dir.listFiles()!!) {
            size += if (file.isFile) {
                file.length()
            } else {
                getFolderSize(file)
            }
        }
        return size
    }

    fun deleteDir(dir: File?) : Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                var success = deleteFile(File(dir, children[i]))
                if(!success){
                    return false
                }
            }
        }
        return true
    }

    fun deleteFile(dir: File?):Boolean{
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteFile(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir?.delete() ?: false
    }

    fun getServiceVersion(myscope: CoroutineScope): Job{
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
                        val jo = JSONObject(jsonData!!)
                        versionCode = jo.getLong("versionCode")
                        versionName = jo.getString("versionName")
                        versionContent = jo.getString("versionContent")
                    }
                    yield()
                    MainScope().launch {
                        if (BuildConfig.VERSION_CODE < versionCode) {
                            val dialogupdate = AlertDialog.Builder(activity)
                                .setTitle("检测到新版本")
                                .setMessage("检测到了新版本：$versionName，\n$versionContent\n是否现在更新？点击去更新即可跳转到萌译官网，在官网中点击下载即可获取最新版本。")
                                .setCancelable(false)
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
                        } else {
                            Toast.makeText(activity, "已是最新版本。", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }catch (_:Exception){
                MainScope().launch{
                    Toast.makeText(activity, "检查更新失败，可能是网络未连接。", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fun sizecalculator() {
            val size:Long = getFolderSize(File(ConstDatas.FilePath))
            val sizedo:Double = size/1024.0
            if(sizedo<1024.0){
                binding.cachesize.text = String.format("%.2f",sizedo)+"KB"
            }else{
                binding.cachesize.text = String.format("%.2f",sizedo/1024.0)+"MB"
            }
        }

        sizecalculator()
        binding.apiselectbtn.setOnClickListener{
            var intent1 = Intent(context, SettingPageActivity::class.java)
            intent1.putExtra("page",1)
            startActivity(intent1)
        }
        binding.apisetbtn.setOnClickListener {
            var intent2 = Intent(context, SettingPageActivity::class.java)
            intent2.putExtra("page",2)
            startActivity(intent2)
        }
        binding.errcodebtn.setOnClickListener {
            var intent3 = Intent(context, SettingPageActivity::class.java)
            intent3.putExtra("page",3)
            startActivity(intent3)
        }
        binding.readbtn.setOnClickListener {
            var intent4 = Intent(context, SettingPageActivity::class.java)
            intent4.putExtra("page",4)
            startActivity(intent4)
        }
        binding.myfaqbtn.setOnClickListener {
            var intent7 = Intent(context, SettingPageActivity::class.java)
            intent7.putExtra("page",7)
            startActivity(intent7)
        }
        binding.updatebtn.setOnClickListener {
            Toast.makeText(context, "正在检查更新...", Toast.LENGTH_LONG).show()
            getServiceVersion(scope)
        }
        binding.cleanbtn.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("清除缓存")
                .setMessage("萌译最多缓存最近200张翻译时的截图，截图大于200张时会进行覆盖，这些截图保存在Android/data/com.moe.moetranslator/cache文件夹中。您可以随时在文件管理中查看。一般来说，这些截图不会占用太多空间，当然，您也可以选择现在清除这些缓存数据。")
                .setCancelable(false)
                .setPositiveButton("清除缓存") { _, _ ->
                    val success = deleteDir(File(ConstDatas.FilePath))
                    if(success){
                        Toast.makeText(context,"清除成功", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(context,"清除失败", Toast.LENGTH_LONG).show()
                    }
                    sizecalculator()
                }
                .setNegativeButton("暂时保留") { _, _ ->}
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
        binding.aboutbtn.setOnClickListener {
            var intent6 = Intent(context, SettingPageActivity::class.java)
            intent6.putExtra("page",6)
            startActivity(intent6)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}