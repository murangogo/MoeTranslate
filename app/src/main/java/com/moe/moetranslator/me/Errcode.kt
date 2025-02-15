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
import com.moe.moetranslator.databinding.FragmentErrcodeBinding
import androidx.recyclerview.widget.RecyclerView
import com.moe.moetranslator.R

class Errcode : Fragment() {

    private lateinit var binding: FragmentErrcodeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentErrcodeBinding.inflate(inflater,container,false)
        recyclerView = binding.cardRecyclerview
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val errs = listOf(
            CustomCard(getText(R.string.err_bing_text_title), getText(R.string.err_bing_text_content)),
            CustomCard(getText(R.string.err_niutrans_text_title), getText(R.string.err_niutrans_text_content)),
            CustomCard(getText(R.string.err_volc_text_title), getText(R.string.err_volc_text_content)),
            CustomCard(getText(R.string.err_azure_text_title), getText(R.string.err_azure_text_content)),
            CustomCard(getText(R.string.err_baidu_text_title), getText(R.string.err_baidu_text_content)),
            CustomCard(getText(R.string.err_baidu_pic_title), getText(R.string.err_baidu_pic_content)),
            CustomCard(getText(R.string.err_tencent_text_title), getText(R.string.err_tencent_text_content)),
            CustomCard(getText(R.string.err_tencent_pic_title), getText(R.string.err_tencent_pic_content))
        )

        adapter = CardAdapter(errs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
}