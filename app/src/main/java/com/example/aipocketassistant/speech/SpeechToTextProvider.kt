package com.example.aipocketassistant.speech

interface SpeechToTextProvider {
    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
}
