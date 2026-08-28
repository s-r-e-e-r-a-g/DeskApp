# AI Pocket Assistant — Full Build Specification

## 1. Project Goal

Build a complete Android application that turns an old Android smartphone into a dedicated, fast, pocket-sized AI voice assistant.

The app should reproduce the useful behavior of a small ESP32-based AI assistant:

- Voice input through the phone microphone
- Speech-to-text
- AI/LLM conversation
- Text-to-speech
- Fast conversational responses
- Full-screen minimal interface
- Large, readable status indicators
- Optional physical-volume/button controls
- Conversation history
- Configurable AI/STT/TTS providers
- Ability to run continuously as a dedicated assistant
- Low resource usage so it works well on older Android phones

Do NOT try to run ESPHome on Android. Recreate the functionality as a native Android application.

The result must be a real, buildable Android project, not a mockup or prototype.

---

# 2. Primary Technology Stack

Use:

- Android
- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin compatible with the currently installed stable Android Studio
- Kotlin Coroutines
- StateFlow
- Android Speech/Audio APIs where appropriate
- OkHttp for HTTP/WebSocket networking
- kotlinx.serialization or another lightweight JSON serializer
- DataStore for settings
- Room only if persistent structured conversation storage is actually needed
- Android TextToSpeech as the default offline/fallback TTS
- Android AudioRecord for reliable microphone recording
- Foreground Service only when required for continuous/background operation

Avoid unnecessary dependencies.

The application should work on older Android hardware. Prefer lightweight components and avoid animations that consume significant CPU/GPU.

---

# 3. Architecture

Use a clean, maintainable architecture.

Recommended structure:

```text
app/
  src/main/java/com/example/aipocketassistant/

    MainActivity.kt

    ui/
      AssistantScreen.kt
      SettingsScreen.kt
      ConversationScreen.kt
      components/
      theme/

    viewmodel/
      AssistantViewModel.kt
      SettingsViewModel.kt

    audio/
      AudioRecorder.kt
      AudioPlayer.kt
      WakeWordManager.kt

    speech/
      SpeechToTextProvider.kt
      AndroidSpeechProvider.kt
      RemoteSpeechProvider.kt

    ai/
      AiProvider.kt
      OpenAiCompatibleProvider.kt
      LocalAiProvider.kt

    tts/
      TtsProvider.kt
      AndroidTtsProvider.kt
      RemoteTtsProvider.kt

    network/
      ApiClient.kt
      WebSocketClient.kt

    data/
      SettingsRepository.kt
      ConversationRepository.kt
      models/

    service/
      AssistantForegroundService.kt

    utils/
      PermissionManager.kt
      AudioUtils.kt
      AppConstants.kt
```

Use interfaces so providers can be replaced without changing the UI.

Example:

```kotlin
interface AiProvider {
    suspend fun sendMessage(
        messages: List<ChatMessage>
    ): AiResponse
}
```

Likewise:

```kotlin
interface SpeechToTextProvider {
    suspend fun transcribe(audio: ByteArray): String
}
```

and:

```kotlin
interface TtsProvider {
    suspend fun synthesize(text: String): AudioData
}
```

---

# 4. Core User Experience

The application is intended to behave like a dedicated AI gadget rather than a normal chatbot.

On launch:

1. Start the app.
2. Hide unnecessary Android UI where possible.
3. Display the assistant screen.
4. Show a large central assistant indicator.
5. Show current status.
6. Provide a large press-to-talk button.
7. Optionally enable hands-free mode.

The main screen should be visually simple.

Example:

```text
┌──────────────────────────────┐
│                              │
│                              │
│             ◉                │
│                              │
│          READY               │
│                              │
│                              │
│      Hold to speak           │
│                              │
│             ●                │
│                              │
└──────────────────────────────┘
```

States:

```text
READY
LISTENING
PROCESSING
THINKING
SPEAKING
ERROR
OFFLINE
```

The state should be clearly visible.

---

# 5. Voice Interaction

## Press-to-talk

Default interaction:

