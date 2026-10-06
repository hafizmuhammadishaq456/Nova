package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MessageContentRenderer
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated

@Composable
fun YouTubeStudioScreen(
    isGenerating: Boolean,
    generatedResult: String?,
    isOnline: Boolean,
    prefilledTopic: String? = null,
    onGenerate: (topic: String, type: String, audience: String, language: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var topic by remember(prefilledTopic) { mutableStateOf(prefilledTopic ?: "") }
    var selectedType by remember { mutableStateOf("Full Script") }
    var selectedAudience by remember { mutableStateOf("USA") }
    var selectedLanguage by remember { mutableStateOf("English") }

    val taskTypes = listOf(
        "Title Ideas",
        "Descriptions",
        "SEO Keywords",
        "Hashtags",
        "Full Script",
        "Thumbnail Ideas"
    )

    val audiences = listOf("USA", "India", "Pakistan", "Global")
    val languages = listOf("English", "Urdu", "Hindi", "Roman Urdu")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("youtube_studio_screen")
    ) {
        // Studio Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF0000).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFFF0000).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = "YouTube Assistant",
                    tint = Color(0xFFFF4D4D),
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "YouTube Creator Studio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Titles, SEO, Scripts & Thumbnails by Nova",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Tool Selection
        Text(
            text = "Feature",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaMint,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (type in taskTypes) {
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyan,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurface,
                        labelColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // 2. Audience Selection
        Text(
            text = "Target Audience",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaMint,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (aud in audiences) {
                FilterChip(
                    selected = selectedAudience == aud,
                    onClick = { selectedAudience = aud },
                    label = { Text(aud, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaMint,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurface,
                        labelColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // 3. Language Selection
        Text(
            text = "Language",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaMint,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (lang in languages) {
                FilterChip(
                    selected = selectedLanguage == lang,
                    onClick = { selectedLanguage = lang },
                    label = { Text(lang, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyan,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurface,
                        labelColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // Video Concept / Topic Input
        Text(
            text = "Video Topic or Niche",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            placeholder = {
                Text(
                    "e.g. 'How to start affiliate marketing', 'Cricket Champions Trophy preview', 'Top 5 AI tools'...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("yt_topic_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaCyan,
                unfocusedBorderColor = NovaBorder,
                focusedContainerColor = NovaSurface,
                unfocusedContainerColor = NovaSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Generate Action Button
        Button(
            onClick = {
                onGenerate(topic, selectedType, selectedAudience, selectedLanguage)
            },
            enabled = topic.isNotBlank() && !isGenerating && isOnline,
            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = NovaBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("yt_generate_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = NovaBackground,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova is creating content...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isOnline) "Generate Content with Nova" else "Offline (Online required)", fontWeight = FontWeight.Bold)
            }
        }

        // Result Card with Copy Button
        if (!generatedResult.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("yt_result_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Result ($selectedType)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaMint
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("YouTube Content", generatedResult)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied content to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("yt_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = NovaCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MessageContentRenderer(content = generatedResult)
                }
            }
        }
    }
}
