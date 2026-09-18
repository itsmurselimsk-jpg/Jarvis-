package com.example.jarvis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.jarvis.model.AIProviderType
import com.example.jarvis.model.ActivityLog
import com.example.jarvis.model.DeviceTelemetry
import com.example.jarvis.model.JarvisState
import com.example.jarvis.model.JarvisTask
import com.example.jarvis.model.MemoryItem
import com.example.jarvis.model.ProviderSettings
import com.example.jarvis.ui.components.JarvisOrb
import com.example.jarvis.ui.components.VoiceWaveform
import com.example.jarvis.ui.theme.JarvisAmber
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisElectricBlue
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisRed
import com.example.jarvis.ui.theme.JarvisSurfaceElevated
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    jarvisState: JarvisState,
    telemetry: DeviceTelemetry,
    tasks: List<JarvisTask> = emptyList(),
    memories: List<MemoryItem> = emptyList(),
    logs: List<ActivityLog> = emptyList(),
    settings: ProviderSettings = ProviderSettings(),
    onVoiceClick: () -> Unit,
    onChatClick: () -> Unit,
    onToolsClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onActivityClick: () -> Unit,
    onVisionClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onBridgeClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onQuickCommand: (String) -> Unit,
    onStateChange: (JarvisState) -> Unit = {}
) {
    // Determine dynamic greeting based on real device time
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good morning, Sir."
        in 12..16 -> "Good afternoon, Sir."
        in 17..21 -> "Good evening, Sir."
        else -> "Good night, Sir."
    }

    val displayTime = telemetry.currentTimeString.ifBlank {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    val providerName = when (settings.providerType) {
        AIProviderType.GEMINI -> "Gemini Core"
        AIProviderType.OPENAI_COMPATIBLE -> "OpenAI Compatible"
        AIProviderType.LOCAL_NEURAL_BRAIN -> "On-Device Neural"
    }

    // Dismissible suggestions state
    val suggestions = remember {
        mutableStateListOf(
            "System ready: All telemetry links and security enclaves verified.",
            "Say 'Check device status' or 'Run diagnostics' for full hardware scan.",
            "Tap the central orb to initiate acoustic voice stream."
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // ==========================================
        // 1. HERO AREA: Identity, Status, Orb & Greeting
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official JARVIS Header Glass Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xEE0B1424),
                                    Color(0xCC060A14)
                                )
                            )
                        )
                        .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.jarvis_logo_round),
                                contentDescription = "Official JARVIS Logo",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, JarvisCyan.copy(alpha = 0.8f), CircleShape),
                                contentScale = ContentScale.Fit
                            )
                            Column {
                                Text(
                                    text = greeting,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisCyanBright,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(JarvisGreen)
                                    )
                                    Text(
                                        text = "$providerName • Ready",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = JarvisGreen
                                    )
                                }
                            }
                        }

                        // Real device time display
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = displayTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisCyan
                            )
                            Text(
                                text = telemetry.networkType.take(8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Atmospheric Glow Backdrop & Large Animated JARVIS Orb
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle radial atmospheric aura
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        JarvisCyan.copy(alpha = 0.14f),
                                        JarvisElectricBlue.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Center Interactive Orb
                    JarvisOrb(
                        size = 220.dp,
                        state = jarvisState,
                        onClick = {
                            val nextState = when (jarvisState) {
                                JarvisState.IDLE -> JarvisState.LISTENING
                                JarvisState.LISTENING -> JarvisState.SPEAKING
                                JarvisState.SPEAKING -> JarvisState.IDLE
                                else -> JarvisState.IDLE
                            }
                            onStateChange(nextState)
                        }
                    )
                }

                // Interactive Orb State Transition Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0B1424))
                        .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(22.dp))
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StateTransitionPill(
                        label = "IDLE",
                        icon = Icons.Default.PlayArrow,
                        isSelected = jarvisState == JarvisState.IDLE,
                        activeColor = JarvisCyan,
                        onClick = { onStateChange(JarvisState.IDLE) }
                    )
                    StateTransitionPill(
                        label = "LISTENING",
                        icon = Icons.Default.Mic,
                        isSelected = jarvisState == JarvisState.LISTENING,
                        activeColor = JarvisCyanBright,
                        onClick = { onStateChange(JarvisState.LISTENING) }
                    )
                    StateTransitionPill(
                        label = "SPEAKING",
                        icon = Icons.Default.VolumeUp,
                        isSelected = jarvisState == JarvisState.SPEAKING,
                        activeColor = JarvisElectricBlue,
                        onClick = { onStateChange(JarvisState.SPEAKING) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Response Prompt
                Text(
                    text = when (jarvisState) {
                        JarvisState.LISTENING -> "Acoustic sensor calibrated. Listening..."
                        JarvisState.THINKING -> "Synthesizing neural matrices & subroutines..."
                        JarvisState.SPEAKING -> "Transmitting acoustic vocal feedback..."
                        JarvisState.ERROR -> "Subsystem anomaly detected. Diagnostics active."
                        else -> "\"All subsystems operational. Awaiting your command, Sir.\""
                    },
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = if (jarvisState == JarvisState.IDLE) JarvisTextSecondary else JarvisCyanBright,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Voice Waveform if active
                AnimatedVisibility(visible = jarvisState == JarvisState.LISTENING || jarvisState == JarvisState.SPEAKING) {
                    VoiceWaveform(
                        modifier = Modifier.padding(top = 12.dp),
                        isActive = true,
                        accentColor = if (jarvisState == JarvisState.SPEAKING) JarvisGreen else JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Voice Action & Primary Chat Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onVoiceClick,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("home_voice_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Initialize Voice",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VOICE STREAM",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Button(
                        onClick = onChatClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("home_chat_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F1B2E),
                            contentColor = JarvisCyanBright
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chat Console",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONSOLE",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. HORIZONTALLY SCROLLABLE SECTION: Quick Actions
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "QUICK ACTIONS",
                subtitle = "Hardware tools & system protocols"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    QuickActionCard(
                        title = "Flashlight",
                        status = if (telemetry.isFlashlightOn) "Active" else "Off",
                        icon = if (telemetry.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        accentColor = if (telemetry.isFlashlightOn) JarvisAmber else JarvisCyan,
                        onClick = { onQuickCommand("toggle flashlight") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Battery",
                        status = "${telemetry.batteryPercent}% ${if (telemetry.isCharging) "Charging" else ""}".trim(),
                        icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        accentColor = if (telemetry.batteryPercent <= 20) JarvisRed else JarvisGreen,
                        onClick = { onQuickCommand("check battery status") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Wi-Fi / Net",
                        status = telemetry.networkType.take(8),
                        icon = Icons.Default.Wifi,
                        accentColor = JarvisCyan,
                        onClick = { onQuickCommand("audit network status") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Volume",
                        status = "${telemetry.volumePercent}%",
                        icon = Icons.Default.VolumeUp,
                        accentColor = JarvisElectricBlue,
                        onClick = { onQuickCommand("show current audio volume") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "Phone Search",
                        status = "Apps & Contacts",
                        icon = Icons.Default.Search,
                        accentColor = JarvisCyanBright,
                        onClick = onSearchClick
                    )
                }
                item {
                    QuickActionCard(
                        title = "Vision HUD",
                        status = "OCR & Inspect",
                        icon = Icons.Default.Visibility,
                        accentColor = JarvisCyan,
                        onClick = onVisionClick
                    )
                }
                item {
                    QuickActionCard(
                        title = "Memory",
                        status = "${memories.size} Items",
                        icon = Icons.Default.Psychology,
                        accentColor = JarvisAmber,
                        onClick = onMemoryClick
                    )
                }
                item {
                    QuickActionCard(
                        title = "Tasks",
                        status = "${tasks.count { !it.isCompleted }} Pending",
                        icon = Icons.Default.CheckCircle,
                        accentColor = JarvisGreen,
                        onClick = onTasksClick
                    )
                }
                item {
                    QuickActionCard(
                        title = "Diagnostics",
                        status = "Hardware Scan",
                        icon = Icons.Default.Build,
                        accentColor = JarvisElectricBlue,
                        onClick = onToolsClick
                    )
                }
                item {
                    QuickActionCard(
                        title = "Settings",
                        status = "Parameters",
                        icon = Icons.Default.Settings,
                        accentColor = JarvisTextSecondary,
                        onClick = onSettingsClick
                    )
                }
            }
        }

        // ==========================================
        // 3. HORIZONTALLY SCROLLABLE SECTION: Device Status
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "DEVICE TELEMETRY",
                subtitle = "Real-time hardware sensors and status"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    TelemetryCard(
                        metric = "BATTERY",
                        value = "${telemetry.batteryPercent}%",
                        detail = if (telemetry.isCharging) "Charging on AC/USB" else "Discharging",
                        statusColor = if (telemetry.batteryPercent > 20) JarvisGreen else JarvisRed
                    )
                }
                item {
                    TelemetryCard(
                        metric = "NETWORK",
                        value = telemetry.networkType,
                        detail = "Validated & Online",
                        statusColor = JarvisCyan
                    )
                }
                item {
                    TelemetryCard(
                        metric = "AI CORE",
                        value = settings.selectedModel.take(14),
                        detail = providerName,
                        statusColor = JarvisCyanBright
                    )
                }
                item {
                    TelemetryCard(
                        metric = "MEMORY / RAM",
                        value = "${telemetry.memoryAvailableMB} MB",
                        detail = "Available Buffer",
                        statusColor = JarvisElectricBlue
                    )
                }
                item {
                    TelemetryCard(
                        metric = "AUDIO LEVEL",
                        value = "${telemetry.volumePercent}%",
                        detail = "Master Media Stream",
                        statusColor = JarvisAmber
                    )
                }
                item {
                    TelemetryCard(
                        metric = "SECURITY ENCLAVE",
                        value = "ACTIVE",
                        detail = "Zero-Leak Masking",
                        statusColor = JarvisGreen
                    )
                }
            }
        }

        // ==========================================
        // 4. HORIZONTALLY SCROLLABLE SECTION: Recent Commands & Activity
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "RECENT COMMANDS",
                subtitle = "Execution history & security clearance",
                actionLabel = "VIEW ALL",
                onActionClick = onActivityClick
            )

            if (logs.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logs.take(6)) { log ->
                        Box(
                            modifier = Modifier
                                .width(230.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF091222))
                                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable(onClick = onActivityClick)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.type.name,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisCyan
                                    )
                                    Text(
                                        text = log.status.name,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (log.status.name == "SUCCESS") JarvisGreen else JarvisAmber
                                    )
                                }
                                Text(
                                    text = log.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JarvisTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = log.detail,
                                    fontSize = 11.sp,
                                    color = JarvisTextDim,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF080E1A))
                        .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent commands executed yet. Say or type a command to begin.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ==========================================
        // 5. HORIZONTALLY SCROLLABLE SECTION: Tasks & Directives
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "TASKS & REMINDERS",
                subtitle = "${tasks.count { !it.isCompleted }} pending directives",
                actionLabel = "MANAGE",
                onActionClick = onTasksClick
            )

            if (tasks.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks.take(5)) { task ->
                        Box(
                            modifier = Modifier
                                .width(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF091222))
                                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable(onClick = onTasksClick)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (task.isCompleted) JarvisGreen else JarvisCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = task.priority.uppercase(),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.priority == "High") JarvisRed else JarvisCyan
                                    )
                                }
                                Text(
                                    text = task.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JarvisTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (task.notes.isNotBlank()) task.notes else "No notes specified",
                                    fontSize = 11.sp,
                                    color = JarvisTextDim,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF080E1A))
                        .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending tasks. Tell JARVIS to schedule or remind you of directives.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ==========================================
        // 6. HORIZONTALLY SCROLLABLE SECTION: Memory Enclave
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "NEURAL MEMORY",
                subtitle = "${memories.size} indexed records in secure enclave",
                actionLabel = "EXPLORE",
                onActionClick = onMemoryClick
            )

            if (memories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memories.take(5)) { memory ->
                        Box(
                            modifier = Modifier
                                .width(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF091222))
                                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable(onClick = onMemoryClick)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = memory.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisAmber
                                )
                                Text(
                                    text = memory.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JarvisTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = memory.content,
                                    fontSize = 11.sp,
                                    color = JarvisTextDim,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF080E1A))
                        .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Memory enclave is empty. Say 'Remember that...' to store custom directives.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ==========================================
        // 7. DISMISSIBLE SUGGESTIONS
        // ==========================================
        if (suggestions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "OPERATIONAL SUGGESTIONS",
                    subtitle = "Actionable intelligence & tips"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEachIndexed { index, suggestion ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF081220))
                                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.sp,
                                        color = JarvisTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }

                                IconButton(
                                    onClick = { suggestions.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = JarvisTextDim,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = JarvisCyan
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = JarvisTextDim
            )
        }

        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = actionLabel,
                    tint = JarvisCyan,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    status: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(115.dp)
            .height(105.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1424),
                        Color(0xFF060B14)
                    )
                )
            )
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = JarvisTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = status,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TelemetryCard(
    metric: String,
    value: String,
    detail: String,
    statusColor: Color
) {
    Box(
        modifier = Modifier
            .width(155.dp)
            .height(95.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1424),
                        Color(0xFF060B14)
                    )
                )
            )
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextDim
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = statusColor
            )

            Text(
                text = detail,
                fontSize = 10.sp,
                color = JarvisTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StateTransitionPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) activeColor.copy(alpha = 0.18f) else Color.Transparent
    val border = if (isSelected) activeColor.copy(alpha = 0.8f) else Color.Transparent
    val textColor = if (isSelected) activeColor else JarvisTextDim

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp,
                color = textColor
            )
        }
    }
}
