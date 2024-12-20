package com.moe.moetranslator

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moe.moetranslator.madoka.DialogManager

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏

        // 初始化弹窗管理类
        DialogManager.init(this)

        setContentView(R.layout.activity_main)

        applySystemBarsPadding(findViewById(R.id.fragment_view), true, false)

        //关联NavController与BottonNavigationView
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_view) as NavHostFragment
        val navController = navHost.navController
        val bottomNavigation:BottomNavigationView=findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)
    }

}