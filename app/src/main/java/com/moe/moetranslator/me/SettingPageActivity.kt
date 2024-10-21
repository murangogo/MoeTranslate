package com.moe.moetranslator.me

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.moe.moetranslator.databinding.ActivitySettingPageBinding

class SettingPageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val TYPE_FRAGMENT_TRANSLATE_MODE = 1
        const val TYPE_FRAGMENT_API_CONFIG = 2
        const val TYPE_FRAGMENT_PERSONALIZATION = 3
        const val TYPE_FRAGMENT_READ = 4
        const val TYPE_FRAGMENT_FAQ = 5
        const val TYPE_FRAGMENT_ERROR_CODE = 6
        const val TYPE_FRAGMENT_DEVELOPER = 7
    }

    private lateinit var binding: ActivitySettingPageBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        binding = ActivitySettingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when(intent.getIntExtra(EXTRA_FRAGMENT_TYPE,0)){
            TYPE_FRAGMENT_TRANSLATE_MODE->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                TranslationMode()
            ).commit()
            TYPE_FRAGMENT_API_CONFIG->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                APIConfig()
            ).commit()
//            TYPE_FRAGMENT_PERSONALIZATION->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
//                //TODO Personalization
//            ).commit()
            TYPE_FRAGMENT_READ->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                ReadPage()
            ).commit()
            TYPE_FRAGMENT_FAQ->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                FAQPage()
            ).commit()
            TYPE_FRAGMENT_ERROR_CODE->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                Errcode()
            ).commit()
            TYPE_FRAGMENT_DEVELOPER->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                Developer()
            ).commit()
            else->Toast.makeText(applicationContext,"Unknown Error.", Toast.LENGTH_LONG).show()
        }
    }
}