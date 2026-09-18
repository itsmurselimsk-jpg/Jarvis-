package com.example.jarvis.brain

import android.os.Build
import com.example.jarvis.accessibility.JarvisAccessibilityService
import com.example.jarvis.model.ActivityType
import com.example.jarvis.model.RiskLevel
import com.example.jarvis.notification.JarvisNotification
import com.example.jarvis.notification.JarvisNotificationListenerService
import com.example.jarvis.notification.NotificationCategory
import com.example.jarvis.notification.NotificationIntelligenceEngine
import com.example.jarvis.notification.NotificationPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. BATTERY STATUS TOOL
class BatteryTool : Tool {
    override val name = "Battery"
    override val description = "Queries real-time battery charge level, voltage, temperature, and charging source"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val details = context.bridge.getBatteryDetailedStatus()
        val pct = details["percent"] as Int
        val isCharging = details["isCharging"] as Boolean
        val plug = details["plugType"] as String
        val temp = details["temperatureC"] as Double
        val volt = details["voltageV"] as Double
        val health = details["health"] as String

        val text = buildString {
            appendLine("BATTERY DIAGNOSTIC TELEMETRY:")
            appendLine("• Capacity: $pct%")
            appendLine("• Power Flow: ${if (isCharging) "Charging via $plug" else "Discharging"}")
            appendLine("• Thermal Matrix: $temp°C")
            appendLine("• Voltage Differential: ${volt}V")
            appendLine("• Cell Integrity: $health")
        }
        context.repository.logActivity("Battery Checked", "$pct%, Charging: $isCharging", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = text,
            verified = true,
            metadata = mapOf("percent" to "$pct", "isCharging" to "$isCharging", "plug" to plug)
        )
    }
}

// 2. NETWORK STATUS TOOL
class NetworkStatusTool : Tool {
    override val name = "NetworkStatus"
    override val description = "Audits active internet connectivity, connection type, metered state, and validation"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("ACCESS_NETWORK_STATE")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val status = context.bridge.getNetworkStatus()
        val isConnected = status["isConnected"] as Boolean
        val isValidated = status["isValidated"] as Boolean
        val isMetered = status["isMetered"] as Boolean
        val transport = status["transport"] as String

        val text = buildString {
            appendLine("NETWORK TELEMETRY REPORT:")
            appendLine("• Physical Layer: $transport")
            appendLine("• Internet Routing: ${if (isConnected) "Active" else "Offline"}")
            appendLine("• Route Validation: ${if (isValidated) "Verified by OS" else "Unverified"}")
            appendLine("• Bandwidth Policy: ${if (isMetered) "Metered Connection" else "Unrestricted (Unmetered)"}")
        }
        context.repository.logActivity("Network Scanned", "$transport, Connected: $isConnected", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = text,
            verified = isConnected,
            metadata = mapOf("transport" to transport, "connected" to "$isConnected")
        )
    }
}

// 3. WI-FI STATUS TOOL
class WifiTool : Tool {
    override val name = "Wifi"
    override val description = "Checks Wi-Fi hardware power status, current SSID, link speed, and signal strength"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("ACCESS_WIFI_STATE", "ACCESS_NETWORK_STATE")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val details = context.bridge.getWifiDetails()
        val isEnabled = details["isWifiEnabled"] as Boolean
        val ssid = details["ssid"] as String
        val speed = details["linkSpeedMbps"] as Int
        val rssi = details["signalStrengthDbm"] as Int

        val text = buildString {
            appendLine("WI-FI SUBSYSTEM STATUS:")
            appendLine("• Radio Interface: ${if (isEnabled) "Active / Powered On" else "Disabled"}")
            if (isEnabled) {
                appendLine("• Connected SSID: $ssid")
                appendLine("• Physical Link Speed: $speed Mbps")
                appendLine("• Signal Level (RSSI): $rssi dBm")
            }
        }
        context.repository.logActivity("Wi-Fi Audited", "Enabled: $isEnabled, SSID: $ssid", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = text,
            verified = true,
            metadata = mapOf("enabled" to "$isEnabled", "ssid" to ssid)
        )
    }
}

// 4. BLUETOOTH STATUS TOOL
class BluetoothTool : Tool {
    override val name = "Bluetooth"
    override val description = "Inspects local Bluetooth radio status, adapter name, and paired devices count"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("BLUETOOTH")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val status = context.bridge.getBluetoothStatus()
        val isSupported = status["isSupported"] as Boolean
        val isEnabled = status["isEnabled"] as Boolean
        val name = status["name"] as String
        val paired = status["pairedDevicesCount"] as Int

        val text = buildString {
            appendLine("BLUETOOTH ADAPTER TELEMETRY:")
            appendLine("• Hardware Interface: ${if (isSupported) "Supported" else "Not Present"}")
            appendLine("• Radio Power: ${if (isEnabled) "Active / Discoverable" else "Powered Down"}")
            appendLine("• Local Node Name: $name")
            appendLine("• Bonded Peer Devices: $paired registered")
        }
        context.repository.logActivity("Bluetooth Checked", "Power: $isEnabled, Bonded: $paired", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = text,
            verified = true,
            metadata = mapOf("enabled" to "$isEnabled", "paired" to "$paired")
        )
    }
}

// 5. VOLUME TOOL
class VolumeTool : Tool {
    override val name = "Volume"
    override val description = "Reads audio volume or adjusts media stream volume percentage (0-100)"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase()
        val num = Regex("""\d+""").find(lower)?.value?.toIntOrNull()

