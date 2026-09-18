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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jarvis.auth.AuthState
import com.example.jarvis.model.JarvisState
import com.example.jarvis.ui.components.BottomNav
import com.example.jarvis.ui.components.JarvisOrb
import com.example.jarvis.ui.components.NavTab
import com.example.jarvis.ui.components.SafetyConfirmationDialog
import com.example.jarvis.ui.components.TopBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.jarvis.ui.screens.AboutScreen
import com.example.jarvis.ui.screens.AccountProfileScreen
import com.example.jarvis.ui.screens.ActivityScreen
import com.example.jarvis.ui.screens.AndroidBridgeScreen
import com.example.jarvis.ui.screens.ChatScreen
import com.example.jarvis.ui.screens.ForgotPasswordScreen
import com.example.jarvis.ui.screens.HomeScreen
import com.example.jarvis.ui.screens.LoginScreen
import com.example.jarvis.ui.screens.MemoryScreen
import com.example.jarvis.ui.screens.PrivacyScreen
import com.example.jarvis.ui.screens.SettingsScreen
import com.example.jarvis.ui.screens.SignUpScreen
import com.example.jarvis.ui.screens.TasksScreen
import com.example.jarvis.ui.screens.ToolsScreen
import com.example.jarvis.ui.screens.VisionScreen
import com.example.jarvis.ui.screens.VoiceSelectionScreen
import com.example.jarvis.ui.screens.VoiceSetupScreen
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisTextSecondary

private enum class AuthNavState {
    LOGIN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@Composable
fun JarvisApp(
    viewModel: JarvisViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var authNavState by remember { mutableStateOf(AuthNavState.LOGIN) }

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
    val activeVisionResult by viewModel.activeVisionResult.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val speechSupported by viewModel.speechSupported.collectAsState()
    val safetyRequest by viewModel.safetyRequest.collectAsState()
    val isContinuousConversationActive by viewModel.isContinuousConversationActive.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isSpeakerEnabled by viewModel.isSpeakerEnabled.collectAsState()

    // Request audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        }
    }

