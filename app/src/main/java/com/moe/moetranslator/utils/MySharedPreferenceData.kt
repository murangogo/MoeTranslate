package com.moe.moetranslator.utils

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferenceData(context: Context) {
    private var pref: SharedPreferences
    companion object {
        //部分配置
        private const val IS_FIRST_RUN = "FirstRun"
        private const val LANGUAGE_FROM = "TranslateFrom"
        private const val LANGUAGE_TO = "TranslateTo"
        private const val FROM_NUMBER = "TranslateFromNumber"
        private const val TO_NUMBER = "TranslateToNumber"
        private const val Is_Croping = "IfCroping"
        private const val Is_Moving_Text = "IfMovingText"
        private const val Crop_Scale_x1 = "CropScalex1"
        private const val Crop_Scale_x2 = "CropScalex2"
        private const val Crop_Scale_y1 = "CropScaley1"
        private const val Crop_Scale_y2 = "CropScaley2"
        private const val Screen_Configuration = "ScreenConfiguration"
        private const val Baidu_Api_A = "BaiduAPIAccount"
        private const val Baidu_Api_P = "BaiduAPIPassword"
    }
    init {
        pref = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    }
    //提取数据
    val IsFirstRun: Boolean get() = pref.getBoolean(IS_FIRST_RUN, true)
    val LanguageFrom : String? get() = pref.getString(LANGUAGE_FROM, "auto")
    val LanguageTo : String? get() = pref.getString(LANGUAGE_TO, "zh")
    val FromNum : Int get() = pref.getInt(FROM_NUMBER, 0)
    val ToNum : Int get() = pref.getInt(TO_NUMBER, 0)
    val IsCrop : Boolean get() = pref.getBoolean(Is_Croping,false)
    val IsMovingText : Boolean get() = pref.getBoolean(Is_Moving_Text,false)
    val CropScaleX1 : Int get() = pref.getInt(Crop_Scale_x1,0)
    val CropScaleX2 : Int get() = pref.getInt(Crop_Scale_x2,0)
    val CropScaleY1 : Int get() = pref.getInt(Crop_Scale_y1,0)
    val CropScaleY2 : Int get() = pref.getInt(Crop_Scale_y2,0)
    val ScreenConfiguration : Int get() = pref.getInt(Screen_Configuration,1)   //1为竖屏，2为横屏
    val BaiduApiA : String? get() = pref.getString(Baidu_Api_A,"")
    val BaiduApiP : String? get() = pref.getString(Baidu_Api_P,"")


    //保存数据
    fun saveFirstRun() {
        val editor = pref.edit()
        editor.putBoolean(IS_FIRST_RUN,false)
        editor.apply()
    }
    fun saveFrom(s:String,i:Int) {
        val editor = pref.edit()
        editor.putString(LANGUAGE_FROM,s)
        editor.putInt(FROM_NUMBER,i)
        editor.apply()
    }
    fun saveTo(s:String,i:Int) {
        val editor = pref.edit()
        editor.putString(LANGUAGE_TO,s)
        editor.putInt(TO_NUMBER,i)
        editor.apply()
    }
    fun saveCrop(b:Boolean){
        val editor = pref.edit()
        editor.putBoolean(Is_Croping,b)
        editor.apply()
    }
    fun saveScale(x1:Int,y1:Int,x2:Int,y2:Int){
        val editor = pref.edit()
        editor.putInt(Crop_Scale_x1,x1)
        editor.putInt(Crop_Scale_x2,x2)
        editor.putInt(Crop_Scale_y1,y1)
        editor.putInt(Crop_Scale_y2,y2)
        editor.apply()
    }
    fun saveMovingText(b:Boolean){
        val editor = pref.edit()
        editor.putBoolean(Is_Moving_Text,b)
        editor.apply()
    }
    fun saveScreenConfiguration(i:Int){
        val editor = pref.edit()
        editor.putInt(Screen_Configuration,i)
        editor.apply()
    }
    fun saveBaiduAPIA(s:String) {
        val editor = pref.edit()
        editor.putString(Baidu_Api_A,s)
        editor.apply()
    }
    fun saveBaiduAPIP(s:String) {
        val editor = pref.edit()
        editor.putString(Baidu_Api_P,s)
        editor.apply()
    }
}