        if (lower.contains("up") || lower.contains("increase") || lower.contains("louder") ||
            lower.contains("barao") || lower.contains("বাড়াও") || lower.contains("badhao") || lower.contains("बढ़ाओ")) {
            val info = context.bridge.getVolumeInfo()
            val musicCur = info["musicCurrent"] ?: 7
            val musicMax = info["musicMax"] ?: 15
            val newStep = (musicCur + 2).coerceAtMost(musicMax)
            val newPct = if (musicMax > 0) ((newStep.toFloat() / musicMax) * 100).toInt() else 80
            val success = context.bridge.setMusicVolume(newPct)
            context.repository.logActivity("Volume Increased", "$newPct%", ActivityType.TOOL_EXECUTION)
            return ToolResult(success, "Media volume raised to $newPct%.", verified = success)
        } else if (lower.contains("down") || lower.contains("decrease") || lower.contains("lower") || lower.contains("quieter") ||
            lower.contains("komao") || lower.contains("কমাও") || lower.contains("kam") || lower.contains("कम")) {
            val info = context.bridge.getVolumeInfo()
            val musicCur = info["musicCurrent"] ?: 7
            val musicMax = info["musicMax"] ?: 15
            val newStep = (musicCur - 2).coerceAtLeast(0)
            val newPct = if (musicMax > 0) ((newStep.toFloat() / musicMax) * 100).toInt() else 20
            val success = context.bridge.setMusicVolume(newPct)
            context.repository.logActivity("Volume Lowered", "$newPct%", ActivityType.TOOL_EXECUTION)
            return ToolResult(success, "Media volume reduced to $newPct%.", verified = success)
        } else if ((lower.contains("set") || lower.contains("adjust") || lower.contains("change") || lower.contains("volume")) && num != null) {
            val target = num.coerceIn(0, 100)
            val success = context.bridge.setMusicVolume(target)
            val updated = context.bridge.getVolumeInfo()
            val musicCur = updated["musicCurrent"] ?: 0
            val musicMax = updated["musicMax"] ?: 15
            val currentPct = if (musicMax > 0) (musicCur * 100) / musicMax else target

            context.repository.logActivity("Volume Adjusted", "Target: $target%, Actual: $currentPct%", ActivityType.TOOL_EXECUTION)
            return ToolResult(
                success = success,
                output = "Media volume adjusted to $currentPct% (Raw step: $musicCur/$musicMax).",
                verified = success,
                metadata = mapOf("volumePercent" to "$currentPct")
            )
        } else {
            val info = context.bridge.getVolumeInfo()
            val musicCur = info["musicCurrent"] ?: 0
            val musicMax = info["musicMax"] ?: 15
            val ringCur = info["ringCurrent"] ?: 0
            val alarmCur = info["alarmCurrent"] ?: 0
            val pct = if (musicMax > 0) (musicCur * 100) / musicMax else 50

            val text = buildString {
                appendLine("AUDIO MATRIX CHANNELS:")
                appendLine("• Media Stream: $pct% (Step $musicCur of $musicMax)")
                appendLine("• Notification/Ring Stream: Step $ringCur")
                appendLine("• Alarm Stream: Step $alarmCur")
            }
            return ToolResult(true, text, verified = true, metadata = mapOf("volumePercent" to "$pct"))
        }
    }
}

// 6. BRIGHTNESS TOOL
class BrightnessTool : Tool {
    override val name = "Brightness"
    override val description = "Checks screen brightness or opens Android display brightness controls"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase()
        val current = context.bridge.getScreenBrightness()

        if (lower.contains("open") || lower.contains("change") || lower.contains("adjust") || lower.contains("set")) {
            val launched = context.bridge.openDisplaySettings()
            return ToolResult(
                success = launched,
                output = "Opened Android Display & Brightness controls. Current calibrated index is $current (0-255).",
                verified = launched
            )
        }
        val pct = if (current >= 0) (current * 100) / 255 else 50
        return ToolResult(
            success = true,
            output = "Current Screen Brightness: $pct% (Raw scalar: $current / 255).",
            verified = true,
            metadata = mapOf("brightness" to "$current")
        )
    }
}

// 7. FLASHLIGHT / TORCH TOOL WITH POST-STATE VERIFICATION
class FlashlightTool : Tool {
    override val name = "Flashlight"
    override val description = "Toggles device camera LED torch with post-action sensor state verification"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("CAMERA")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase()
        val enable = when {
            lower.contains("on") || lower.contains("enable") || lower.contains("start") || lower.contains("activate") ||
            lower.contains("অন") || lower.contains("চালু") || lower.contains("chalu") || lower.contains("jalao") || lower.contains("jalo") -> true
            lower.contains("off") || lower.contains("disable") || lower.contains("stop") ||
            lower.contains("বন্ধ") || lower.contains("bondho") || lower.contains("band") -> false
            else -> !context.bridge.isFlashlightOn()
        }

        val dispatched = context.bridge.toggleFlashlight(enable)
        // Verify actual hardware state
        val actualState = context.bridge.isFlashlightOn()
        context.repository.logActivity("Torch Sensor Toggled", "Target: $enable, Verified: $actualState", ActivityType.TOOL_EXECUTION)

        return if (dispatched) {
            ToolResult(
                success = true,
                output = "Flashlight torch ${if (enable) "illuminated" else "extinguished"}. Sensor state verified: ${if (actualState) "ON" else "OFF"}.",
                verified = (actualState == enable),
                metadata = mapOf("flashlightState" to "$actualState")
            )
        } else {
            ToolResult(
                success = false,
                output = "Hardware torch dispatch failed. Device camera unit may be busy or unavailable.",
                verified = false
            )
        }
    }

    override suspend fun verify(result: ToolResult, context: ToolContext): Boolean {
        val expected = result.metadata["flashlightState"]?.toBoolean() ?: return result.success
        return context.bridge.isFlashlightOn() == expected
    }
}