- User presses and holds the microphone button.
- App starts recording.
- UI changes to `LISTENING`.
- User speaks.
- User releases the button.
- Recording stops.
- Audio is sent to STT.
- UI changes to `PROCESSING`.
- Transcribed text is sent to the AI.
- AI response is displayed.
- TTS starts.
- UI changes to `SPEAKING`.
- Response is spoken through the phone speaker.
- Return to `READY`.

Do not require the user to manually type anything for normal operation.

---

# 6. Audio Recording

Use Android's `AudioRecord`.

Requirements:

- Request `RECORD_AUDIO`.
- Handle permission denial gracefully.
- Use mono audio.
- Prefer 16 kHz PCM for speech APIs unless the selected provider requires another format.
- 16-bit PCM.
- Stop and release the recorder correctly.
- Never leak microphone resources.
- Handle interruptions and lifecycle changes.

The recorder must not crash if:

- Microphone is unavailable.
- Another application temporarily owns the microphone.
- Permission is revoked.
- The phone is rotated.
- App goes into background.

Provide clear error states.

---

# 7. Speech-to-Text

Create a provider abstraction.

Support at least:

### Provider 1 — Android Speech Recognition

Use Android's built-in speech recognition when available.

Advantages:

- Simple
- No API key
- Good for basic operation
- Can work without implementing a custom audio upload pipeline

### Provider 2 — Remote STT

Implement a generic HTTP provider for services such as Whisper-compatible APIs.

Configuration:

```text
STT Provider
STT Endpoint
STT API Key
STT Model
Language
```

Do not hardcode API keys.

The user should be able to select:

```text
Android Speech
Remote STT
```

---

# 8. AI / LLM

Create a provider abstraction.

The default remote implementation should support OpenAI-compatible chat APIs.

Settings:

```text
AI Provider
API Base URL
API Key
Model
System Prompt
Temperature
Max Tokens
```

The base URL must be configurable.

Do NOT hardcode a provider-specific URL.

Support normal request/response chat initially.

If streaming is supported by the endpoint, implement streaming so the UI can show the response while it is being generated.

Recommended behavior:

```text
User speech
    ↓
STT
    ↓
User text
    ↓
LLM
    ↓
Streaming response
    ↓
UI updates immediately
    ↓
TTS
```

---

# 9. AI System Prompt

Provide a default system prompt:

```text
You are a fast, helpful personal AI voice assistant.

Give concise answers by default because your responses are normally spoken aloud.

Avoid unnecessary formatting, long introductions, and repetitive explanations.

If the user asks for a simple fact, answer directly.

If a task requires multiple steps, explain the steps clearly.

Do not claim to have performed an action unless you actually performed it.

If information is uncertain, say so.

Optimize responses for natural text-to-speech.
```

Allow the user to customize this prompt.

---

# 10. Conversation Memory

Maintain the current conversation context.

Example:

```text
User:
What is the capital of Japan?

Assistant:
Tokyo.

User:
What is its population?

Assistant:
...
```

The second question should have access to the previous conversation context.

Implement a configurable context limit.

For older phones, avoid storing an unnecessarily large conversation in memory.

Settings:

```text
Conversation history: ON/OFF
Maximum messages
Clear conversation
Export conversation
```

Persistent history can use Room.

---

# 11. Text-to-Speech

Implement a TTS abstraction.

Default:

### Android TextToSpeech

Requirements:

- No API key
- Works locally when the required language engine is installed
- Adjustable speed
- Adjustable pitch
- Selectable voice when supported

Settings:

```text
TTS Provider
Voice
Language
Speech Rate
Pitch
```

Optional:

### Remote TTS

Allow a configurable HTTP TTS provider.

The app must be able to fall back to Android TTS if remote TTS fails.

---

# 12. Fast Response Mode

The assistant should feel fast.

Optimize the pipeline:

```text
Record
 ↓
STT
 ↓
LLM streaming
 ↓
TTS
 ↓
Speaker
```

Do not wait unnecessarily between stages.

If the LLM supports streaming, begin preparing TTS as soon as practical.

However, correctness is more important than overly complicated streaming.

