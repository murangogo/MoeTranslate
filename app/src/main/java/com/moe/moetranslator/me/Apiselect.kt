package com.moe.moetranslator.me

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moe.moetranslator.databinding.FragmentApiselectBinding
import com.moe.moetranslator.utils.MySharedPreferenceData
import android.R

import android.widget.RadioGroup





class Apiselect : Fragment() {
    private lateinit var binding: FragmentApiselectBinding
    private lateinit var repository: MySharedPreferenceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MySharedPreferenceData(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApiselectBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatgptbtn.setOnClickListener {
            binding.chatgptbtn.isChecked = false;
            Toast.makeText(context,"将在后续版本中推出。", Toast.LENGTH_LONG).show()
        }
        when(repository.ApiChoose){
            0->binding.tencentyunbtn.isChecked = true
            1->binding.baiduyibtn.isChecked = true
        }
        binding.APIRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("ID","$checkedId")
            when (checkedId) {
                binding.tencentyunbtn.id->repository.saveApi(0)
                binding.baiduyibtn.id->repository.saveApi(1)
            }
        }
    }
}