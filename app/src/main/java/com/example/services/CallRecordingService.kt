package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.database.Recording
import com.example.data.database.RecordingDatabase
import com.example.utils.AudioRecorderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CallRecordingService : Service() {
    private val TAG = "CallRecordingService"
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "call_recording_channel"

    private lateinit var recorderManager: AudioRecorderManager
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null

    companion object {
        const val ACTION_START_RECORDING = "com.example.services.ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.services.ACTION_STOP_RECORDING"
        const val EXTRA_PHONE_NUMBER = "com.example.services.EXTRA_PHONE_NUMBER"
        const val EXTRA_CALL_DIRECTION = "com.example.services.EXTRA_CALL_DIRECTION" // INBOUND or OUTBOUND
    }

    override fun onCreate() {
        super.onCreate()
        recorderManager = AudioRecorderManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand: action = $action")

        if (action == ACTION_START_RECORDING) {
            val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
            val direction = intent.getStringExtra(EXTRA_CALL_DIRECTION) ?: "INBOUND"
            startRecordingCall(phoneNumber, direction)
        } else if (action == ACTION_STOP_RECORDING) {
            stopRecordingCall()
        }

        return START_NOT_STICKY
    }

    private fun startRecordingCall(phoneNumber: String?, direction: String) {
        if (CallStateTracker.isRecording.value) {
            Log.d(TAG, "Recording is already active.")
            return
        }

        // Get Contact Name or Fallback
        val resolvedName = if (!phoneNumber.isNullOrEmpty()) {
            getContactName(this, phoneNumber)
        } else {
            if (direction == "INBOUND") "مكالمة واردة" else "مكالمة صادرة"
        }

        // Setup notification
        val notification = buildNotification(resolvedName)

        // Start Foreground Service with type Microphone for Android 14 compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val prefix = if (direction == "INBOUND") "call_in" else "call_out"
        
        serviceScope.launch {
            // Delay to allow the dialer's audio routing to settle and our MainActivity to come to foreground
            delay(1500)
            
            val file = recorderManager.startRecording(prefix, isCallRecording = true)

            if (file != null) {
                // Update Tracker
                CallStateTracker.isRecording.value = true
                CallStateTracker.callerName.value = resolvedName
                CallStateTracker.durationSec.value = 0
                CallStateTracker.platform.value = "CELLULAR"
                CallStateTracker.direction.value = direction
                CallStateTracker.activeFilePath.value = file.absolutePath
                CallStateTracker.amplitudeList.value = emptyList()

                // Start active monitoring timers
                startTimers()
                Log.d(TAG, "Call recording started for $resolvedName")
            } else {
                Log.e(TAG, "Failed to start call recording.")
                stopSelf()
            }
        }
    }

    private fun stopRecordingCall() {
        if (!CallStateTracker.isRecording.value) {
            stopSelf()
            return
        }

        stopTimers()
        val result = recorderManager.stopRecording()
        val filePath = CallStateTracker.activeFilePath.value
        val callerName = CallStateTracker.callerName.value
        val direction = CallStateTracker.direction.value
        val duration = result.durationSec

        // Reset Tracker
        CallStateTracker.isRecording.value = false
        CallStateTracker.callerName.value = "مكالمة جارية"
        CallStateTracker.durationSec.value = 0
        CallStateTracker.activeFilePath.value = null
        CallStateTracker.amplitudeList.value = emptyList()

        if (result.file != null && result.file.exists()) {
            serviceScope.launch {
                try {
                    // Delay slightly to allow the OS to write the final call log entry
                    delay(1200)
                    
                    var finalName = callerName
                    try {
                        val callDetails = getLastCallDetails(applicationContext)
                        if (callDetails != null) {
                            finalName = if (!callDetails.name.isNullOrEmpty()) {
                                callDetails.name
                            } else if (!callDetails.number.isNullOrEmpty()) {
                                callDetails.number
                            } else {
                                finalName
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to fetch name from call log", ex)
                    }

                    val database = RecordingDatabase.getDatabase(applicationContext)
                    val newRecording = Recording(
                        title = finalName,
                        source = "CELLULAR",
                        direction = direction,
                        durationSec = duration,
                        filePath = result.file.absolutePath,
                        timestamp = System.currentTimeMillis(),
                        notes = "مكالمة مسجلة تلقائياً بفضل كاشف الإشارات والاتصال المباشر."
                    )
                    database.recordingDao().insertRecording(newRecording)
                    Log.d(TAG, "Successfully saved call recording to database as: $finalName")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving recording to Room", e)
                } finally {
                    stopSelf()
                }
            }
        } else {
            Log.e(TAG, "No recording file found after stopping recording.")
            stopSelf()
        }
    }

    private fun getLastCallDetails(context: Context): CallDetails? {
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE
                ),
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val number = it.getString(0)
                    val name = it.getString(1)
                    return CallDetails(number, name)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying call log details", e)
        }
        return null
    }

    private data class CallDetails(val number: String?, val name: String?)

    private fun startTimers() {
        timerJob?.cancel()
        timerJob = serviceScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000)
                CallStateTracker.durationSec.value += 1
            }
        }

        amplitudeJob?.cancel()
        amplitudeJob = serviceScope.launch(Dispatchers.Main) {
            while (true) {
                delay(100)
                val amp = recorderManager.getAmplitude()
                val normalized = (amp.toFloat() / 32767f).coerceIn(0f, 1f)
                val currentList = CallStateTracker.amplitudeList.value.toMutableList()
                if (currentList.size > 50) {
                    currentList.removeAt(0)
                }
                currentList.add(normalized)
                CallStateTracker.amplitudeList.value = currentList
            }
        }
    }

    private fun stopTimers() {
        timerJob?.cancel()
        timerJob = null
        amplitudeJob?.cancel()
        amplitudeJob = null
    }

    private fun buildNotification(callerName: String): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مسجل المكالمات الذكي نشط")
            .setContentText("جاري تسجيل المكالمة مع: $callerName تلقائياً...")
            .setSmallIcon(android.R.drawable.presence_audio_online)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Recording Services",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies user of background call recording services."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getContactName(context: Context, phoneNumber: String?): String {
        if (phoneNumber.isNullOrEmpty()) return "مكالمة مجهولة"
        try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(0) ?: phoneNumber
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact name", e)
        }
        return phoneNumber
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTimers()
        super.onDestroy()
    }
}
