package com.example.aipocketassistant.tts

interface TtsProvider {
    fun speak(text: String, onDone: () -> Unit, onError: (String) -> Unit)
    fun stop()
    fun shutdown()
}
