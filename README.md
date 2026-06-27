# Sajil - مسجل المكالمات الذكي (Smart Call Recorder)

Sajil is a professional Android application designed for recording standard phone calls, as well as calls from popular messaging apps (WhatsApp, Messenger). It features automatic speech-to-text transcription and AI-powered call summarization.

## Features

- **Automatic Call Recording:** Seamlessly records incoming and outgoing cellular calls.
- **AI Speech-to-Text & Summarization:** Automatically transcribes and summarizes the content of your calls using Gemini AI.
- **Material Design 3:** A clean, modern interface featuring a dark slate theme with fluid animations.
- **Local Persistence:** Securely stores your audio recordings and call logs locally using Room Database.
- **Playback Controls:** Built-in audio player with waveform visualization.

## Permissions Required

For the app to function properly and record calls in the background, the following permissions are requested:
- `Microphone` (RECORD_AUDIO)
- `Phone State` (READ_PHONE_STATE)
- `Call Logs` (READ_CALL_LOG)
- `Notifications` (POST_NOTIFICATIONS)
- `Display over other apps` (SYSTEM_ALERT_WINDOW)

## Technologies Used

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Database:** Room
- **Media:** `MediaRecorder`, `MediaPlayer`
- **Architecture:** MVVM, Coroutines, Flow
