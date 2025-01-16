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

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.moe.moetranslator.databinding.FragmentFaqPageBinding
import androidx.recyclerview.widget.RecyclerView

class FAQPage : Fragment() {

    private lateinit var binding: FragmentFaqPageBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFaqPageBinding.inflate(inflater,container,false)
        recyclerView = binding.cardRecyclerview
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val faqs = listOf(
            CustomCard("问题1答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2", "答案1"),
            CustomCard("问题2", "答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2答案2"),
            // 添加更多FAQ项目
        )

        adapter = CardAdapter(faqs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
}