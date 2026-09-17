package com.example.smartmarkdownnotes.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.smartmarkdownnotes.data.network.*
import com.example.smartmarkdownnotes.util.AISettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
    
    fun getSettings(): AISettings {
        return AISettings(
            provider = prefs.getString("provider", "openai") ?: "openai",
            apiKey = prefs.getString("api_key", "") ?: "",
            apiUrl = prefs.getString("api_url", "https://api.openai.com/") ?: "https://api.openai.com/",
            model = prefs.getString("model", "gpt-3.5-turbo") ?: "gpt-3.5-turbo",
            customHeaders = prefs.getString("custom_headers", "") ?: ""
        )
    }
    
    fun saveSettings(settings: AISettings) {
        prefs.edit {
            putString("provider", settings.provider)
            putString("api_key", settings.apiKey)
            putString("api_url", settings.apiUrl)
            putString("model", settings.model)
            putString("custom_headers", settings.customHeaders)
        }
    }
    
    suspend fun generateText(
        prompt: String,
        instruction: String = "",
        settings: AISettings
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<Message>()
            if (instruction.isNotEmpty()) {
                messages.add(Message("system", instruction))
            }
            messages.add(Message("user", prompt))
            
            val request = ChatRequest(
                model = settings.model,
                messages = messages,
                temperature = 0.7,
                max_tokens = 2000
            )
            
            val authService = RetrofitClient.getAIService(settings.apiUrl)
            val authHeader = "Bearer ${settings.apiKey}"
            val response = authService.chatCompletion(authHeader, request)
            
            if (response.choices.isNotEmpty()) {
                Result.success(response.choices[0].message.content)
            } else {
                Result.failure(Exception("Empty response from AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun continueWriting(text: String, settings: AISettings): Result<String> {
        val instruction = "你是一个写作助手。请根据用户提供的文本，自然地续写下文。保持原文的风格和语气。"
        return generateText("请续写以下内容:\n\n$text", instruction, settings)
    }
    
    suspend fun polishText(text: String, settings: AISettings): Result<String> {
        val instruction = "你是一个文字润色专家。请优化用户的文本，使其更加流畅、专业，但保持原意不变。"
        return generateText("请润色以下内容:\n\n$text", instruction, settings)
    }
    
    suspend fun summarizeText(text: String, settings: AISettings): Result<String> {
        val instruction = "你是一个摘要专家。请为用户的内容生成简洁的摘要。"
        return generateText("请总结以下内容:\n\n$text", instruction, settings)
    }
    
    suspend fun translateText(text: String, targetLang: String, settings: AISettings): Result<String> {
        val instruction = "你是一个翻译专家。请将用户的内容翻译成$targetLang，保持原意和语气。"
        return generateText("请翻译成$targetLang:\n\n$text", instruction, settings)
    }
}
