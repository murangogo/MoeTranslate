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

package com.moe.moetranslator.launch

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.moe.moetranslator.MainActivity
import com.moe.moetranslator.R
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Locale
import java.util.concurrent.TimeUnit


class TutorialFragment(val position:Int) : Fragment() {

    lateinit var root : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val party = Party(
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
        val party2 = Party(
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
        when(position) {
            0->{
                root = inflater.inflate(R.layout.fragment_tutorial_0,container,false)
                val cele = root.findViewById<KonfettiView>(R.id.konfettiView)
                val again = root.findViewById<Button>(R.id.again)
                val next = root.findViewById<Button>(R.id.buttonnext)
                val logoImageView = root.findViewById<ImageView>(R.id.logo_name)
                if (Locale.getDefault().language == "zh") {
                    logoImageView.setImageResource(R.drawable.logo_design)
                } else {
                    logoImageView.setImageResource(R.drawable.logo_design_en)
                }
                cele.start(party)
                cele.start(party2)
                again.setOnClickListener{
                    cele.start(party)
                    cele.start(party2)
                }
                next.setOnClickListener{
                    val activity = activity as FirstLaunchPage
                    activity.nextPage()
                }
            }

            1->{
                root = inflater.inflate(R.layout.fragment_tutorial_1_2_3,container,false)
                val tuimg = root.findViewById<ImageView>(R.id.FrimageView)
                tuimg.setImageResource(R.drawable.tutorial_accessibility)
                val next = root.findViewById<Button>(R.id.next)
                val grant = root.findViewById<Button>(R.id.grant)
                next.setOnClickListener{
                    val activity = activity as FirstLaunchPage
                    activity.nextPage()
                }
                grant.setOnClickListener {
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle(R.string.tutorial_access_title)
                        .setMessage(R.string.tutorial_access_content)
                        .setPositiveButton(R.string.go_to_grant) { _, _ ->
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .setNegativeButton(R.string.user_cancel, null)
                        .setCancelable(false)
                        .create()
                    dialog.show()
                    dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                }
            }

            2->{
                root = inflater.inflate(R.layout.fragment_tutorial_1_2_3,container,false)
                val tuimg = root.findViewById<ImageView>(R.id.FrimageView)
                tuimg.setImageResource(R.drawable.tutorial_notify)
                val next = root.findViewById<Button>(R.id.next)
                val gotoset = root.findViewById<Button>(R.id.grant)
                next.setOnClickListener {
                    val activity = activity as FirstLaunchPage
                    activity.nextPage()
                }
                gotoset.setOnClickListener {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }
            }

            3->{
                root = inflater.inflate(R.layout.fragment_tutorial_1_2_3,container,false)
                val tuimg = root.findViewById<ImageView>(R.id.FrimageView)
                tuimg.setImageResource(R.drawable.tutorial_ball)
                val next = root.findViewById<Button>(R.id.next)
                val grant = root.findViewById<Button>(R.id.grant)
                next.setOnClickListener {
                    val activity = activity as FirstLaunchPage
                    activity.nextPage()
                }
                grant.setOnClickListener {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)
                }
            }

            4,5,6->{
                root = inflater.inflate(R.layout.fragment_tutorial_use,container,false)
                val imguse = root.findViewById<ImageView>(R.id.UseView)
                val nextuse = root.findViewById<Button>(R.id.nextuse)
                when(position){
                    4->imguse.setImageResource(R.drawable.tutorial_use_1)
                    5->imguse.setImageResource(R.drawable.tutorial_use_2)
                    6->imguse.setImageResource(R.drawable.tutorial_use_3)
                }
                nextuse.setOnClickListener{
                    val activity = activity as FirstLaunchPage
                    activity.nextPage()
                }
            }

            7->{
                root = inflater.inflate(R.layout.fragment_tutorial_last,container,false)
                val cele = root.findViewById<KonfettiView>(R.id.konfettiView)
                val again = root.findViewById<Button>(R.id.again)
                val enter = root.findViewById<Button>(R.id.enter)
                cele.start(party)
                cele.start(party2)
                again.setOnClickListener{
                    cele.start(party)
                    cele.start(party2)
                }
                enter.setOnClickListener{
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
        return root
    }


}