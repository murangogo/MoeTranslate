package com.moe.moetranslator

import android.view.MotionEvent

interface FragmentTouchListener {
    fun onTouchEvent(event: MotionEvent):Boolean
}