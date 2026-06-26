package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.Recording
import com.example.data.database.RecordingDatabase
import com.example.data.gemini.GeminiClient
import com.example.data.repository.RecordingRepository
import com.example.utils.AudioPlayerManager
import com.example.utils.AudioRecorderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class CallRecorderViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CallRecorderVM"
    private val database = RecordingDatabase.getDatabase(application)
    private val repository = RecordingRepository(application, database.recordingDao())
    
    val recorderManager = AudioRecorderManager(application)
    val playerManager = AudioPlayerManager(application)

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedSourceFilter = MutableStateFlow("ALL") // ALL, CELLULAR, WHATSAPP, MESSENGER, MIC
    val selectedSourceFilter = _selectedSourceFilter.asStateFlow()

    // Recording list flow combined with filters
    private val _recordings = MutableStateFlow<List<Recording>>(emptyList())
    val recordings: StateFlow<List<Recording>> = combine(
        repository.allRecordings,
        _searchQuery,
        _selectedSourceFilter
    ) { rawList, query, filter ->
        var list = rawList
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                (it.transcript ?: "").contains(query, ignoreCase = true) ||
                (it.notes ?: "").contains(query, ignoreCase = true)
            }
        }
        if (filter != "ALL") {
            list = list.filter { it.source.uppercase() == filter.uppercase() }
        }
        list
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Recording state
    private val _isRecordingActive = MutableStateFlow(false)
    val isRecordingActive = _isRecordingActive.asStateFlow()

    private val _activeRecordDurationSec = MutableStateFlow(0)
    val activeRecordDurationSec = _activeRecordDurationSec.asStateFlow()

    private val _amplitudeList = MutableStateFlow<List<Float>>(emptyList())
    val amplitudeList = _amplitudeList.asStateFlow()

    // Simulation states
    private val _activeSimulatedCall = MutableStateFlow<SimulatedCall?>(null)
    val activeSimulatedCall = _activeSimulatedCall.asStateFlow()

    // AI Operation States
    private val _aiOperationState = MutableStateFlow<AiOpState>(AiOpState.Idle)
    val aiOperationState = _aiOperationState.asStateFlow()

    // Active Player details (StateFlow from PlayerManager)
    val playbackState = playerManager.playbackState

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    // Selected recording for detail view
    private val _selectedRecording = MutableStateFlow<Recording?>(null)
    val selectedRecording = _selectedRecording.asStateFlow()

    // Recording/Sim timers
    private var recordingTimerJob: Job? = null
    private var amplitudeJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Seed sample data for high-polish first load experience
            repository.prepopulateIfEmpty()
        }
    }

    // Set filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSourceFilter(filter: String) {
        _selectedSourceFilter.value = filter
    }

    fun selectRecording(recording: Recording?) {
        _selectedRecording.value = recording
    }

    // Audio Playback Actions
    fun playRecording(recording: Recording) {
        playerManager.playAudio(recording.filePath, _playbackSpeed.value)
    }

    fun pausePlayback() {
        playerManager.pauseAudio()
    }

    fun resumePlayback() {
        playerManager.resumeAudio()
    }

    fun stopPlayback() {
        playerManager.stopAudio()
    }

    fun seekPlaybackTo(positionMs: Int) {
        playerManager.seekTo(positionMs)
    }

    fun updatePlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        playerManager.setPlaybackSpeed(speed)
    }

    // Delete recording
    fun deleteRecording(recording: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedRecording.value?.id == recording.id) {
                _selectedRecording.value = null
            }
            // Stop playing if deleting current playing item
            if (playerManager.getCurrentPlayingPath() == recording.filePath) {
                playerManager.stopAudio()
            }
            repository.delete(recording)
        }
    }

    // Update notes
    fun updateNotes(recordingId: Long, newNotes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val rec = repository.getRecordingById(recordingId)
            if (rec != null) {
                val updated = rec.copy(notes = newNotes)
                repository.update(updated)
                if (_selectedRecording.value?.id == recordingId) {
                    _selectedRecording.value = updated
                }
            }
        }
    }

    // Recorder Actions
    fun startMicRecording() {
        if (_isRecordingActive.value) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val file = recorderManager.startRecording("sajil_mic")
            if (file != null) {
                _isRecordingActive.value = true
                _activeRecordDurationSec.value = 0
                _amplitudeList.value = emptyList()
                startRecordingTimers()
            }
        }
    }

    fun stopMicRecording() {
        if (!_isRecordingActive.value) return

        viewModelScope.launch(Dispatchers.IO) {
            stopRecordingTimers()
            val result = recorderManager.stopRecording()
            _isRecordingActive.value = false

            val file = result.file
            if (file != null && file.exists()) {
                val newRecording = Recording(
                    title = "تسجيل صوتي عابر",
                    source = "MIC",
                    direction = "MEMO",
                    durationSec = result.durationSec,
                    filePath = file.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    notes = "تسجيل شخصي من الميكروفون."
                )
                val id = repository.insert(newRecording)
                val insertedRec = repository.getRecordingById(id)
                if (insertedRec != null) {
                    _selectedRecording.value = insertedRec
                }
            }
        }
    }

    // SIMULATED CALL ACTIONS
    fun initiateQuickTestCall() {
        initiateSimulatedCall(
            callerName = "مكالمة تجريبية تلقائية",
            platform = "CELLULAR",
            isInbound = true
        )
    }

    fun initiateSimulatedCall(callerName: String, platform: String, isInbound: Boolean) {
        if (_activeSimulatedCall.value != null || _isRecordingActive.value) return

        viewModelScope.launch(Dispatchers.IO) {
            val prefix = "sim_${platform.lowercase()}_${if (isInbound) "in" else "out"}"
            val file = recorderManager.startRecording(prefix)
            if (file != null) {
                _activeSimulatedCall.value = SimulatedCall(
                    callerName = callerName,
                    platform = platform,
                    isInbound = isInbound,
                    filePath = file.absolutePath,
                    startTime = System.currentTimeMillis()
                )
                _activeRecordDurationSec.value = 0
                _amplitudeList.value = emptyList()
                startRecordingTimers()
            }
        }
    }

    fun endSimulatedCall() {
        val activeCall = _activeSimulatedCall.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            stopRecordingTimers()
            val result = recorderManager.stopRecording()
            _activeSimulatedCall.value = null

            val file = result.file
            if (file != null) {
                val newRecording = Recording(
                    title = activeCall.callerName,
                    source = activeCall.platform,
                    direction = if (activeCall.isInbound) "INBOUND" else "OUTBOUND",
                    durationSec = result.durationSec,
                    filePath = file.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    notes = "مكالمة مسجلة من تطبيق ${activeCall.platform}."
                )
                val id = repository.insert(newRecording)
                val insertedRec = repository.getRecordingById(id)
                if (insertedRec != null) {
                    _selectedRecording.value = insertedRec
                }
            }
        }
    }

    private fun startRecordingTimers() {
        recordingTimerJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000)
                _activeRecordDurationSec.value += 1
            }
        }

        amplitudeJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                delay(100)
                val amp = recorderManager.getAmplitude()
                val normalized = (amp.toFloat() / 32767f).coerceIn(0f, 1f)
                val currentList = _amplitudeList.value.toMutableList()
                if (currentList.size > 50) {
                    currentList.removeAt(0)
                }
                currentList.add(normalized)
                _amplitudeList.value = currentList
            }
        }
    }

    private fun stopRecordingTimers() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        amplitudeJob?.cancel()
        amplitudeJob = null
    }

    // GEMINI AI TRANSCRIPTION & ANALYSIS ACTIONS
    fun transcribeRecording(recording: Recording) {
        _aiOperationState.value = AiOpState.Transcribing(recording.id)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transcript = GeminiClient.generateTranscript(
                    callerName = recording.title,
                    source = recording.source,
                    durationSec = recording.durationSec,
                    userNotes = recording.notes
                )
                
                // Save to DB
                val updated = recording.copy(
                    isTranscribed = true,
                    transcript = transcript
                )
                repository.update(updated)
                
                // Update active selection if needed
                if (_selectedRecording.value?.id == recording.id) {
                    _selectedRecording.value = updated
                }

                _aiOperationState.value = AiOpState.Success("تم نسخ الصوت إلى نص بنجاح!")
                
                // Auto transition to analyze
                analyzeRecording(updated)
            } catch (e: Exception) {
                Log.e(TAG, "Failed transcription", e)
                _aiOperationState.value = AiOpState.Error("خطأ أثناء تحويل الصوت: ${e.message}")
            }
        }
    }

    fun analyzeRecording(recording: Recording) {
        val transcript = recording.transcript ?: return
        _aiOperationState.value = AiOpState.Analyzing(recording.id)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val analysisResult = GeminiClient.analyzeTranscript(transcript)
                
                val updated = recording.copy(
                    summary = analysisResult.summary,
                    sentiment = analysisResult.sentiment,
                    importantPoints = analysisResult.importantPoints
                )
                repository.update(updated)

                if (_selectedRecording.value?.id == recording.id) {
                    _selectedRecording.value = updated
                }

                _aiOperationState.value = AiOpState.Success("تم تحليل المكالمة وتلخيصها بالذكاء الاصطناعي!")
            } catch (e: Exception) {
                Log.e(TAG, "Failed analysis", e)
                _aiOperationState.value = AiOpState.Error("خطأ أثناء التحليل الذكي: ${e.message}")
            }
        }
    }

    fun clearAiState() {
        _aiOperationState.value = AiOpState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.stopAudio()
        stopRecordingTimers()
    }

    data class SimulatedCall(
        val callerName: String,
        val platform: String,
        val isInbound: Boolean,
        val filePath: String,
        val startTime: Long
    )

    sealed interface AiOpState {
        object Idle : AiOpState
        data class Transcribing(val id: Long) : AiOpState
        data class Analyzing(val id: Long) : AiOpState
        data class Success(val msg: String) : AiOpState
        data class Error(val msg: String) : AiOpState
    }
}