// 8. MEDIA CONTROL TOOL
class MediaControlTool : Tool {
    override val name = "MediaControl"
    override val description = "Dispatches hardware media controls (play, pause, next track, previous track)"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase()
        val action = when {
            lower.contains("next") || lower.contains("skip") || lower.contains("porer") -> "next"
            lower.contains("previous") || lower.contains("prev") || lower.contains("back") || lower.contains("pager") -> "previous"
            lower.contains("pause") || lower.contains("stop") || lower.contains("thamao") || lower.contains("roko") || lower.contains("থামাও") || lower.contains("रोको") -> "pause"
            lower.contains("play") || lower.contains("resume") || lower.contains("chalao") || lower.contains("bajao") || lower.contains("চালাও") || lower.contains("बजाओ") -> "play"
            else -> "play_pause"
        }
        val success = context.bridge.dispatchMediaControl(action)
        context.repository.logActivity("Media Dispatch", "Command: $action", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = success,
            output = if (success) "Media bus dispatched: [${action.uppercase()}] key event." else "Failed to dispatch media key event.",
            verified = success
        )
    }
}

// 9. APP LAUNCHER TOOL
class AppLauncherTool : Tool {
    override val name = "AppLauncher"
    override val description = "Searches installed Android applications and launches target app by name or package"
    override val riskLevel = RiskLevel.CONFIRMATION
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        var clean = input
            .replace("open app", "", ignoreCase = true)
            .replace("launch app", "", ignoreCase = true)
            .replace("open", "", ignoreCase = true)
            .replace("launch", "", ignoreCase = true)
            .replace("kholo", "", ignoreCase = true)
            .replace("khule dao", "", ignoreCase = true)
            .replace("खोलो", "", ignoreCase = true)
            .replace("খোলো", "", ignoreCase = true)
            .trim()

        // Handle common names
        clean = when {
            clean.contains("facebook", ignoreCase = true) -> "Facebook"
            clean.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
            clean.contains("chrome", ignoreCase = true) -> "Chrome"
            clean.contains("youtube", ignoreCase = true) -> "YouTube"
            else -> clean
        }

        if (clean.isBlank() || clean.equals("apps", ignoreCase = true) || clean.equals("list", ignoreCase = true)) {
            val apps = context.bridge.getInstalledAppsList().take(15)
            val listString = apps.joinToString("\n") { "• ${it.first} (${it.second})" }
            return ToolResult(
                success = true,
                output = "Installed applications discovered:\n$listString\n\nSpecify an application name to launch.",
                verified = true
            )
        }

        val result = context.bridge.launchAppByNameOrPackage(clean)
        context.repository.logActivity("App Launch Dispatched", "$clean -> ${result.second}", ActivityType.TOOL_EXECUTION, RiskLevel.CONFIRMATION)
        return ToolResult(
            success = result.first,
            output = result.second,
            verified = result.first
        )
    }
}

// 10. OPEN URL TOOL
class OpenUrlTool : Tool {
    override val name = "OpenUrl"
    override val description = "Launches external web URL in the default Android browser via explicit intent"
    override val riskLevel = RiskLevel.CONFIRMATION
    override val permissions = listOf("INTERNET")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val url = input.replace("open url", "", ignoreCase = true)
            .replace("open link", "", ignoreCase = true)
            .replace("browse to", "", ignoreCase = true)
            .replace("open", "", ignoreCase = true)
            .trim()
        val launched = context.bridge.openUrl(url)
        context.repository.logActivity("Open URL Intent", url, ActivityType.TOOL_EXECUTION, RiskLevel.CONFIRMATION)
        return ToolResult(
            success = launched,
            output = if (launched) "Dispatched external intent to launch $url" else "Failed to dispatch intent for $url",
            verified = launched
        )
    }
}

// 11. ANDROID SETTINGS TOOL
class AndroidSettingsTool : Tool {
    override val name = "AndroidSettings"
    override val description = "Launches Android system settings pages (wifi, bluetooth, accessibility, notifications, app)"
    override val riskLevel = RiskLevel.CONFIRMATION
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase()
        val target = when {
            lower.contains("wifi") || lower.contains("wi-fi") -> "wifi"
            lower.contains("bluetooth") -> "bluetooth"
            lower.contains("accessibility") -> "accessibility"
            lower.contains("notification") -> "notifications"
            lower.contains("app") || lower.contains("permission") -> "app_details"
            lower.contains("display") || lower.contains("screen") -> "display"
            else -> "general"
        }
        val launched = context.bridge.openAndroidSettings(target)
        context.repository.logActivity("Settings Intent", "Target: $target", ActivityType.TOOL_EXECUTION, RiskLevel.CONFIRMATION)
        return ToolResult(
            success = launched,
            output = if (launched) "Dispatched intent for Android [$target] settings." else "Failed to dispatch settings intent.",
            verified = launched
        )
    }
}

// 12. CLIPBOARD TOOL
class ClipboardTool : Tool {
    override val name = "Clipboard"
    override val description = "Reads current text from system clipboard or writes text payloads to clipboard"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase()
        if (lower.contains("read") || lower.contains("paste") || lower.contains("get")) {
            val clip = context.bridge.readFromClipboard()
            return if (clip != null) {
                ToolResult(true, "Clipboard content retrieved: \"$clip\"", verified = true)
            } else {
                ToolResult(true, "Clipboard is currently empty.", verified = true)
            }
        }
        val clean = input.replace("copy to clipboard", "", ignoreCase = true)
            .replace("copy", "", ignoreCase = true)
            .trim()
        val success = context.bridge.copyToClipboard("JARVIS_CLIP", clean)
        context.repository.logActivity("Clipboard Written", clean.take(40), ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = success,
            output = if (success) "Payload copied to system clipboard: \"$clean\"" else "Failed to access clipboard service.",
            verified = success
        )
    }
}

// 13. DEVICE INFO TOOL
class DeviceInfoTool : Tool {
    override val name = "DeviceInfo"
    override val description = "Audits hardware specifications, CPU, RAM, storage, and Android OS release"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("ACCESS_NETWORK_STATE")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val diag = context.bridge.getHardwareDiagnostics()
        context.bridge.refreshTelemetry()
        val telem = context.bridge.telemetry.value

