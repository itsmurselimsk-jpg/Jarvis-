package com.example.jarvis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jarvis.auth.AuthManager
import com.example.jarvis.brain.AgentBrain
import com.example.jarvis.brain.ToolContext
import com.example.jarvis.bridge.AndroidBridge
import com.example.jarvis.model.ActivityType
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.JarvisState
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.SafetyRequest
import com.example.jarvis.privacy.PrivacyAuditor
import com.example.jarvis.provider.JarvisUnifiedAIProvider
import com.example.jarvis.storage.JarvisRepository
import com.example.jarvis.ui.components.NavTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SubScreen {
    TOOLS,
    TASKS,
    MEMORY,
    ACTIVITY,
    VISION,
    PRIVACY,
    BRIDGE,
    VOICE_SETUP,
    VOICE_SELECTION,
    ACCOUNT,
    ABOUT
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    val repository = JarvisRepository(application)
    val bridge = AndroidBridge(application)
    val aiProvider = JarvisUnifiedAIProvider(repository)
    val privacyAuditor = PrivacyAuditor(application, repository)
    val authManager = AuthManager(application)
    val authState = authManager.authState

    private val _safetyRequest = MutableStateFlow<SafetyRequest?>(null)
    val safetyRequest: StateFlow<SafetyRequest?> = _safetyRequest.asStateFlow()

    private val _isContinuousConversationActive = MutableStateFlow(false)
    val isContinuousConversationActive: StateFlow<Boolean> = _isContinuousConversationActive.asStateFlow()

    val isMicMuted = bridge.isMicMuted
    val isSpeakerEnabled = bridge.isSpeakerEnabled

    val brain = AgentBrain(
        repository = repository,
        bridge = bridge,
        aiProvider = aiProvider,
        onConfirmationRequired = { request ->
            _safetyRequest.value = request
        }
    )

    val toolContext = ToolContext(repository, bridge)

    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _activeSubScreen = MutableStateFlow<SubScreen?>(null)
    val activeSubScreen: StateFlow<SubScreen?> = _activeSubScreen.asStateFlow()

    private val _lastResponse = MutableStateFlow("")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    // Expose repository & bridge flows
    val messages = repository.messages
    val memories = repository.memories
    val tasks = repository.tasks
    val timers = repository.timers
    val activityLogs = repository.activityLogs
    val visionScans = repository.visionScans
    val settings = repository.settings
    val telemetry = bridge.telemetry
    val isListening = bridge.isListening
    val liveTranscript = bridge.liveTranscript
    val isSpeaking = bridge.isSpeaking
    val speechSupported = bridge.speechSupported

    init {
        // Continuous speech-to-speech loop: return to listening upon speech completion
        brain.onSpeechCompletedCallback = {
            if (_isContinuousConversationActive.value && !bridge.isMicMuted.value) {
                viewModelScope.launch {
                    delay(400)
                    startListening()
                }
            }
        }

        // Periodic telemetry refresh
        viewModelScope.launch {
            while (true) {
                delay(4000)
                bridge.refreshTelemetry()
            }
        }
    }

    fun setTab(tab: NavTab) {
        _activeSubScreen.value = null
        _currentTab.value = tab
    }

    fun openSubScreen(sub: SubScreen) {
        _activeSubScreen.value = sub
    }

    fun closeSubScreen() {
        _activeSubScreen.value = null
    }

    fun setJarvisState(state: JarvisState) {
        _jarvisState.value = state
        when (state) {
            JarvisState.IDLE -> {
                if (isListening.value) stopListening()
                if (isSpeaking.value) stopSpeaking()
            }
            JarvisState.SPEAKING -> {
                if (!isSpeaking.value) {
                    speakText("JARVIS neural matrix online and fully operational, Sir.")
                }
            }
            JarvisState.LISTENING -> {
                startListening()
            }
            else -> {}
        }
    }

    fun dismissSafetyDialog() {
        _safetyRequest.value = null
    }

    fun startListening() {
        _jarvisState.value = JarvisState.LISTENING
        bridge.startListening { spokenText ->
            _jarvisState.value = JarvisState.IDLE
            if (spokenText.isNotBlank()) {
                sendUserMessage(spokenText)
            }
        }
    }

    fun interruptAndListen() {
        _jarvisState.value = JarvisState.LISTENING
        bridge.interruptAndListen { spokenText ->
            _jarvisState.value = JarvisState.IDLE
            if (spokenText.isNotBlank()) {
                sendUserMessage(spokenText)
            }
        }
    }

    fun toggleContinuousConversation() {
        _isContinuousConversationActive.value = !_isContinuousConversationActive.value
        if (_isContinuousConversationActive.value) {
            startListening()
        } else {
            stopListening()
            if (isSpeaking.value) stopSpeaking()
        }
    }

    fun toggleMicMute() {
        bridge.toggleMicMute()
    }

    fun toggleSpeaker() {
        bridge.toggleSpeaker()
    }

    fun stopListening() {
        bridge.stopListening()
        _jarvisState.value = JarvisState.IDLE
    }

    fun speakText(
        text: String,
        rate: Float? = null,
        pitch: Float? = null,
        locale: java.util.Locale? = null,
        onDone: () -> Unit = {}
    ) {
        _jarvisState.value = JarvisState.SPEAKING
        val r = rate ?: settings.value.speechRate
        val p = pitch ?: settings.value.speechPitch
        bridge.speak(text, r, p, locale) {
            _jarvisState.value = JarvisState.IDLE
            onDone()
        }
    }

    fun triggerWakeSession() {
        val intent = android.content.Intent(getApplication(), com.example.jarvis.service.JarvisVoiceService::class.java).apply {
            action = com.example.jarvis.service.JarvisVoiceService.ACTION_TRIGGER_WAKE
        }
        getApplication<Application>().startService(intent)
    }

    fun stopSpeaking() {
        bridge.stopSpeaking()
        _jarvisState.value = JarvisState.IDLE
    }

    fun toggleFlashlight(on: Boolean) {
        bridge.toggleFlashlight(on)
    }

    fun openAndroidSettings(targetScreen: String = "general") {
        bridge.openAndroidSettings(targetScreen)
    }

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user message
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        repository.addMessage(userMsg)
        repository.logActivity("User Directive", text.take(40), ActivityType.VOICE_EVENT)

        // 2. Dispatch into AgentLoop
        brain.processUserInput(
            input = text,
            onThinking = { _jarvisState.value = JarvisState.THINKING },
            onSpeaking = { _jarvisState.value = JarvisState.SPEAKING },
            onIdle = { _jarvisState.value = JarvisState.IDLE }
        )
    }

    fun retryLastMessage() {
        val userMsgs = messages.value.filter { it.sender == MessageSender.USER }
        val lastUser = userMsgs.lastOrNull()
        if (lastUser != null) {
            sendUserMessage(lastUser.text)
        }
    }

    fun copyToClipboard(text: String) {
        bridge.copyToClipboard("JARVIS_COPY", text)
        repository.logActivity("Clipboard Copy", text.take(30), ActivityType.TOOL_EXECUTION)
    }

    override fun onCleared() {
        super.onCleared()
        bridge.destroy()
    }
}