Implement a stable basic pipeline first.

---

# 13. UI Design

Use a futuristic but minimal pocket-device aesthetic.

Requirements:

- Dark background
- Large central assistant indicator
- Large readable text
- Minimal controls
- Smooth but lightweight state transitions
- No unnecessary cards or menus on the main screen
- Responsive layout for small screens
- Landscape should not break the interface

Main screen:

```text
┌──────────────────────────────┐
│             AI               │
│                              │
│             ◉                │
│                              │
│           READY              │
│                              │
│  "Ask me anything..."        │
│                              │
│                              │
│        ┌───────────┐         │
│        │   TALK    │         │
│        └───────────┘         │
│                              │
│                       ⚙      │
└──────────────────────────────┘
```

The central indicator should animate differently for:

```text
READY
LISTENING
THINKING
SPEAKING
ERROR
```

Animations must be lightweight.

---

# 14. Conversation Display

Show recent conversation optionally.

Example:

```text
YOU
What's the weather?

ASSISTANT
I can check that if you have weather access enabled.
```

Because this is primarily a voice assistant, conversation text should remain secondary.

Add a setting:

```text
Show conversation: ON/OFF
```

---

# 15. Settings Screen

Create a complete settings screen.

Sections:

## AI

```text
Provider
Base URL
API Key
Model
Temperature
Max Tokens
System Prompt
```

## Speech Recognition

```text
Provider
Language
STT Endpoint
STT API Key
STT Model
```

## Voice

```text
TTS Provider
Language
Voice
Speech Rate
Pitch
```

## Assistant

```text
Press-to-talk
Hands-free mode
Wake word
Auto-speak responses
Show transcript
Conversation memory
```

## Appearance

```text
Theme
Animations
Show clock
Show battery
```

## Storage

```text
Clear conversation
Clear cached audio
Export settings
Import settings
```

## About

Show:

```text
App name
Version
Open-source licenses
```

---

# 16. API Key Security

Never place API keys directly in source code.

Bad:

```kotlin
const val API_KEY = "sk-..."
```

Good:

- Store user-entered keys in encrypted Android storage where practical.
- Never log keys.
- Never display full keys after saving.
- Mask keys in the UI.
- Do not commit secrets to Git.

For local development, allow environment/local configuration without committing secrets.

---

# 17. Network Layer

Use OkHttp.

Implement:

- Connection timeout
- Read timeout
- Write timeout
- Retry only when safe
- Clear error messages
- HTTP status handling
- JSON parsing errors
- Offline detection

Do not endlessly retry.

Example states:

```text
No Internet
Invalid API key
Rate limited
Server error
Invalid response
Timeout
```

---

# 18. WebSocket Support

Optional but recommended.

Implement a WebSocket abstraction for future low-latency providers.

Do not make WebSocket mandatory for the first working version.

The app should work using standard HTTPS APIs.

---

# 19. Offline Mode

The app should still open and display the interface without Internet.

Offline capabilities:

- UI
- Settings
- Conversation history
- Android TTS
- Android speech recognition if available offline
- Previously cached information where appropriate

When Internet is unavailable:

```text
OFFLINE
```

should be shown clearly.

Do not crash.

---

# 20. Wake Word

Wake-word support should be optional.

Do NOT require a cloud service for basic press-to-talk operation.

Create:

```kotlin
interface WakeWordManager {
    fun start()
    fun stop()
    fun setEnabled(enabled: Boolean)
}
```

For the initial release:

- Implement the interface.
- Use press-to-talk as the reliable default.
- If a local wake-word engine is added later, it should plug into this interface.

Avoid continuously recording/uploading audio without explicit user consent.

---

# 21. Physical Button Support

The app should work with:

- Touchscreen
- Volume buttons where Android permits it
- Bluetooth button devices where Android exposes them as input events
- Optional wired button/accessory in future versions

Provide a central action:

```text
Start listening
```

Keep hardware-specific functionality isolated.

---

# 22. Dedicated Device / Kiosk Mode

Add an optional "Pocket Device Mode".

