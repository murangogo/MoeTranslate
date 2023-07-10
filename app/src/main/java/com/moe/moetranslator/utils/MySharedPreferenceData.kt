package com.moe.moetranslator.utils

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferenceData(context: Context) {
    private var pref: SharedPreferences
    companion object {
        //部分配置
        private const val IS_FIRST_RUN = "FirstRun"
        private const val BLANGUAGE_FROM = "BTranslateFrom"
        private const val BLANGUAGE_TO = "BTranslateTo"
        private const val TLANGUAGE_FROM = "TTranslateFrom"
        private const val TLANGUAGE_TO = "TTranslateTo"
        private const val BFROM_NUMBER = "BTranslateFromNumber"
        private const val BTO_NUMBER = "BTranslateToNumber"
        private const val TFROM_NUMBER = "TTranslateFromNumber"
        private const val TTO_NUMBER = "TTranslateToNumber"
        private const val Is_Croping = "IfCroping"
        private const val Is_Moving_Text = "IfMovingText"
        private const val Crop_Scale_x1 = "CropScalex1"
        private const val Crop_Scale_x2 = "CropScalex2"
        private const val Crop_Scale_y1 = "CropScaley1"
        private const val Crop_Scale_y2 = "CropScaley2"
        private const val Screen_Configuration = "ScreenConfiguration"
        private const val Baidu_Api_A = "BaiduAPIAccount"
        private const val Baidu_Api_P = "BaiduAPIPassword"
        private const val Tencent_Api_S = "TencentAPISecretId"
        private const val Tencent_Api_K = "TencentAPISecretKey"
        private const val Api_Choose = "ApiChoose"
    }
    init {
        pref = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    }
    //提取数据
    val IsFirstRun: Boolean get() = pref.getBoolean(IS_FIRST_RUN, true)
    val LanguageFrom_Baidu : String? get() = pref.getString(BLANGUAGE_FROM, "auto")
    val LanguageTo_Baidu : String? get() = pref.getString(BLANGUAGE_TO, "zh")
    val LanguageFrom_Tencent : String? get() = pref.getString(TLANGUAGE_FROM, "auto")
    val LanguageTo_Tencent : String? get() = pref.getString(TLANGUAGE_TO, "zh")
    val BFromNum : Int get() = pref.getInt(BFROM_NUMBER, 0)
    val BToNum : Int get() = pref.getInt(BTO_NUMBER, 0)
    val TFromNum : Int get() = pref.getInt(TFROM_NUMBER, 0)
    val TToNum : Int get() = pref.getInt(TTO_NUMBER, 0)
    val IsCrop : Boolean get() = pref.getBoolean(Is_Croping,false)
    val IsMovingText : Boolean get() = pref.getBoolean(Is_Moving_Text,false)
    val CropScaleX1 : Int get() = pref.getInt(Crop_Scale_x1,0)
    val CropScaleX2 : Int get() = pref.getInt(Crop_Scale_x2,0)
    val CropScaleY1 : Int get() = pref.getInt(Crop_Scale_y1,0)
    val CropScaleY2 : Int get() = pref.getInt(Crop_Scale_y2,0)
    val ScreenConfiguration : Int get() = pref.getInt(Screen_Configuration,1)   //1为竖屏，2为横屏
    val BaiduApiA : String? get() = pref.getString(Baidu_Api_A,"")
    val BaiduApiP : String? get() = pref.getString(Baidu_Api_P,"")
    val TencentApiS : String? get() = pref.getString(Tencent_Api_S,"")
    val TencentApiK : String? get() = pref.getString(Tencent_Api_K,"")
    val ApiChoose : Int get() = pref.getInt(Api_Choose,0) //0为腾讯云，1为百度翻译


    //保存数据
    fun saveFirstRun() {
        val editor = pref.edit()
        editor.putBoolean(IS_FIRST_RUN,false)
        editor.apply()
    }
    fun saveBFrom(s:String,i:Int) {
        val editor = pref.edit()
        editor.putString(BLANGUAGE_FROM,s)
        editor.putInt(BFROM_NUMBER,i)
        editor.apply()
    }
    fun saveBTo(s:String,i:Int) {
        val editor = pref.edit()
        editor.putString(BLANGUAGE_TO,s)
        editor.putInt(BTO_NUMBER,i)
        editor.apply()
    }
    fun saveTFrom(s:String,i:Int) {
        val editor = pref.edit()
        editor.putString(TLANGUAGE_FROM,s)
        editor.putInt(TFROM_NUMBER,i)
        editor.apply()
    }
    fun saveTTo(s:String,i:Int) {
        val editor = pref.edit()
        editor.putString(TLANGUAGE_TO,s)
        editor.putInt(TTO_NUMBER,i)
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
    fun saveTencentAPIS(s:String) {
        val editor = pref.edit()
        editor.putString(Tencent_Api_S,s)
        editor.apply()
    }
    fun saveTencentAPIK(s:String) {
        val editor = pref.edit()
        editor.putString(Tencent_Api_K,s)
        editor.apply()
    }
    fun saveApi(i:Int){
        val editor = pref.edit()
        editor.putInt(Api_Choose,i)
        editor.apply()
    }
}