package com.moe.moetranslator.launch

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moe.moetranslator.MainActivity
import com.moe.moetranslator.utils.MySharedPreferenceData
import com.moe.moetranslator.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchActivity : AppCompatActivity() {
    private lateinit var repository: MySharedPreferenceData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        setContentView(R.layout.activity_launch)
        repository = MySharedPreferenceData(this)
        MainScope().launch{
            delay(1500)
            finish()
            if(repository.IsFirstRun){
                var myintent1 = Intent(this@LaunchActivity, FirstLaunchPage::class.java)
                repository.saveFirstRun()
                startActivity(myintent1)
            }else{
                var myintent2 = Intent(this@LaunchActivity, MainActivity::class.java)
                startActivity(myintent2)
            }
        }
    }
}