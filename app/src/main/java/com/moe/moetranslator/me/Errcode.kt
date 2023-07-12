package com.moe.moetranslator.me

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentApiselectBinding
import com.moe.moetranslator.databinding.FragmentErrcodeBinding
import com.moe.moetranslator.utils.MySharedPreferenceData


class Errcode : Fragment() {
    private lateinit var binding: FragmentErrcodeBinding
    private lateinit var repository: MySharedPreferenceData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MySharedPreferenceData(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentErrcodeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(repository.ApiChoose==0){
            binding.errsheetview.setImageResource(R.drawable.errsheet_tencent)
        }else{
            binding.errsheetview.setImageResource(R.drawable.errsheet_baidu)
        }
    }

}