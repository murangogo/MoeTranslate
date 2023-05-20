package com.moe.moetranslator.me

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.moe.moetranslator.databinding.FragmentApiselectBinding


class Apiselect : Fragment() {
    private lateinit var binding: FragmentApiselectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        binding.baiduyibtn.toggle()
        binding.gptlayout.setOnClickListener {
            Toast.makeText(context,"将在后续版本中推出。", Toast.LENGTH_LONG).show()
        }
    }


}