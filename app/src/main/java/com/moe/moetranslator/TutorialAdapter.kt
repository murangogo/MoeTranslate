package com.moe.moetranslator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

const val NUM = 9

class TutorialAdapter(act:FragmentActivity):FragmentStateAdapter(act) {
    override fun getItemCount(): Int {
        return NUM
    }

    override fun createFragment(position: Int): Fragment {
        return TutorialFragment(position)
    }
}