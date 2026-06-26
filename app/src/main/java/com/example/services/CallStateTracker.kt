package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow

object CallStateTracker {
    val isRecording = MutableStateFlow(false)
    val callerName = MutableStateFlow("مكالمة جارية")
    val durationSec = MutableStateFlow(0)
    val amplitudeList = MutableStateFlow<List<Float>>(emptyList())
    val platform = MutableStateFlow("CELLULAR")
    val direction = MutableStateFlow("INBOUND") // INBOUND or OUTBOUND
    val activeFilePath = MutableStateFlow<String?>(null)

    var initialSpeakerState: Boolean = false
    var initialAudioMode: Int = 0 // AudioManager.MODE_NORMAL
}
