package com.example.jarvis.model

import java.util.UUID

enum class JarvisState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class RiskLevel {
    SAFE,
    CONFIRMATION,
    RESTRICTED
}

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCallName: String? = null,
    val toolRiskLevel: RiskLevel? = null,
    val isStreaming: Boolean = false
)

data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

data class JarvisTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "Normal", // Low, Normal, High
    val isRecurring: Boolean = false,
    val dueDate: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class JarvisTimer(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = false
)

enum class ActivityType {
    TOOL_EXECUTION,
    VOICE_EVENT,
    SAFETY_ALERT,
    TASK_EVENT,
    SYSTEM_EVENT
}

data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val detail: String,
    val type: ActivityType,
    val riskLevel: RiskLevel = RiskLevel.SAFE,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val networkType: String = "Online",
    val volumePercent: Int = 75,
    val isFlashlightOn: Boolean = false,
    val memoryAvailableMB: Long = 2048,
    val currentTimeString: String = ""
)

data class SafetyRequest(
    val id: String = UUID.randomUUID().toString(),
    val toolName: String,
    val actionDescription: String,
    val reason: String = "External interaction or state alteration requires confirmation",
    val riskLevel: RiskLevel,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)

enum class AIProviderType {
    GEMINI,
    OPENAI_COMPATIBLE,
    LOCAL_NEURAL_BRAIN
}

data class ProviderSettings(
    val providerType: AIProviderType = AIProviderType.GEMINI,
    val customApiKey: String = "",
    val customEndpoint: String = "https://api.openai.com/v1",
    val selectedModel: String = "gemini-3.5-flash",
    val systemPrompt: String = "You are JARVIS, an ultra-intelligent, sophisticated, polite personal AI operating layer. Address user as Sir or Ma'am. Be concise, precise, proactive, and futuristic.",
    val temperature: Float = 0.7f,
    val autoSpeakResponses: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val voiceProfileName: String = "JARVIS Natural",
    val languageCode: String = "auto",
    val continuousWakeEnabled: Boolean = true,
    val continuousConversationEnabled: Boolean = true,
    val lockScreenWakeEnabled: Boolean = true
)

data class VisionScan(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String? = null,
    val fileName: String,
    val fileSizeFormatted: String,
    val analysisResult: String = "",
    val status: String = "Pending", // Ready, Analyzing, Completed
    val timestamp: Long = System.currentTimeMillis()
)
