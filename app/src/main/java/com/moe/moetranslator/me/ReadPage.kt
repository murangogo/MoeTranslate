package com.moe.moetranslator.me

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReadPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReadPage : Fragment() {
    private lateinit var binding: FragmentReadPageBinding
    private val client = OkHttpClient()
    private var currenttime = ""
    private val scope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scope.launch {
            val html = correctHtml()
            if (html.startsWith("Error")) {
                MainScope().launch {
                    binding.passageContent.text = html + "\n检查检查网络连接吧～"
                }
            } else {
                val jsonObject = JSONObject(html)
                val retCode = jsonObject.getInt("code")
                if (retCode != 200) {
                    val errorMsg = jsonObject.getString("msg")
                    MainScope().launch {
                        binding.passageContent.text = errorMsg + "\n稍后再试试吧～"
                    }
                } else {
                    val data = jsonObject.getJSONObject("data")
                    val title = data.getString("title")
                    val author = data.getString("subtitle")
                    val date = "收录时间：$currenttime"
                    val contentElements = data.getString("content")
                    val cleanContent = contentElements
                        .replace("<p>", "     ")
                        .replace("<br>", "\n     ")
                        .replace("</p>", "\n")
                        .replace("<div class=\"text-content\">", "")
                        .replace("<strong>","")
                        .replace("</strong>","")
                    val textNum = cleanContent.length
                    MainScope().launch {
                        binding.passageTitle.text = title
                        binding.passageAuthor.text = author
                        binding.passageDate.text = date
                        binding.passageContent.text = cleanContent
                        binding.passageTextnum.text = "共${textNum}字。"
                    }
                }
            }
        }
    }


    private suspend fun correctHtml():String = withContext(Dispatchers.IO){
        var html = ""
        while(true){
            currenttime = randomDate("20121007","20220429")
            html = fetchHtml()
            val jsonObject = JSONObject(html)
            if(jsonObject.getInt("code")==429){
                kotlinx.coroutines.delay(1500)
            }else{
                val jsondata = jsonObject.getJSONObject("data")
                if(jsondata.getString("title")!="墨香是江湖的酒"){
                    break
                }
                kotlinx.coroutines.delay(1500)
            }
        }
        return@withContext html
    }

    fun randomDate(starttime: String, endtime: String): String {
        val startDate = LocalDate.parse(starttime, DateTimeFormatter.ofPattern("yyyyMMdd"))
        val endDate = LocalDate.parse(endtime, DateTimeFormatter.ofPattern("yyyyMMdd"))
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)

        // Randomly pick a day within the range
        val randomDay = Random.nextLong(daysBetween + 1)
        val randomDate = startDate.plusDays(randomDay)

        // Return the date as a string in the format "YYYY-MM-DD"
        return randomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private suspend fun fetchHtml(): String = withContext(Dispatchers.IO) {
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = "token=zun5Ot3HQ9e0hjXF&date=$currenttime".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://v2.alapi.cn/api/one")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
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