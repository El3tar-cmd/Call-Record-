package com.example.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class CallRecordingAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "CallRecordingA11y"
        var isServiceEnabled = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceEnabled = true
        Log.d(TAG, "Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We do not need to process any events. 
        // Just having this service bound allows the app to record VOICE_RECOGNITION in background.
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceEnabled = false
        Log.d(TAG, "Accessibility Service unbound")
        return super.onUnbind(intent)
    }
}
