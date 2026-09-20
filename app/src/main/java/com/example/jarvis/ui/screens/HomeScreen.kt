package com.example.jarvis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.ActivityLog
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.DeviceTelemetry
import com.example.jarvis.model.JarvisState
import com.example.jarvis.model.JarvisTask
import com.example.jarvis.model.MemoryItem
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.ProviderSettings
import com.example.jarvis.storage.db.ExpenseEntity
import com.example.jarvis.storage.db.HabitEntity
import com.example.jarvis.ui.components.HolographicCoreHero
import com.example.jarvis.ui.theme.JarvisAmber
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisElectricBlue
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisPurpleHighlight
import com.example.jarvis.ui.theme.JarvisRed
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary
import com.example.jarvis.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    jarvisState: JarvisState,
    telemetry: DeviceTelemetry,
    isListening: Boolean,
    isSpeaking: Boolean,
    liveTranscript: String,
    lastResponse: String,
    messages: List<ChatMessage> = emptyList(),
    tasks: List<JarvisTask> = emptyList(),
    memories: List<MemoryItem> = emptyList(),
    logs: List<ActivityLog> = emptyList(),
    settings: ProviderSettings = ProviderSettings(),
    expenses: List<ExpenseEntity> = emptyList(),
    habits: List<HabitEntity> = emptyList(),
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    rmsDb: Float = 0f,
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
    onArmorClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    onHabitClick: () -> Unit = {},
    onCodeStudioClick: () -> Unit = {},
    onTimerClick: () -> Unit = {},
    onVoiceNotesClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onDiagnosticsClick: () -> Unit = {},
    onQuickCommand: (String) -> Unit
) {
    val context = LocalContext.current
    val activeArmorTheme by ThemeManager.currentTheme.collectAsState()
    var quickInputText by remember { mutableStateOf("") }
    var isConversationExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val currentDateStr = remember { dateFormat.format(Date()) }
    val currentTimeStr = remember { timeFormat.format(Date()) }

    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good morning, Sir."
        in 12..16 -> "Good afternoon, Sir."
        in 17..21 -> "Good evening, Sir."
        else -> "Good night, Sir."
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TOP HUD TELEMETRY HEADER
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(JarvisCyanBright)
                            )
                            Text(
                                text = "J.A.R.V.I.S. 2.0 // ONLINE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                color = JarvisCyanBright
                            )
                        }
                        Text(
                            text = "$greeting $currentDateStr",
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                    }

                    // Real-time HUD Clock Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC090F1E))
                            .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentTimeStr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisCyan
                        )
                    }
                }

                // Telemetry Quick Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HudTelemetryPill(
                        icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        label = "${telemetry.batteryPercent}% ${if (telemetry.isCharging) "⚡" else ""}",
                        color = if (telemetry.batteryPercent > 20) JarvisCyanBright else JarvisAmber,
                        modifier = Modifier.weight(1f)
                    )
                    HudTelemetryPill(
                        icon = Icons.Default.Wifi,
                        label = telemetry.networkType.uppercase(),
                        color = JarvisElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    HudTelemetryPill(
                        icon = Icons.Default.Speed,
                        label = "${telemetry.memoryAvailableMB}MB RAM",
                        color = JarvisCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. MAIN HOLOGRAPHIC AI CORE (Centerpiece of the Home Screen)
        item {
            HolographicCoreHero(
                state = jarvisState,
                isListening = isListening,
                isSpeaking = isSpeaking,
                liveTranscript = liveTranscript,
                lastResponse = lastResponse,
                rmsDb = rmsDb,
                coreSize = 250.dp,
                onCoreClick = onVoiceClick
            )
        }

        // 3. VOICE-FIRST INTERACTION & QUICK COMMAND DOCK
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Glowing Voice Trigger Arc Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isListening) listOf(JarvisRed, JarvisAmber, JarvisCyan)
                                else listOf(JarvisCyanBright, JarvisCyan, JarvisElectricBlue)
                            )
                        )
                        .clickable { onVoiceClick() }
                        .testTag("home_voice_trigger_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Activation",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = if (isListening) "LISTENING • TAP TO CANCEL" else "TAP CORE OR MIC TO SPEAK",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isListening) JarvisAmber else JarvisCyan.copy(alpha = 0.8f)
                )

                // Quick Command Text Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quickInputText,
                        onValueChange = { quickInputText = it },
                        placeholder = {
                            Text(
                                "Ask JARVIS or give a command...",
                                fontSize = 12.sp,
                                color = JarvisTextDim
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_quick_command_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderSubtle,
                            focusedContainerColor = Color(0x99060F1E),
                            unfocusedContainerColor = Color(0x99060F1E),
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (quickInputText.isNotBlank()) {
                                onQuickCommand(quickInputText)
                                quickInputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan)
                            .testTag("home_quick_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Command",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. EXPANDABLE CONVERSATION STREAM PREVIEW
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC07101E))
                    .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isConversationExpanded = !isConversationExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Conversation",
                            tint = JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "CONVERSATION STREAM (${messages.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisCyan
                        )
                    }

                    Icon(
                        imageVector = if (isConversationExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Expand",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recent message preview when collapsed
                if (!isConversationExpanded) {
                    val lastMsg = messages.lastOrNull()
                    if (lastMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${if (lastMsg.sender == MessageSender.USER) "OPERATOR:" else "JARVIS:"} ${lastMsg.text}",
                            fontSize = 11.sp,
                            color = if (lastMsg.sender == MessageSender.USER) JarvisCyanBright else JarvisTextPrimary,
                            maxLines = 2
                        )
                    }
                }

                // Expanded conversation view
                AnimatedVisibility(
                    visible = isConversationExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val recentMessages = messages.takeLast(4)
                        if (recentMessages.isEmpty()) {
                            Text(
                                text = "No recent messages. Speak or type to begin.",
                                fontSize = 11.sp,
                                color = JarvisTextDim
                            )
                        } else {
                            recentMessages.forEach { msg ->
                                val isUser = msg.sender == MessageSender.USER
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isUser) Color(0x3300B0FF) else Color(0x3300E5FF))
                                        .border(0.5.dp, if (isUser) JarvisElectricBlue else JarvisCyan, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isUser) "// OPERATOR" else "// J.A.R.V.I.S.",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) JarvisElectricBlue else JarvisCyanBright
                                        )
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.sp,
                                            color = JarvisTextPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "OPEN FULL CHAT TAB →",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyanBright,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable { onChatClick() }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. STARK PROTOCOLS & QUICK DISPATCH
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "STARK PROTOCOLS // QUICK DISPATCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    color = JarvisCyan
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ProtocolChip(
                            title = "Protocol Zero",
                            desc = "Full System Sync",
                            icon = Icons.Default.Bolt,
                            color = JarvisCyanBright,
                            onClick = { onQuickCommand("JARVIS, protocol zero") }
                        )
                    }
                    item {
                        ProtocolChip(
                            title = "Morning Brief",
                            desc = "Agenda & Weather",
                            icon = Icons.Default.WbSunny,
                            color = Color(0xFFFFD700),
                            onClick = { onQuickCommand("JARVIS, morning protocol") }
                        )
                    }
                    item {
                        ProtocolChip(
                            title = "Focus Lock",
                            desc = "Pomodoro matrix",
                            icon = Icons.Default.Adjust,
                            color = JarvisPurpleHighlight,
                            onClick = { onQuickCommand("JARVIS, focus mode") }
                        )
                    }
                    item {
                        ProtocolChip(
                            title = "Night Standby",
                            desc = "Quiet mode",
                            icon = Icons.Default.Bedtime,
                            color = Color(0xFF90CAF9),
                            onClick = { onQuickCommand("JARVIS, night protocol") }
                        )
                    }
                    item {
                        ProtocolChip(
                            title = "Diagnostics",
                            desc = "Health Scan",
                            icon = Icons.Default.Speed,
                            color = JarvisElectricBlue,
                            onClick = { onQuickCommand("JARVIS, diagnostic scan") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudTelemetryPill(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC090F1E))
            .border(0.5.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = JarvisTextPrimary
            )
        }
    }
}

@Composable
private fun ProtocolChip(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDE081124))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary
                )
                Text(
                    text = desc,
                    fontSize = 9.sp,
                    color = JarvisTextSecondary
                )
            }
        }
    }
}
