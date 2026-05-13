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

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //锁定竖屏

        // =====llamacpp测试====
        // === 临时验证：跑完删除 ===
        LlamaAndroid.backendInit()
        Log.i("llama-test", LlamaAndroid.systemInfo())

        // 初始化弹窗管理类
        DialogManager.init(this)

        setContentView(R.layout.activity_main)

        applySystemBarsPadding(findViewById(R.id.fragment_view), true, false)

        //关联NavController与BottonNavigationView
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_view) as NavHostFragment
        val navController = navHost.navController
        val bottomNavigation:BottomNavigationView=findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)

        // === 临时验证：跑完删除 ===
        Thread {
            try {
                LlamaAndroid.backendInit()
                Log.i("llama-test", LlamaAndroid.systemInfo())

                val modelFile = File(getExternalFilesDir("models"), "test.gguf")
                if (!modelFile.exists()) {
                    Log.e("llama-test", "Model not found at ${modelFile.absolutePath}")
                    return@Thread
                }
                Log.i("llama-test", "Loading model: ${modelFile.absolutePath}(${modelFile.length() / 1024 / 1024} MB)")
                val t0 = System.currentTimeMillis()
                LlamaAndroid.load(modelFile.absolutePath, nCtx = 2048, nThreads = 4)
                Log.i("llama-test", "Model loaded in ${System.currentTimeMillis() - t0} ms")

                // Qwen2.5 chat template
                val prompt = """<|im_start|>system
  You are a professional translator. Translate the user's text from Japanese to Chinese. 
  Output only the translation, no explanations.<|im_end|>
  <|im_start|>user
  こんにちは、世界！<|im_end|>
  <|im_start|>assistant
  """

                val t1 = System.currentTimeMillis()
                val output = LlamaAndroid.complete(
                    prompt = prompt,
                    maxTokens = 64,
                    temperature = 0.2f,
                    topP = 0.9f,
                )
                val elapsed = System.currentTimeMillis() - t1
                Log.i("llama-test", "Translation result: [$output]")
                Log.i("llama-test", "Inference took ${elapsed} ms")
            } catch (e: Throwable) {
                Log.e("llama-test", "Test failed", e)
            }
        }.start()
    }

}