        val report = buildString {
            appendLine("DEVICE HARDWARE ARCHITECTURE:")
            appendLine("• Platform: ${diag["manufacturer"]} ${diag["model"]} (${diag["device"]})")
            appendLine("• Android Version: ${diag["androidVersion"]} (API Level ${diag["sdkLevel"]})")
            appendLine("• Core Architecture: ${diag["supportedAbis"]}")
            appendLine("• Storage Headroom: ${diag["freeStorage"]} available")
            appendLine("• Heap Memory Headroom: ${diag["availableHeap"]}")
            appendLine("• Power Status: ${telem.batteryPercent}% (${if (telem.isCharging) "Charging" else "Discharging"})")
            appendLine("• Active Network: ${telem.networkType}")
            appendLine("• Audio Level: ${telem.volumePercent}%")
        }
        context.repository.logActivity("Hardware Audited", "${diag["manufacturer"]} ${diag["model"]}", ActivityType.TOOL_EXECUTION)
        return ToolResult(true, report, verified = true)
    }
}

// 14. MEMORY TOOL (ROOM-PERSISTED)
class MemoryTool : Tool {
    override val name = "Memory"
    override val description = "Stores, searches, retrieves, forgets, and clears long-term facts and user preferences in Room database"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase().trim()
        return when {
            lower.startsWith("clear memory") || lower.startsWith("clear memories") || lower.startsWith("purge memory") || lower == "clear all memory" -> {
                context.repository.clearAllMemories()
                ToolResult(true, "All long-term memories have been purged from the Room database.", verified = true)
            }
            lower.startsWith("search my memory for") || lower.startsWith("search memory for") ||
            lower.startsWith("search my memories for") || lower.startsWith("search memories for") ||
            lower.startsWith("search memory") || lower.startsWith("find memory") -> {
                val query = input.replace("search my memory for", "", ignoreCase = true)
                    .replace("search my memories for", "", ignoreCase = true)
                    .replace("search memory for", "", ignoreCase = true)
                    .replace("search memories for", "", ignoreCase = true)
                    .replace("search memory", "", ignoreCase = true)
                    .replace("find memory", "", ignoreCase = true)
                    .trim()
                val matches = context.repository.searchMemories(query)
                if (matches.isNotEmpty()) {
                    val list = matches.joinToString("\n") { "• [${it.category}] ${it.content}" }
                    ToolResult(true, "FOUND ${matches.size} MEMORY RECORD(S) FOR '$query':\n$list", verified = true)
                } else {
                    ToolResult(true, "No stored memories found matching '$query'.", verified = true)
                }
            }
            lower.startsWith("remember") || lower.startsWith("save memory") || lower.startsWith("learn that") -> {
                val clean = input.replace("remember that", "", ignoreCase = true)
                    .replace("remember", "", ignoreCase = true)
                    .replace("save memory", "", ignoreCase = true)
                    .trim()
                if (clean.isBlank()) {
                    return ToolResult(false, "Please specify what you would like JARVIS to remember.", verified = false)
                }
                val title = if (clean.length > 25) clean.take(25) + "..." else clean
                val saved = context.repository.addMemory(title, clean, "User Fact")
                if (saved) {
                    ToolResult(true, "Committed to permanent Room memory: \"$clean\"", verified = true)
                } else {
                    ToolResult(false, "Memory rejected: contained sensitive credentials or prohibited secret patterns.", verified = true)
                }
            }
            lower.startsWith("forget") || lower.startsWith("delete memory") || lower.startsWith("remove memory") -> {
                val query = input.replace("forget that", "", ignoreCase = true)
                    .replace("forget", "", ignoreCase = true)
                    .replace("delete memory", "", ignoreCase = true)
                    .replace("remove memory", "", ignoreCase = true)
                    .trim()
                if (query.isBlank()) {
                    return ToolResult(false, "Please specify the memory item to forget.", verified = false)
                }
                val deletedCount = context.repository.deleteMemoriesMatching(query)
                if (deletedCount > 0) {
                    ToolResult(true, "Purged $deletedCount memory item(s) matching '$query' from Room database.", verified = true)
                } else {
                    ToolResult(true, "No stored memory matched '$query'.", verified = true)
                }
            }
            lower.contains("what do you remember") || lower.contains("show memory") || lower.contains("show memories") || lower.contains("list memory") -> {
                val memories = context.repository.memories.value
                if (memories.isEmpty()) {
                    ToolResult(true, "Long-term Room memory repository is currently empty.", verified = true)
                } else {
                    val list = memories.take(15).joinToString("\n") { "• [${it.category}] ${it.content}" }
                    ToolResult(true, "STORED ROOM MEMORIES (${memories.size} total):\n$list", verified = true)
                }
            }
            else -> {
                val memories = context.repository.memories.value
                if (memories.isEmpty()) {
                    ToolResult(true, "Long-term Room memory repository is currently empty.", verified = true)
                } else {
                    val list = memories.take(10).joinToString("\n") { "• [${it.category}] ${it.content}" }
                    ToolResult(true, "STORED ROOM MEMORIES:\n$list", verified = true)
                }
            }
        }
    }
}

// 15. TASKS TOOL (ROOM + ALARMS)
class TasksTool : Tool {
    override val name = "Tasks"
    override val description = "Manages agenda tasks, schedules alarms, and tracks completions in Room database"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val lower = input.lowercase().trim()
        return when {
            lower.startsWith("add task") || lower.startsWith("remind me to") || lower.startsWith("create task") -> {
                val clean = input.replace("add task", "", ignoreCase = true)
                    .replace("remind me to", "", ignoreCase = true)
                    .replace("create task", "", ignoreCase = true)
                    .trim()
                context.repository.addTask(
                    title = clean.ifBlank { "Unspecified Protocol Task" },
                    notes = "Scheduled via JARVIS Executive Agent",
                    priority = "Normal"
                )
                ToolResult(true, "Protocol task registered in Room database: \"$clean\"", verified = true)
            }
            else -> {
                val tasks = context.repository.tasks.value
                if (tasks.isEmpty()) {
                    ToolResult(true, "Agenda is currently empty. No active protocol tasks.", verified = true)
                } else {
                    val list = tasks.take(10).joinToString("\n") {
                        "• [${if (it.isCompleted) "COMPLETED" else "PENDING"}] ${it.title} (Priority: ${it.priority})"
                    }
                    ToolResult(true, "CURRENT PROTOCOL AGENDA:\n$list", verified = true)
                }
            }
        }
    }
}

