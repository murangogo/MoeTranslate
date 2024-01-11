package com.moe.moetranslator.geminiapi

import com.google.ai.client.generativeai.GenerativeModel

class GeminiModelFactory {
    fun createGeminiModel(ModelName: String?, apiKey: String?): GenerativeModel {
        return GenerativeModel(
            // For text-only input, use the gemini-pro model
            modelName = ModelName!!,
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = apiKey!!
        )
    }
}