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

    // Short-term conversation context buffer: stores last N turns without sending entire history to provider
    private val conversationBuffer = mutableListOf<Pair<String, String>>()
    private val maxBufferTurns = 4

    // Recent tool execution tracking
    private var lastExecutedToolName: String? = null
    private var lastExecutedToolResult: String? = null

    var onSpeechCompletedCallback: (() -> Unit)? = null

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
        registry.register(YouTubeSearchTool())
        registry.register(PhoneCallTool())
        registry.register(DiagnosticsTool())
        registry.register(UniversalSearchTool())
        registry.register(VisionOcrTool())
    }

    // Active vision / image OCR context if user recently scanned or inspected an image
    var activeVisionResult: com.example.jarvis.vision.VisionResult? = null

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

            // 1. Context Assembly (Compact & Bounded)
            bridge.refreshTelemetry()
            val telemetry = bridge.telemetry.value
            val contextBuilder = StringBuilder()
            contextBuilder.appendLine("DEVICE TELEMETRY: Battery ${telemetry.batteryPercent}%, Net: ${telemetry.networkType}, Audio: ${telemetry.volumePercent}%")

            // Active tasks summary (max 3 pending)
            val pendingTasks = repository.tasks.value.filter { !it.isCompleted }.take(3)
            if (pendingTasks.isNotEmpty()) {
                val taskList = pendingTasks.joinToString(", ") { it.title }
                contextBuilder.appendLine("ACTIVE PENDING TASKS: $taskList")
            }

            // Recent tool result if available
            if (lastExecutedToolName != null && lastExecutedToolResult != null) {
                val briefResult = lastExecutedToolResult!!.take(120).replace("\n", " ")
                contextBuilder.appendLine("RECENT TOOL EXECUTED: $lastExecutedToolName -> $briefResult")
            }

            // Short-term conversation context (last turns)
            if (conversationBuffer.isNotEmpty()) {
                contextBuilder.appendLine("RECENT DIALOGUE CONTEXT:")
                conversationBuffer.takeLast(3).forEach { (user, jarvis) ->
                    contextBuilder.appendLine("User: $user")
                    contextBuilder.appendLine("JARVIS: ${jarvis.take(100)}")
                }
            }

            // Active Vision / Image OCR Context if available
            val vision = activeVisionResult
            if (vision != null && vision.success && vision.extractedText.isNotBlank()) {
                val sanitizedText = if (vision.containsSensitiveData) {
                    com.example.jarvis.vision.SensitiveDataFilter.redactSensitiveData(vision.extractedText)
                } else {
                    vision.extractedText
                }
                contextBuilder.appendLine("ACTIVE OCR VISION CONTEXT:")
                contextBuilder.appendLine("Extracted Text: \"${sanitizedText.take(400)}\"")
                if (vision.containsSensitiveData) {
                    contextBuilder.appendLine("SENSITIVITY ALERT: Contains sensitive tokens (${vision.sensitiveEntitiesDetected.joinToString()}). Must NOT be stored in persistent long-term memory.")
                }
            }

            val contextString = contextBuilder.toString().trimEnd()

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
                                    executeAndDeliverTool(selectedTool, toolInput, input, onSpeaking, onIdle)
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
                        executeAndDeliverTool(selectedTool, toolInput, input, onSpeaking, onIdle)
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
                recordTurn(input, finalResponse)
                deliverFinalResponse(finalResponse, onSpeaking, onIdle)
            }
        }
    }

    private suspend fun executeAndDeliverTool(
        tool: Tool,
        toolInput: String,
        originalUserInput: String,
        onSpeaking: () -> Unit,
        onIdle: () -> Unit
    ) {
        val toolContext = ToolContext(repository = repository, bridge = bridge, activeVisionResult = activeVisionResult)

        // 5. Tool Execution
        val result = tool.execute(toolInput, toolContext)

        // 6. Result Verification
        val isVerified = tool.verify(result, toolContext)
        val verifiedOutput = if (isVerified) {
            result.output
        } else {
            "${result.output}\n\n*(Post-action verification alert: Device hardware state did not reflect expected change.)*"
        }

        // Record in chat & update context
        lastExecutedToolName = tool.name
        lastExecutedToolResult = verifiedOutput
        recordTurn(originalUserInput, verifiedOutput)

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

    private fun recordTurn(userInput: String, assistantOutput: String) {
        conversationBuffer.add(Pair(userInput, assistantOutput))
        while (conversationBuffer.size > maxBufferTurns) {
            conversationBuffer.removeAt(0)
        }
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
            bridge.speak(
                text = text,
                speechRate = settings.speechRate,
                pitch = settings.speechPitch,
                onDone = {
                    onIdle()
                    onSpeechCompletedCallback?.invoke()
                }
            )
        } else {
            onIdle()
            onSpeechCompletedCallback?.invoke()
        }
    }

    private fun retrieveRelevantMemories(query: String): String {
        val memories = repository.memories.value
        if (memories.isEmpty()) return ""

        val cleaned = query.lowercase().trim()
        val stopWords = setOf("the", "a", "an", "is", "are", "was", "were", "what", "where", "who", "how", "when", "why", "my", "your", "me", "you", "i", "to", "in", "on", "for", "with", "about", "tell", "jarvis", "please", "can")
        val queryTokens = cleaned.split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 && !stopWords.contains(it) }

        // Scored retrieval
        val scored = memories.map { mem ->
            var score = 0
            val titleLower = mem.title.lowercase()
            val contentLower = mem.content.lowercase()

            if (titleLower.contains(cleaned) || contentLower.contains(cleaned)) {
                score += 10
            }

            for (token in queryTokens) {
                if (titleLower.contains(token)) score += 3
                if (contentLower.contains(token)) score += 2
            }

            Pair(mem, score)
        }.filter { it.second > 0 }
         .sortedByDescending { it.second }
         .take(4)

        return if (scored.isNotEmpty()) {
            "RELEVANT LONG-TERM MEMORIES:\n" + scored.joinToString("\n") { "• [${it.first.title}] ${it.first.content}" }
        } else ""
    }
}
