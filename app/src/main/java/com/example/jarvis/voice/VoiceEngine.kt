package com.example.jarvis.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

interface WakeWordEngine {
    val name: String
    val isSupported: Boolean
    fun startListening(onWakeWordDetected: () -> Unit)
    fun stopListening()
}

class DefaultWakeWordEngine : WakeWordEngine {
    override val name = "Heuristic Acoustic Detector"
    override val isSupported = false // Stubbed for continuous wake-word hardware integration

    override fun startListening(onWakeWordDetected: () -> Unit) {
        // Continuous background listening will integrate when hardware low-power DSP is configured
    }

    override fun stopListening() {}
}

object VoicePermissions {
    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
