package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.services.CallRecordingService

class PhoneCallReceiver : BroadcastReceiver() {
    private val TAG = "PhoneCallReceiver"

    companion object {
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        private var isIncoming = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d(TAG, "onReceive: stateStr = $stateStr, number = $number")

        if (number != null) {
            savedNumber = number
        }

        when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                isIncoming = true
                lastState = TelephonyManager.EXTRA_STATE_RINGING
                Log.d(TAG, "Ringing... Incoming call detected.")
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call answered or outgoing call placed
                val direction = if (isIncoming) "INBOUND" else "OUTBOUND"
                Log.d(TAG, "Off-hook: starting CallRecordingService. Direction = $direction, Number = $savedNumber")
                
                val serviceIntent = Intent(context, CallRecordingService::class.java).apply {
                    action = CallRecordingService.ACTION_START_RECORDING
                    putExtra(CallRecordingService.EXTRA_PHONE_NUMBER, savedNumber)
                    putExtra(CallRecordingService.EXTRA_CALL_DIRECTION, direction)
                }
                
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    // Auto-launch MainActivity so the app wakes up and connects to the active call automatically!
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start call recording service or wake up main activity", e)
                }

                lastState = TelephonyManager.EXTRA_STATE_OFFHOOK
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d(TAG, "Idle: stopping CallRecordingService.")
                val serviceIntent = Intent(context, CallRecordingService::class.java).apply {
                    action = CallRecordingService.ACTION_STOP_RECORDING
                }
                try {
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to stop recording service", e)
                }
                
                isIncoming = false
                savedNumber = null
                lastState = TelephonyManager.EXTRA_STATE_IDLE
            }
        }
    }
}
