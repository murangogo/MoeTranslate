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
import java.util.concurrent.TimeUnit


class TutorialFragment(val position:Int) : Fragment() {

    lateinit var root : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                root = inflater.inflate(R.layout.fragment_tutorial0,container,false)
                var cele = root.findViewById<KonfettiView>(R.id.konfettiView)
                var again = root.findViewById<Button>(R.id.again)
                var next = root.findViewById<Button>(R.id.buttonnext)
                cele.start(party)
                cele.start(party2)
                again.setOnClickListener{
                    cele.start(party)
                    cele.start(party2)
                }
                next.setOnClickListener{
                    var myactivity = activity as FirstLaunchPage
                    myactivity.nextPage()
                }
            }
            1->{
                root = inflater.inflate(R.layout.fragment_tutorial1_2,container,false)
                var tuimg = root.findViewById<ImageView>(R.id.FrimageView)
                tuimg.setImageResource(R.drawable.accessibility)
                var next = root.findViewById<Button>(R.id.accessnext)
                var gotoaccess = root.findViewById<Button>(R.id.gotoaccesss)
                next.setOnClickListener{
                    var myactivity = activity as FirstLaunchPage
                    myactivity.nextPage()
                }
                gotoaccess.setOnClickListener {
                    startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    )
                }
            }
            2->{
                root = inflater.inflate(R.layout.fragment_tutorial1_2,container,false)
                var tuimg = root.findViewById<ImageView>(R.id.FrimageView)
                tuimg.setImageResource(R.drawable.pers)
                var next = root.findViewById<Button>(R.id.accessnext)
                var gotoset = root.findViewById<Button>(R.id.gotoaccesss)
                gotoset.text = "去授予这两个权限"
                var intent = Intent()
                next.setOnClickListener {
                    var myactivity = activity as FirstLaunchPage
                    myactivity.nextPage()
                }
                gotoset.setOnClickListener {
                    intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS";
                    intent.data = Uri.fromParts("package", requireContext().packageName, null)
                    requireContext().startActivity(intent)
                }
            }
            3,4,5,6,7->{
                root = inflater.inflate(R.layout.fragment_tutorial_use,container,false)
                var imguse = root.findViewById<ImageView>(R.id.UseView)
                var nextuse = root.findViewById<Button>(R.id.nextuse)
                when(position){
                    3->imguse.setImageResource(R.drawable.use_0)
                    4->imguse.setImageResource(R.drawable.use_1)
                    5->imguse.setImageResource(R.drawable.use_2)
                    6->imguse.setImageResource(R.drawable.use_3)
                    7->imguse.setImageResource(R.drawable.use_4)
                }
                nextuse.setOnClickListener{
                    var myactivity = activity as FirstLaunchPage
                    myactivity.nextPage()
                }
            }
            8->{
                root = inflater.inflate(R.layout.fragment_tutorial3,container,false)
                var cele = root.findViewById<KonfettiView>(R.id.konfettiView2)
                var again = root.findViewById<Button>(R.id.agains2)
                var next = root.findViewById<Button>(R.id.buttonnext2)
                cele.start(party)
                cele.start(party2)
                again.setOnClickListener{
                    cele.start(party)
                    cele.start(party2)
                }
                next.setOnClickListener{
                    var startintent = Intent(context, MainActivity::class.java)
                    startActivity(startintent)
                    requireActivity().finish()
                }
            }
        }
        return root
    }


}