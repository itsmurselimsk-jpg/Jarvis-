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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
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
import com.example.jarvis.ui.theme.JarvisElectricBlue
import com.example.jarvis.ui.theme.JarvisGreen
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
    onNavigateVision: () -> Unit,
    onNavigateVoiceSetup: () -> Unit = {},
    onNavigateVoiceProfiles: () -> Unit = {},
    onNavigateAbout: () -> Unit = {},
    onNavigateDiagnostics: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSearch: () -> Unit = {},
    onNavigatePlugins: () -> Unit = {}
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
            .background(JarvisBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "JARVIS SYSTEM SETTINGS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    color = JarvisCyan
                )
                Text(
                    text = "Core parameters, vocal synthesizer, and security enclaves",
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }
        }

        // ==========================================
        // 1. AI Provider & Neural Engine Section
        // ==========================================
        item {
            SettingsCategoryCard(title = "AI / PROVIDER & NEURAL ENGINE") {
                // Provider Selection Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AIProviderType.values().forEach { type ->
                        val isSelected = type == providerType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.18f) else Color(0xFF0C1422))
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisBorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { providerType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (type) {
                                    AIProviderType.GEMINI -> "GEMINI"
                                    AIProviderType.OPENAI_COMPATIBLE -> "OPENAI"
                                    AIProviderType.LOCAL_NEURAL_BRAIN -> "ON-DEVICE"
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) JarvisCyanBright else JarvisTextSecondary
                            )
                        }
                    }
                }

                // Selected Model Tag
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = { selectedModel = it },
                    label = { Text("Model Tag", color = JarvisTextSecondary, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyan
                    ),
                    singleLine = true
                )

                // Custom API Key
                OutlinedTextField(
                    value = customApiKey,
                    onValueChange = { customApiKey = it },
                    label = { Text("Custom API Key (Overrides runtime env)", color = JarvisTextSecondary, fontSize = 11.sp) },
                    placeholder = { Text("Enter key...", color = JarvisTextDim, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyan
                    ),
                    singleLine = true
                )

                if (providerType == AIProviderType.OPENAI_COMPATIBLE) {
                    OutlinedTextField(
                        value = customEndpoint,
                        onValueChange = { customEndpoint = it },
                        label = { Text("Custom Base URL", color = JarvisTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            cursorColor = JarvisCyan
                        ),
                        singleLine = true
                    )
                }

                // System Prompt Directive
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("JARVIS System Directive & Persona", color = JarvisTextSecondary, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyan
                    ),
                    maxLines = 4
                )
            }
        }

        // ==========================================
        // 2. Voice & Audio Section
        // ==========================================
        item {
            SettingsCategoryCard(title = "VOICE & AUDIO CALIBRATION") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Speak AI Responses", fontSize = 13.sp, color = JarvisTextPrimary)
                        Text("Vocalize text responses automatically", fontSize = 10.sp, color = JarvisTextDim)
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

                // Rate Slider
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Speech Rate", fontSize = 12.sp, color = JarvisTextSecondary)
                        Text("${String.format("%.1f", speechRate)}x", fontSize = 12.sp, color = JarvisCyan, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = Color(0xFF1E293B)
                        )
                    )
                }

                // Pitch Slider
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

                SubmoduleNavigationRow("Acoustic Wake Engine ('Hey JARVIS')", Icons.Default.Mic, onNavigateVoiceSetup)
                SubmoduleNavigationRow("Vocal Profiles & Modulation Tuning", Icons.Default.RecordVoiceOver, onNavigateVoiceProfiles)
            }
        }

        // ==========================================
        // 3. Memory & Context Section
        // ==========================================
        item {
            SettingsCategoryCard(title = "MEMORY & CONTEXT ENCLAVE") {
                SubmoduleNavigationRow("Neural Memory Index & Records", Icons.Default.Psychology, onNavigateMemory)
                SubmoduleNavigationRow("Privacy & Local Memory Shield", Icons.Default.Security, onNavigatePrivacy)
            }
        }

        // ==========================================
        // 4. Permissions & Privacy Section
        // ==========================================
        item {
            SettingsCategoryCard(title = "PERMISSIONS & SECURITY") {
                SubmoduleNavigationRow("Security Clearance & Zero-Leak Enclave", Icons.Default.Security, onNavigatePrivacy)
                SubmoduleNavigationRow("Hardware Access Permissions", Icons.Default.Bolt, onNavigateBridge)
            }
        }

        // ==========================================
        // 5. System Tools & Diagnostics Section
        // ==========================================
        item {
            SettingsCategoryCard(title = "SYSTEM TOOLS & PLUGINS") {
                SubmoduleNavigationRow("Connected Services & Plugin Vault", Icons.Default.Build, onNavigatePlugins)
                SubmoduleNavigationRow("Universal Phone Search", Icons.Default.Search, onNavigateSearch)
                SubmoduleNavigationRow("Optical Vision HUD & OCR", Icons.Default.Visibility, onNavigateVision)
                SubmoduleNavigationRow("Hardware Telemetry Bridge", Icons.Default.Bolt, onNavigateBridge)
                SubmoduleNavigationRow("Smart Notification Intelligence", Icons.Default.Notifications, onNavigateNotifications)
                SubmoduleNavigationRow("System Self-Diagnostics", Icons.Default.Build, onNavigateDiagnostics)
            }
        }

        // ==========================================
        // 6. About / Info Section
        // ==========================================
        item {
            SettingsCategoryCard(title = "ABOUT & SYSTEM TELEMETRY") {
                SubmoduleNavigationRow("System Activity Logs", Icons.Default.History, onNavigateActivity)
                SubmoduleNavigationRow("About JARVIS & Architecture Specs", Icons.Default.Info, onNavigateAbout)
            }
        }

        // ==========================================
        // Save Parameters Action Button
        // ==========================================
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
                    .height(52.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = Color.Black
                )
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "APPLY & SAVE PARAMETERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            AnimatedVisibility(visible = showSavedNotice) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF091F18))
                        .border(1.dp, JarvisGreen, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = JarvisGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Parameters synchronized with JARVIS Core.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    content: @Composable () -> Unit
) {
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
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = JarvisCyan
            )
            content()
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
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF08101E))
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Text(text = title, fontSize = 12.sp, color = JarvisTextPrimary)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = JarvisTextDim, modifier = Modifier.size(18.dp))
    }
}