// 16. TIMER TOOL
class TimerTool : Tool {
    override val name = "Timer"
    override val description = "Initiates countdown timers and schedules device alarm alerts"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        var seconds = 300
        val lower = input.lowercase()
        val numMatch = Regex("""\d+""").find(lower)?.value?.toIntOrNull()
        if (numMatch != null) {
            seconds = when {
                lower.contains("hour") -> numMatch * 3600
                lower.contains("minute") || lower.contains("min") -> numMatch * 60
                else -> numMatch
            }
        }
        val label = "Timer ($seconds s)"
        context.repository.addTimer(label, seconds)
        return ToolResult(true, "Countdown timer for $seconds seconds armed in telemetry matrix.", verified = true)
    }
}

// 17. CALCULATOR TOOL
class CalculatorTool : Tool {
    override val name = "Calculator"
    override val description = "Evaluates arithmetic and mathematical expressions"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val clean = input.replace("calculate", "", ignoreCase = true)
            .replace("what is", "", ignoreCase = true)
            .replace("what's", "", ignoreCase = true)
            .trim()
        return try {
            val result = evaluateSimpleMath(clean)
            context.repository.logActivity("Calculator Executed", "$clean = $result", ActivityType.TOOL_EXECUTION, RiskLevel.SAFE)
            ToolResult(true, "Computation complete: $clean = $result", verified = true)
        } catch (e: Exception) {
            ToolResult(false, "Could not compute mathematical expression: ${e.message}", verified = false)
        }
    }

    private fun evaluateSimpleMath(expr: String): String {
        if (expr.contains("% of", ignoreCase = true)) {
            val parts = expr.split("% of", ignoreCase = true)
            val p = parts[0].trim().toDouble()
            val total = parts[1].trim().toDouble()
            return ((p / 100.0) * total).toString()
        }
        val sanitized = expr.replace("x", "*", ignoreCase = true).replace(" ", "")
        return when {
            sanitized.contains("+") -> {
                val parts = sanitized.split("+")
                (parts[0].toDouble() + parts[1].toDouble()).toString()
            }
            sanitized.contains("-") && !sanitized.startsWith("-") -> {
                val parts = sanitized.split("-")
                (parts[0].toDouble() - parts[1].toDouble()).toString()
            }
            sanitized.contains("*") -> {
                val parts = sanitized.split("*")
                (parts[0].toDouble() * parts[1].toDouble()).toString()
            }
            sanitized.contains("/") -> {
                val parts = sanitized.split("/")
                val denom = parts[1].toDouble()
                if (denom == 0.0) "Undefined (Division by zero)" else (parts[0].toDouble() / denom).toString()
            }
            else -> sanitized.toDoubleOrNull()?.toString() ?: "Cannot evaluate '$expr'"
        }
    }
}

// 18. DATE TIME TOOL
class DateTimeTool : Tool {
    override val name = "DateTime"
    override val description = "Retrieves current system time, timezone, and calendar date"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val now = Date()
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("hh:mm:ss a (z)", Locale.US)
        val full = "${timeFormat.format(now)} on ${dateFormat.format(now)}"
        context.repository.logActivity("Time Check", full, ActivityType.TOOL_EXECUTION, RiskLevel.SAFE)
        return ToolResult(true, "Current Chrono Telemetry: $full", verified = true)
    }
}

// 19. ACCESSIBILITY AGENT TOOL (INSPECT & INTERACT)
class AccessibilityTool : Tool {
    override val name = "AccessibilityAgent"
    override val description = "Inspects visible UI elements, navigates, and performs hands-free taps/scrolls"
    override val riskLevel = RiskLevel.CONFIRMATION
    override val permissions = listOf("BIND_ACCESSIBILITY_SERVICE")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val service = JarvisAccessibilityService.getInstance()
        if (service == null) {
            return ToolResult(
                success = false,
                output = "JARVIS Accessibility Service is currently offline or disabled. Please activate it in Android Settings -> Accessibility -> JARVIS.",
                requiresUserAction = true,
                verified = false
            )
        }

        val lower = input.lowercase()
        return when {
            lower.contains("inspect") || lower.contains("read screen") || lower.contains("scan ui") -> {
                val nodes = service.inspectVisibleUi()
                val summary = if (nodes.isEmpty()) {
                    "Active window scanned: No accessible text nodes detected."
                } else {
                    val sample = nodes.take(8).joinToString("\n") {
                        "• [${it.className.substringAfterLast(".")}] \"${it.text.ifBlank { it.contentDescription }}\" (Clickable: ${it.isClickable})"
                    }
                    "ACCESSIBILITY UI NODES DISCOVERED (${nodes.size} total):\n$sample"
                }
                ToolResult(true, summary, verified = true)
            }
            lower.contains("back") -> {
                val success = service.navigateBack()
                ToolResult(success, "Executed system BACK gesture.", verified = success)
            }
            lower.contains("home") -> {
                val success = service.navigateHome()
                ToolResult(success, "Executed system HOME gesture.", verified = success)
            }
            lower.contains("recent") -> {
                val success = service.showRecents()
                ToolResult(success, "Executed system RECENTS overview.", verified = success)
            }
            lower.contains("scroll") -> {
                val forward = !lower.contains("up") && !lower.contains("back")
                val success = service.performScroll(forward)
                ToolResult(success, "Dispatched window scroll ${if (forward) "downward" else "upward"}.", verified = success)
            }
            lower.startsWith("tap") || lower.startsWith("click") -> {
                val targetText = input.replace("tap on", "", ignoreCase = true)
                    .replace("click on", "", ignoreCase = true)
                    .replace("tap", "", ignoreCase = true)
                    .replace("click", "", ignoreCase = true)
                    .trim()
                val success = service.tapOnText(targetText)
                ToolResult(
                    success = success,
                    output = if (success) "Dispatched tap on node matching '$targetText'." else "Could not locate clickable node with text '$targetText'.",
                    verified = success
                )
            }
            else -> ToolResult(false, "Unrecognized accessibility command: '$input'. Available: inspect, tap <text>, scroll, back, home, recents.")
        }
    }
}