When enabled:

- Launch the assistant screen automatically.
- Keep the screen awake while actively using the assistant.
- Hide unnecessary UI where Android permits.
- Prevent accidental navigation where possible.
- Provide an obvious way to exit device mode from Settings.
- Do not attempt to bypass Android security restrictions.

If the device supports Android kiosk/lock-task mode, document how to enable it properly.

Do not require root.

---

# 23. Battery Optimization

The application should be optimized for an old phone.

Rules:

- Do not continuously poll the network.
- Do not keep the microphone open unless explicitly enabled.
- Stop audio resources when unused.
- Stop animations when the app is not visible.
- Avoid large images.
- Avoid heavy background processing.
- Cache only what is necessary.

Add:

```text
Battery saver mode
```

which disables:

- Wake word
- Background service
- Continuous animations

unless required.

---

# 24. Audio Playback

For TTS/audio playback:

- Use Android AudioTrack or MediaPlayer/ExoPlayer only when appropriate.
- Release resources correctly.
- Allow interruption when the user starts speaking again.
- Stop current speech when the user presses the talk button.

Behavior:

```text
Assistant speaking
       ↓
User presses TALK
       ↓
Stop speech immediately
       ↓
Start recording
```

---

# 25. Permissions

Request only necessary permissions.

Likely permissions:

```xml
RECORD_AUDIO
INTERNET
```

Only add other permissions when actually required.

Explain microphone permission before requesting it.

If permission is permanently denied, provide instructions to open Android App Settings.

---

# 26. Error Handling

Every failure must produce a user-friendly message.

Examples:

```text
Microphone permission required.

No Internet connection.

Speech recognition failed.

AI request timed out.

Invalid API key.

AI server returned an error.

Text-to-speech is unavailable.
```

Do not show raw stack traces to normal users.

For developers, provide optional debug logging.

---

# 27. Logging

Implement structured logging.

Never log:

- API keys
- Passwords
- Full private conversation content by default
- Raw microphone recordings

Debug logs can contain:

```text
STT started
STT completed
LLM request started
LLM response received
TTS started
TTS completed
Network error
```

Provide:

```text
Debug logging: ON/OFF
```

---

# 28. Project Configuration

Create:

```text
README.md
ARCHITECTURE.md
SETUP.md
API.md
TROUBLESHOOTING.md
```

README must explain how to:

1. Open the project.
2. Install dependencies.
3. Build APK.
4. Install APK on an old phone.
5. Grant microphone permission.
6. Configure AI.
7. Configure STT.
8. Configure TTS.
9. Use press-to-talk.
10. Enable dedicated-device mode.

---

# 29. Build Requirements

The generated project must:

- Compile successfully.
- Have no placeholder implementations in the main flow.
- Not contain fake API responses.
- Not require proprietary IDE plugins.
- Produce a normal APK.
- Support debug builds.
- Provide release-build instructions.

Before considering the project complete:

```text
./gradlew assembleDebug
```

must succeed.

Fix all compile errors.

---

# 30. Testing

Create unit tests for:

- Settings repository
- AI request creation
- JSON parsing
- Conversation management
- Provider selection
- Error handling

Create instrumented/UI tests where practical for:

- Main screen
- Settings
- Permission handling
- Talk button state changes

Manually verify:

```text
Launch
 ↓
Permission
 ↓
READY
 ↓
Press TALK
 ↓
LISTENING
 ↓
Release
 ↓
TRANSCRIBING
 ↓
THINKING
 ↓
SPEAKING
 ↓
READY
```

---

# 31. Important Implementation Rule

Do not build the entire application at once without validating each layer.

Build in this order:

## Phase 1 — UI

Create:

- Main screen
- Status states
- Talk button
- Settings screen

No AI yet.

Verify the UI works.

## Phase 2 — Microphone

Implement:

- Permission
- AudioRecord
- Start/stop recording

Verify microphone capture.

## Phase 3 — STT

Implement Android Speech Recognition first.

Verify:

```text
Voice → Text
```

## Phase 4 — AI

Implement the configurable OpenAI-compatible provider.

