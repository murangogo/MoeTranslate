package com.moe.moetranslator.me

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.ActivityManageBinding
import com.moe.moetranslator.databinding.ActivitySettingPageBinding

class ManageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val TYPE_FRAGMENT_MANAGE_NLLB = 1
        const val TYPE_FRAGMENT_MANAGE_BAIDU_API = 2
        const val TYPE_FRAGMENT_MANAGE_TENCENT_API = 3
        const val TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API = 4
        const val TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API = 5
    }

    private lateinit var binding: ActivityManageBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        binding = ActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when(intent.getIntExtra(EXTRA_FRAGMENT_TYPE,0)){
//            TYPE_FRAGMENT_MANAGE_NLLB->supportFragmentManager.beginTransaction().replace(binding.manageFragmentContainer.id,
//                //TODO DOWNLOAD
//            ).commit()
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
//            TYPE_FRAGMENT_MANAGE_CUSTOM_TEXT_API->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
//                //TODO CUSTOM
//            ).commit()
//            TYPE_FRAGMENT_MANAGE_CUSTOM_PIC_API->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
//                //TODO CUSTOM
//            ).commit()
            else-> Toast.makeText(applicationContext,"Unknown Error.", Toast.LENGTH_LONG).show()
        }
    }
}