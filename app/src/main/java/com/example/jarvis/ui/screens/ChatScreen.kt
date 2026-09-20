package com.example.jarvis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.JarvisState
import com.example.jarvis.model.MessageSender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    jarvisState: JarvisState,
    isDarkTheme: Boolean = false,
    accentColor: Color = Color(0xFF2563EB),
    onSendMessage: (String) -> Unit,
    onCopyMessage: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onRetryMessage: () -> Unit,
    onClearChat: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onVisionClick: () -> Unit = {},
    onCodeStudioClick: () -> Unit = {},
    onGetPlusClick: () -> Unit = {},
    isOfflineBrain: Boolean = false,
    onNavigateSettings: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val bg = if (isDarkTheme) Color(0xFF171717) else Color(0xFFFFFFFF)
    val cardBg = if (isDarkTheme) Color(0xFF262626) else Color(0xFFF3F4F6)
    val inputBg = if (isDarkTheme) Color(0xFF262626) else Color(0xFFF4F4F6)
    val textPrimary = if (isDarkTheme) Color(0xFFECECF1) else Color(0xFF0D0D0D)
    val textSecondary = if (isDarkTheme) Color(0xFF9E9E9E) else Color(0xFF707070)
    val pillBg = if (isDarkTheme) Color(0xFF262626) else Color(0xFFEBF3FE)

    // Auto-scroll to bottom when messages update
    LaunchedEffect(messages.size, messages.lastOrNull()?.text, jarvisState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .imePadding()
    ) {
        // Top Bar: [Menu Drawer Button], [+ Get Plus Pill], [New Chat Button]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger Menu Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .clickable { onOpenDrawer() }
                    .testTag("chat_drawer_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Sidebar",
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center: "+ Get Plus" / "Stark Plus" Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .clickable { onGetPlusClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Get Plus",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Get Plus",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }

            // New Chat / Clear Conversation Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .clickable { onClearChat() }
                    .testTag("new_chat_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Chat",
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Empty State Starter Screen (Matching Screenshot 6)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    ActionStarterItem(
                        icon = Icons.Default.Image,
                        title = "Create an image",
                        textColor = textPrimary,
                        onClick = {
                            inputText = "Generate a high quality visual illustration of "
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ActionStarterItem(
                        icon = Icons.Default.Edit,
                        title = "Write or edit",
                        textColor = textPrimary,
                        onClick = {
                            inputText = "Help me write and edit: "
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ActionStarterItem(
                        icon = Icons.Default.Language,
                        title = "Search the web",
                        textColor = textPrimary,
                        onClick = {
                            inputText = "Search the latest web updates on "
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ActionStarterItem(
                        icon = Icons.Default.Lightbulb,
                        title = "Brainstorm ideas",
                        textColor = textPrimary,
                        onClick = {
                            inputText = "Brainstorm creative ideas for "
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Active Message Stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        ChatBubbleItem(
                            message = message,
                            isDarkTheme = isDarkTheme,
                            accentColor = accentColor,
                            onCopy = { onCopyMessage(message.text) },
                            onSpeak = { onSpeakMessage(message.text) }
                        )
                    }

                    if (jarvisState == JarvisState.THINKING) {
                        item {
                            ThinkingIndicator(isDarkTheme = isDarkTheme, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        // Attachment Sheet Popup
        AnimatedVisibility(visible = showAttachmentMenu) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttachmentOption(icon = Icons.Default.CameraAlt, title = "Vision Camera", onClick = { showAttachmentMenu = false; onVisionClick() }, color = accentColor)
                AttachmentOption(icon = Icons.Default.Code, title = "Code Studio", onClick = { showAttachmentMenu = false; onCodeStudioClick() }, color = Color(0xFF10A37F))
                AttachmentOption(icon = Icons.Default.GraphicEq, title = "Voice Vault", onClick = { showAttachmentMenu = false; onVoiceClick() }, color = Color(0xFF8B5CF6))
            }
        }

        // Bottom Rounded Input Capsule (Matching Screenshot 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(inputBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "+" Attachment / Tools Button
                IconButton(
                    onClick = { showAttachmentMenu = !showAttachmentMenu },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attachment Menu",
                        tint = textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text Input Field
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask ChatGPT",
                            fontSize = 15.sp,
                            color = textSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    maxLines = 4
                )

                if (inputText.isNotBlank()) {
                    // Send Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .clickable {
                                val msg = inputText.trim()
                                inputText = ""
                                onSendMessage(msg)
                            }
                            .testTag("send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Microphone Dictation Button
                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Dictation",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Solid Blue Voice Orb Waveform Button (Opens Advanced Voice Mode)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .clickable { onVoiceClick() }
                            .testTag("voice_mode_launch_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Advanced Voice Mode",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionStarterItem(
    icon: ImageVector,
    title: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isDarkTheme: Boolean,
    accentColor: Color,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val bubbleBg = if (isUser) {
        if (isDarkTheme) Color(0xFF2E2E2E) else Color(0xFFE5E7EB)
    } else {
        if (isDarkTheme) Color(0xFF1F1F1F) else Color(0xFFF9FAFB)
    }
    val textColor = if (isDarkTheme) Color(0xFFECECF1) else Color(0xFF111827)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(bubbleBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = textColor
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator(isDarkTheme: Boolean, accentColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDarkTheme) Color(0xFF262626) else Color(0xFFF3F4F6))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = accentColor
        )
        Text(
            text = "ChatGPT is thinking...",
            fontSize = 13.sp,
            color = if (isDarkTheme) Color(0xFFECECF1) else Color(0xFF374151)
        )
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 11.sp, color = Color.Gray)
    }
}
