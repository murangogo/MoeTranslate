package com.moe.moetranslator.me

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moe.moetranslator.databinding.FragmentTranslationModeBinding
import com.moe.moetranslator.R

import com.moe.moetranslator.utils.CustomPreference


class TranslationMode : Fragment() {
    private lateinit var binding: FragmentTranslationModeBinding
    private lateinit var prefs: CustomPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = CustomPreference.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTranslationModeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(prefs.getInt("Translate_Mode", 0) == 0){
            binding.ocrModeLayout.setBackgroundResource(R.drawable.custom_radio_button_selected_background)
            binding.picModeLayout.setBackgroundResource(R.drawable.custom_radio_button_background)
        }else{
            binding.ocrModeLayout.setBackgroundResource(R.drawable.custom_radio_button_background)
            binding.picModeLayout.setBackgroundResource(R.drawable.custom_radio_button_selected_background)
        }
        binding.ocrModeLayout.setOnClickListener {
            binding.ocrModeLayout.setBackgroundResource(R.drawable.custom_radio_button_selected_background)
            binding.picModeLayout.setBackgroundResource(R.drawable.custom_radio_button_background)
            prefs.setInt("Translate_Mode", 0)
            Log.d("RADIO","A")
        }

        binding.picModeLayout.setOnClickListener {
            binding.ocrModeLayout.setBackgroundResource(R.drawable.custom_radio_button_background)
            binding.picModeLayout.setBackgroundResource(R.drawable.custom_radio_button_selected_background)
            prefs.setInt("Translate_Mode", 1)
            Log.d("RADIO","B")
        }

    }
}