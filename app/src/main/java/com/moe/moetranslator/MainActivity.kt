package com.moe.moetranslator

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import androidx.navigation.NavHost
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private  var mFragmentTouchListeners:ArrayList<FragmentTouchListener> = ArrayList<FragmentTouchListener>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        setContentView(R.layout.activity_main)

        //关联NavController与BottonNavigationView
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_view) as NavHost
        val navController = navHost.navController
        val bottomNavigation:BottomNavigationView=findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)
    }

    fun registerFragmentTouchListener(listener:FragmentTouchListener) {
        mFragmentTouchListeners.add(listener);
    }

    fun unRegisterFragmentTouchListener(listener:FragmentTouchListener) {
        mFragmentTouchListeners.remove(listener);
    }

    override fun dispatchTouchEvent(event: MotionEvent):Boolean {
        mFragmentTouchListeners.forEach {
            it.onTouchEvent(event)
        }
        return super.dispatchTouchEvent(event);
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

}