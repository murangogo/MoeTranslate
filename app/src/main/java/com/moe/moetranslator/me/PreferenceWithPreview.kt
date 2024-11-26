package com.moe.moetranslator.me

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.moe.moetranslator.R
import com.moe.moetranslator.utils.CustomPreference
import java.io.File

class PreferenceWithPreview : Preference {
    private var previewImageView: ImageView? = null
    private lateinit var prefs: CustomPreference

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutResource = R.layout.item_preference_with_preview
        prefs = CustomPreference.getInstance(context)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        previewImageView = holder.findViewById(R.id.preview_image) as ImageView
        updatePreviewImage()
    }

    // 添加public方法用于更新预览图
    fun refreshPreview() {
        updatePreviewImage()
    }

    private fun updatePreviewImage() {
        previewImageView?.let { imageView ->
            // 读取SharedPreferences中的自定义图片名称
            val customPicName = prefs.getString("Custom_Floating_Pic", "")

            if (customPicName.isEmpty()) {
                // 如果没有自定义图片，加载默认图片
                Glide.with(context)
                    .load(R.drawable.floating_ball_icon)
                    .transform(CircleCrop())
                    .override(100, 100)
                    .into(imageView)
            } else {
                // 构建自定义图片文件路径
                val iconFile = File(context.getExternalFilesDir(null), "icon/$customPicName")

                if (iconFile.exists()) {
                    // 如果文件存在，加载自定义图片
                    Glide.with(context)
                        .load(iconFile)
                        .transform(CircleCrop())
                        .override(100, 100)
                        .error(R.drawable.floating_ball_icon) // 加载失败时显示默认图片
                        .into(imageView)
                } else {
                    // 如果文件不存在，加载默认图片
                    Glide.with(context)
                        .load(R.drawable.floating_ball_icon)
                        .transform(CircleCrop())
                        .override(100, 100)
                        .into(imageView)
                }
            }
        }
    }
}