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

package com.moe.moetranslator

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moe.moetranslator.llama.LlamaAndroid
import java.io.File
import com.moe.moetranslator.madoka.DialogManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.moe.moetranslator.ppocr.PPOcrV6Engine

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

        // ===== PP-OCRv6 冒烟测试：识别 assets 里的示例日语，结果走 Log + Toast =====
        // 验证通过后删除本调用与下方 runPPOcrV6SmokeTest()。
        runPPOcrV6SmokeTest()
    }

    /** 临时：加载 assets/ppocrv6/AJapanese.png，跑 PP-OCRv6 并用 Log + Toast 输出结果。 */
    private fun runPPOcrV6SmokeTest() {
        Thread {
            try {
                val t0 = System.currentTimeMillis()
                PPOcrV6Engine.initialize(this)
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bmp = assets.open("ppocrv6/AJapanese.png").use { BitmapFactory.decodeStream(it, null, opts) }
                    ?: throw IllegalStateException("示例图片解码失败")
                val lines = PPOcrV6Engine.runOCR(bmp)
                bmp.recycle()
                val merged = lines.joinToString("") { it.text }
                val cost = System.currentTimeMillis() - t0
                Log.i("PPOcrSmoke", "识别 ${lines.size} 行，耗时 ${cost}ms")
                lines.forEachIndexed { i, l -> Log.i("PPOcrSmoke", "[$i] (%.2f) %s".format(l.score, l.text)) }
                Log.i("PPOcrSmoke", "合并: $merged")
                runOnUiThread {
                    Toast.makeText(
                        this, "PP-OCRv6 识别 ${lines.size} 行 (${cost}ms):\n$merged", Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Throwable) {
                Log.e("PPOcrSmoke", "PP-OCRv6 冒烟测试失败", e)
                runOnUiThread {
                    Toast.makeText(this, "PP-OCR 失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.apply { name = "PPOcr-Smoke"; start() }
    }

}