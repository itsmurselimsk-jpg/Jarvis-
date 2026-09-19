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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.JarvisState
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.RiskLevel
import com.example.jarvis.ui.theme.JarvisAmber
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisElectricBlue
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisRed
import com.example.jarvis.ui.theme.JarvisSurface
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    jarvisState: JarvisState,
    onSendMessage: (String) -> Unit,
    onCopyMessage: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onRetryMessage: () -> Unit,
    onClearChat: () -> Unit,
    onVoiceClick: () -> Unit = {},
    onVisionClick: () -> Unit = {},
    isOfflineBrain: Boolean = false,
    onNavigateSettings: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages update
    LaunchedEffect(messages.size, messages.lastOrNull()?.text, jarvisState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Run full device telemetry scan",
        "Check battery and radio frequencies",
        "What time and date is it?",
        "Remember that meeting is at 14:00",
        "Toggle flashlight",
        "Show current audio volume",
        "Summarize recent security logs"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .imePadding()
    ) {
        // Chat Header Bar with Neural Matrix Status & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0B1424),
                            Color(0xDD060B14)
                        )
                    )
                )
                .border(0.5.dp, JarvisBorderSubtle)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                                JarvisState.THINKING -> JarvisElectricBlue
                                JarvisState.SPEAKING -> JarvisGreen
                                JarvisState.ERROR -> JarvisRed
                                else -> JarvisCyan
                            }
                        )
                )
                Column {
                    Text(
                        text = "NEURAL CHAT CONSOLE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = when (jarvisState) {
                            JarvisState.THINKING -> "Synthesizing response..."
                            JarvisState.SPEAKING -> "Transmitting response..."
                            else -> if (isOfflineBrain) "Offline Mode • Basic Responses" else "Cloud Matrix Active • Ultra Intelligence"
                        },
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isOfflineBrain) JarvisAmber else JarvisTextDim
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onRetryMessage,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry Last Response",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Conversation",
                        tint = JarvisTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Offline notice banner with direct Settings shortcut
        if (isOfflineBrain) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B150A))
                    .border(0.8.dp, JarvisAmber.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Offline Notice",
                            tint = JarvisAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Offline Mode: Add free Gemini or OpenAI key in Settings for full ChatGPT-level replies.",
                            fontSize = 11.sp,
                            color = JarvisAmber,
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyanBright,
                        modifier = Modifier
                            .clickable { onNavigateSettings() }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onCopy = { onCopyMessage(msg.text) },
                    onSpeak = { onSpeakMessage(msg.text) }
                )
            }

            // Thinking & Tool Execution Indicator
            if (jarvisState == JarvisState.THINKING) {
                item {
                    ThinkingIndicatorBubble()
                }
            }
        }

        // Quick Prompt Suggestions
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x66060B14))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF091424))
                        .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { inputText = prompt }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyan
                    )
                }
            }
        }

        // Message Input Bar with Attachments, Mic & Send
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0B1424),
                            Color(0xF5060A14)
                        )
                    )
                )
                .border(0.5.dp, JarvisBorderSubtle)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Optical Vision HUD / Attachment Button
                IconButton(
                    onClick = onVisionClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1B2E))
                        .border(0.5.dp, JarvisBorderSubtle, CircleShape)
                        .testTag("chat_vision_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach / OCR Vision",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Main Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Transmit directive to JARVIS...",
                            fontSize = 12.sp,
                            color = JarvisTextDim
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyan,
                        focusedContainerColor = Color(0xFF080F1C),
                        unfocusedContainerColor = Color(0xFF080F1C)
                    ),
                    maxLines = 4
                )

                // Voice / Mic shortcut button
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1B2E))
                        .border(0.5.dp, JarvisBorderSubtle, CircleShape)
                        .testTag("chat_mic_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Switch to Voice",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val textToSend = inputText.trim()
                            inputText = ""
                            onSendMessage(textToSend)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) JarvisCyan else Color(0xFF142033)
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.Black else JarvisTextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Metadata Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isUser) "OPERATOR" else "J.A.R.V.I.S.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = if (isUser) JarvisTextSecondary else JarvisCyan
            )

            // Tool Execution Status Badge
            message.toolCallName?.let { toolName ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0B1E34))
                        .border(0.5.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(9.dp)
                        )
                        Text(
                            text = "TOOL: $toolName",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanBright
                        )
                    }
                }
            }

            // Risky Action / Confirmation Badge
            if (message.toolRiskLevel == RiskLevel.CONFIRMATION) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(JarvisAmber.copy(alpha = 0.15f))
                        .border(0.5.dp, JarvisAmber, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = JarvisAmber,
                            modifier = Modifier.size(9.dp)
                        )
                        Text(
                            text = "SECURITY CHECKPOINT",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisAmber
                        )
                    }
                }
            }

            Text(
                text = timeStr,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextDim
            )
        }

        // Bubble Surface Container
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.92f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF101B2E),
                                Color(0xFF0C1424)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0B1424),
                                Color(0xFF060B14)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) JarvisBorderSubtle else JarvisCyan.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                // Streaming / Typing indicator pulse
                if (message.isStreaming) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(JarvisCyanBright)
                        )
                        Text(
                            text = "STREAMING NEURAL RESPONSE...",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisCyan
                        )
                    }
                }

                // Message Text
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = if (isUser) JarvisTextPrimary else JarvisCyanBright
                )

                // Actions row for JARVIS responses
                if (!isUser && !message.isStreaming) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = JarvisTextDim,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak message",
                                tint = JarvisCyan,
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
private fun ThinkingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF091222))
            .border(1.dp, JarvisCyan.copy(alpha = alpha * 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = JarvisCyan
        )
        Text(
            text = "JARVIS is synthesizing response...",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = JarvisCyanBright
        )
    }
}
