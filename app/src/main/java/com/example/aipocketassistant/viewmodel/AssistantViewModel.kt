package com.example.aipocketassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aipocketassistant.ai.AiProvider
import com.example.aipocketassistant.speech.SpeechToTextProvider
import com.example.aipocketassistant.tts.TtsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AssistantState {
    READY,
    LISTENING,
    PROCESSING, // STT processing
    THINKING,   // AI processing
    SPEAKING,
    ERROR
}

class AssistantViewModel(
    private val sttProvider: SpeechToTextProvider,
    private val aiProvider: AiProvider,
    private val ttsProvider: TtsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantState.READY)
    val uiState: StateFlow<AssistantState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _lastQuery = MutableStateFlow("")
    val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    private val _lastResponse = MutableStateFlow("")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    fun startListening() {
        if (_uiState.value == AssistantState.SPEAKING) {
            ttsProvider.stop()
        }
        
        _uiState.value = AssistantState.LISTENING
        _errorMessage.value = null

        sttProvider.startListening(
            onResult = { text ->
                _uiState.value = AssistantState.PROCESSING
                _lastQuery.value = text
                processWithAi(text)
            },
            onError = { error ->
                handleError(error)
            }
        )
    }

    fun stopListening() {
        if (_uiState.value == AssistantState.LISTENING) {
            sttProvider.stopListening()
            // State will be updated by STT callbacks or we can revert to READY if cancelled manually
        }
    }

    private fun processWithAi(text: String) {
        _uiState.value = AssistantState.THINKING
        viewModelScope.launch {
            try {
                val response = aiProvider.sendMessage(text)
                _lastResponse.value = response
                speakResponse(response)
            } catch (e: Exception) {
                handleError(e.message ?: "AI Request Failed")
            }
        }
    }

    private fun speakResponse(text: String) {
        _uiState.value = AssistantState.SPEAKING
        ttsProvider.speak(
            text = text,
            onDone = {
                _uiState.value = AssistantState.READY
            },
            onError = { error ->
                handleError("TTS Error: $error")
            }
        )
    }

    private fun handleError(message: String) {
        _errorMessage.value = message
        _uiState.value = AssistantState.ERROR
        // Reset to READY after a delay could be done here
    }
    
    fun resetToReady() {
        _uiState.value = AssistantState.READY
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        sttProvider.stopListening()
        ttsProvider.shutdown()
    }
}
