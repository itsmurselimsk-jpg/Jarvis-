package com.example.jarvis.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jarvis.model.JarvisState
import com.example.jarvis.ui.components.BottomNav
import com.example.jarvis.ui.components.NavTab
import com.example.jarvis.ui.components.SafetyConfirmationDialog
import com.example.jarvis.ui.components.TopBar
import com.example.jarvis.ui.screens.ActivityScreen
import com.example.jarvis.ui.screens.AndroidBridgeScreen
import com.example.jarvis.ui.screens.ChatScreen
import com.example.jarvis.ui.screens.HomeScreen
import com.example.jarvis.ui.screens.MemoryScreen
import com.example.jarvis.ui.screens.PrivacyScreen
import com.example.jarvis.ui.screens.SettingsScreen
import com.example.jarvis.ui.screens.TasksScreen
import com.example.jarvis.ui.screens.ToolsScreen
import com.example.jarvis.ui.screens.VisionScreen
import com.example.jarvis.ui.screens.VoiceSelectionScreen
import com.example.jarvis.ui.screens.VoiceSetupScreen
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan

@Composable
fun JarvisApp(
    viewModel: JarvisViewModel = viewModel()
) {
    val jarvisState by viewModel.jarvisState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val activeSubScreen by viewModel.activeSubScreen.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val timers by viewModel.timers.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()
    val scans by viewModel.visionScans.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val speechSupported by viewModel.speechSupported.collectAsState()
    val safetyRequest by viewModel.safetyRequest.collectAsState()

    // Request audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        }
    }

    // Handle back button if in a SubScreen
    BackHandler(enabled = activeSubScreen != null) {
        viewModel.closeSubScreen()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        topBar = {
            TopBar(
                telemetry = telemetry,
                isOnline = true
            )
        },
        bottomBar = {
            BottomNav(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    viewModel.setTab(tab)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSubScreen != null) {
                // SubScreen Container with Header
                Column(modifier = Modifier.fillMaxSize()) {
                    SubScreenHeader(
                        title = activeSubScreen?.name ?: "SUBMODULE",
                        onBack = { viewModel.closeSubScreen() }
                    )

                    when (activeSubScreen) {
                        SubScreen.TOOLS -> ToolsScreen(
                            tools = viewModel.brain.registry.getAllTools(),
                            toolContext = viewModel.toolContext
                        )
                        SubScreen.MEMORY -> MemoryScreen(
                            memories = memories,
                            onAddMemory = { title, content, cat ->
                                viewModel.repository.addMemory(title, content, cat)
                            },
                            onDeleteMemory = { viewModel.repository.deleteMemory(it) },
                            onClearAllMemories = { viewModel.repository.clearAllMemories() }
                        )
                        SubScreen.ACTIVITY -> ActivityScreen(
                            logs = logs,
                            onClearLogs = { viewModel.repository.clearActivityLogs() }
                        )
                        SubScreen.VISION -> VisionScreen(
                            scans = scans,
                            aiProvider = viewModel.aiProvider,
                            onAddScan = { viewModel.repository.addVisionScan(it) }
                        )
                        SubScreen.PRIVACY -> PrivacyScreen(
                            auditor = viewModel.privacyAuditor,
                            onOpenSettings = { viewModel.openAndroidSettings(it) }
                        )
                        SubScreen.BRIDGE -> AndroidBridgeScreen(
                            telemetry = telemetry,
                            onRefreshTelemetry = { viewModel.bridge.refreshTelemetry() },
                            onToggleFlashlight = { viewModel.toggleFlashlight(it) },
                            onOpenSystemSettings = { viewModel.openAndroidSettings() }
                        )
                        SubScreen.VOICE_SETUP -> VoiceSetupScreen(
                            currentSettings = settings,
                            onUpdateSettings = { viewModel.repository.updateSettings(it) },
                            onTestWakeTrigger = { viewModel.triggerWakeSession() }
                        )
                        SubScreen.VOICE_SELECTION -> VoiceSelectionScreen(
                            currentSettings = settings,
                            onUpdateSettings = { viewModel.repository.updateSettings(it) },
                            onTestSpeak = { text, rate, pitch ->
                                viewModel.speakText(text, rate, pitch)
                            }
                        )
                        null -> {}
                    }
                }
            } else {
                // Main BottomNav Screens
                when (currentTab) {
                    NavTab.HOME -> HomeScreen(
                        jarvisState = jarvisState,
                        telemetry = telemetry,
                        onVoiceClick = {
                            viewModel.setTab(NavTab.VOICE)
                        },
                        onChatClick = {
                            viewModel.setTab(NavTab.CHAT)
                        },
                        onToolsClick = {
                            viewModel.openSubScreen(SubScreen.TOOLS)
                        },
                        onMemoryClick = {
                            viewModel.openSubScreen(SubScreen.MEMORY)
                        },
                        onActivityClick = {
                            viewModel.openSubScreen(SubScreen.ACTIVITY)
                        },
                        onVisionClick = {
                            viewModel.openSubScreen(SubScreen.VISION)
                        },
                        onPrivacyClick = {
                            viewModel.openSubScreen(SubScreen.PRIVACY)
                        },
                        onBridgeClick = {
                            viewModel.openSubScreen(SubScreen.BRIDGE)
                        },
                        onQuickCommand = { cmd ->
                            viewModel.setTab(NavTab.CHAT)
                            viewModel.sendUserMessage(cmd)
                        },
                        onStateChange = { newState ->
                            viewModel.setJarvisState(newState)
                        }
                    )

                    NavTab.VOICE -> com.example.jarvis.ui.screens.VoiceScreen(
                        jarvisState = jarvisState,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        liveTranscript = liveTranscript,
                        lastResponse = lastResponse,
                        speechSupported = speechSupported,
                        onStartListening = {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onStopListening = { viewModel.stopListening() },
                        onSpeakText = { text, rate, pitch ->
                            viewModel.speakText(text, rate, pitch)
                        },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onNavigateVoiceSetup = { viewModel.openSubScreen(SubScreen.VOICE_SETUP) },
                        onNavigateVoiceProfiles = { viewModel.openSubScreen(SubScreen.VOICE_SELECTION) }
                    )

                    NavTab.CHAT -> ChatScreen(
                        messages = messages,
                        jarvisState = jarvisState,
                        onSendMessage = { viewModel.sendUserMessage(it) },
                        onCopyMessage = { viewModel.copyToClipboard(it) },
                        onSpeakMessage = { viewModel.speakText(it) },
                        onRetryMessage = { viewModel.retryLastMessage() },
                        onClearChat = { viewModel.repository.clearMessages() }
                    )

                    NavTab.TASKS -> TasksScreen(
                        tasks = tasks,
                        timers = timers,
                        onToggleTask = { viewModel.repository.toggleTask(it) },
                        onDeleteTask = { viewModel.repository.deleteTask(it) },
                        onAddTask = { title, notes, priority ->
                            viewModel.repository.addTask(title, notes, priority)
                        },
                        onAddTimer = { label, seconds ->
                            viewModel.repository.addTimer(label, seconds)
                        },
                        onDeleteTimer = { viewModel.repository.deleteTimer(it) },
                        onUpdateTimer = { id, remaining, running ->
                            viewModel.repository.updateTimer(id, remaining, running)
                        }
                    )

                    NavTab.SETTINGS -> SettingsScreen(
                        currentSettings = settings,
                        onSaveSettings = { viewModel.repository.updateSettings(it) },
                        onNavigatePrivacy = { viewModel.openSubScreen(SubScreen.PRIVACY) },
                        onNavigateMemory = { viewModel.openSubScreen(SubScreen.MEMORY) },
                        onNavigateBridge = { viewModel.openSubScreen(SubScreen.BRIDGE) },
                        onNavigateActivity = { viewModel.openSubScreen(SubScreen.ACTIVITY) },
                        onNavigateVision = { viewModel.openSubScreen(SubScreen.VISION) },
                        onNavigateVoiceSetup = { viewModel.openSubScreen(SubScreen.VOICE_SETUP) },
                        onNavigateVoiceProfiles = { viewModel.openSubScreen(SubScreen.VOICE_SELECTION) }
                    )
                }
            }

            // Safety Confirmation Dialog (Always high-priority overlay if active)
            safetyRequest?.let { request ->
                SafetyConfirmationDialog(
                    request = request,
                    onDismiss = { viewModel.dismissSafetyDialog() }
                )
            }
        }
    }
}

@Composable
private fun SubScreenHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF090E1A))
            .border(0.5.dp, JarvisBorderSubtle)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = JarvisCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "SUBMODULE: $title",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = JarvisCyan
        )
    }
}
