package com.example.jarvis.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.VisionScan
import com.example.jarvis.provider.AIProvider
import com.example.jarvis.ui.theme.JarvisBackground
import com.example.jarvis.ui.theme.JarvisBorder
import com.example.jarvis.ui.theme.JarvisBorderSubtle
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisGreen
import com.example.jarvis.ui.theme.JarvisTextDim
import com.example.jarvis.ui.theme.JarvisTextPrimary
import com.example.jarvis.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.launch

@Composable
fun VisionScreen(
    scans: List<VisionScan>,
    aiProvider: AIProvider,
    onAddScan: (VisionScan) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }

    // Android Photo Picker (zero broad storage permission compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isAnalyzing = true
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val sizeBytes = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                    val sizeFormatted = "${sizeBytes / 1024} KB"

                    val analysis = if (bitmap != null) {
                        aiProvider.analyzeImage("Perform HUD tactical vision scan of this imagery.", bitmap)
                    } else {
                        "Imagery decoded but bitmap conversion returned null."
                    }

                    val scan = VisionScan(
                        uriString = uri.toString(),
                        fileName = "IMG_SCAN_${System.currentTimeMillis().toString().takeLast(4)}.jpg",
                        fileSizeFormatted = sizeFormatted,
                        analysisResult = analysis,
                        status = "Completed"
                    )
                    onAddScan(scan)
                } catch (e: Exception) {
                    val scan = VisionScan(
                        uriString = uri.toString(),
                        fileName = "IMG_SCAN_ERROR.jpg",
                        fileSizeFormatted = "0 KB",
                        analysisResult = "Analysis aborted: ${e.message}",
                        status = "Error"
                    )
                    onAddScan(scan)
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OPTICAL VISION & FILE HUD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyan
                )
                Text(
                    text = "Multimodal sensory telemetry & inspection",
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("scan_image_button")
            ) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("SCAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF091424))
                    .border(1.dp, JarvisCyan, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = JarvisCyan, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Deconstructing optical pixels in neural core...", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = JarvisCyan)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (scans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No visual assets scanned. Tap SCAN to submit imagery.", color = JarvisTextDim, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            } else {
                items(scans, key = { it.id }) { scan ->
                    VisionScanCard(scan = scan)
                }
            }
        }
    }
}

@Composable
private fun VisionScanCard(scan: VisionScan) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF090E1A))
            .border(0.5.dp, JarvisBorderSubtle, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                    Text(text = scan.fileName, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = JarvisTextPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0D233A))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = scan.fileSizeFormatted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = JarvisCyan)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.analysisResult,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisCyanBright,
                lineHeight = 16.sp
            )
        }
    }
}
