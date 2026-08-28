# Architecture

The AI Pocket Assistant follows MVVM (Model-View-ViewModel) and Clean Architecture principles.

## Core Components

- **UI Layer (`ui/`)**: Built with Jetpack Compose. `AssistantScreen` hosts the main interaction loop (talk button, clock, weather). `SettingsScreen` provides API configuration.
- **ViewModel (`viewmodel/`)**: `AssistantViewModel` manages the state machine (`READY`, `LISTENING`, `PROCESSING`, `THINKING`, `SPEAKING`, `ERROR`).
- **Data Layer (`data/`)**: `SettingsRepository` uses Jetpack DataStore to persist preferences and API keys.
- **Providers (`ai/`, `speech/`, `tts/`)**: Interfaces defining the core capabilities. Implementations include `GeminiAiProvider` (OkHttp), `AndroidSpeechProvider` (SpeechRecognizer), and `AndroidTtsProvider` (TextToSpeech).

## Communication Pipeline

1. **User Action**: Press "Talk" button -> `sttProvider.startListening()`
2. **STT Processing**: Audio captured -> Transcribed to Text -> `processWithAi(text)`
3. **AI Processing**: Text -> `aiProvider.sendMessage()` -> Gemini API -> Response Text
4. **TTS Output**: Response Text -> `ttsProvider.speak()` -> Audio Output
