package com.moe.moetranslator.me

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moe.moetranslator.BaseActivity
import com.moe.moetranslator.databinding.ActivityManageBinding

class ManageActivity : BaseActivity() {

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val EXTRA_CUSTOM_CODE = "custom_code"
        const val TYPE_FRAGMENT_MANAGE_MLKIT = 1
        const val TYPE_FRAGMENT_MANAGE_NLLB = 2
        const val TYPE_FRAGMENT_MANAGE_NIU_API = 3
        const val TYPE_FRAGMENT_MANAGE_VOLC_API = 4
        const val TYPE_FRAGMENT_MANAGE_AZURE_API = 5
        const val TYPE_FRAGMENT_MANAGE_BAIDU_API = 6
        const val TYPE_FRAGMENT_MANAGE_TENCENT_API = 7
        const val TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API = 8
        const val TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API = 9
        const val CODE_CUSTOM_0 = 0
        const val CODE_CUSTOM_1 = 1
        const val CODE_CUSTOM_2 = 2
    }

    private lateinit var binding: ActivityManageBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        binding = ActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applySystemBarsPadding(binding.manageFragmentContainer, true, true)

        when(intent.getIntExtra(EXTRA_FRAGMENT_TYPE,0)){
            TYPE_FRAGMENT_MANAGE_MLKIT-> {
                val fragment = MLKitDownloadFragment()
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_NLLB-> {
                val fragment = NLLBDownloadFragment()
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_NIU_API->{
                val fragment = OnlineAPI()
                val args = Bundle().apply {
                    putString("api_type", "niu")
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_VOLC_API->{
                val fragment = OnlineAPI()
                val args = Bundle().apply {
                    putString("api_type", "volc")
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_AZURE_API->{
                val fragment = OnlineAPI()
                val args = Bundle().apply {
                    putString("api_type", "azure")
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_BAIDU_API->{
                val fragment = OnlineAPI()
                val args = Bundle().apply {
                    putString("api_type", "baidu")
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_TENCENT_API->{
                val fragment = OnlineAPI()
                val args = Bundle().apply {
                    putString("api_type", "tencent")
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API->{
                val fragment = CustomTextAPI()
                val args = Bundle().apply {
                    putInt(EXTRA_CUSTOM_CODE, intent.getIntExtra(EXTRA_CUSTOM_CODE,0))
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API->{
                val fragment = CustomPicAPI()
                val args = Bundle().apply {
                    putInt(EXTRA_CUSTOM_CODE, intent.getIntExtra(EXTRA_CUSTOM_CODE,0))
                }
                fragment.arguments = args
                supportFragmentManager.beginTransaction()
                    .replace(binding.manageFragmentContainer.id, fragment)
                    .commit()
            }
            else-> Toast.makeText(applicationContext,"Unknown Error.", Toast.LENGTH_LONG).show()
        }
    }
}