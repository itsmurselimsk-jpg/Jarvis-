package com.example.jarvis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.AIProviderType
import com.example.jarvis.model.ActivityLog
import com.example.jarvis.model.DeviceTelemetry
import com.example.jarvis.model.JarvisState
import com.example.jarvis.model.JarvisTask
import com.example.jarvis.model.MemoryItem
import com.example.jarvis.model.ProviderSettings
import com.example.jarvis.ui.components.JarvisOrb
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
    var quickInputText by remember { mutableStateOf("") }

    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good morning, Sir."
        in 12..16 -> "Good afternoon, Sir."
        in 17..21 -> "Good evening, Sir."
        else -> "Good night, Sir."
    }

    val stateColor by animateColorAsState(
        targetValue = when (jarvisState) {
            JarvisState.IDLE -> JarvisCyan
            JarvisState.LISTENING -> JarvisCyanBright
            JarvisState.THINKING -> JarvisAmber
            JarvisState.SPEAKING -> JarvisGreen
            JarvisState.ERROR -> JarvisRed
        },
        label = "stateColor"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Header: JARVIS Title & Status Indicators
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "J.A.R.V.I.S. • ONLINE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            color = JarvisCyanBright
                        )
                        Text(
                            text = "$greeting Neural Matrix Operational",
                            fontSize = 12.sp,
                            color = JarvisTextSecondary
                        )
                    }

                    // Telemetry Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCC0D1526))
                            .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(stateColor)
                            )
                            Text(
                                text = jarvisState.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = stateColor
                            )
                        }
                    }
                }

                // Telemetry Quick Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryPill(
                        icon = Icons.Default.BatteryFull,
                        label = "${telemetry.batteryPercent}%",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryPill(
                        icon = Icons.Default.Wifi,
                        label = telemetry.networkType.uppercase(),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryPill(
                        icon = Icons.Default.Speed,
                        label = "${telemetry.memoryAvailableMB}MB FREE",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Large Animated JARVIS Orb in Center
        item {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                JarvisOrb(
                    state = jarvisState,
                    size = 230.dp,
                    onClick = onVoiceClick
                )
            }
        }

        // 3. Short Assistant Glass Card Message
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xDC0C1425),
                                Color(0xF0060A14)
                            )
                        )
                    )
                    .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (jarvisState) {
                        JarvisState.IDLE -> "Systems fully operational, Sir. Tap the microphone or enter a command below."
                        JarvisState.LISTENING -> "Listening... Speak your command now, Sir."
                        JarvisState.THINKING -> "Processing neural intent and querying executive tool matrix..."
                        JarvisState.SPEAKING -> "Synthesizing response and executing requested workflows."
                        JarvisState.ERROR -> "Safety or system exception detected. Tap to retry."
                    },
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = JarvisTextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // 4. Large Microphone Button & Compact Input Bar
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Glowing Microphone Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    JarvisCyanBright,
                                    JarvisCyan,
                                    JarvisElectricBlue
                                )
                            )
                        )
                        .clickable { onVoiceClick() }
                        .testTag("home_large_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Compact Command Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quickInputText,
                        onValueChange = { quickInputText = it },
                        placeholder = {
                            Text(
                                "Enter command or ask JARVIS...",
                                fontSize = 13.sp,
                                color = JarvisTextDim
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_quick_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderSubtle,
                            focusedContainerColor = Color(0xFF090E1A),
                            unfocusedContainerColor = Color(0xFF090E1A),
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
                            .testTag("home_quick_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 5. Small Quick Actions Grid
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "EXECUTIVE MODULES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionChip(
                            icon = Icons.Default.ChatBubble,
                            label = "Chat",
                            onClick = onChatClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.GraphicEq,
                            label = "Voice",
                            onClick = onVoiceClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.Build,
                            label = "Tools",
                            onClick = onToolsClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.Folder,
                            label = "Files",
                            onClick = onToolsClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.AutoMode,
                            label = "Automation",
                            onClick = onTasksClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.Psychology,
                            label = "Memory",
                            onClick = onMemoryClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.Visibility,
                            label = "Vision",
                            onClick = onVisionClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.Shield,
                            label = "Privacy",
                            onClick = onPrivacyClick
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Default.Smartphone,
                            label = "Bridge",
                            onClick = onBridgeClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC090F1E))
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JarvisCyan,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextSecondary
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xEE0B1222))
            .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JarvisCyanBright,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextPrimary
            )
        }
    }
}
