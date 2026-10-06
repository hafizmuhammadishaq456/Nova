package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoProjectEntity
import com.example.ui.components.MessageContentRenderer
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoCreatorStudioScreen(
    durationMinutes: Int,
    isGenerating: Boolean,
    generatedResult: String?,
    currentProject: VideoProjectEntity?,
    savedProjects: List<VideoProjectEntity>,
    isOnline: Boolean,
    onDurationSelected: (Int) -> Unit,
    onGenerate: (
        durationMinutes: Int,
        idea: String,
        language: String,
        style: String,
        audience: String,
        characterSubject: String,
        voicePref: String,
        aspectRatio: String,
        visualStyle: String,
        musicPref: String
    ) -> Unit,
    onLoadProject: (VideoProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onDuplicateProject: (Long) -> Unit,
    onSendToYouTubeStudio: (
        title: String,
        description: String,
        keywords: String,
        hashtags: String,
        script: String,
        thumbnailPrompt: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var idea by remember { mutableStateOf(currentProject?.idea ?: "") }
    var selectedLanguage by remember { mutableStateOf(currentProject?.language ?: "English") }
    var selectedStyle by remember { mutableStateOf(currentProject?.style ?: "Cinematic") }
    var selectedAudience by remember { mutableStateOf(currentProject?.audience ?: "USA") }
    var characterSubject by remember { mutableStateOf(currentProject?.characterLockDescription ?: "") }
    var voicePref by remember { mutableStateOf("Narrator") }
    var selectedAspectRatio by remember { mutableStateOf(currentProject?.aspectRatio ?: "16:9 Landscape") }
    var visualStyle by remember { mutableStateOf("Hyper-realistic 8k, Unreal Engine 5 aesthetic, volumetric lighting") }
    var musicPref by remember { mutableStateOf("Cinematic Orchestral") }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val languages = listOf("English", "Urdu", "Hindi", "Roman Urdu")
    val audiences = listOf("USA", "Pakistan", "India", "Global")
    val styles = listOf(
        "Cinematic", "Realistic", "3D Animation", "Pixar-style",
        "Cartoon", "Anime", "Documentary", "Horror", "Comedy",
        "Educational", "YouTube Shorts", "Social Media"
    )
    val visualStyles = listOf(
        "Hyper-realistic 8k Photorealism",
        "Unreal Engine 5 Volumetric",
        "35mm Vintage Film Grain",
        "Cyberpunk Neon Glow",
        "Anime Shinkai Aesthetic",
        "Pixar Stylized 3D",
        "Moody Noir Shadows",
        "Documentary Handheld Raw"
    )
    val aspectRatios = listOf("16:9 Landscape", "9:16 Vertical", "1:1 Square")
    val voiceOptions = listOf("Narrator", "Male voice", "Female voice", "Character dialogue")
    val musicOptions = listOf(
        "Cinematic Orchestral", "Lo-Fi Chill", "Cyberpunk Synth",
        "Uplifting Corporate", "Dramatic Suspense", "Traditional Acoustic", "Upbeat Pop"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("video_creator_studio_screen")
    ) {
        // Header with History Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(NovaViolet.copy(alpha = 0.2f))
                        .border(1.dp, NovaViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Video Studio",
                        tint = NovaPink,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Video Creator Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Multi-Scene Scripts, Timelines & AI Prompts",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Project History button
            Button(
                onClick = { showHistoryDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaSurfaceElevated,
                    contentColor = NovaMint
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("video_history_button")
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Projects (${savedProjects.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DURATION SELECTOR (1 to 5 Minutes)
        Text(
            text = "⏱ Video Duration",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NovaCyan
        )
        Text(
            text = "Select length to calculate scene breakdown and production timeline",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Five Clear Duration Buttons / Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val durations = listOf(1, 2, 3, 4, 5)
            for (min in durations) {
                val isSelected = durationMinutes == min
                val sceneEstimate = when (min) {
                    1 -> "6-8"
                    2 -> "10-14"
                    3 -> "15-20"
                    4 -> "20-26"
                    else -> "25-35"
                }

                Card(
                    onClick = { onDurationSelected(min) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NovaCyan else NovaSurfaceElevated
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NovaMint else NovaBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("duration_${min}_min_button")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "[ $min MIN ]",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NovaBackground else Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "~$sceneEstimate scenes",
                            fontSize = 9.sp,
                            color = if (isSelected) NovaBackground.copy(alpha = 0.8f) else NovaMint
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VIDEO INPUT SECTION
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Topic input
                Text(
                    text = "Video Idea / Topic",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = idea,
                    onValueChange = { idea = it },
                    placeholder = {
                        Text(
                            "e.g., 'A cyberpunk samurai protecting the last neon bonsai garden in Neo-Tokyo' or 'The rise of artificial general intelligence'...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_idea_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = NovaBorder,
                        focusedContainerColor = NovaBackground,
                        unfocusedContainerColor = NovaBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Character / Subject & Consistency Lock
                Text(
                    text = "Character / Subject (For Consistency Lock)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaPink
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = characterSubject,
                    onValueChange = { characterSubject = it },
                    placeholder = {
                        Text(
                            "e.g., 'Kenji: 32yo Japanese cyborg samurai, chrome left arm, matte black tactical kimono, glowing cyan katana'...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("character_subject_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaPink,
                        unfocusedBorderColor = NovaBorder,
                        focusedContainerColor = NovaBackground,
                        unfocusedContainerColor = NovaBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Language
                Text("Video Language", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (lang in languages) {
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { selectedLanguage = lang },
                            label = { Text(lang, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaCyan,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Video Style
                Text("Video Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (st in styles) {
                        FilterChip(
                            selected = selectedStyle == st,
                            onClick = { selectedStyle = st },
                            label = { Text(st, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaMint,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aspect Ratio
                Text("Aspect Ratio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (ar in aspectRatios) {
                        FilterChip(
                            selected = selectedAspectRatio == ar,
                            onClick = { selectedAspectRatio = ar },
                            label = { Text(ar, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaCyan,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target Audience
                Text("Target Audience", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (aud in audiences) {
                        FilterChip(
                            selected = selectedAudience == aud,
                            onClick = { selectedAudience = aud },
                            label = { Text(aud, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaPink,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Voice Preference
                Text("Voice Preference", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (v in voiceOptions) {
                        FilterChip(
                            selected = voicePref == v,
                            onClick = { voicePref = v },
                            label = { Text(v, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaViolet,
                                selectedLabelColor = Color.White,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Visual Style
                Text("Visual Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (vs in visualStyles) {
                        FilterChip(
                            selected = visualStyle.startsWith(vs.take(15)),
                            onClick = { visualStyle = vs },
                            label = { Text(vs, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaMint,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = visualStyle,
                    onValueChange = { visualStyle = it },
                    placeholder = { Text("Custom visual style instructions...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("visual_style_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaMint,
                        unfocusedBorderColor = NovaBorder,
                        focusedContainerColor = NovaBackground,
                        unfocusedContainerColor = NovaBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Music Preference
                Text("Background Music", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaMint)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (m in musicOptions) {
                        FilterChip(
                            selected = musicPref == m,
                            onClick = { musicPref = m },
                            label = { Text(m, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaCyan,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GENERATE VIDEO BUTTON
        Button(
            onClick = {
                onGenerate(
                    durationMinutes,
                    idea,
                    selectedLanguage,
                    selectedStyle,
                    selectedAudience,
                    characterSubject,
                    voicePref,
                    selectedAspectRatio,
                    visualStyle,
                    musicPref
                )
            },
            enabled = idea.isNotBlank() && !isGenerating && isOnline,
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyan,
                contentColor = NovaBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_video_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = NovaBackground,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Nova is generating $durationMinutes-min video plan...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOnline) "🎬 Generate Video ($durationMinutes Min)" else "Offline (Online required to generate)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // GENERATED RESULT VIEWER
        if (!generatedResult.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_result_container")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Bar with Copy All & Export Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎬 Video Production Plan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaMint
                            )
                            Text(
                                text = "Duration: $durationMinutes min (~${durationMinutes * 60}s) • $selectedAspectRatio",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy All Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Video Plan", generatedResult)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied entire video plan", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_all_video_plan_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy all",
                                    tint = NovaCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Export Project Button
                            IconButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier.testTag("export_project_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Export project",
                                    tint = NovaMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ACTION CHIPS: Quick Copy Sections
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Copy Script
                        Button(
                            onClick = {
                                val scriptSection = extractSection(generatedResult, "SCENE-BY-SCENE", "VIDEO EDITING")
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Script", scriptSection))
                                Toast.makeText(context, "Copied video script", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSurface, contentColor = NovaCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Copy Script", fontSize = 11.sp)
                        }

                        // Copy Prompts
                        Button(
                            onClick = {
                                val prompts = extractAllPrompts(generatedResult)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("AI Prompts", prompts))
                                Toast.makeText(context, "Copied all AI video prompts", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSurface, contentColor = NovaMint),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Copy All Prompts", fontSize = 11.sp)
                        }

                        // Copy YouTube Metadata
                        Button(
                            onClick = {
                                val title = extractSingleLine(generatedResult, "YouTube Title:").ifBlank { "Video: $idea" }
                                val desc = extractSection(generatedResult, "Description:", "SEO Keywords:")
                                val keywords = extractSingleLine(generatedResult, "SEO Keywords:")
                                val tags = extractSingleLine(generatedResult, "Hashtags:")
                                val thumb = extractSingleLine(generatedResult, "Thumbnail Prompt:")

                                val metadata = """
TITLE:
$title

DESCRIPTION:
$desc

SEO KEYWORDS:
$keywords

HASHTAGS:
$tags

THUMBNAIL PROMPT:
$thumb
                                """.trimIndent()

                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("YouTube Metadata", metadata))
                                Toast.makeText(context, "Copied YouTube Title, Description & Tags", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSurface, contentColor = NovaPink),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Copy YouTube Metadata", fontSize = 11.sp)
                        }

                        // Send to YouTube Studio
                        Button(
                            onClick = {
                                val title = extractSingleLine(generatedResult, "YouTube Title:").ifBlank { "Video: $idea" }
                                val desc = extractSection(generatedResult, "Description:", "SEO Keywords:")
                                val keywords = extractSingleLine(generatedResult, "SEO Keywords:")
                                val tags = extractSingleLine(generatedResult, "Hashtags:")
                                val script = extractSection(generatedResult, "SCENE-BY-SCENE", "VIDEO EDITING")
                                val thumb = extractSingleLine(generatedResult, "Thumbnail Prompt:")

                                onSendToYouTubeStudio(title, desc, keywords, tags, script, thumb)
                                Toast.makeText(context, "Sent to YouTube Creator Studio!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000).copy(alpha = 0.2f), contentColor = Color(0xFFFF4D4D)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send to YouTube Studio", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice card regarding external video generation engine
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NovaBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Video prompts generated successfully. Connect a supported video-generation API (Runway Gen-3 / Luma Dream Machine / Sora / Kling / Veo) to render final video clips using the generated prompts below.",
                            fontSize = 11.sp,
                            color = NovaCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Render Complete Markdown Plan
                    MessageContentRenderer(content = generatedResult)
                }
            }
        }
    }

    // PROJECT HISTORY DIALOG
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = NovaMint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saved Video Projects (${savedProjects.size})")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (savedProjects.isEmpty()) {
                        Text(
                            text = "No saved video projects yet. Generate a video to save your first project!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (p in savedProjects) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NovaSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = p.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${p.durationMinutes}m",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NovaMint
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = p.idea,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            // Open / Load
                                            TextButton(
                                                onClick = {
                                                    onLoadProject(p)
                                                    showHistoryDialog = false
                                                    Toast.makeText(context, "Loaded project: ${p.title}", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text("Open", fontSize = 11.sp, color = NovaCyan)
                                            }

                                            // Duplicate
                                            TextButton(
                                                onClick = {
                                                    onDuplicateProject(p.id)
                                                    Toast.makeText(context, "Duplicated project", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text("Duplicate", fontSize = 11.sp, color = NovaMint)
                                            }

                                            // Delete
                                            IconButton(
                                                onClick = {
                                                    onDeleteProject(p.id)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete project",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // EXPORT PROJECT DIALOG
    if (showExportDialog && !generatedResult.isNullOrBlank()) {
        val exportJson = """
{
  "projectTitle": "${currentProject?.title ?: idea.take(30)}",
  "durationMinutes": $durationMinutes,
  "aspectRatio": "$selectedAspectRatio",
  "style": "$selectedStyle",
  "language": "$selectedLanguage",
  "audience": "$selectedAudience",
  "timestamp": ${System.currentTimeMillis()},
  "content": ${quoteForJson(generatedResult)}
}
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Video Project") },
            text = {
                Column {
                    Text("Export formatted project specification ready for automated video pipeline ingestion:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NovaBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportJson,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Exported Video Project", exportJson))
                        showExportDialog = false
                        Toast.makeText(context, "Exported project copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = NovaBackground)
                ) {
                    Text("Copy Export JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helpers for extracting sections
private fun extractSection(fullText: String, startHeader: String, endHeader: String): String {
    val startIndex = fullText.indexOf(startHeader, ignoreCase = true)
    if (startIndex == -1) return fullText
    val endIndex = fullText.indexOf(endHeader, startIndex + startHeader.length, ignoreCase = true)
    return if (endIndex != -1) {
        fullText.substring(startIndex, endIndex).trim()
    } else {
        fullText.substring(startIndex).trim()
    }
}

private fun extractSingleLine(fullText: String, prefix: String): String {
    return fullText.lines()
        .firstOrNull { it.contains(prefix, ignoreCase = true) }
        ?.replace(prefix, "", ignoreCase = true)
        ?.replace("*", "")
        ?.replace("-", "")
        ?.trim() ?: ""
}

private fun extractAllPrompts(fullText: String): String {
    val lines = fullText.lines()
    val prompts = mutableListOf<String>()
    var inPrompt = false
    var currentPrompt = StringBuilder()

    for (line in lines) {
        if (line.contains("AI VIDEO GENERATION PROMPT", ignoreCase = true) || line.startsWith(">")) {
            inPrompt = true
            currentPrompt.append(line.replace(">", "").trim()).append(" ")
        } else if (inPrompt && line.isBlank()) {
            if (currentPrompt.isNotEmpty()) {
                prompts.add(currentPrompt.toString().trim())
                currentPrompt = StringBuilder()
            }
            inPrompt = false
        } else if (inPrompt) {
            currentPrompt.append(line.replace(">", "").trim()).append(" ")
        }
    }
    if (currentPrompt.isNotEmpty()) {
        prompts.add(currentPrompt.toString().trim())
    }
    return if (prompts.isNotEmpty()) prompts.joinToString("\n\n") else fullText
}

private fun quoteForJson(text: String): String {
    val escaped = text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")
        .replace("\t", "\\t")
    return "\"$escaped\""
}