    when (val state = authState) {
        is AuthState.Initializing -> {
            // Futuristic Boot Splash with Official JARVIS Logo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JarvisBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(2.dp, JarvisCyan.copy(alpha = 0.8f), CircleShape)
                            .background(Color(0xFF070D18)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.jarvis_logo_round),
                            contentDescription = "Official JARVIS Logo",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = "J.A.R.V.I.S.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "INITIALIZING SECURE PROTOCOLS...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        is AuthState.Unauthenticated, is AuthState.AuthError -> {
            when (authNavState) {
                AuthNavState.LOGIN -> {
                    LoginScreen(
                        authManager = viewModel.authManager,
                        onNavigateToSignUp = { authNavState = AuthNavState.SIGN_UP },
                        onNavigateToForgotPassword = { authNavState = AuthNavState.FORGOT_PASSWORD },
                        onLoginSuccess = {}
                    )
                }
                AuthNavState.SIGN_UP -> {
                    SignUpScreen(
                        authManager = viewModel.authManager,
                        onNavigateBackToLogin = { authNavState = AuthNavState.LOGIN },
                        onSignUpSuccess = {}
                    )
                }
                AuthNavState.FORGOT_PASSWORD -> {
                    ForgotPasswordScreen(
                        authManager = viewModel.authManager,
                        onNavigateBack = { authNavState = AuthNavState.LOGIN }
                    )
                }
            }
        }

        is AuthState.Authenticated -> {
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
                        isOnline = true,
                        onProfileClick = {
                            viewModel.openSubScreen(SubScreen.ACCOUNT)
                        }
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
                    val currentSubScreen = activeSubScreen
                    if (currentSubScreen != null) {
                        // SubScreen Container with Header
                        Column(modifier = Modifier.fillMaxSize()) {
                            SubScreenHeader(
                                title = currentSubScreen.name,
                                onBack = { viewModel.closeSubScreen() }
                            )

                            when (currentSubScreen) {
                                SubScreen.ACCOUNT -> AccountProfileScreen(
                                    user = state.user,
                                    authManager = viewModel.authManager,
                                    onSignOut = {
                                        viewModel.authManager.signOut()
                                        viewModel.closeSubScreen()
                                    }
                                )
                                SubScreen.TOOLS -> ToolsScreen(
                                    tools = viewModel.brain.registry.getAllTools(),
                                    toolContext = viewModel.toolContext
                                )
                                SubScreen.TASKS -> TasksScreen(
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
                                    onAddScan = { viewModel.repository.addVisionScan(it) },
                                    visionEngine = viewModel.visionEngine,
                                    activeResult = activeVisionResult,
                                    onSetActiveResult = { res, uri -> viewModel.setActiveVisionContext(res, uri) },
                                    onExecuteAction = { action -> viewModel.executeVisionDerivedAction(action) },
                                    onAskJarvis = { prompt -> viewModel.askJarvisAboutVision(prompt) },
                                    onCopyToClipboard = { text -> viewModel.copyToClipboard(text) }
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
                                SubScreen.ABOUT -> AboutScreen()
                                SubScreen.DIAGNOSTICS -> com.example.jarvis.ui.screens.DiagnosticsScreen(
                                    repository = viewModel.repository,
                                    bridge = viewModel.bridge
                                )
                                SubScreen.NOTIFICATIONS -> com.example.jarvis.ui.screens.NotificationIntelligenceScreen(
                                    repository = viewModel.repository,
                                    onOpenNotificationSettings = {
                                        viewModel.bridge.getApplicationContext().startActivity(
                                            com.example.jarvis.notification.JarvisNotificationListenerService.getNotificationSettingsIntent()
                                        )
                                    }
                                )
                                SubScreen.SEARCH -> com.example.jarvis.ui.screens.UniversalSearchScreen(
                                    repository = viewModel.repository,
                                    bridge = viewModel.bridge,
                                    onOpenSubScreen = { subScreen ->
                                        viewModel.openSubScreen(subScreen)
                                    }
                                )
                            }
                        }
                    } else {
                        // Main BottomNav Screens
                        when (currentTab) {
                            NavTab.HOME -> HomeScreen(
                                jarvisState = jarvisState,
                                telemetry = telemetry,
                                tasks = tasks,
                                memories = memories,
                                logs = logs,
                                settings = settings,
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
                                onSearchClick = {
                                    viewModel.openSubScreen(SubScreen.SEARCH)
                                },
                                onTasksClick = {
                                    viewModel.setTab(NavTab.TASKS)
                                },
                                onSettingsClick = {
                                    viewModel.setTab(NavTab.SETTINGS)
                                },
                                onQuickCommand = { cmd ->
                                    viewModel.setTab(NavTab.CHAT)
                                    viewModel.sendUserMessage(cmd)
                                },
                                onStateChange = { newState ->
                                    viewModel.setJarvisState(newState)
                                }
                            )

                            NavTab.CHAT -> ChatScreen(
                                messages = messages,
                                jarvisState = jarvisState,
                                onSendMessage = { viewModel.sendUserMessage(it) },
                                onCopyMessage = { viewModel.copyToClipboard(it) },
                                onSpeakMessage = { viewModel.speakText(it) },
                                onRetryMessage = { viewModel.retryLastMessage() },
                                onClearChat = { viewModel.repository.clearMessages() },
                                onVoiceClick = { viewModel.setTab(NavTab.VOICE) },
                                onVisionClick = { viewModel.openSubScreen(SubScreen.VISION) }
                            )

                            NavTab.VOICE -> com.example.jarvis.ui.screens.VoiceScreen(
                                jarvisState = jarvisState,
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                liveTranscript = liveTranscript,
                                lastResponse = lastResponse,
                                speechSupported = speechSupported,
                                isContinuousModeActive = isContinuousConversationActive,
                                isMicMuted = isMicMuted,
                                isSpeakerEnabled = isSpeakerEnabled,
                                onStartListening = {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                onStopListening = { viewModel.stopListening() },
                                onSpeakText = { text, rate, pitch ->
                                    viewModel.speakText(text, rate, pitch)
                                },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onToggleContinuousMode = { viewModel.toggleContinuousConversation() },
                                onToggleMicMute = { viewModel.toggleMicMute() },
                                onToggleSpeaker = { viewModel.toggleSpeaker() },
                                onInterruptAndListen = { viewModel.interruptAndListen() },
                                onNavigateVoiceSetup = { viewModel.openSubScreen(SubScreen.VOICE_SETUP) },
                                onNavigateVoiceProfiles = { viewModel.openSubScreen(SubScreen.VOICE_SELECTION) }
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
                                onNavigateVoiceProfiles = { viewModel.openSubScreen(SubScreen.VOICE_SELECTION) },
                                onNavigateAbout = { viewModel.openSubScreen(SubScreen.ABOUT) },
                                onNavigateDiagnostics = { viewModel.openSubScreen(SubScreen.DIAGNOSTICS) },
                                onNavigateNotifications = { viewModel.openSubScreen(SubScreen.NOTIFICATIONS) },
                                onNavigateSearch = { viewModel.openSubScreen(SubScreen.SEARCH) }
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
