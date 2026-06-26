package com.example.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class AudioPlayerManager(private val context: Context) {
    private val TAG = "AudioPlayerManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentFilePath: String? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val updateProgressAction = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    val duration = player.duration
                    _playbackState.value = PlaybackState.Playing(currentPos, duration)
                    handler.postDelayed(this, 250) // Update every 250ms
                }
            }
        }
    }

    fun playAudio(filePath: String, playbackSpeed: Float = 1.0f) {
        // If playing the same file and paused, resume
        if (currentFilePath == filePath && _playbackState.value is PlaybackState.Paused) {
            mediaPlayer?.let { player ->
                player.start()
                setPlaybackSpeed(playbackSpeed)
                _playbackState.value = PlaybackState.Playing(player.currentPosition, player.duration)
                handler.post(updateProgressAction)
                return
            }
        }

        // Otherwise stop any existing playback
        stopAudio()

        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: $filePath")
                _playbackState.value = PlaybackState.Error("ملف الصوت غير موجود")
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.Completed
                    handler.removeCallbacks(updateProgressAction)
                    currentFilePath = null
                }
            }

            currentFilePath = filePath
            setPlaybackSpeed(playbackSpeed)
            _playbackState.value = PlaybackState.Playing(0, mediaPlayer?.duration ?: 0)
            handler.post(updateProgressAction)
            Log.d(TAG, "Playback started for: $filePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio", e)
            _playbackState.value = PlaybackState.Error("فشل في تشغيل ملف الصوت")
        }
    }

    fun pauseAudio() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = PlaybackState.Paused(player.currentPosition, player.duration)
                handler.removeCallbacks(updateProgressAction)
                Log.d(TAG, "Playback paused")
            }
        }
    }

    fun resumeAudio() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                _playbackState.value = PlaybackState.Playing(player.currentPosition, player.duration)
                handler.post(updateProgressAction)
                Log.d(TAG, "Playback resumed")
            }
        }
    }

    fun stopAudio() {
        handler.removeCallbacks(updateProgressAction)
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            currentFilePath = null
            _playbackState.value = PlaybackState.Idle
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs)
            val currentState = _playbackState.value
            if (currentState is PlaybackState.Playing) {
                _playbackState.value = PlaybackState.Playing(positionMs, player.duration)
            } else if (currentState is PlaybackState.Paused) {
                _playbackState.value = PlaybackState.Paused(positionMs, player.duration)
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.playbackParams = player.playbackParams.setSpeed(speed)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set playback speed", e)
            }
        }
    }

    fun getCurrentPlayingPath(): String? = currentFilePath

    sealed interface PlaybackState {
        object Idle : PlaybackState
        data class Playing(val currentPositionMs: Int, val durationMs: Int) : PlaybackState
        data class Paused(val currentPositionMs: Int, val durationMs: Int) : PlaybackState
        object Completed : PlaybackState
        data class Error(val message: String) : PlaybackState
    }
}