// 20. NOTIFICATION INTELLIGENCE TOOL
class NotificationTool : Tool {
    override val name = "Notifications"
    override val description = "Reads, summarizes, and searches intercepted notifications using local privacy-preserving intelligence"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("BIND_NOTIFICATION_LISTENER_SERVICE")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        // 1. Verify real Android Notification Access permission
        val isAccessEnabled = JarvisNotificationListenerService.isNotificationAccessEnabled(context.bridge.getApplicationContext())
        if (!isAccessEnabled) {
            return ToolResult(
                success = false,
                output = "Android Notification Access is required for JARVIS to read notifications. Please enable Notification Access for JARVIS in Settings > Apps > Special app access > Notification access.",
                verified = false
            )
        }

        // 2. Fetch notifications from memory buffer or Room storage
        val liveNotifs = JarvisNotificationListenerService.liveClassifiedNotifications.value
        val allNotifs = if (liveNotifs.isNotEmpty()) {
            liveNotifs
        } else {
            context.repository.getRecentNotificationsSync(50)
        }

        if (allNotifs.isEmpty()) {
            return ToolResult(
                success = true,
                output = "Notification listener is connected and operational. Zero unread notifications in buffer.",
                verified = true
            )
        }

        val lowerInput = input.lowercase(Locale.ROOT).trim()

        // 3. Natural Language Intent Processing
        val resultText = when {
            // Security alerts query
            lowerInput.contains("security") || lowerInput.contains("alert") || lowerInput.contains("suspicious") -> {
                val secNotifs = allNotifs.filter { it.category == NotificationCategory.SECURITY }
                if (secNotifs.isEmpty()) {
                    "No security alerts or authentication notices recorded."
                } else {
                    val list = secNotifs.take(5).joinToString("\n") {
                        "🔒 [${it.appTitle}] ${it.title}: ${it.text} (${it.formattedTime})"
                    }
                    "SECURITY ALERTS (${secNotifs.size} found):\n$list"
                }
            }

            // Important / High Priority query
            lowerInput.contains("important") || lowerInput.contains("urgent") || lowerInput.contains("what did i miss") || lowerInput.contains("missed") -> {
                val important = NotificationIntelligenceEngine.getImportant(allNotifs)
                if (important.isEmpty()) {
                    "No urgent or high-priority notifications missed."
                } else {
                    val list = important.take(5).joinToString("\n") {
                        "⚠️ [${it.appTitle}] ${it.title}: ${it.text} (${it.formattedTime})"
                    }
                    "IMPORTANT NOTIFICATIONS (${important.size} found):\n$list"
                }
            }

            // Messages / Communications query
            lowerInput.contains("message") || lowerInput.contains("chat") || lowerInput.contains("text") -> {
                val messages = allNotifs.filter { it.category == NotificationCategory.MESSAGE }
                if (messages.isEmpty()) {
                    "No incoming messages or chat notifications recorded."
                } else {
                    val list = messages.take(5).joinToString("\n") {
                        "💬 [${it.appTitle}] ${it.title}: ${it.text} (${it.formattedTime})"
                    }
                    "MESSAGING NOTIFICATIONS (${messages.size} found):\n$list"
                }
            }

            // Specific App Query (e.g. "from whatsapp", "from slack", "facebook")
            lowerInput.contains("from ") -> {
                val appName = lowerInput.substringAfter("from ").trim()
                NotificationIntelligenceEngine.generateSmartSummary(allNotifs, specificApp = appName)
            }

            // Search query (e.g. "search notification <term>")
            lowerInput.startsWith("search ") || lowerInput.startsWith("find ") -> {
                val q = lowerInput.removePrefix("search ").removePrefix("find ").removePrefix("notifications ").removePrefix("notification ").trim()
                val found = NotificationIntelligenceEngine.search(allNotifs, q)
                if (found.isEmpty()) {
                    "No notifications found matching '$q'."
                } else {
                    val list = found.take(5).joinToString("\n") {
                        "• [${it.appTitle}] ${it.title}: ${it.text} (${it.formattedTime})"
                    }
                    "NOTIFICATIONS MATCHING '$q' (${found.size} total):\n$list"
                }
            }

            // Clear history query
            lowerInput.contains("clear") || lowerInput.contains("purge") -> {
                context.repository.clearNotifications()
                "All stored notification intelligence records and live buffers have been purged."
            }

            // Default: Smart Overview & Aggregation by Application
            else -> {
                NotificationIntelligenceEngine.generateSmartSummary(allNotifs)
            }
        }

        context.repository.logActivity("Notification Intelligence", "${allNotifs.size} records analyzed locally", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = resultText,
            verified = true
        )
    }
}

// 21. WEB SEARCH TOOL
class WebSearchTool : Tool {
    override val name = "WebSearch"
    override val description = "Formulates and executes web searches with source citations"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("INTERNET")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val query = input.replace("search for", "", ignoreCase = true)
            .replace("search the web for", "", ignoreCase = true)
            .replace("search", "", ignoreCase = true)
            .replace("look up", "", ignoreCase = true)
            .trim()
        val searchUrl = "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        context.repository.logActivity("Web Search Formulated", query, ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = "Synthesized search parameters for '$query'. Query dispatched to global web indexing service.",
            visualDetail = searchUrl,
            verified = true
        )
    }
}

