package com.moe.moetranslator.launch

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moe.moetranslator.MainActivity
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchActivity : AppCompatActivity() {
    private lateinit var prefs: CustomPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        prefs = CustomPreference.getInstance(this)
        setContentView(R.layout.activity_launch)
        MainScope().launch{
            delay(1500)
            finish()
            if(prefs.getBoolean("Is_First_Run",true)){
                val intent = Intent(this@LaunchActivity, FirstLaunchPage::class.java)
                prefs.setBoolean("Is_First_Run", false)
                startActivity(intent)
            }else{
                val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}