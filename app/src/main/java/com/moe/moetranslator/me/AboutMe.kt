package com.moe.moetranslator.me

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.moe.moetranslator.utils.ConstDatas
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentAboutMeBinding
import com.moe.moetranslator.utils.UpdateChecker
import com.moe.moetranslator.utils.UpdateResult
import kotlinx.coroutines.launch
import java.io.File


class AboutMe : Fragment() {
    private lateinit var binding: FragmentAboutMeBinding
    private lateinit var updateChecker: UpdateChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateChecker = UpdateChecker(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAboutMeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun sizecalculator() {
            val size:Long = getFolderSize(File(requireContext().externalCacheDir.toString()))
            Log.d("DIR",requireContext().externalCacheDirs.toString())
            val sizedo:Double = size/1024.0
            if(sizedo<1024.0){
                binding.cachesize.text = String.format("%.2f",sizedo)+"KB"
            }else{
                binding.cachesize.text = String.format("%.2f",sizedo/1024.0)+"MB"
            }
        }

        sizecalculator()
        binding.translateModeBtn.setOnClickListener{
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_TRANSLATE_MODE)
            startActivity(intent)
        }
        binding.apiConfigBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_API_CONFIG)
            startActivity(intent)
        }
        binding.personalizationBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_PERSONALIZATION)
            startActivity(intent)
        }
        binding.readBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_READ)
            startActivity(intent)
        }
        binding.faqBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_FAQ)
            startActivity(intent)
        }
        binding.errorCodeBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_ERROR_CODE)
            startActivity(intent)
        }
        binding.updateBtn.setOnClickListener{
            makeToast(getString(R.string.getting_update))
            checkForUpdate()
        }
        binding.cleanBtn.setOnClickListener {
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
        binding.developerBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingPageActivity::class.java)
            intent.putExtra(SettingPageActivity.EXTRA_FRAGMENT_TYPE, SettingPageActivity.TYPE_FRAGMENT_DEVELOPER)
            startActivity(intent)
        }
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

    private fun checkForUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = updateChecker.checkForUpdate()) {
                is UpdateResult.UpdateAvailable -> { showUpdateDialog(result) }
                is UpdateResult.NoUpdate -> { makeToast(getString(R.string.no_update)) }
                else -> { makeToast(getString(R.string.internet_error)) }
            }
        }
    }

    private fun showUpdateDialog(update: UpdateResult.UpdateAvailable) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.find_new_version)
            .setMessage(getString(R.string.version_name)+ update.versionName+"\n{${update.versionDescription}}\n"+getString(R.string.update_prompt))
            .setCancelable(false)
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

    fun makeToast(str: String){
        Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}