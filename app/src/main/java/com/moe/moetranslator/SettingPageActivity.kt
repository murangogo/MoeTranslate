package com.moe.moetranslator

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
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
            1->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,Apiselect()).commit()
            2->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,ApiConfig()).commit()
            3->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,Errcode()).commit()
            6->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,Developer()).commit()
            else->Toast.makeText(applicationContext,"未知错误。", Toast.LENGTH_LONG).show()
        }
    }
}