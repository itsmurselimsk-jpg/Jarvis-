package com.example.jarvis.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.jarvis.model.ActivityLog
import com.example.jarvis.model.ActivityType
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.JarvisTask
import com.example.jarvis.model.JarvisTimer
import com.example.jarvis.model.MemoryItem
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.ProviderSettings
import com.example.jarvis.model.RiskLevel
import com.example.jarvis.model.VisionScan
import com.example.jarvis.security.EncryptedStorage
import com.example.jarvis.security.SensitiveDataFilter
import com.example.jarvis.storage.db.JarvisDatabase
import com.example.jarvis.storage.db.MemoryEntity
import com.example.jarvis.storage.db.TaskEntity
import com.example.jarvis.tasks.JarvisAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class JarvisRepository(private val context: Context) {

    private val db = JarvisDatabase.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_operating_layer_prefs", Context.MODE_PRIVATE)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    private val _tasks = MutableStateFlow<List<JarvisTask>>(emptyList())
    val tasks: StateFlow<List<JarvisTask>> = _tasks.asStateFlow()

    private val _timers = MutableStateFlow<List<JarvisTimer>>(emptyList())
    val timers: StateFlow<List<JarvisTimer>> = _timers.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    private val _visionScans = MutableStateFlow<List<VisionScan>>(emptyList())
    val visionScans: StateFlow<List<VisionScan>> = _visionScans.asStateFlow()

    private val _settings = MutableStateFlow(ProviderSettings())
    val settings: StateFlow<ProviderSettings> = _settings.asStateFlow()

    init {
        loadSettings()
        loadMessages()
        observeRoomEntities()
        initDefaultTimersAndLogs()
    }

    private fun loadSettings() {
        val encryptedKey = prefs.getString("encrypted_custom_api_key", "") ?: ""
        val decryptedKey = if (encryptedKey.isNotEmpty()) EncryptedStorage.decrypt(encryptedKey) else ""
        val endpoint = prefs.getString("custom_endpoint", "https://api.openai.com/v1") ?: ""
        val model = prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
        val autoSpeak = prefs.getBoolean("auto_speak", true)
        val speechRate = prefs.getFloat("speech_rate", 1.0f)
        val speechPitch = prefs.getFloat("speech_pitch", 1.0f)
        val voiceProfileName = prefs.getString("voice_profile_name", "JARVIS Natural") ?: "JARVIS Natural"
        val languageCode = prefs.getString("language_code", "auto") ?: "auto"
        val continuousWake = prefs.getBoolean("continuous_wake_enabled", true)
        val continuousConversation = prefs.getBoolean("continuous_conversation_enabled", true)
        val lockScreenWake = prefs.getBoolean("lock_screen_wake_enabled", true)

        _settings.value = ProviderSettings(
            customApiKey = decryptedKey,
            customEndpoint = endpoint,
            selectedModel = model,
            autoSpeakResponses = autoSpeak,
            speechRate = speechRate,
            speechPitch = speechPitch,
            voiceProfileName = voiceProfileName,
            languageCode = languageCode,
            continuousWakeEnabled = continuousWake,
            continuousConversationEnabled = continuousConversation,
            lockScreenWakeEnabled = lockScreenWake
        )
    }

    private fun observeRoomEntities() {
        scope.launch {
            db.memoryDao().getAllMemories().collect { entities ->
                if (entities.isEmpty()) {
                    // Seed initial operational memories if fresh
                    val defaults = listOf(
                        MemoryEntity(
                            title = "Primary User Protocol",
                            content = "User preference: Conciseness prioritized, proactive telemetry alerts enabled.",
                            category = "Preference"
                        ),
                        MemoryEntity(
                            title = "Security Clearance",
                            content = "Level 5 Executive Override active for local tool dispatch.",
                            category = "Security"
                        )
                    )
                    defaults.forEach { db.memoryDao().insertMemory(it) }
                } else {
                    _memories.value = entities.map {
                        MemoryItem(
                            id = it.id,
                            title = it.title,
                            content = it.content,
                            category = it.category,
                            timestamp = it.timestamp
                        )
                    }
                }
            }
        }

        scope.launch {
            db.taskDao().getAllTasks().collect { entities ->
                if (entities.isEmpty()) {
                    val defaults = listOf(
                        TaskEntity(
                            title = "Diagnostic Review of Quantum Core",
                            notes = "Check latency on neural tool router and telemetry bus.",
                            priority = "High"
                        ),
                        TaskEntity(
                            title = "Calibrate Vocal Modulation Profile",
                            notes = "Adjust speech synthesizer cadence.",
                            priority = "Normal"
                        )
                    )
                    defaults.forEach { db.taskDao().insertTask(it) }
                } else {
                    _tasks.value = entities.map {
                        JarvisTask(
                            id = it.id,
                            title = it.title,
                            notes = it.notes,
                            isCompleted = it.isCompleted,
                            priority = it.priority,
                            isRecurring = it.isRecurring,
                            dueDate = it.dueDate,
                            timestamp = it.timestamp
                        )
                    }
                }
            }
        }
    }

    private fun loadMessages() {
        val rawMessages = prefs.getString("saved_messages", null)
        if (!rawMessages.isNullOrBlank()) {
            try {
                val array = JSONArray(rawMessages)
                val list = mutableListOf<ChatMessage>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ChatMessage(
                            id = obj.optString("id"),
                            sender = MessageSender.valueOf(obj.optString("sender", MessageSender.JARVIS.name)),
                            text = obj.optString("text"),
                            timestamp = obj.optLong("timestamp"),
                            toolCallName = if (obj.has("toolCallName")) obj.optString("toolCallName") else null
                        )
                    )
                }
                _messages.value = list
            } catch (_: Exception) {}
        } else {
            _messages.value = listOf(
                ChatMessage(
                    sender = MessageSender.JARVIS,
                    text = "JARVIS Core operational, Sir. All sensory arrays, tool executors, and device bridges are active. What shall we coordinate today?"
                )
            )
        }
    }

    private fun initDefaultTimersAndLogs() {
        _timers.value = listOf(
            JarvisTimer(
                label = "Focus Interval",
                totalSeconds = 1500,
                remainingSeconds = 1500
            )
        )
        _activityLogs.value = listOf(
            ActivityLog(
                title = "JARVIS Subsystem Initialization",
                detail = "Quantum telemetry, Room database, and hardware bridges online.",
                type = ActivityType.SYSTEM_EVENT
            )
        )
    }

    fun addMessage(message: ChatMessage) {
        val updated = _messages.value + message
        _messages.value = updated
        saveMessages(updated)
    }

    fun updateStreamingMessage(text: String) {
        val current = _messages.value
        if (current.isNotEmpty() && current.last().isStreaming) {
            val last = current.last().copy(text = text)
            _messages.value = current.dropLast(1) + last
        }
    }

    fun finalizeStreamingMessage(text: String) {
        val current = _messages.value
        if (current.isNotEmpty() && current.last().isStreaming) {
            val last = current.last().copy(text = text, isStreaming = false)
            val updated = current.dropLast(1) + last
            _messages.value = updated
            saveMessages(updated)
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        prefs.edit().remove("saved_messages").apply()
        logActivity("Conversation Cleared", "Purged local conversation history.", ActivityType.SYSTEM_EVENT)
    }

    private fun saveMessages(list: List<ChatMessage>) {
        val array = JSONArray()
        list.takeLast(100).forEach { msg ->
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("sender", msg.sender.name)
            obj.put("text", msg.text)
            obj.put("timestamp", msg.timestamp)
            msg.toolCallName?.let { obj.put("toolCallName", it) }
            array.put(obj)
        }
        prefs.edit().putString("saved_messages", array.toString()).apply()
    }

    fun addMemory(title: String, content: String, category: String = "General") {
        if (SensitiveDataFilter.containsSensitiveData(content)) {
            logActivity("Security Guard Alert", "Memory rejected: contained unencrypted credentials/card numbers.", ActivityType.SAFETY_ALERT, RiskLevel.RESTRICTED)
            return
        }
        val entity = MemoryEntity(
            title = title,
            content = content,
            category = category
        )
        scope.launch {
            db.memoryDao().insertMemory(entity)
        }
        logActivity("Memory Encoded", "Stored in Room: $title", ActivityType.SYSTEM_EVENT)
    }

    fun deleteMemory(id: String) {
        scope.launch {
            db.memoryDao().deleteMemoryById(id)
        }
        logActivity("Memory Deleted", "Purged memory ID: $id", ActivityType.SYSTEM_EVENT)
    }

    fun clearAllMemories() {
        scope.launch {
            db.memoryDao().clearAllMemories()
            _memories.value = emptyList()
        }
        logActivity("Memory Store Purged", "All memories deleted from Room.", ActivityType.SAFETY_ALERT, RiskLevel.CONFIRMATION)
    }

    fun reloadMemories() {
        scope.launch {
            val list = db.memoryDao().getRecentMemoriesSync(100)
            _memories.value = list.map {
                MemoryItem(
                    id = it.id,
                    title = it.title,
                    content = it.content,
                    category = it.category,
                    timestamp = it.timestamp
                )
            }
        }
    }

    fun addTask(title: String, notes: String = "", priority: String = "Normal", dueDate: String? = null, dueTimestamp: Long? = null) {
        val entity = TaskEntity(
            title = title,
            notes = notes,
            priority = priority,
            dueDate = dueDate,
            dueTimestamp = dueTimestamp
        )
        scope.launch {
            db.taskDao().insertTask(entity)
            if (dueTimestamp != null && dueTimestamp > System.currentTimeMillis()) {
                JarvisAlarmScheduler.scheduleTaskReminder(context, entity.id, title, dueTimestamp)
            }
        }
        logActivity("Task Scheduled", title, ActivityType.TASK_EVENT)
    }

    fun toggleTask(id: String) {
        scope.launch {
            val tasks = _tasks.value
            val target = tasks.find { it.id == id } ?: return@launch
            val updated = target.copy(isCompleted = !target.isCompleted)
            db.taskDao().updateTask(
                TaskEntity(
                    id = updated.id,
                    title = updated.title,
                    notes = updated.notes,
                    isCompleted = updated.isCompleted,
                    priority = updated.priority,
                    isRecurring = updated.isRecurring,
                    dueDate = updated.dueDate,
                    timestamp = updated.timestamp
                )
            )
        }
    }

    fun deleteTask(id: String) {
        scope.launch {
            db.taskDao().deleteTaskById(id)
            JarvisAlarmScheduler.cancelReminder(context, id)
        }
        logActivity("Task Deleted", "Removed task ID: $id", ActivityType.TASK_EVENT)
    }

    fun reloadTasks() {
        scope.launch {
            val list = db.taskDao().getScheduledTasksSync()
            _tasks.value = list.map {
                JarvisTask(
                    id = it.id,
                    title = it.title,
                    notes = it.notes,
                    isCompleted = it.isCompleted,
                    priority = it.priority,
                    isRecurring = it.isRecurring,
                    dueDate = it.dueDate,
                    timestamp = it.timestamp
                )
            }
        }
    }

    fun updateTimer(id: String, remaining: Int, isRunning: Boolean) {
        _timers.value = _timers.value.map {
            if (it.id == id) it.copy(remainingSeconds = remaining, isRunning = isRunning) else it
        }
    }

    fun addTimer(label: String, seconds: Int) {
        val timer = JarvisTimer(label = label, totalSeconds = seconds, remainingSeconds = seconds)
        _timers.value = listOf(timer) + _timers.value
        logActivity("Timer Created", "$label for ${seconds}s", ActivityType.TASK_EVENT)
    }

    fun deleteTimer(id: String) {
        _timers.value = _timers.value.filter { it.id != id }
    }

    fun logActivity(title: String, detail: String, type: ActivityType, risk: RiskLevel = RiskLevel.SAFE) {
        val entry = ActivityLog(title = title, detail = detail, type = type, riskLevel = risk)
        _activityLogs.value = listOf(entry) + _activityLogs.value.take(49)
    }

    fun clearActivityLogs() {
        _activityLogs.value = emptyList()
    }

    fun addVisionScan(scan: VisionScan) {
        _visionScans.value = listOf(scan) + _visionScans.value
        logActivity("Vision File Analyzed", scan.fileName, ActivityType.TOOL_EXECUTION)
    }

    fun updateSettings(newSettings: ProviderSettings) {
        _settings.value = newSettings
        val encryptedKey = if (newSettings.customApiKey.isNotEmpty()) {
            EncryptedStorage.encrypt(newSettings.customApiKey)
        } else ""

        prefs.edit().apply {
            putString("encrypted_custom_api_key", encryptedKey)
            remove("custom_api_key") // Remove any old plaintext key
            putString("custom_endpoint", newSettings.customEndpoint)
            putString("selected_model", newSettings.selectedModel)
            putBoolean("auto_speak", newSettings.autoSpeakResponses)
            putFloat("speech_rate", newSettings.speechRate)
            putFloat("speech_pitch", newSettings.speechPitch)
            putString("voice_profile_name", newSettings.voiceProfileName)
            putString("language_code", newSettings.languageCode)
            putBoolean("continuous_wake_enabled", newSettings.continuousWakeEnabled)
            putBoolean("continuous_conversation_enabled", newSettings.continuousConversationEnabled)
            putBoolean("lock_screen_wake_enabled", newSettings.lockScreenWakeEnabled)
            apply()
        }
        logActivity("Settings Updated", "AI Provider & speech config securely committed.", ActivityType.SYSTEM_EVENT)
    }

    fun wipeAllLocalData() {
        scope.launch {
            db.memoryDao().clearAllMemories()
            db.taskDao().clearAllTasks()
            db.notificationDao().clearAllNotifications()
            prefs.edit().clear().apply()
            _messages.value = emptyList()
            _memories.value = emptyList()
            _tasks.value = emptyList()
            _timers.value = emptyList()
            _activityLogs.value = emptyList()
            _visionScans.value = emptyList()
        }
        logActivity("Complete Data Wipe", "All preferences, Room DB, and logs purged.", ActivityType.SAFETY_ALERT, RiskLevel.RESTRICTED)
    }
}
