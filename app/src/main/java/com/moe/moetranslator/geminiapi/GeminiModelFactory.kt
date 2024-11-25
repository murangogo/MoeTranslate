package com.moe.moetranslator.geminiapi

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting

object GeminiModelFactory {
    fun createGeminiModel(modelName: String, apiKey: String): GenerativeModel {
        val safetySettings: List<SafetySetting> = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )
        return GenerativeModel(
            // For text-only input, use the gemini-pro model
            modelName = modelName,
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = apiKey,
            null,
            safetySettings
        )
    }
}