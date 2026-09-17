package com.example.smartmarkdownnotes.util

data class AISettings(
    val provider: String = "openai",  // openai, azure, custom
    val apiKey: String = "",
    val apiUrl: String = "https://api.openai.com/",
    val model: String = "gpt-3.5-turbo",
    val customHeaders: String = ""    // JSON格式的额外请求头
)
