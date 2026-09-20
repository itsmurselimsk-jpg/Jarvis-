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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.ProviderSettings

@Composable
fun SettingsScreen(
    currentSettings: ProviderSettings,
    onSaveSettings: (ProviderSettings) -> Unit = {},
    isDarkTheme: Boolean = false,
    selectedAccentColor: Color = Color(0xFF2563EB),
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onSelectAccentColor: (Color) -> Unit = {},
    onNavigatePrivacy: () -> Unit = {},
    onNavigateMemory: () -> Unit = {},
    onNavigateBridge: () -> Unit = {},
    onNavigateActivity: () -> Unit = {},
    onNavigateVision: () -> Unit = {},
    onNavigateVoiceSetup: () -> Unit = {},
    onNavigateVoiceProfiles: () -> Unit = {},
    onNavigateAbout: () -> Unit = {},
    onNavigateDiagnostics: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSearch: () -> Unit = {},
    onNavigatePlugins: () -> Unit = {},
    onNavigateBackup: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAppearanceDropdown by remember { mutableStateOf(false) }
    var showAccentDropdown by remember { mutableStateOf(false) }

    val bg = if (isDarkTheme) Color(0xFF171717) else Color(0xFFF3F4F6)
    val cardBg = if (isDarkTheme) Color(0xFF262626) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkTheme) Color(0xFFECECF1) else Color(0xFF0D0D0D)
    val textSecondary = if (isDarkTheme) Color(0xFF9E9E9E) else Color(0xFF707070)
    val dividerColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation & Profile Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Avatar with edit badge
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(selectedAccentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "IM",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Edit pen badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.5.dp, bg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = textPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ITS MUSAROF",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Section: My ChatGPT / My JARVIS
            item {
                SectionHeader(title = "My ChatGPT", textColor = textSecondary)
                InsetCardGroup(cardBg = cardBg, dividerColor = dividerColor) {
                    SettingsRowItem(
                        icon = Icons.Default.SentimentSatisfiedAlt,
                        title = "Personalization",
                        textColor = textPrimary,
                        onClick = onNavigateVoiceProfiles
                    )
                    SettingsRowItem(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Memory",
                        textColor = textPrimary,
                        onClick = onNavigateMemory
                    )
                    SettingsRowItem(
                        icon = Icons.Default.Extension,
                        title = "Plugins",
                        textColor = textPrimary,
                        onClick = onNavigatePlugins
                    )
                }
            }

            // Section: Account
            item {
                SectionHeader(title = "Account", textColor = textSecondary)
                InsetCardGroup(cardBg = cardBg, dividerColor = dividerColor) {
                    SettingsRowItem(
                        icon = Icons.Default.ShoppingBag,
                        title = "Workspace",
                        subtitle = "Personal",
                        textColor = textPrimary,
                        subTextColor = textSecondary,
                        onClick = {}
                    )
                    SettingsRowItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Upgrade plan",
                        subtitle = "ChatGPT Plus / Stark Pro",
                        textColor = textPrimary,
                        subTextColor = selectedAccentColor,
                        onClick = {}
                    )
                    SettingsRowItem(
                        icon = Icons.Default.Speed,
                        title = "Usage and limits",
                        textColor = textPrimary,
                        onClick = onNavigateDiagnostics
                    )
                    SettingsRowItem(
                        icon = Icons.Default.FamilyRestroom,
                        title = "Parental controls",
                        textColor = textPrimary,
                        onClick = onNavigatePrivacy
                    )
                    SettingsRowItem(
                        icon = Icons.Default.Email,
                        title = "Email",
                        subtitle = "itsmusarof@gmail.com",
                        textColor = textPrimary,
                        subTextColor = textSecondary,
                        onClick = {}
                    )
                    SettingsRowItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Age verification",
                        textColor = textPrimary,
                        onClick = {}
                    )
                }
            }

            // Section: Appearance & Accent color
            item {
                InsetCardGroup(cardBg = cardBg, dividerColor = dividerColor) {
                    // Appearance (Theme)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAppearanceDropdown = !showAppearanceDropdown }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = textPrimary, modifier = Modifier.size(20.dp))
                                Text("Appearance", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = textSecondary)
                        }

                        if (showAppearanceDropdown) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val themes = listOf("System", "Light", "Dark")
                                themes.forEach { t ->
                                    val isSelected = (t == "Dark" && isDarkTheme) || (t == "Light" && !isDarkTheme)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) selectedAccentColor else bg)
                                            .clickable { onToggleDarkTheme(t == "Dark") }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(t, fontSize = 13.sp, color = if (isSelected) Color.White else textPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Accent Color
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAccentDropdown = !showAccentDropdown }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = textPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Accent color", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(selectedAccentColor)
                                        )
                                        Text("Blue", fontSize = 12.sp, color = textSecondary)
                                    }
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = textSecondary)
                        }

                        if (showAccentDropdown) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val colors = listOf(
                                    Color(0xFF2563EB), // Blue
                                    Color(0xFF10A37F), // Emerald / ChatGPT Green
                                    Color(0xFF8B5CF6), // Purple
                                    Color(0xFFF59E0B), // Amber
                                    Color(0xFFEF4444)  // Coral
                                )
                                colors.forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .clickable { onSelectAccentColor(c) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (c == selectedAccentColor) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: General Controls
            item {
                InsetCardGroup(cardBg = cardBg, dividerColor = dividerColor) {
                    SettingsRowItem(icon = Icons.Default.Settings, title = "General", textColor = textPrimary, onClick = onNavigateBridge)
                    SettingsRowItem(icon = Icons.Default.Notifications, title = "Notifications", textColor = textPrimary, onClick = onNavigateNotifications)
                    SettingsRowItem(icon = Icons.Default.GraphicEq, title = "Voice", textColor = textPrimary, onClick = onNavigateVoiceSetup)
                    SettingsRowItem(icon = Icons.Default.Security, title = "Safety & wellbeing", textColor = textPrimary, onClick = onNavigatePrivacy)
                    SettingsRowItem(icon = Icons.Default.Lock, title = "Security and login", textColor = textPrimary, onClick = onNavigatePrivacy)
                    SettingsRowItem(icon = Icons.Default.Computer, title = "Remote control", textColor = textPrimary, onClick = onNavigateBridge)
                    SettingsRowItem(icon = Icons.Default.Storage, title = "Storage", textColor = textPrimary, onClick = onNavigateBackup)
                    SettingsRowItem(icon = Icons.Default.Storage, title = "Data controls", textColor = textPrimary, onClick = onNavigateBackup)
                    SettingsRowItem(icon = Icons.Default.Notifications, title = "Ads controls", textColor = textPrimary, onClick = {})
                    SettingsRowItem(icon = Icons.Default.BugReport, title = "Report bug", textColor = textPrimary, onClick = onNavigateDiagnostics)
                    SettingsRowItem(icon = Icons.Default.Info, title = "About", textColor = textPrimary, onClick = onNavigateAbout)
                }
            }

            // Section: Log Out Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .clickable { onLogout() }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log out",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Log out",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // Fixed Floating Bottom Search Capsule (Matching Screenshot 1 & 2)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .clickable { onNavigateSearch() }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search settings",
                    tint = textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Search settings",
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, textColor: Color) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = textColor,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun InsetCardGroup(
    cardBg: Color,
    dividerColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
    ) {
        content()
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color,
    subTextColor: Color = Color.Gray,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subTextColor
                )
            }
        }
    }
}
