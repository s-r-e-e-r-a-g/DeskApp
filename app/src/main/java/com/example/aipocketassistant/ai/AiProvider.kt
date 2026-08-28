package com.example.aipocketassistant.ai

interface AiProvider {
    suspend fun sendMessage(message: String): String
}
