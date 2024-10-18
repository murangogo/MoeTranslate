package com.moe.moetranslator.me

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.moe.moetranslator.databinding.ActivitySettingPageBinding

class SettingPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingPageBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        binding = ActivitySettingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        when(intent.getIntExtra("page",0)){
            1->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                TranslationMode()
            ).commit()
            2->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                APIConfig()
            ).commit()
            3->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                Errcode()
            ).commit()
            4->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                ReadPage()
            ).commit()
            6->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                Developer()
            ).commit()
            7->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                FAQPage()
            ).commit()
            else->Toast.makeText(applicationContext,"未知错误。", Toast.LENGTH_LONG).show()
        }
    }
}