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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.AIProviderType
import com.example.jarvis.model.ProviderSettings
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

@Composable
fun SettingsScreen(
    currentSettings: ProviderSettings,
    onSaveSettings: (ProviderSettings) -> Unit,
    onNavigatePrivacy: () -> Unit,
    onNavigateMemory: () -> Unit,
    onNavigateBridge: () -> Unit,
    onNavigateActivity: () -> Unit,
    onNavigateVision: () -> Unit
) {
    var providerType by remember { mutableStateOf(currentSettings.providerType) }
    var customApiKey by remember { mutableStateOf(currentSettings.customApiKey) }
    var customEndpoint by remember { mutableStateOf(currentSettings.customEndpoint) }
    var selectedModel by remember { mutableStateOf(currentSettings.selectedModel) }
    var systemPrompt by remember { mutableStateOf(currentSettings.systemPrompt) }
    var autoSpeak by remember { mutableStateOf(currentSettings.autoSpeakResponses) }
    var speechRate by remember { mutableFloatStateOf(currentSettings.speechRate) }
    var speechPitch by remember { mutableFloatStateOf(currentSettings.speechPitch) }
    var showSavedNotice by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "JARVIS CONFIGURATION & NEURAL CORE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyan
                )
                Text(
                    text = "Model endpoints, speech synthesis, and operating submodules",
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }
        }

        // AI Provider Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E1A))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "AI PROVIDER & ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim
                    )

                    // Provider Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AIProviderType.values().forEach { type ->
                            val isSelected = type == providerType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else Color(0xFF0C1422))
                                    .border(0.5.dp, if (isSelected) JarvisCyan else JarvisBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { providerType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (type) {
                                        AIProviderType.GEMINI -> "GEMINI"
                                        AIProviderType.OPENAI_COMPATIBLE -> "OPENAI"
                                        AIProviderType.LOCAL_NEURAL_BRAIN -> "ONBOARD"
                                    },
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary
                                )
                            }
                        }
                    }

                    // Model Name / Selection
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = { selectedModel = it },
                        label = { Text("Model Tag (e.g. gemini-3.5-flash)", color = JarvisTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    // Custom API Key
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        label = { Text("Custom API Key (Overrides default env key)", color = JarvisTextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("Paste API key here...", color = JarvisTextDim, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    if (providerType == AIProviderType.OPENAI_COMPATIBLE) {
                        OutlinedTextField(
                            value = customEndpoint,
                            onValueChange = { customEndpoint = it },
                            label = { Text("Custom Base URL", color = JarvisTextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorder,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    // System Prompt Directive
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        label = { Text("JARVIS System Persona & Instructions", color = JarvisTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        maxLines = 4
                    )
                }
            }
        }

        // Voice & Audio Configuration
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E1A))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SPEECH & VOCAL MODULATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Speak AI Responses", fontSize = 13.sp, color = JarvisTextPrimary)
                            Text("Engage TextToSpeech automatically upon reply", fontSize = 10.sp, color = JarvisTextDim)
                        }

                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { autoSpeak = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = JarvisTextDim,
                                uncheckedTrackColor = Color(0xFF162032)
                            )
                        )
                    }

                    // Rate & Pitch sliders
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Speech Rate (${String.format("%.1f", speechRate)}x)", fontSize = 12.sp, color = JarvisTextSecondary)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan, inactiveTrackColor = Color(0xFF1E293B))
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vocal Modulation Pitch (${String.format("%.1f", speechPitch)}x)", fontSize = 12.sp, color = JarvisTextSecondary)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { speechPitch = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan, inactiveTrackColor = Color(0xFF1E293B))
                    )
                }
            }
        }

        // Submodule Navigation Hub
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E1A))
                    .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "OPERATING LAYER SUBMODULES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim
                    )

                    SubmoduleNavigationRow("Privacy & Local Enclave", Icons.Default.Security, onNavigatePrivacy)
                    SubmoduleNavigationRow("Neural Memory Index", Icons.Default.Psychology, onNavigateMemory)
                    SubmoduleNavigationRow("Optical Vision HUD", Icons.Default.Visibility, onNavigateVision)
                    SubmoduleNavigationRow("Hardware Telemetry Bridge", Icons.Default.Bolt, onNavigateBridge)
                    SubmoduleNavigationRow("System Activity Logs", Icons.Default.History, onNavigateActivity)
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    onSaveSettings(
                        ProviderSettings(
                            providerType = providerType,
                            customApiKey = customApiKey,
                            customEndpoint = customEndpoint,
                            selectedModel = selectedModel,
                            systemPrompt = systemPrompt,
                            autoSpeakResponses = autoSpeak,
                            speechRate = speechRate,
                            speechPitch = speechPitch
                        )
                    )
                    showSavedNotice = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("APPLY & SAVE PARAMETERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            if (showSavedNotice) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Parameters synchronized with JARVIS Core.",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisGreen,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SubmoduleNavigationRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Text(text = title, fontSize = 13.sp, color = JarvisTextPrimary)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = JarvisTextDim, modifier = Modifier.size(18.dp))
    }
}
