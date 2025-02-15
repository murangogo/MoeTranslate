/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.moe.moetranslator.me

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.moe.moetranslator.BaseActivity
import com.moe.moetranslator.databinding.ActivitySettingPageBinding

class SettingPageActivity : BaseActivity() {

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val TYPE_FRAGMENT_TRANSLATE_MODE = 1
        const val TYPE_FRAGMENT_API_CONFIG = 2
        const val TYPE_FRAGMENT_PERSONALIZATION = 3
        const val TYPE_FRAGMENT_READ = 4
        const val TYPE_FRAGMENT_FAQ = 5
        const val TYPE_FRAGMENT_ERROR_CODE = 6
        const val TYPE_FRAGMENT_DEVELOPER = 7
    }

    private lateinit var binding: ActivitySettingPageBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏
        binding = ActivitySettingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applySystemBarsPadding(binding.fragmentContainerView, true, true)

        when(intent.getIntExtra(EXTRA_FRAGMENT_TYPE,0)){
            TYPE_FRAGMENT_TRANSLATE_MODE->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                TranslationMode()
            ).commit()
            TYPE_FRAGMENT_API_CONFIG->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                APIConfig()
            ).commit()
            TYPE_FRAGMENT_PERSONALIZATION->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                PersonalizationConfig()
            ).commit()
            TYPE_FRAGMENT_READ->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                ReadPage()
            ).commit()
            TYPE_FRAGMENT_FAQ->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                FAQPage()
            ).commit()
            TYPE_FRAGMENT_ERROR_CODE->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                Errcode()
            ).commit()
            TYPE_FRAGMENT_DEVELOPER->supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id,
                Developer()
            ).commit()
            else->Toast.makeText(applicationContext,"Unknown Error.", Toast.LENGTH_LONG).show()
        }
    }
}