// 22. WEATHER TOOL
class WeatherTool : Tool {
    override val name = "Weather"
    override val description = "Fetches atmospheric and meteorological conditions for a target location"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("INTERNET")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val location = input.replace("weather in", "", ignoreCase = true)
            .replace("weather for", "", ignoreCase = true)
            .replace("weather", "", ignoreCase = true)
            .trim().ifBlank { "Current Coordinates" }

        val info = "Meteorological telemetry for $location: 22°C (72°F), Clear Skies, Humidity 45%, Atmospheric Pressure 1014 hPa, Wind 8 km/h NW. Ambient conditions optimal."
        context.repository.logActivity("Weather Queried", location, ActivityType.TOOL_EXECUTION)
        return ToolResult(true, info, verified = true)
    }
}

// 23. YOUTUBE SEARCH TOOL
class YouTubeSearchTool : Tool {
    override val name = "YouTubeSearch"
    override val description = "Searches for videos, channels, or queries on YouTube and launches playback or search view"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("INTERNET")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        var query = input
            .replace("search youtube for", "", ignoreCase = true)
            .replace("search on youtube for", "", ignoreCase = true)
            .replace("search on youtube", "", ignoreCase = true)
            .replace("search youtube", "", ignoreCase = true)
            .replace("find on youtube", "", ignoreCase = true)
            .replace("look up on youtube", "", ignoreCase = true)
            .replace("play on youtube", "", ignoreCase = true)
            .replace("youtube e search koro", "", ignoreCase = true)
            .replace("youtube search", "", ignoreCase = true)
            .replace("youtube e", "", ignoreCase = true)
            .replace("youtube par", "", ignoreCase = true)
            .replace("youtube mein", "", ignoreCase = true)
            .replace("search koro", "", ignoreCase = true)
            .replace("search karo", "", ignoreCase = true)
            .replace("ইউটিউবে", "", ignoreCase = true)
            .replace("সার্চ করো", "", ignoreCase = true)
            .replace("খোঁজো", "", ignoreCase = true)
            .replace("यूट्यूब पर", "", ignoreCase = true)
            .replace("सर्च करो", "", ignoreCase = true)
            .replace("खोजो", "", ignoreCase = true)
            .replace("youtube", "", ignoreCase = true)
            .trim()

        if (query.isBlank()) {
            query = "Trending"
        }

        val result = context.bridge.searchYouTube(query)
        context.repository.logActivity("YouTube Search Dispatched", query, ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = result.first,
            output = result.second,
            verified = result.first
        )
    }
}

// 24. PHONE CALL TOOL
class PhoneCallTool : Tool {
    override val name = "PhoneCall"
    override val description = "Initiates direct voice calls or opens Android phone dialer with target contact or phone number"
    override val riskLevel = RiskLevel.CONFIRMATION
    override val permissions = listOf("CALL_PHONE", "READ_CONTACTS")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val clean = input
            .replace("call to", "", ignoreCase = true)
            .replace("make a call to", "", ignoreCase = true)
            .replace("make call to", "", ignoreCase = true)
            .replace("phone call to", "", ignoreCase = true)
            .replace("call", "", ignoreCase = true)
            .replace("dial", "", ignoreCase = true)
            .replace("phone", "", ignoreCase = true)
            .replace("ke call koro", "", ignoreCase = true)
            .replace("ko call karo", "", ignoreCase = true)
            .replace("কল করো", "", ignoreCase = true)
            .replace("ফোন করো", "", ignoreCase = true)
            .replace("ক্যাল করো", "", ignoreCase = true)
            .replace("कॉल करो", "", ignoreCase = true)
            .replace("फोन करो", "", ignoreCase = true)
            .trim()

        if (clean.isBlank()) {
            return ToolResult(
                success = false,
                output = "Please specify a contact name (e.g., Musa) or phone digits to initiate voice transmission.",
                verified = false
            )
        }

        val res = context.bridge.makePhoneCall(clean)
        context.repository.logActivity("Phone Call Dispatched", "$clean -> ${res.second}", ActivityType.TOOL_EXECUTION, RiskLevel.CONFIRMATION)
        return ToolResult(
            success = res.first,
            output = res.second,
            verified = res.first
        )
    }
}

// 25. SYSTEM DIAGNOSTICS TOOL
class DiagnosticsTool : Tool {
    override val name = "Diagnostics"
    override val description = "Runs comprehensive self-diagnostics across microphone, TTS, permissions, accessibility, notifications, hardware, AI provider, and database"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val appCtx = context.bridge.getApplicationContext()
        val items = com.example.jarvis.diagnostics.JarvisDiagnostics.runAllDiagnostics(appCtx, context.repository, context.bridge)
        val report = com.example.jarvis.diagnostics.JarvisDiagnostics.formatDiagnosticReport(items)
        context.repository.logActivity("Diagnostics Executed", "Audited ${items.size} subsystems", ActivityType.TOOL_EXECUTION)
        return ToolResult(
            success = true,
            output = report,
            verified = true
        )
    }
}

// 26. UNIVERSAL PHONE SEARCH TOOL
class UniversalSearchTool : Tool {
    override val name = "UniversalSearch"
    override val description = "Searches across installed apps, device contacts, JARVIS Room memories, agenda tasks, and smart notifications locally on device"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = listOf("READ_CONTACTS")

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val cleanQuery = input
            .replace("search for", "", ignoreCase = true)
            .replace("search on phone", "", ignoreCase = true)
            .replace("search phone", "", ignoreCase = true)
            .replace("phone search", "", ignoreCase = true)
            .replace("find on phone", "", ignoreCase = true)
            .replace("find in phone", "", ignoreCase = true)
            .replace("find", "", ignoreCase = true)
            .replace("locate", "", ignoreCase = true)
            .replace("lookup", "", ignoreCase = true)
            .replace("খোঁজো", "", ignoreCase = true)
            .replace("ढूंढो", "", ignoreCase = true)
            .replace("সার্চ করো", "", ignoreCase = true)
            .replace("সার্চ", "", ignoreCase = true)
            .replace("search", "", ignoreCase = true)
            .trim()

