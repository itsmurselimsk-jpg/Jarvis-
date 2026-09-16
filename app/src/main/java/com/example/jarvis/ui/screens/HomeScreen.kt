package com.example.jarvis.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.jarvis.model.DeviceTelemetry
import com.example.jarvis.model.JarvisState
import com.example.jarvis.ui.components.JarvisOrb
import com.example.jarvis.ui.components.VoiceWaveform
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisElectricBlue
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisSurfaceElevated
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary

@Composable
fun HomeScreen(
    jarvisState: JarvisState,
    telemetry: DeviceTelemetry,
    onVoiceClick: () -> Unit,
    onChatClick: () -> Unit,
    onToolsClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onActivityClick: () -> Unit,
    onVisionClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onBridgeClick: () -> Unit,
    onQuickCommand: (String) -> Unit,
    onStateChange: (JarvisState) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1. Central Animated JARVIS Orb with multi-state transition
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                JarvisOrb(
                    size = 230.dp,
                    state = jarvisState,
                    onClick = {
                        // Cycle through states on tap: IDLE -> LISTENING -> SPEAKING -> IDLE
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
        }

        // 2. Interactive State Transition Switcher (Idle / Listening / Speaking)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "ORB STATE TRANSITION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    color = JarvisTextDim
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisBorderSubtle, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StateTransitionChip(
                        label = "IDLE",
                        icon = Icons.Default.PlayArrow,
                        isSelected = jarvisState == JarvisState.IDLE,
                        activeColor = JarvisCyan,
                        onClick = { onStateChange(JarvisState.IDLE) }
                    )
                    StateTransitionChip(
                        label = "LISTENING",
                        icon = Icons.Default.Mic,
                        isSelected = jarvisState == JarvisState.LISTENING,
                        activeColor = JarvisCyanBright,
                        onClick = { onStateChange(JarvisState.LISTENING) }
                    )
                    StateTransitionChip(
                        label = "SPEAKING",
                        icon = Icons.Default.VolumeUp,
                        isSelected = jarvisState == JarvisState.SPEAKING,
                        activeColor = JarvisElectricBlue,
                        onClick = { onStateChange(JarvisState.SPEAKING) }
                    )
                }
            }
        }

        // 3. Status Typography & Prompt
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "JARVIS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    color = JarvisTextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(JarvisGreen)
                    )
                    Text(
                        text = "ONLINE — CORE OPERATING LAYER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        color = JarvisGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (jarvisState) {
                        JarvisState.LISTENING -> "Listening to audio frequency..."
                        JarvisState.THINKING -> "Synthesizing neural matrix..."
                        JarvisState.SPEAKING -> "Transmitting vocal response..."
                        JarvisState.ERROR -> "Subsystem attention required."
                        else -> "\"How can I help you, Sir?\""
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (jarvisState == JarvisState.IDLE) JarvisCyanBright else JarvisCyan,
                    textAlign = TextAlign.Center
                )

                // Voice Waveform if active
                AnimatedVisibility(visible = jarvisState == JarvisState.LISTENING || jarvisState == JarvisState.SPEAKING) {
                    VoiceWaveform(
                        modifier = Modifier.padding(top = 14.dp),
                        isActive = true,
                        accentColor = JarvisCyan
                    )
                }
            }
        }

        // 3. Primary Buttons: VOICE, CHAT, TOOLS
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // VOICE Button
                PrimaryNavButton(
                    modifier = Modifier.weight(1f),
                    label = "VOICE",
                    icon = Icons.Default.Mic,
                    tag = "home_voice_button",
                    onClick = onVoiceClick
                )

                // CHAT Button
                PrimaryNavButton(
                    modifier = Modifier.weight(1f),
                    label = "CHAT",
                    icon = Icons.Default.ChatBubble,
                    tag = "home_chat_button",
                    onClick = onChatClick
                )

                // TOOLS Button
                PrimaryNavButton(
                    modifier = Modifier.weight(1f),
                    label = "TOOLS",
                    icon = Icons.Default.Extension,
                    tag = "home_tools_button",
                    onClick = onToolsClick
                )
            }
        }

        // 4. Secondary Quick Operating Layer Modules
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniModuleChip(
                    modifier = Modifier.weight(1f),
                    label = "Memory",
                    icon = Icons.Default.Psychology,
                    onClick = onMemoryClick
                )
                MiniModuleChip(
                    modifier = Modifier.weight(1f),
                    label = "Vision",
                    icon = Icons.Default.Visibility,
                    onClick = onVisionClick
                )
                MiniModuleChip(
                    modifier = Modifier.weight(1f),
                    label = "Privacy",
                    icon = Icons.Default.Security,
                    onClick = onPrivacyClick
                )
                MiniModuleChip(
                    modifier = Modifier.weight(1f),
                    label = "Bridge",
                    icon = Icons.Default.Bolt,
                    onClick = onBridgeClick
                )
            }
        }

        // 5. Recent Commands & Quick Protocol Triggers
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "DISPATCH PROTOCOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = JarvisTextDim
                )

                Spacer(modifier = Modifier.height(10.dp))

                QuickCommandCard(
                    title = "System Telemetry Diagnostic",
                    subtitle = "Verify battery, radio frequencies, and memory buffers",
                    onClick = { onQuickCommand("System status and telemetry report") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickCommandCard(
                    title = "Chrono & Meteorological Sync",
                    subtitle = "Check current time and atmospheric forecast",
                    onClick = { onQuickCommand("What is the current time and weather forecast?") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickCommandCard(
                    title = "Encrypted Memory Verification",
                    subtitle = "Inspect and query user-approved memory indexes",
                    onClick = onMemoryClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickCommandCard(
                    title = "Operating Layer Activity Log",
                    subtitle = "Review tool executions and security checkpoints",
                    onClick = onActivityClick
                )
            }
        }
    }
}

@Composable
private fun PrimaryNavButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF091222))
            .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JarvisCyan,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = JarvisTextPrimary
            )
        }
    }
}

@Composable
private fun MiniModuleChip(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF080D18))
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
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
                modifier = Modifier.size(13.dp)
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
private fun QuickCommandCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCC090E1A))
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = JarvisTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = JarvisTextDim,
                    lineHeight = 14.sp
                )
            }
            Text(
                text = "EXECUTE →",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = JarvisCyan
            )
        }
    }
}

@Composable
private fun StateTransitionChip(
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
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = textColor
            )
        }
    }
}