Verify:

```text
Text → AI → Text
```

## Phase 5 — TTS

Implement Android TTS.

Verify:

```text
Text → Speech
```

## Phase 6 — Full pipeline

Connect:

```text
Voice
 ↓
STT
 ↓
AI
 ↓
TTS
 ↓
Voice
```

## Phase 7 — Optimization

Add:

- Streaming
- Better UI
- Conversation memory
- Error recovery
- Battery optimization

## Phase 8 — Dedicated-device mode

Add:

- Auto-launch
- Kiosk/lock-task support where appropriate
- Wake word interface
- Physical button support

---

# 32. Do Not Overengineer

The first working version should be simple.

Do NOT initially add:

- Complex microservices
- Kubernetes
- Firebase unless necessary
- Large databases
- Multiple unnecessary frameworks
- Custom backend if direct API calls are safe and appropriate
- Continuous microphone recording
- Root requirements
- Native code unless required for a specific audio/wake-word feature

The goal is a fast, reliable application for an old smartphone.

---

# 33. Optional Node.js Backend

If direct API calls from Android are undesirable because API credentials must remain private, create an optional Node.js backend.

Recommended:

```text
backend/
  src/
    server.js
    routes/
      chat.js
      stt.js
      tts.js
    services/
      ai.js
      stt.js
      tts.js
    config.js
```

Use:

- Node.js
- Fastify or Express
- WebSocket support
- Environment variables
- CORS configuration
- Rate limiting
- Request validation

Environment variables:

```env
PORT=3000

AI_BASE_URL=
AI_API_KEY=
AI_MODEL=

STT_BASE_URL=
STT_API_KEY=
STT_MODEL=

TTS_BASE_URL=
TTS_API_KEY=
TTS_MODEL=
```

Never commit `.env`.

Create:

```text
.env.example
```

The Android application should support both:

```text
Direct API mode
Backend mode
```

---

# 34. Backend Security

If a Node.js backend is used:

- Validate request bodies.
- Limit request size.
- Add rate limiting.
- Never expose provider API keys to the Android client.
- Use HTTPS in production.
- Add authentication if the backend is publicly accessible.
- Do not expose an unrestricted proxy endpoint.

For a local home-network backend, document the security implications.

---

# 35. Provider Configuration

Design the app so providers can be swapped.

Example:

```text
AI:
  OpenAI-compatible
  Custom server

STT:
  Android
  Whisper-compatible API

TTS:
  Android
  Custom TTS API
```

Do not couple the UI directly to any one provider.

---

# 36. Response Formatting

Because the assistant speaks responses aloud:

Prefer:

```text
The capital of Japan is Tokyo.
```

instead of:

```text
## Answer

- Capital: Tokyo
- Country: Japan
```

The UI may render markdown if desired, but TTS should receive cleaned natural language.

Create a utility:

```kotlin
fun cleanTextForSpeech(text: String): String
```

It should remove or simplify:

- Markdown headings
- Code fences
- Excessive symbols
- URLs where appropriate
- Repeated whitespace

Do not destroy meaningful content.

---

# 37. Interruption

The assistant must be interruptible.

If:

```text
SPEAKING
```

and the user presses TALK:

```text
stop TTS
clear current playback
start recording
```

This should feel instantaneous.

---

# 38. Long Responses

Do not force the phone to speak enormous responses.

For voice mode:

- Ask the LLM for concise responses.
- Allow "tell me more" for expansion.
- Stream text where practical.
- Split very long TTS content into manageable chunks if necessary.

---

# 39. Privacy

Clearly explain:

- Microphone is used for voice interaction.
- Audio may be sent to a configured remote STT service.
- Text may be sent to the configured AI service.
- Remote TTS may receive response text.
- Android TTS can be used for local speech output.

Provide:

```text
Privacy
```

in Settings.

Do not silently upload audio.

---

# 40. First-Run Setup

On first launch:

```text
Welcome

Let's configure your AI assistant.

1. Microphone permission
2. Choose STT
3. Configure AI
4. Choose TTS
5. Test microphone
6. Test AI
7. Test speaker
```

