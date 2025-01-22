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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeveloperBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cele = binding.konfettiViewd
        cele.start(party)
        cele.start(party2)
        binding.officialwebsite.setOnClickListener {
            val url = "https://www.moetranslate.top/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.ideas.setOnClickListener {
            val url = "https://www.wjx.cn/vm/rXUEKnh.aspx"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.wechat.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.wechat_official_account_title)
                .setMessage(R.string.wechat_official_account_content)
                .setCancelable(false)
                .setPositiveButton(R.string.user_known) { _, _ -> }
                .create()

            val imageView = ImageView(requireContext())
            val qrCodeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.qrcode, null)
            imageView.setImageDrawable(qrCodeDrawable)
            dialog.setView(imageView)
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        }

        binding.github.setOnClickListener {
            val dialogperapi = AlertDialog.Builder(activity)
                .setTitle(R.string.github_repo_title)
                .setMessage(R.string.github_repo_content)
                .setCancelable(false)
                .setPositiveButton(R.string.have_look) { _, _ ->
                    val url = "https://github.com/murangogo/MoeTranslate"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                .setNegativeButton(R.string.user_cancel) { _, _ ->}
                .create()
            dialogperapi.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialogperapi.show()
        }
    }

}