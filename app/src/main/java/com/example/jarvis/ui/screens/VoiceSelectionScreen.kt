package com.example.jarvis.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.ProviderSettings
import com.example.jarvis.ui.theme.JarvisAmber
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisSurfaceElevated
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary
import com.example.jarvis.voice.SupportedLanguage
import com.example.jarvis.voice.VoiceProfileType

@Composable
fun VoiceSelectionScreen(
    currentSettings: ProviderSettings,
    onUpdateSettings: (ProviderSettings) -> Unit,
    onTestSpeak: (String, Float, Float) -> Unit
) {
    val profiles = VoiceProfileType.values().toList()
    var languageDropdownOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "VOCAL PROFILES & SYNTHESIS CALIBRATION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyan
                )
                Text(
                    text = "Configure acoustic persona, resonance, and multilingual phonetics",
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }
        }

        // Language Mode Dropdown Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E1A))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Multilingual Recognition & Speech",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                                Text(
                                    text = "English, Bengali, and Hindi supported seamlessly",
                                    fontSize = 10.sp,
                                    color = JarvisTextDim
                                )
                            }
                        }

                        Box {
                            val activeLang = SupportedLanguage.fromCode(currentSettings.languageCode)
                            OutlinedButton(
                                onClick = { languageDropdownOpen = true },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, JarvisCyan),
                                modifier = Modifier.testTag("language_selector_button")
                            ) {
                                Text(
                                    text = activeLang.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisCyan
                                )
                            }

                            DropdownMenu(
                                expanded = languageDropdownOpen,
                                onDismissRequest = { languageDropdownOpen = false }
                            ) {
                                SupportedLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${lang.displayName} (${lang.nativeName})",
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            onUpdateSettings(currentSettings.copy(languageCode = lang.code))
                                            languageDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Voice Profiles Section
        item {
            Text(
                text = "SELECTABLE VOCAL PROFILES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextSecondary
            )
        }

        items(profiles) { profile ->
            val isSelected = currentSettings.voiceProfileName == profile.profileName
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF0A1E30) else Color(0xFF090E1A))
                    .border(
                        if (isSelected) 1.dp else 0.5.dp,
                        if (isSelected) JarvisCyan else JarvisBorderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onUpdateSettings(currentSettings.copy(voiceProfileName = profile.profileName))
                    }
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) JarvisCyan else Color(0xFF14223A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.Black else JarvisCyan
                                )
                            }
                            Column {
                                Text(
                                    text = profile.profileName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) JarvisCyanBright else JarvisTextPrimary
                                )
                                Text(
                                    text = profile.tagline,
                                    fontSize = 10.sp,
                                    color = JarvisTextDim
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = profile.description,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speed: ${profile.defaultSpeed}x  •  Pitch: ${profile.defaultPitch}x",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextDim
                        )

                        Button(
                            onClick = {
                                val testPhrase = when (SupportedLanguage.fromCode(currentSettings.languageCode)) {
                                    SupportedLanguage.BENGALI -> "আমি জারভিস। সব সিস্টেম স্বাভাবিক এবং কার্যকর।"
                                    SupportedLanguage.HINDI -> "मैं जार्विस हूँ। सभी सिस्टम सामान्य और चालू हैं।"
                                    else -> "JARVIS vocal matrix online and ready for deployment, Sir."
                                }
                                onTestSpeak(
                                    testPhrase,
                                    profile.defaultSpeed * currentSettings.speechRate,
                                    profile.defaultPitch * currentSettings.speechPitch
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) JarvisCyan else Color(0xFF1B2A47)
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) Color.Black else JarvisCyan
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Preview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else JarvisCyan
                            )
                        }
                    }
                }
            }
        }

        // Custom Fine Tuning
        item {
            Text(
                text = "ACOUSTIC FINE-TUNING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextSecondary
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E1A))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Speech Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speech Cadence (Speed)", fontSize = 12.sp, color = JarvisTextPrimary)
                            Text(
                                "${String.format("%.2f", currentSettings.speechRate)}x",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisCyan
                            )
                        }
                        Slider(
                            value = currentSettings.speechRate,
                            onValueChange = { onUpdateSettings(currentSettings.copy(speechRate = it)) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = Color(0xFF14243B)
                            )
                        )
                    }

                    // Speech Pitch Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Modulation (Pitch)", fontSize = 12.sp, color = JarvisTextPrimary)
                            Text(
                                "${String.format("%.2f", currentSettings.speechPitch)}x",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisCyan
                            )
                        }
                        Slider(
                            value = currentSettings.speechPitch,
                            onValueChange = { onUpdateSettings(currentSettings.copy(speechPitch = it)) },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = Color(0xFF14243B)
                            )
                        )
                    }
                }
            }
        }
    }
}