Allow the user to skip configuration and enter Settings later.

---

# 41. Test Assistant

Add a diagnostic page:

```text
SYSTEM TEST

✓ Microphone
✓ Speech recognition
✓ Internet
✓ AI
✓ Text-to-speech
✓ Speaker
✓ Storage
```

Each test should have:

```text
TEST
```

button.

This is especially important because the app will run on old phones with unknown hardware/software configurations.

---

# 42. Old Phone Compatibility

Optimize for devices with approximately:

- 2 GB RAM or more
- Older Android versions where technically feasible
- Low-resolution screens
- Slow CPUs
- Limited storage

Do not assume:

- Modern neural processing hardware
- Latest Android APIs
- High refresh-rate displays
- Large amounts of RAM

Use graceful feature detection.

If a feature is unsupported:

```text
This feature isn't available on this device.
```

instead of crashing.

---

# 43. Deliverables

Generate the complete project.

Required:

```text
Android project
README.md
ARCHITECTURE.md
SETUP.md
API.md
TROUBLESHOOTING.md
.env.example (if backend exists)
Node.js backend (if backend mode is implemented)
```

The final project should be ready to open in Android Studio and build.

---

# 44. Final Acceptance Test

The project is complete only when this works:

```text
1. Install APK on Android phone.

2. Open app.

3. Grant microphone permission.

4. Main screen shows READY.

5. Hold TALK.

6. App shows LISTENING.

7. Say:
   "Hello, who are you?"

8. Release TALK.

9. App converts speech to text.

10. App sends text to configured AI.

11. AI response appears.

12. Phone speaks the response.

13. App returns to READY.

14. Ask:
   "What did I just ask you?"

15. Assistant understands the conversation context.

16. Turn off Internet.

17. App does not crash.

18. Restore Internet.

19. Assistant works again.

20. Open Settings and change provider/model.

21. Configuration persists after app restart.
```

---

# 45. Development Instructions to the Coding AI

You are the lead Android engineer.

Build this project rather than merely describing it.

Rules:

1. Inspect the existing project before modifying it.
2. Preserve working code.
3. Do not overwrite unrelated functionality.
4. Use clean architecture.
5. Keep dependencies minimal.
6. Do not hardcode secrets.
7. Do not create fake implementations for the core pipeline.
8. Clearly mark optional features.
9. Make the basic voice assistant work first.
10. Compile frequently.
11. Fix all compiler errors.
12. Handle Android lifecycle correctly.
13. Handle permissions correctly.
14. Handle network failures.
15. Handle old-device limitations.
16. Keep the UI responsive.
17. Do not block the main thread.
18. Use coroutines for asynchronous operations.
19. Do not leak microphone/audio/network resources.
20. Add useful comments only where they explain non-obvious behavior.

When an external API is required, implement it behind an interface and make its endpoint/model/API key configurable.

If an API's exact request format is unknown, do not invent one silently. Implement a clearly documented adapter interface and ask for the provider's API specification if needed.

---

# 46. Recommended Default Configuration

For the first working build:

```text
UI:
Jetpack Compose

Speech-to-text:
Android Speech Recognition

AI:
OpenAI-compatible HTTP API

TTS:
Android TextToSpeech

Networking:
OkHttp

Settings:
DataStore

Conversation:
In-memory initially
Room optionally later

Backend:
Not required initially
```

This minimizes complexity.

---

# 47. Important Goal

The final application should feel like a dedicated physical AI assistant:

```text
       ┌─────────────────────────┐
       │                         │
       │           ◉             │
       │                         │
       │        READY            │
       │                         │
       │                         │
       │       HOLD TO TALK      │
       │                         │
       └─────────────────────────┘
```

The user should be able to pick up an old Android phone, open the application, press one button, speak naturally, and hear an AI response.

Do not turn the application into a conventional chatbot UI unless the user explicitly asks for that.

The primary experience is:

**PRESS → SPEAK → THINK → HEAR**

Build the simplest reliable version first, then add advanced capabilities.
