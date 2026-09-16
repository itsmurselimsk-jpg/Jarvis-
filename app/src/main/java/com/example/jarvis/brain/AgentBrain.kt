package com.example.jarvis.brain

import com.example.jarvis.bridge.AndroidBridge
import com.example.jarvis.model.ActivityType
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.RiskLevel
import com.example.jarvis.model.SafetyRequest
import com.example.jarvis.provider.AIProvider
import com.example.jarvis.safety.RiskAssessment
import com.example.jarvis.safety.RiskEngine
import com.example.jarvis.storage.JarvisRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name.lowercase()] = tool
    }

    fun getTool(name: String): Tool? = tools[name.lowercase()]

    fun getAllTools(): List<Tool> = tools.values.toList()

    fun getToolDefinitions(): List<Pair<String, String>> =
        tools.values.map { Pair(it.name, it.description) }
}

class AgentBrain(
    private val repository: JarvisRepository,
    private val bridge: AndroidBridge,
    private val aiProvider: AIProvider,
    private val onConfirmationRequired: (SafetyRequest) -> Unit
) {
    val registry = ToolRegistry()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currentPlanExplanation = MutableStateFlow<String?>(null)
    val currentPlanExplanation: StateFlow<String?> = _currentPlanExplanation.asStateFlow()

    init {
        // Register all real Android tools
        registry.register(BatteryTool())
        registry.register(NetworkStatusTool())
        registry.register(WifiTool())
        registry.register(BluetoothTool())
        registry.register(VolumeTool())
        registry.register(BrightnessTool())
        registry.register(FlashlightTool())
        registry.register(MediaControlTool())
        registry.register(AppLauncherTool())
        registry.register(OpenUrlTool())
        registry.register(AndroidSettingsTool())
        registry.register(ClipboardTool())
        registry.register(DeviceInfoTool())
        registry.register(MemoryTool())
        registry.register(TasksTool())
        registry.register(TimerTool())
        registry.register(CalculatorTool())
        registry.register(DateTimeTool())
        registry.register(AccessibilityTool())
        registry.register(NotificationTool())
        registry.register(WebSearchTool())
        registry.register(WeatherTool())
    }

    /**
     * Canonical AgentLoop:
     * User Input
     * → Context
     * → Memory Retrieval
     * → AI Planning
     * → Tool Selection
     * → Safety Check
     * → Confirmation if required
     * → Tool Execution
     * → Result Verification
     * → Final Response
     * → TTS
     */
    fun processUserInput(
        input: String,
        onThinking: () -> Unit,
        onSpeaking: () -> Unit,
        onIdle: () -> Unit
    ) {
        scope.launch {
            onThinking()

            // 1. Context Assembly
            bridge.refreshTelemetry()
            val telemetry = bridge.telemetry.value
            val contextString = "DEVICE TELEMETRY: Battery ${telemetry.batteryPercent}%, Net: ${telemetry.networkType}, Audio: ${telemetry.volumePercent}%"

            // 2. Memory Retrieval
            val relevantMemories = retrieveRelevantMemories(input)

            // 3. AI Planning & Tool Selection
            _currentPlanExplanation.value = "Analyzing intent & selecting tools..."
            val decision = aiProvider.decideTool(
                userInput = input,
                availableTools = registry.getToolDefinitions(),
                contextHistory = "$contextString\n$relevantMemories"
            )

            val selectedTool = if (decision.useTool && decision.toolName != null) {
                registry.getTool(decision.toolName)
            } else null

            val toolInput = decision.toolInput ?: input

            if (selectedTool != null) {
                _currentPlanExplanation.value = "Selected Tool: ${selectedTool.name} (Risk: ${selectedTool.riskLevel})"

                // 4. Safety Check
                val assessment = RiskEngine.assessAction(
                    actionName = selectedTool.name,
                    actionPayload = toolInput,
                    baseRisk = selectedTool.riskLevel
                )

                when (assessment.level) {
                    RiskLevel.RESTRICTED -> {
                        // Strictly rejected
                        _currentPlanExplanation.value = "Security Alert: Action Restricted"
                        val rejectionMsg = "Security protocol override: ${assessment.reason}"
                        repository.addMessage(ChatMessage(sender = MessageSender.SYSTEM, text = rejectionMsg, toolRiskLevel = RiskLevel.RESTRICTED))
                        repository.logActivity("Safety Restricted", assessment.actionSummary, ActivityType.SAFETY_ALERT, RiskLevel.RESTRICTED)
                        deliverFinalResponse(rejectionMsg, onSpeaking, onIdle)
                        return@launch
                    }

                    RiskLevel.CONFIRMATION -> {
                        // Prompt user confirmation in UI before executing
                        _currentPlanExplanation.value = "Awaiting user authorization for ${selectedTool.name}..."
                        val safetyReq = RiskEngine.buildSafetyRequest(
                            toolName = selectedTool.name,
                            actionPayload = toolInput,
                            reason = assessment.reason,
                            riskLevel = RiskLevel.CONFIRMATION,
                            onConfirm = {
                                scope.launch {
                                    executeAndDeliverTool(selectedTool, toolInput, onSpeaking, onIdle)
                                }
                            },
                            onCancel = {
                                scope.launch {
                                    val cancelMsg = "Operation cancelled by user clearance override."
                                    repository.addMessage(ChatMessage(sender = MessageSender.JARVIS, text = cancelMsg))
                                    _currentPlanExplanation.value = null
                                    onIdle()
                                }
                            }
                        )
                        onConfirmationRequired(safetyReq)
                        return@launch
                    }

                    RiskLevel.SAFE -> {
                        // Execute immediately
                        executeAndDeliverTool(selectedTool, toolInput, onSpeaking, onIdle)
                    }
                }
            } else {
                // Conversational reasoning via AIProvider
                _currentPlanExplanation.value = "Synthesizing response with AI brain..."
                val streamingMessage = ChatMessage(sender = MessageSender.JARVIS, text = "", isStreaming = true)
                repository.addMessage(streamingMessage)

                val prompt = buildString {
                    if (relevantMemories.isNotBlank()) {
                        appendLine(relevantMemories)
                    }
                    appendLine(contextString)
                    appendLine("User: $input")
                }

                val finalResponse = aiProvider.generateResponse(
                    prompt = prompt,
                    systemInstruction = repository.settings.value.systemPrompt,
                    onChunkReceived = { chunk ->
                        repository.updateStreamingMessage(chunk)
                    }
                )

                repository.finalizeStreamingMessage(finalResponse)
                deliverFinalResponse(finalResponse, onSpeaking, onIdle)
            }
        }
    }

    private suspend fun executeAndDeliverTool(
        tool: Tool,
        toolInput: String,
        onSpeaking: () -> Unit,
        onIdle: () -> Unit
    ) {
        val toolContext = ToolContext(repository = repository, bridge = bridge)

        // 5. Tool Execution
        val result = tool.execute(toolInput, toolContext)

        // 6. Result Verification
        val isVerified = tool.verify(result, toolContext)
        val verifiedOutput = if (isVerified) {
            result.output
        } else {
            "${result.output}\n\n*(Post-action verification alert: Device hardware state did not reflect expected change.)*"
        }

        // Record in chat
        repository.addMessage(
            ChatMessage(
                sender = MessageSender.JARVIS,
                text = verifiedOutput,
                toolCallName = tool.name,
                toolRiskLevel = tool.riskLevel
            )
        )

        deliverFinalResponse(verifiedOutput, onSpeaking, onIdle)
    }

    private fun deliverFinalResponse(
        text: String,
        onSpeaking: () -> Unit,
        onIdle: () -> Unit
    ) {
        _currentPlanExplanation.value = null
        val settings = repository.settings.value
        if (settings.autoSpeakResponses) {
            onSpeaking()
            bridge.speak(text, speechRate = settings.speechRate, pitch = settings.speechPitch)
        }
        onIdle()
    }

    private fun retrieveRelevantMemories(query: String): String {
        val memories = repository.memories.value
        if (memories.isEmpty()) return ""
        val matches = memories.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }.take(3)

        return if (matches.isNotEmpty()) {
            "RELEVANT LONG-TERM MEMORIES:\n" + matches.joinToString("\n") { "• [${it.title}] ${it.content}" }
        } else ""
    }
}
