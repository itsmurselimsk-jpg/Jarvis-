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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.JarvisState
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
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary

@Composable
fun VoiceScreen(
    jarvisState: JarvisState,
    isListening: Boolean,
    isSpeaking: Boolean,
    liveTranscript: String,
    lastResponse: String,
    speechSupported: Boolean,
    isContinuousModeActive: Boolean = false,
    isMicMuted: Boolean = false,
    isSpeakerEnabled: Boolean = true,
    wakeWordStatus: String = "Active (Hey JARVIS)",
    currentLanguage: String = "Multilingual (EN/HI/BN)",
    currentVoiceProfile: String = "JARVIS Natural",
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSpeakText: (String, Float, Float) -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleContinuousMode: () -> Unit = {},
    onToggleMicMute: () -> Unit = {},
    onToggleSpeaker: () -> Unit = {},
    onInterruptAndListen: () -> Unit = {},
    onNavigateVoiceSetup: () -> Unit = {},
    onNavigateVoiceProfiles: () -> Unit = {}
) {
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }
    var speechPitch by remember { mutableFloatStateOf(1.0f) }
    var showGesturePanel by remember { mutableStateOf(false) }
    var isGestureActive by remember { mutableStateOf(false) }
    var gestureModeText by remember { mutableStateOf("STANDBY") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Glass Status Capsule & Quick Mute/Speaker Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-Contrast State Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF091424))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (jarvisState) {
                                    JarvisState.LISTENING -> JarvisCyanBright
                                    JarvisState.THINKING -> JarvisElectricBlue
                                    JarvisState.SPEAKING -> JarvisGreen
                                    JarvisState.ERROR -> JarvisRed
                                    else -> JarvisCyan
                                }
                            )
                    )
                    Text(
                        text = "STATE: ${jarvisState.name}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = when (jarvisState) {
                            JarvisState.LISTENING -> JarvisCyanBright
                            JarvisState.THINKING -> JarvisElectricBlue
                            JarvisState.SPEAKING -> JarvisGreen
                            JarvisState.ERROR -> JarvisRed
                            else -> JarvisCyan
                        }
                    )
                }
            }

            // Quick Mute & Speaker Toggles (Always accessible)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Mic Mute Button
                IconButton(
                    onClick = onToggleMicMute,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isMicMuted) JarvisRed.copy(alpha = 0.2f) else Color(0xFF0D1B2E))
                        .border(1.dp, if (isMicMuted) JarvisRed else JarvisBorderSubtle, CircleShape)
                        .testTag("toggle_mic_mute_button")
                ) {
                    Icon(
                        imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Mic Mute",
                        tint = if (isMicMuted) JarvisRed else JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Speaker Toggle Button
                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (!isSpeakerEnabled) JarvisRed.copy(alpha = 0.2f) else Color(0xFF0D1B2E))
                        .border(1.dp, if (!isSpeakerEnabled) JarvisRed else JarvisBorderSubtle, CircleShape)
                        .testTag("toggle_speaker_button")
                ) {
                    Icon(
                        imageVector = if (!isSpeakerEnabled) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Speaker",
                        tint = if (!isSpeakerEnabled) JarvisRed else JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Ultron Holographic HUD / Gestures Toggle Button
                IconButton(
                    onClick = { showGesturePanel = !showGesturePanel },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (showGesturePanel) JarvisCyan.copy(alpha = 0.25f) else Color(0xFF0D1B2E))
                        .border(1.dp, if (showGesturePanel) JarvisCyanBright else JarvisBorderSubtle, CircleShape)
                        .testTag("toggle_hologram_hud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "Toggle Hologram Gestures HUD",
                        tint = if (showGesturePanel) JarvisCyanBright else JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Atmospheric Aura & Animated Orb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                JarvisCyan.copy(alpha = 0.12f),
                                JarvisElectricBlue.copy(alpha = 0.04f),
                                Color.Transparent
                            )
                        )
                    )
            )

            JarvisOrb(
                size = 210.dp,
                state = jarvisState,
                onClick = {
                    if (isSpeaking) {
                        onInterruptAndListen()
                    } else if (isListening) {
                        onStopListening()
                    } else {
                        onStartListening()
                    }
                }
            )
        }

        if (showGesturePanel) {
            Spacer(modifier = Modifier.height(10.dp))
            com.example.jarvis.ui.components.GestureCameraPanel(
                isVisible = showGesturePanel,
                isGestureActive = isGestureActive,
                gestureMode = gestureModeText,
                onToggleGesture = {
                    isGestureActive = !isGestureActive
                    gestureModeText = if (isGestureActive) "1 HAND • SPIN" else "STANDBY"
                },
                onZoomIn = {},
                onZoomOut = {},
                onReset = {
                    isGestureActive = false
                    gestureModeText = "STANDBY"
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Waveform
        VoiceWaveform(
            barCount = 28,
            height = 38.dp,
            isActive = isListening || isSpeaking,
            accentColor = if (isSpeaking) JarvisGreen else JarvisCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Continuous S2S Mode Banner & Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isContinuousModeActive) {
                        Brush.horizontalGradient(
                            listOf(
                                JarvisCyan.copy(alpha = 0.18f),
                                Color(0xFF0E1A2E)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0B1424),
                                Color(0xFF060B14)
                            )
                        )
                    }
                )
                .border(
                    1.dp,
                    if (isContinuousModeActive) JarvisCyan else JarvisBorderSubtle,
                    RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onToggleContinuousMode)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("toggle_s2s_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (isContinuousModeActive) JarvisCyan else JarvisTextDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isContinuousModeActive) "CONTINUOUS S2S: ACTIVE" else "CONTINUOUS S2S: STANDBY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isContinuousModeActive) JarvisCyan else JarvisTextPrimary
                        )
                        Text(
                            text = if (isContinuousModeActive) "Auto-listens after vocal transmission" else "Tap to engage continuous speech loop",
                            fontSize = 10.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }
                Text(
                    text = if (isContinuousModeActive) "ON" else "OFF",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isContinuousModeActive) JarvisGreen else JarvisCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Unsupported Speech Warning
        if (!speechSupported) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FF1744))
                    .border(1.dp, JarvisRed, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Voice Warning",
                        tint = JarvisRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Voice synthesis or capture service unavailable on this host.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Live Transcript Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B1424),
                            Color(0xFF060B14)
                        )
                    )
                )
                .border(1.dp, JarvisCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "ACOUSTIC TRANSCRIPT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        liveTranscript.isNotBlank() -> "\"$liveTranscript\""
                        isListening -> "Listening to operator... Speak freely."
                        else -> "Tap START LISTENING to engage acoustic link."
                    },
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (liveTranscript.isNotBlank()) JarvisCyanBright else JarvisTextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Last JARVIS Response Preview
        if (lastResponse.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF08101E))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIOUS SYNTHESIS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan
                        )
                        IconButton(
                            onClick = {
                                if (isSpeaking) onStopSpeaking() else onSpeakText(lastResponse, speechSpeed, speechPitch)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Playback",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastResponse.take(220) + if (lastResponse.length > 220) "..." else "",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Primary Voice Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (isListening) onStopListening() else onStartListening()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("voice_toggle_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) JarvisRed else JarvisCyan,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isListening) "STOP LISTENING" else "START LISTENING",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            if (isSpeaking) {
                Button(
                    onClick = onInterruptAndListen,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("interrupt_and_speak_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisAmber,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.Hearing, contentDescription = "Interrupt", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("INTERRUPT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onStopSpeaking,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop TTS")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Vocal Modulation Sliders Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF091222))
                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "VOCAL SYNTHESIZER CALIBRATION",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextDim
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Speech Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Rate", fontSize = 12.sp, color = JarvisTextSecondary)
                    Text("${String.format("%.1f", speechSpeed)}x", fontSize = 12.sp, color = JarvisCyan, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = speechSpeed,
                    onValueChange = { speechSpeed = it },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    )
                )

                // Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vocal Modulation Pitch", fontSize = 12.sp, color = JarvisTextSecondary)
                    Text("${String.format("%.1f", speechPitch)}x", fontSize = 12.sp, color = JarvisCyan, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = speechPitch,
                    onValueChange = { speechPitch = it },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Navigation to Acoustic Wake Engine & Vocal Profiles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF091222))
                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACOUSTIC PROTOCOLS & PROFILES",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextDim
                )

                // 1. Acoustic Wake Engine Setup
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateVoiceSetup)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Always-Available 'Hey JARVIS' Setup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
                            Text("Service state, background permissions & overlay", fontSize = 10.sp, color = JarvisTextDim)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = JarvisTextDim, modifier = Modifier.size(18.dp))
                }

                // 2. Vocal Profiles & Multilingual Calibration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateVoiceProfiles)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Selectable Vocal Profiles & Languages", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
                            Text("5 tonal calibrations & English/Bengali/Hindi tuning", fontSize = 10.sp, color = JarvisTextDim)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = JarvisTextDim, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
