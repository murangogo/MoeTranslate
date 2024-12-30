package com.moe.moetranslator.me

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentDeveloperBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit


class Developer : Fragment() {
    private lateinit var binding: FragmentDeveloperBinding
    private val party = Party(
        angle = 300,
        spread = 60,
        speed = 60f,
        maxSpeed = 70f,
        damping = 0.9f,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        shapes = listOf(Shape.Square, Shape.Circle),
        timeToLive = 5000L,
        fadeOutEnabled = true,
        position = Position.Relative(0.0,0.6),
        emitter = Emitter(duration = 5000, TimeUnit.MILLISECONDS).max(600)
    )
    private val party2 = Party(
        angle = 240,
        spread = 60,
        speed = 60f,
        maxSpeed = 70f,
        damping = 0.9f,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        shapes = listOf(Shape.Square, Shape.Circle),
        timeToLive = 5000L,
        fadeOutEnabled = true,
        position = Position.Relative(1.0,0.6),
        emitter = Emitter(duration = 5000, TimeUnit.MILLISECONDS).max(600)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeveloperBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val celebra = binding.konfettiViewd
        celebra.start(party)
        celebra.start(party2)
        binding.officialwebsite.setOnClickListener {
            val url = "https://www.moetranslate.top/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.ideas.setOnClickListener {
            val url = "https://www.wjx.cn/vm/Oky2ycy.aspx/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.wechat.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("萌译官方公众号")
                .setMessage("    您可以截图下面的二维码，在微信中扫一扫；或者在微信搜索“MoeTranslator”来关注官方公众号。\n")
                .setCancelable(false)
                .setPositiveButton("我知道了") { _, _ -> }
                .create()

            // 创建一个新的ImageView
            val imageView = ImageView(activity)
            // 转换二维码图片
            val qrCodeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.qrcode, null)
            // 设置ImageView的图像为二维码
            imageView.setImageDrawable(qrCodeDrawable)
            // 使用setView方法将ImageView添加到AlertDialog中
            dialogperapi.setView(imageView)

            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }

        binding.github.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle("萌译的GitHub仓库")
                .setMessage("萌译是开源的，您可以访问GitHub仓库。但如果您在国内，可能会比较困难。")
                .setCancelable(false)
                .setPositiveButton("去GitHub看看") { _, _ ->
                    val url = "https://github.com/murangogo/MoeTranslate"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                .setNegativeButton("暂时不看了") { _, _ ->}
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
    }

}