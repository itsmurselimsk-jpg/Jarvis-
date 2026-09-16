package com.example.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.jarvis.brain.Tool
import com.example.jarvis.brain.ToolContext
import com.example.jarvis.model.RiskLevel
import com.example.jarvis.ui.theme.JarvisAmber
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisRed
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ToolsScreen(
    tools: List<Tool>,
    toolContext: ToolContext
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "TOOL CENTER & EXECUTOR MATRIX",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = JarvisCyan
        )
        Text(
            text = "${tools.size} registered executive capabilities",
            fontSize = 11.sp,
            color = JarvisTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools, key = { it.name }) { tool ->
                ToolCard(
                    tool = tool,
                    onExecute = { input, onResult ->
                        coroutineScope.launch {
                            val res = tool.execute(input, toolContext)
                            onResult(res.output)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: Tool,
    onExecute: (String, (String) -> Unit) -> Unit
) {
    var inputVal by remember { mutableStateOf("") }
    var executionOutput by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090E1A))
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
                    text = tool.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary
                )

                // Risk Level Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (tool.riskLevel) {
                                RiskLevel.SAFE -> JarvisGreen.copy(alpha = 0.2f)
                                RiskLevel.CONFIRMATION -> JarvisAmber.copy(alpha = 0.2f)
                                RiskLevel.RESTRICTED -> JarvisRed.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tool.riskLevel.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = when (tool.riskLevel) {
                            RiskLevel.SAFE -> JarvisGreen
                            RiskLevel.CONFIRMATION -> JarvisAmber
                            RiskLevel.RESTRICTED -> JarvisRed
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.description,
                fontSize = 12.sp,
                color = JarvisTextSecondary
            )

            if (tool.permissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Permissions: ${tool.permissions.joinToString(", ")}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextDim
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test input & execute
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    placeholder = { Text("Sample parameter...", fontSize = 11.sp, color = JarvisTextDim) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        isRunning = true
                        onExecute(inputVal.ifBlank { "test" }) { result ->
                            executionOutput = result
                            isRunning = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
                    modifier = Modifier.testTag("execute_${tool.name.lowercase()}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("RUN", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Output preview
            executionOutput?.let { output ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF030712))
                        .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = output,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyanBright,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
