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
import com.moe.moetranslator.databinding.FragmentReadPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class ReadPage : Fragment() {
    private lateinit var binding: FragmentReadPageBinding
    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReadPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scope.launch {
            val html = fetchHtml()
            if (html.startsWith("Error")) {
                MainScope().launch {
                    binding.passageContent.text = html+"\n检查检查网络连接吧～"
                }
            } else {
                val document = Jsoup.parse(html)
                val title = document.select("h2.title").first()?.text()
                val author = document.select("h3.author").first()?.text()
                val date = document.select("h5.dates").first()?.text()!!.replace("更新","收录")
                val contentElements = document.select("div.contents p")
                val content = StringBuilder()
                for (element in contentElements) {
                    content.append("     ").append(element.text()).append("\n")
                }
                val textnum = content.length
                MainScope().launch {
                    binding.passageTitle.text = title
                    binding.passageAuthor.text = author
                    binding.passageDate.text = date
                    binding.passageContent.text = content
                    binding.passageTextnum.text = "共${textnum}字。"
                }
            }
        }
    }

    private suspend fun fetchHtml(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("http://htwinkle.cn/article")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use "Error：Response unsuccessful"

                response.body?.string() ?: "Error：Content Error"
            }
        } catch (e: Exception) {
            return@withContext "Error：$e"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()  // 当Fragment销毁时，取消所有的协程
    }

}