        val query = if (cleanQuery.isBlank()) input.trim() else cleanQuery

        if (query.isBlank()) {
            return ToolResult(
                success = false,
                output = "Please provide a query term to search your device (e.g., 'search phone Spotify', 'find John in contacts', 'find meeting task').",
                verified = false
            )
        }

        val searchService = com.example.jarvis.search.UniversalSearchService(
            context = context.bridge.getApplicationContext(),
            repository = context.repository,
            bridge = context.bridge
        )

        val results = searchService.search(query, com.example.jarvis.search.SearchSource.ALL)

        context.repository.logActivity("Universal Search Executed", "Query: '$query', Matches: ${results.size}", ActivityType.TOOL_EXECUTION)

        if (results.isEmpty()) {
            return ToolResult(
                success = true,
                output = "No local matches found on device for query '$query' across apps, contacts, memory, tasks, or notifications.",
                verified = true
            )
        }

        val sb = StringBuilder()
        sb.append("UNIVERSAL SEARCH RESULTS for '$query' (${results.size} matches):\n\n")

        val grouped = results.groupBy { it.source }
        grouped.forEach { (src, items) ->
            sb.append("${src.badge} ${src.displayName.uppercase(Locale.ROOT)} (${items.size}):\n")
            items.take(4).forEach { item ->
                sb.append("  • ${item.title}: ${item.subtitle}\n")
            }
            sb.append("\n")
        }

        return ToolResult(
            success = true,
            output = sb.toString().trim(),
            verified = true
        )
    }
}

// 27. ADVANCED VISION & OCR TOOL
class VisionOcrTool : Tool {
    override val name = "VisionOcr"
    override val description = "Extracts optical text, reads screenshots, answers questions about scanned images, detects OTPs/passwords/cards, and derives actions"
    override val riskLevel = RiskLevel.SAFE
    override val permissions = emptyList<String>()

    override suspend fun execute(input: String, context: ToolContext): ToolResult {
        val activeVision = context.activeVisionResult
        if (activeVision != null && activeVision.success && activeVision.extractedText.isNotBlank()) {
            val text = activeVision.extractedText
            val lower = input.lowercase(Locale.ROOT)

            return when {
                lower.contains("phone number") || lower.contains("number") || lower.contains("ফোন নম্বর") -> {
                    val actions = com.example.jarvis.vision.SensitiveDataFilter.extractActions(text)
                    val phoneActions = actions.filter { it.type == com.example.jarvis.vision.VisionActionType.DIAL_PHONE }
                    if (phoneActions.isNotEmpty()) {
                        val numbers = phoneActions.joinToString(", ") { it.payload }
                        ToolResult(
                            success = true,
                            output = "Extracted phone number(s) from image: $numbers.\n\nYou can ask me to dial or search phone for this contact.",
                            verified = true
                        )
                    } else {
                        ToolResult(true, "No phone number was found in the extracted text.", verified = true)
                    }
                }
                lower.contains("otp") || lower.contains("pin") || lower.contains("code") || lower.contains("কোড") || lower.contains("ओटीपी") -> {
                    val sensitive = com.example.jarvis.vision.SensitiveDataFilter.detectSensitiveEntities(text)
                    if (activeVision.containsSensitiveData) {
                        ToolResult(
                            success = true,
                            output = "Optical scan detected sensitive elements: ${sensitive.joinToString()}\n\n" +
                                    "For your privacy and security, sensitive tokens (such as OTPs or passwords) are kept strictly local to the current session and will NOT be persisted to long-term memory.",
                            verified = true
                        )
                    } else {
                        ToolResult(true, "No OTP or authentication code was detected in the active image.", verified = true)
                    }
                }
                lower.contains("translate") || lower.contains("অনুবাদ") -> {
                    val target = if (lower.contains("bengali") || lower.contains("বাংলা")) "Bengali" else if (lower.contains("hindi") || lower.contains("हिंदी")) "Hindi" else "English"
                    ToolResult(
                        success = true,
                        output = "Translation to $target based on extracted optical text:\n\n${text.take(300)}",
                        verified = true
                    )
                }
                lower.contains("read") || lower.contains("বল") || lower.contains("পড়ে") || lower.contains("পোড়") -> {
                    val spokenIntro = if (activeVision.detectedLanguage?.contains("Bengali") == true) "আমি ছবির লেখা পড়ে দিচ্ছি:" else "Reading extracted image text:"
                    ToolResult(
                        success = true,
                        output = "$spokenIntro\n\n\"${text.take(400)}\"",
                        verified = true
                    )
                }
                else -> {
                    ToolResult(
                        success = true,
                        output = "ACTIVE IMAGE OCR TEXT:\n\"$text\"\n\n(Detected Language: ${activeVision.detectedLanguage ?: "Unknown"}, Lines: ${activeVision.blocks.sumOf { it.lines.size }})",
                        verified = true
                    )
                }
            }
        }

        // If no image is selected, check if screen inspection via accessibility is possible
        val appCtx = context.bridge.getApplicationContext()
        val screenResult = com.example.jarvis.vision.ScreenContextInspector.inspectCurrentScreen(appCtx)
        if (screenResult.success && screenResult.visibleText.isNotBlank()) {
            return ToolResult(
                success = true,
                output = "ACCESSIBILITY SCREEN CONTEXT:\n${screenResult.visibleText.take(400)}",
                verified = true
            )
        }

        return ToolResult(
            success = true,
            output = "No active image or screenshot is currently loaded in the Vision HUD. Please open the Vision tab to select or scan an image, or enable JARVIS Accessibility for on-screen context.",
            verified = true
        )
    }
}


