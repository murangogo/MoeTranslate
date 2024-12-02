package com.moe.moetranslator

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        setContentView(R.layout.activity_main)

        //关联NavController与BottonNavigationView
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_view) as NavHostFragment
        val navController = navHost.navController
        val bottomNavigation:BottomNavigationView=findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)
    }

}