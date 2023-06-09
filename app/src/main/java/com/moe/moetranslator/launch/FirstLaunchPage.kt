package com.moe.moetranslator.launch

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.moe.moetranslator.R

class FirstLaunchPage : AppCompatActivity() {
    lateinit var pager:ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        setContentView(R.layout.activity_first_launch_page)
        pager = findViewById(R.id.teachyou)
        pager.adapter = TutorialAdapter(this)
    }

    fun nextPage(){
        pager.currentItem = pager.currentItem+1
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}