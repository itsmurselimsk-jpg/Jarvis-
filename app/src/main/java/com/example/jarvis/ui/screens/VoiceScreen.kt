package com.example.jarvis.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.jarvis.ui.theme.JarvisSurfaceElevated
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
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSpeakText: (String, Float, Float) -> Unit,
    onStopSpeaking: () -> Unit,
    onNavigateVoiceSetup: () -> Unit = {},
    onNavigateVoiceProfiles: () -> Unit = {}
) {
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }
    var speechPitch by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // State Header
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF091222))
                .border(0.5.dp, JarvisBorder, RoundedCornerShape(20.dp))
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
                    fontSize = 12.sp,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Center Animated Orb
        JarvisOrb(
            size = 200.dp,
            state = jarvisState,
            onClick = {
                if (isListening) onStopListening() else onStartListening()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Voice Waveform
        VoiceWaveform(
            barCount = 24,
            height = 36.dp,
            isActive = isListening || isSpeaking,
            accentColor = if (isSpeaking) JarvisGreen else JarvisCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Support Warning (Honest message as required by prompt)
        if (!speechSupported) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33FF1744))
                    .border(1.dp, JarvisRed, RoundedCornerShape(10.dp))
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
                        text = "Voice support unavailable in this browser / environment.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Live Transcript Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090E1A))
                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "LIVE TRANSCRIPT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextDim,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        liveTranscript.isNotBlank() -> "\"$liveTranscript\""
                        isListening -> "Listening... Speak now, Sir."
                        else -> "Tap START LISTENING to engage acoustic frequency."
                    },
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (liveTranscript.isNotBlank()) JarvisCyanBright else JarvisTextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Last JARVIS Response Preview & Playback
        if (lastResponse.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF060B14))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAST SYNTHESIS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan
                        )
                        IconButton(
                            onClick = {
                                if (isSpeaking) onStopSpeaking() else onSpeakText(lastResponse, speechSpeed, speechPitch)
                            },
                            modifier = Modifier.size(24.dp)
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
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Voice Controls (Start / Stop)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isListening) onStopListening() else onStartListening()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("voice_toggle_button"),
                shape = RoundedCornerShape(10.dp),
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
                    fontSize = 12.sp
                )
            }

            if (isSpeaking) {
                OutlinedButton(
                    onClick = onStopSpeaking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop TTS")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SILENCE")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Speech Parameters Card (Speed & Pitch)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090E1A))
                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
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
                    Text("Speech Speed", fontSize = 12.sp, color = JarvisTextSecondary)
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
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090E1A))
                .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ADVANCED ACOUSTIC & PROFILE HUBS",
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
