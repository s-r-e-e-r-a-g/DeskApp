package com.example.aipocketassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aipocketassistant.ai.AiProvider
import com.example.aipocketassistant.speech.SpeechToTextProvider
import com.example.aipocketassistant.tts.TtsProvider

class AssistantViewModelFactory(
    private val sttProvider: SpeechToTextProvider,
    private val aiProvider: AiProvider,
    private val ttsProvider: TtsProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(sttProvider, aiProvider, ttsProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
