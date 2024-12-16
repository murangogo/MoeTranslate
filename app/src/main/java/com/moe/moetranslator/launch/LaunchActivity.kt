package com.moe.moetranslator.launch

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.moe.moetranslator.MainActivity
import com.moe.moetranslator.R
import com.moe.moetranslator.madoka.DialogManager
import com.moe.moetranslator.madoka.Live2DFileUtil
import com.moe.moetranslator.madoka.Live2DModel
import com.moe.moetranslator.madoka.ModelInfoRepository
import com.moe.moetranslator.madoka.ModelInfoRoomDatabase
import com.moe.moetranslator.utils.AppPathManager
import com.moe.moetranslator.utils.CustomPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaunchActivity : AppCompatActivity() {
    private lateinit var prefs: CustomPreference
    private lateinit var database: ModelInfoRoomDatabase
    private lateinit var repository: ModelInfoRepository
    private lateinit var fileUtil: Live2DFileUtil


    // 使用lifecycleScope替代MainScope
    private val activityScope = lifecycleScope


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 锁定竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        // 初始化路径管理类
        AppPathManager.init(this)
        // 初始化弹窗管理类
        DialogManager.init(this)
        prefs = CustomPreference.getInstance(this)
        database = ModelInfoRoomDatabase.getDatabase(this)
        repository = ModelInfoRepository(database.ModelInfoDAO())
        fileUtil = Live2DFileUtil(this)

        setContentView(R.layout.activity_launch)

        activityScope.launch {
            try {
                delay(1500)

                if (prefs.getBoolean("Is_First_Run", true)) {
                    // 在IO线程中初始化资源
                    withContext(Dispatchers.IO) {
                        val initializer = AssetsInitializer(this@LaunchActivity)
                        initializer.initializeLive2DResources()
                        // 保存模型信息
                        repository.insertModel(Live2DModel("model_1", getString(R.string.madoka_newyear)))

                        // 扫描并保存表情信息
                        val expressions = fileUtil.scanExpressions("model_1")
                        if (expressions.isNotEmpty()) {
                            repository.insertExpressions(expressions)
                        }

                        // 扫描并保存动作信息
                        val motions = fileUtil.scanMotions("model_1")
                        if (motions.isNotEmpty()) {
                            repository.insertMotions(motions)
                        }
                    }

                    prefs.setBoolean("Is_First_Run", false)
                    startActivity(Intent(this@LaunchActivity, FirstLaunchPage::class.java))
                } else {
                    startActivity(Intent(this@LaunchActivity, MainActivity::class.java))
                }

                finish()
            } catch (e: Exception) {
                // 处理初始化失败的情况
                Log.e("LaunchActivity", "Initialization failed", e)
                Toast.makeText(
                    this@LaunchActivity,
                    getString(R.string.init_error, e),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保Activity销毁时取消所有协程
        activityScope.cancel()
    }
}