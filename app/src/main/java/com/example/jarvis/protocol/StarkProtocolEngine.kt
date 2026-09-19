package com.example.jarvis.protocol

import android.content.Context
import android.os.Build
import com.example.jarvis.bridge.AndroidBridge
import com.example.jarvis.model.ActivityType
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.MessageSender
import com.example.jarvis.privacy.PrivacyAuditor
import com.example.jarvis.storage.JarvisRepository
import com.example.jarvis.voice.CyberneticAudioEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stark Operational Protocols Engine.
 * Formulates and executes comprehensive, charismatic executive protocols
 * inspired by Tony Stark's J.A.R.V.I.S. operating environment.
 */
object StarkProtocolEngine {

    enum class StarkProtocolType(val title: String, val code: String) {
        MORNING("Morning Briefing Protocol", "protocol_morning"),
        NIGHT("Night Standby Protocol", "protocol_night"),
        SECURE_PERIMETER("Secure Perimeter Protocol", "protocol_perimeter"),
        POWER_SURGE("Reactor Overclock Surge", "protocol_surge");

        companion object {
            fun fromInput(input: String): StarkProtocolType {
                val lower = input.lowercase()
                return when {
                    lower.contains("night") || lower.contains("sleep") || lower.contains("bedtime") || lower.contains("so jao") || lower.contains("goodnight") -> NIGHT
                    lower.contains("secure") || lower.contains("perimeter") || lower.contains("lockdown") || lower.contains("security") -> SECURE_PERIMETER
                    lower.contains("surge") || lower.contains("overclock") || lower.contains("power") || lower.contains("maximum") -> POWER_SURGE
                    else -> MORNING
                }
            }
        }
    }

    data class ProtocolResult(
        val protocol: StarkProtocolType,
        val speechText: String,
        val summaryBadge: String,
        val telemetryPoints: List<String>,
        val executionTimestamp: Long = System.currentTimeMillis()
    )

    suspend fun executeProtocol(
        protocolType: StarkProtocolType,
        bridge: AndroidBridge,
        repository: JarvisRepository,
        context: Context
    ): ProtocolResult {
        bridge.refreshTelemetry()
        val telemetry = bridge.telemetry.value

        return when (protocolType) {
            StarkProtocolType.MORNING -> executeMorningProtocol(bridge, repository, telemetry)
            StarkProtocolType.NIGHT -> executeNightProtocol(bridge, repository, telemetry)
            StarkProtocolType.SECURE_PERIMETER -> executeSecurePerimeterProtocol(bridge, repository, context)
            StarkProtocolType.POWER_SURGE -> executePowerSurgeProtocol(bridge, repository, telemetry)
        }
    }

    private fun executeMorningProtocol(
        bridge: AndroidBridge,
        repository: JarvisRepository,
        telemetry: com.example.jarvis.model.DeviceTelemetry
    ): ProtocolResult {
        CyberneticAudioEngine.playWakeChime()

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val now = Date()
        val currentTime = timeFormat.format(now)
        val currentDate = dateFormat.format(now)

        val pendingTasks = repository.tasks.value.filter { !it.isCompleted }
        val highPriorityTasks = pendingTasks.filter { it.priority.equals("High", ignoreCase = true) }

        val telemetryPoints = mutableListOf<String>()
        telemetryPoints.add("Temporal Coordinates: $currentDate, $currentTime")
        telemetryPoints.add("Main Power Cell: ${telemetry.batteryPercent}% (${if (telemetry.isCharging) "Charging" else "Discharging"})")
        telemetryPoints.add("Uplink Matrix: ${telemetry.networkType} / Wi-Fi Active")
        telemetryPoints.add("Pending Directives: ${pendingTasks.size} active (${highPriorityTasks.size} high priority)")

        val speechBuilder = StringBuilder()
        speechBuilder.append("Good morning, Sir. ")
        speechBuilder.append("The time is $currentTime on $currentDate. ")
        speechBuilder.append("Main power is at ${telemetry.batteryPercent} percent. ")
        speechBuilder.append("Network telemetry indicates an optimal ${telemetry.networkType} uplink. ")

        if (pendingTasks.isNotEmpty()) {
            val taskMention = if (highPriorityTasks.isNotEmpty()) {
                "You have ${pendingTasks.size} operational directives scheduled, with ${highPriorityTasks.first().title} prioritized. "
            } else {
                "You have ${pendingTasks.size} pending tasks logged in the queue. "
            }
            speechBuilder.append(taskMention)
        } else {
            speechBuilder.append("Your agenda is currently clear of pending conflicts. ")
        }

        val upcomingEvents = bridge.queryUpcomingCalendarEvents()
        if (upcomingEvents.isNotEmpty()) {
            telemetryPoints.add("Calendar Schedule: ${upcomingEvents.size} appointment(s) registered today")
            speechBuilder.append("Calendar scan reveals ${upcomingEvents.size} scheduled item${if (upcomingEvents.size > 1) "s" else ""}: ${upcomingEvents.take(2).joinToString("; ")}. ")
        }

        speechBuilder.append("All primary diagnostics are nominal. I remain online and awaiting your directives.")

        val speech = speechBuilder.toString()
        repository.logActivity("Stark Protocol", "Morning Briefing Executed", ActivityType.SYSTEM_EVENT)

        return ProtocolResult(
            protocol = StarkProtocolType.MORNING,
            speechText = speech,
            summaryBadge = "MORNING PROTOCOL ACTIVE",
            telemetryPoints = telemetryPoints
        )
    }

