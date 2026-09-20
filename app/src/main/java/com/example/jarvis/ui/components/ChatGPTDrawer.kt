package com.example.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatGPTDrawer(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    accentColor: Color = Color(0xFF2563EB),
    userName: String = "ITS MUSAROF",
    recentConversations: List<String> = listOf(
        "Casual Conversation Explanation",
        "Human Conversation Engine",
        "वॉयस इंजन अपग्रेड",
        "AI Studio File Use",
        "प्रोजेक्ट बातचीत",
        "Marvel Watch Order",
        "Purana IGN batana",
        "JARVIS Arc Reactor Telemetry",
        "Personal Expense Ledger"
    ),
    onImagesClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onProjectsClick: () -> Unit = {},
    onScheduledClick: () -> Unit = {},
    onPluginsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSelectConversation: (String) -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onVoiceModeClick: () -> Unit = {}
) {
    val bg = if (isDarkTheme) Color(0xFF171717) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkTheme) Color(0xFFECECF1) else Color(0xFF0D0D0D)
    val textSecondary = if (isDarkTheme) Color(0xFF9E9E9E) else Color(0xFF707070)
    val dividerColor = if (isDarkTheme) Color(0xFF262626) else Color(0xFFEEEEEE)
    val searchBg = if (isDarkTheme) Color(0xFF262626) else Color(0xFFF3F4F6)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(bg)
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Top Header: ChatGPT and Search Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ChatGPT",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(searchBg)
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Quick Access Items (Images, Library, Projects, Scheduled, Plugins)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrawerNavItem(
                icon = Icons.Default.Image,
                title = "Images",
                textColor = textPrimary,
                onClick = onImagesClick
            )
            DrawerNavItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Library",
                textColor = textPrimary,
                onClick = onLibraryClick
            )
            DrawerNavItem(
                icon = Icons.Default.Folder,
                title = "Projects",
                textColor = textPrimary,
                onClick = onProjectsClick
            )
            DrawerNavItem(
                icon = Icons.Default.AccessTime,
                title = "Scheduled",
                textColor = textPrimary,
                onClick = onScheduledClick
            )
            DrawerNavItem(
                icon = Icons.Default.Extension,
                title = "Plugins",
                textColor = textPrimary,
                onClick = onPluginsClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = dividerColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Recent Conversations List
        Text(
            text = "Recents",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textSecondary,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(recentConversations) { title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectConversation(title) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Action Bar: [+ Chat], [Profile Monogram "IM"], [Voice Audio Mode]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // New Chat pill
            Button(
                onClick = onNewChatClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Chat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar "IM"
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Voice Mode Waveform Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color(0xFF262626) else Color(0xFF2563EB))
                        .clickable { onVoiceModeClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Mode",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    title: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