    private fun executeNightProtocol(
        bridge: AndroidBridge,
        repository: JarvisRepository,
        telemetry: com.example.jarvis.model.DeviceTelemetry
    ): ProtocolResult {
        CyberneticAudioEngine.playShieldEngage()

        // Softly balance audio volume for night hours
        bridge.setVolume(20)

        val completedToday = repository.tasks.value.filter { it.isCompleted }

        val telemetryPoints = mutableListOf<String>()
        telemetryPoints.add("Acoustic Output: Calibrated to Night Standby (20%)")
        telemetryPoints.add("Power Cell: ${telemetry.batteryPercent}%")
        telemetryPoints.add("Accomplished Objectives: ${completedToday.size} tasks completed")
        telemetryPoints.add("Security Perimeter: Vigilant Standby")

        val speech = "All core systems are switching to auxiliary standby mode, Sir. " +
                "Audio levels have been reduced for evening comfort. " +
                "Battery is at ${telemetry.batteryPercent} percent. " +
                (if (completedToday.isNotEmpty()) "You have accomplished ${completedToday.size} operational goals today. " else "") +
                "I will monitor perimeter telemetry and alarms in background. Have a restful evening, Sir."

        repository.logActivity("Stark Protocol", "Night Standby Protocol Engaged", ActivityType.SYSTEM_EVENT)

        return ProtocolResult(
            protocol = StarkProtocolType.NIGHT,
            speechText = speech,
            summaryBadge = "NIGHT STANDBY ENGAGED",
            telemetryPoints = telemetryPoints
        )
    }

    private suspend fun executeSecurePerimeterProtocol(
        bridge: AndroidBridge,
        repository: JarvisRepository,
        context: Context
    ): ProtocolResult {
        CyberneticAudioEngine.playScanPing()

        val auditor = PrivacyAuditor(context, repository)
        val telemetry = auditor.getPrivacyTelemetry()

        val telemetryPoints = mutableListOf<String>()
        telemetryPoints.add("Acoustic Sensor (Microphone): ${if (telemetry.hasMicPermission) "Protected & Monitored" else "Access Restricted"}")
        telemetryPoints.add("Optical Sensor (Camera): ${if (telemetry.hasCameraPermission) "Secured" else "Shielded"}")
        telemetryPoints.add("Accessibility Shield: ${if (telemetry.isAccessibilityServiceEnabled) "Active & Guarding" else "Disabled"}")
        telemetryPoints.add("Notification Intercept: ${if (telemetry.isNotificationAccessEnabled) "Vigilant" else "Offline"}")
        telemetryPoints.add("Active AI Core: ${telemetry.activeProviderName}")

        val speech = "Perimeter security scan complete, Sir. " +
                "Hardware sensors verified: Microphone is ${if (telemetry.hasMicPermission) "operational" else "restricted"}, and optical sensors are secured. " +
                "Accessibility defense matrix is ${if (telemetry.isAccessibilityServiceEnabled) "online" else "in standby"}. " +
                "Zero unauthorized data leaks or perimeter breaches detected. Security posture remains fortified."

        repository.logActivity("Stark Protocol", "Perimeter Security Lockdown Verified", ActivityType.SYSTEM_EVENT)

        return ProtocolResult(
            protocol = StarkProtocolType.SECURE_PERIMETER,
            speechText = speech,
            summaryBadge = "PERIMETER SECURED",
            telemetryPoints = telemetryPoints
        )
    }

    private fun executePowerSurgeProtocol(
        bridge: AndroidBridge,
        repository: JarvisRepository,
        telemetry: com.example.jarvis.model.DeviceTelemetry
    ): ProtocolResult {
        CyberneticAudioEngine.playReactorSurge()
        bridge.vibrate(250L)

        val telemetryPoints = mutableListOf<String>()
        telemetryPoints.add("Arc Reactor Output: 100% MAXIMUM OVERCLOCK")
        telemetryPoints.add("Acoustic Resonator: Online")
        telemetryPoints.add("Neural Bus Frequency: Peak Operating Capacity")
        telemetryPoints.add("Battery Reserve: ${telemetry.batteryPercent}%")

        val speech = "Arc Reactor core overclocked to maximum yield, Sir. " +
                "All quantum neural channels and responsive relays operating at peak operational velocity. Systems primed."

        repository.logActivity("Stark Protocol", "Arc Reactor Overclock Surge", ActivityType.SYSTEM_EVENT)

        return ProtocolResult(
            protocol = StarkProtocolType.POWER_SURGE,
            speechText = speech,
            summaryBadge = "OVERCLOCK POWER SURGE",
            telemetryPoints = telemetryPoints
        )
    }
}
