package com.example.videomaker.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaViolet
import com.example.videomaker.export.VideoExportSystem
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VideoAspectRatio
import com.example.videomaker.model.VideoMakerMode
import com.example.videomaker.model.VideoStyle
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVideoMakerScreen(
    viewModel: AiVideoMakerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projectState by viewModel.projectState.collectAsState()
    val isPlaying by viewModel.previewEngine.isPlaying.collectAsState()
    val currentTimeSec by viewModel.previewEngine.currentTimeSec.collectAsState()
    val currentSceneIndex by viewModel.previewEngine.currentSceneIndex.collectAsState()
    val totalDurationSec by viewModel.previewEngine.totalDurationSec.collectAsState()
    val currentScene by viewModel.previewEngine.currentScene.collectAsState()
    val lipOpenness by viewModel.previewEngine.voiceService.lipOpenness.collectAsState()
    val isSpeakingVoice by viewModel.previewEngine.voiceService.isSpeaking.collectAsState()

    var showAudioMixerSheet by remember { mutableStateOf(false) }
    var showSceneEditorSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showStyleDropdown by remember { mutableStateOf(false) }
    var showCharacterLockDialog by remember { mutableStateOf(false) }
    var selectedSceneToEdit by remember { mutableStateOf<SceneData?>(null) }
    var sceneEditIndex by remember { mutableStateOf(0) }

    // Android Photo Picker (zero broad storage permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setUploadedImageUri(uri)
            Toast.makeText(context, "Image selected for Video Maker", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("ai_video_maker_screen")
    ) {
        // HEADER: AI VIDEO MAKER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NovaCyan.copy(alpha = 0.15f))
                        .border(1.dp, NovaCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "AI Video Maker",
                        tint = NovaCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🎬 AI VIDEO MAKER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Google Flow-grade AI video creation • Free & Unlimited",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (projectState.isGenerated) {
                OutlinedButton(
                    onClick = { viewModel.resetProject() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NovaMint),
                    border = BorderStroke(1.dp, NovaMint.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("New Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. GENERATION MODES: Text to Video, Image to Video, Story to Video
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (m in VideoMakerMode.values()) {
                val isSelected = projectState.mode == m
                Surface(
                    onClick = { viewModel.setMode(m) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NovaCyan else NovaSurfaceElevated,
                    border = BorderStroke(1.dp, if (isSelected) NovaMint else NovaBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_${m.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = m.iconEmoji,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = m.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NovaBackground else Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. PROMPT / STORY INPUT
        Text(
            text = when (projectState.mode) {
                VideoMakerMode.STORY_TO_VIDEO -> "Story / Narrative"
                VideoMakerMode.IMAGE_TO_VIDEO -> "Animation & Motion Prompt"
                VideoMakerMode.TEXT_TO_VIDEO -> "Prompt"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = when (projectState.mode) {
                VideoMakerMode.STORY_TO_VIDEO -> projectState.storyText
                else -> projectState.prompt
            },
            onValueChange = {
                when (projectState.mode) {
                    VideoMakerMode.STORY_TO_VIDEO -> viewModel.setStoryText(it)
                    else -> viewModel.setPrompt(it)
                }
            },
            placeholder = {
                Text(
                    text = when (projectState.mode) {
                        VideoMakerMode.STORY_TO_VIDEO -> "Enter a full story. AI will partition into scenes, lock characters, and generate dialogue..."
                        VideoMakerMode.IMAGE_TO_VIDEO -> "Describe how to animate the image (e.g. camera pans, wind blowing hair, neon glow)..."
                        VideoMakerMode.TEXT_TO_VIDEO -> "Describe your video here... (e.g. A cyber samurai entering a neon forest under rain)"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            minLines = if (projectState.mode == VideoMakerMode.STORY_TO_VIDEO) 4 else 2,
            maxLines = 7,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_prompt_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaCyan,
                unfocusedBorderColor = NovaBorder,
                focusedContainerColor = NovaSurface,
                unfocusedContainerColor = NovaSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // IMAGE UPLOAD CARD (Prominent in Image-to-Video, also available as reference)
        if (projectState.mode == VideoMakerMode.IMAGE_TO_VIDEO || projectState.uploadedImageUri != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NovaSurface,
                border = BorderStroke(1.dp, if (projectState.uploadedImageUri != null) NovaMint else NovaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (projectState.uploadedImageUri != null) {
                            AsyncImage(
                                model = projectState.uploadedImageUri,
                                contentDescription = "Uploaded source image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, NovaMint, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Source Image Loaded",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NovaMint
                                )
                                Text(
                                    text = "AI will animate this keyframe into video",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NovaSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = NovaCyan)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Upload Image to Animate",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "JPG/PNG to generate seamless motion",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NovaCyan,
                                contentColor = NovaBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("upload_image_button")
                        ) {
                            Text(
                                text = if (projectState.uploadedImageUri != null) "Change" else "Upload",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (projectState.uploadedImageUri != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { viewModel.setUploadedImageUri(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. DURATION SELECTOR (1, 2, 3, 4, 5 Minutes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Duration",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "${projectState.durationMinutes * 60}s total • ~${when (projectState.durationMinutes) { 1 -> 6; 2 -> 12; 3 -> 18; 4 -> 24; else -> 30 }} scenes",
                fontSize = 11.sp,
                color = NovaMint
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val durations = listOf(1, 2, 3, 4, 5)
            for (min in durations) {
                val isSelected = projectState.durationMinutes == min
                Card(
                    onClick = { viewModel.setDurationMinutes(min) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NovaCyan else NovaSurfaceElevated
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NovaMint else NovaBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("duration_${min}_min")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "[ $min Min ]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NovaBackground else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. ASPECT RATIO (9:16, 16:9, 1:1)
        Text(
            text = "Ratio",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val ratios = listOf(
                VideoAspectRatio.RATIO_9_16 to "[ 9:16 ]",
                VideoAspectRatio.RATIO_16_9 to "[ 16:9 ]",
                VideoAspectRatio.RATIO_1_1 to "[ 1:1 ]"
            )
            for ((ar, label) in ratios) {
                val isSelected = projectState.aspectRatio == ar
                Card(
                    onClick = { viewModel.setAspectRatio(ar) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NovaCyan else NovaSurfaceElevated
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NovaMint else NovaBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NovaBackground else Color.White
                        )
                        Text(
                            text = ar.ratioLabel.substringAfter(" "),
                            fontSize = 9.sp,
                            color = if (isSelected) NovaBackground.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. STYLE & CHARACTER CONSISTENCY ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Style Selector Dropdown Card
            Box(modifier = Modifier.weight(1.2f)) {
                Surface(
                    onClick = { showStyleDropdown = true },
                    shape = RoundedCornerShape(10.dp),
                    color = NovaSurfaceElevated,
                    border = BorderStroke(1.dp, NovaBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Style", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "[ ${projectState.style.displayName} ▼ ]",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaMint
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showStyleDropdown,
                    onDismissRequest = { showStyleDropdown = false },
                    modifier = Modifier.background(NovaSurfaceElevated)
                ) {
                    for (st in VideoStyle.values()) {
                        DropdownMenuItem(
                            text = { Text(st.displayName, color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                viewModel.setStyle(st)
                                showStyleDropdown = false
                            }
                        )
                    }
                }
            }

            // Character Consistency Lock Button
            Surface(
                onClick = { showCharacterLockDialog = true },
                shape = RoundedCornerShape(10.dp),
                color = NovaSurfaceElevated,
                border = BorderStroke(1.dp, if (projectState.character.name.isNotBlank()) NovaPink else NovaBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (projectState.character.name.isNotBlank()) NovaPink else NovaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Character Lock", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (projectState.character.name.isNotBlank()) projectState.character.name else "Set Lock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // VOICE & LANGUAGE SETTINGS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (lang in VoiceLanguage.values()) {
                FilterChip(
                    selected = projectState.voiceLanguage == lang,
                    onClick = { viewModel.setVoiceLanguage(lang) },
                    label = { Text("🗣 ${lang.displayName}", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyan,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurfaceElevated,
                        labelColor = Color.White
                    )
                )
            }

            for (gen in VoiceGender.values()) {
                FilterChip(
                    selected = projectState.voiceGender == gen,
                    onClick = { viewModel.setVoiceGender(gen) },
                    label = { Text(gen.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaPink,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurfaceElevated,
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // GENERATE VIDEO BUTTON
        Button(
            onClick = { viewModel.generateVideo() },
            enabled = !projectState.isGenerating &&
                    (projectState.prompt.isNotBlank() || projectState.storyText.isNotBlank() || projectState.uploadedImageUri != null),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyan,
                contentColor = NovaBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_video_button")
        ) {
            if (projectState.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = NovaBackground,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = projectState.generationStepText.ifBlank { "Generating AI Video..." },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎬 Generate Video (${projectState.durationMinutes} Min)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (projectState.isGenerating) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { projectState.generationProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = NovaCyan,
                trackColor = NovaSurfaceElevated
            )
        }

        // POST-GENERATION SUITE: PREVIEW, AUDIO, EDIT, EXPORT
        if (projectState.isGenerated && projectState.scenes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            // Action Ribbon: [ ▶ Preview ] [ 🔊 Audio ] [ ✂ Edit ] [ ⬇ Export ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preview Toggle
                Button(
                    onClick = { viewModel.previewEngine.togglePlayPause() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) NovaPink else NovaCyan,
                        contentColor = NovaBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("preview_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlaying) "Pause" else "▶ Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Audio Mixer Button
                Button(
                    onClick = { showAudioMixerSheet = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaSurfaceElevated,
                        contentColor = NovaMint
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("audio_button")
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🔊 Audio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Edit Scenes Button
                Button(
                    onClick = { showSceneEditorSheet = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaSurfaceElevated,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("edit_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✂ Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Export Button
                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaMint,
                        contentColor = NovaBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("export_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("⬇ Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // IN-APP VIDEO CANVAS PLAYER
            VideoPlayerCanvas(
                projectState = projectState,
                currentScene = currentScene,
                isPlaying = isPlaying,
                currentTimeSec = currentTimeSec,
                totalDurationSec = totalDurationSec,
                currentSceneIndex = currentSceneIndex,
                lipOpenness = lipOpenness,
                isSpeakingVoice = isSpeakingVoice,
                onTogglePlayPause = { viewModel.previewEngine.togglePlayPause() },
                onSeek = { viewModel.previewEngine.seekTo(it) },
                onSceneSelect = { viewModel.previewEngine.seekToScene(it) }
            )
        }
    }

    // BOTTOM SHEETS & DIALOGS

    // 1. AUDIO MIXER SHEET
    if (showAudioMixerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioMixerSheet = false },
            containerColor = NovaSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            AudioMixerSheetContent(
                currentMix = projectState.audioMix,
                onMixChanged = { viewModel.updateAudioMix(it) },
                onClose = { showAudioMixerSheet = false }
            )
        }
    }

    // 2. SCENE SCRIPT & DIALOGUE EDITOR SHEET
    if (showSceneEditorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSceneEditorSheet = false },
            containerColor = NovaSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            SceneEditorSheetContent(
                scenes = projectState.scenes,
                onSaveScene = { idx, sc -> viewModel.updateScene(idx, sc) },
                onClose = { showSceneEditorSheet = false }
            )
        }
    }

    // 3. EXPORT DIALOG (720p, 1080p, 4K — No Watermark)
    if (showExportDialog) {
        ExportDialog(
            projectState = projectState,
            onExport = { res ->
                viewModel.exportVideo(res) { fileUri ->
                    Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                }
            },
            onShare = { uri ->
                VideoExportSystem.shareExportedVideo(context, uri, projectState.title)
            },
            onDismiss = { showExportDialog = false }
        )
    }

    // 4. CHARACTER CONSISTENCY LOCK DIALOG
    if (showCharacterLockDialog) {
        CharacterConsistencyDialog(
            initial = projectState.character,
            onSave = {
                viewModel.updateCharacter(it)
                showCharacterLockDialog = false
            },
            onDismiss = { showCharacterLockDialog = false }
        )
    }
}

// IN-APP VIDEO CANVAS PLAYER
@Composable
private fun VideoPlayerCanvas(
    projectState: com.example.videomaker.model.VideoProjectState,
    currentScene: SceneData?,
    isPlaying: Boolean,
    currentTimeSec: Float,
    totalDurationSec: Int,
    currentSceneIndex: Int,
    lipOpenness: Float,
    isSpeakingVoice: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSceneSelect: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val kenBurnsScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF030712),
        border = BorderStroke(1.dp, NovaBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_player_canvas")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Player Top Info Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentScene?.title ?: "Scene ${currentSceneIndex + 1}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = NovaMint.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Scene ${currentSceneIndex + 1}/${projectState.scenes.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaMint,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = projectState.aspectRatio.ratioLabel.substringBefore(" "),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Aspect Ratio Video Canvas Container
            val aspectFloat = when (projectState.aspectRatio) {
                VideoAspectRatio.RATIO_9_16 -> 9f / 16f
                VideoAspectRatio.RATIO_1_1 -> 1f
                VideoAspectRatio.RATIO_16_9 -> 16f / 9f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (projectState.aspectRatio == VideoAspectRatio.RATIO_9_16) 360.dp else 220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E1B4B),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .clickable { onTogglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                // If user uploaded an image, display it with dynamic Ken Burns motion
                if (projectState.uploadedImageUri != null) {
                    AsyncImage(
                        model = projectState.uploadedImageUri,
                        contentDescription = "Source animated keyframe",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (isPlaying) kenBurnsScale else 1.0f)
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
                } else {
                    // Stylized cinematic ambient atmosphere
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .scale(if (isPlaying) kenBurnsScale else 1.0f)
                    ) {
                        Text(
                            text = "🎬 ${projectState.style.displayName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentScene?.description ?: projectState.title,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Play / Pause Indicator Overlay
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, NovaCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = NovaCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Lower-Third Subtitle & Lip-Sync Bar
                if (currentScene?.dialogue?.isNotBlank() == true) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentScene.characterSpeaker,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NovaMint
                                )
                                if (isSpeakingVoice) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Visual mouth aperture / lip-sync pulse
                                    Box(
                                        modifier = Modifier
                                            .size(width = 12.dp, height = (8 + (lipOpenness * 10)).dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(NovaPink)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${currentScene.dialogue}\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TIMELINE SCRUBBER & TIMECODE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentMin = currentTimeSec.toInt() / 60
                val currentSecRemainder = currentTimeSec.toInt() % 60
                val totalMin = totalDurationSec / 60
                val totalSecRemainder = totalDurationSec % 60

                Text(
                    text = String.format("%02d:%02d", currentMin, currentSecRemainder),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaCyan
                )

                Slider(
                    value = currentTimeSec,
                    onValueChange = { onSeek(it) },
                    valueRange = 0f..totalDurationSec.toFloat(),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = NovaCyan,
                        activeTrackColor = NovaMint,
                        inactiveTrackColor = NovaSurfaceElevated
                    )
                )

                Text(
                    text = String.format("%02d:%02d", totalMin, totalSecRemainder),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // SCENE THUMBNAIL SELECTOR CAROUSEL
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(projectState.scenes) { idx, sc ->
                    val isCurrent = idx == currentSceneIndex
                    Surface(
                        onClick = { onSceneSelect(idx) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) NovaCyan.copy(alpha = 0.25f) else NovaSurfaceElevated,
                        border = BorderStroke(1.dp, if (isCurrent) NovaCyan else NovaBorder),
                        modifier = Modifier.widthIn(min = 90.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "Scene ${sc.sceneNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) NovaCyan else Color.White
                            )
                            Text(
                                text = "${sc.startSec}s - ${sc.endSec}s",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// 1. AUDIO MIXER SHEET CONTENT
@Composable
private fun AudioMixerSheetContent(
    currentMix: com.example.videomaker.model.AudioMix,
    onMixChanged: (com.example.videomaker.model.AudioMix) -> Unit,
    onClose: () -> Unit
) {
    var voiceVol by remember { mutableStateOf(currentMix.voiceVolume) }
    var musicVol by remember { mutableStateOf(currentMix.musicVolume) }
    var sfxVol by remember { mutableStateOf(currentMix.sfxVolume) }
    var lipSyncEnabled by remember { mutableStateOf(currentMix.isLipSyncEnabled) }
    var selectedMusic by remember { mutableStateOf(currentMix.musicTrack) }

    val musicTracks = listOf(
        "Cinematic Ambient",
        "Dramatic Orchestral",
        "Cyberpunk Pulse",
        "Lo-Fi Chill",
        "Epic Adventure"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("audio_mixer_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NovaMint)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔊 Audio Studio Mixer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Volume Slider
        Text("Character Voice Volume: ${(voiceVol * 100).toInt()}%", fontSize = 12.sp, color = NovaCyan)
        Slider(
            value = voiceVol,
            onValueChange = {
                voiceVol = it
                onMixChanged(currentMix.copy(voiceVolume = it))
            },
            colors = SliderDefaults.colors(thumbColor = NovaCyan, activeTrackColor = NovaCyan)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Music Volume Slider
        Text("Background Music Volume: ${(musicVol * 100).toInt()}%", fontSize = 12.sp, color = NovaMint)
        Slider(
            value = musicVol,
            onValueChange = {
                musicVol = it
                onMixChanged(currentMix.copy(musicVolume = it))
            },
            colors = SliderDefaults.colors(thumbColor = NovaMint, activeTrackColor = NovaMint)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SFX Volume Slider
        Text("Sound Effects (SFX) Volume: ${(sfxVol * 100).toInt()}%", fontSize = 12.sp, color = NovaPink)
        Slider(
            value = sfxVol,
            onValueChange = {
                sfxVol = it
                onMixChanged(currentMix.copy(sfxVolume = it))
            },
            colors = SliderDefaults.colors(thumbColor = NovaPink, activeTrackColor = NovaPink)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Lip-Sync Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Natural Lip-Sync", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Calculates speech phonemes & mouth aperture", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = lipSyncEnabled,
                onCheckedChange = {
                    lipSyncEnabled = it
                    onMixChanged(currentMix.copy(isLipSyncEnabled = it))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = NovaMint, checkedTrackColor = NovaCyan)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Music Track Choice
        Text("Background Score Track", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (track in musicTracks) {
                FilterChip(
                    selected = selectedMusic == track,
                    onClick = {
                        selectedMusic = track
                        onMixChanged(currentMix.copy(musicTrack = track))
                    },
                    label = { Text(track, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaMint,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurfaceElevated,
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// 2. SCENE EDITOR SHEET CONTENT
@Composable
private fun SceneEditorSheetContent(
    scenes: List<SceneData>,
    onSaveScene: (Int, SceneData) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(0) }
    val current = scenes.getOrNull(selectedIndex)

    var editDialogue by remember(selectedIndex) { mutableStateOf(current?.dialogue ?: "") }
    var editSpeaker by remember(selectedIndex) { mutableStateOf(current?.characterSpeaker ?: "") }
    var editCamera by remember(selectedIndex) { mutableStateOf(current?.cameraDirection ?: "") }
    var editPrompt by remember(selectedIndex) { mutableStateOf(current?.aiVideoPrompt ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("scene_editor_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = NovaCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("✂ Scene & Dialogue Editor", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scene Picker Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(scenes) { idx, sc ->
                FilterChip(
                    selected = idx == selectedIndex,
                    onClick = { selectedIndex = idx },
                    label = { Text("Scene ${sc.sceneNumber}", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyan,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurfaceElevated,
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (current != null) {
            Text("Dialogue & Voiceover", fontSize = 12.sp, color = NovaMint)
            OutlinedTextField(
                value = editDialogue,
                onValueChange = { editDialogue = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaMint,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Speaker Name", fontSize = 12.sp, color = NovaMint)
            OutlinedTextField(
                value = editSpeaker,
                onValueChange = { editSpeaker = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaMint,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("AI Video Prompt", fontSize = 12.sp, color = NovaCyan)
            OutlinedTextField(
                value = editPrompt,
                onValueChange = { editPrompt = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaCyan,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val updated = current.copy(
                        dialogue = editDialogue,
                        characterSpeaker = editSpeaker,
                        aiVideoPrompt = editPrompt
                    )
                    onSaveScene(selectedIndex, updated)
                    Toast.makeText(context, "Saved Scene ${current.sceneNumber}", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = NovaBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Scene Changes", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// 3. EXPORT DIALOG (720p, 1080p, 4K — No Watermark)
@Composable
private fun ExportDialog(
    projectState: com.example.videomaker.model.VideoProjectState,
    onExport: (String) -> Unit,
    onShare: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf("1080p") }
    val resolutions = listOf("720p", "1080p", "4K")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NovaSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, tint = NovaMint)
                Spacer(modifier = Modifier.width(8.dp))
                Text("⬇ Export Video (No Watermark)")
            }
        },
        text = {
            Column {
                Text(
                    text = "Export production master without artificial watermarks. Clean, uncompressed audio & visual sequence.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Resolution:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (res in resolutions) {
                        val isSelected = selectedRes == res
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRes = res },
                            label = { Text(res, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaMint,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaSurfaceElevated,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NovaSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("• Aspect Ratio: ${projectState.aspectRatio.ratioLabel}", fontSize = 11.sp, color = NovaCyan)
                        Text("• Duration: ${projectState.durationMinutes} Min (${projectState.scenes.size} Scenes)", fontSize = 11.sp, color = NovaMint)
                        Text("• Watermark: None (100% Clean)", fontSize = 11.sp, color = NovaPink)
                    }
                }

                if (projectState.isExporting) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NovaMint, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering video file...", fontSize = 11.sp, color = NovaMint)
                    }
                }

                if (projectState.exportedFilePath != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✓ Ready: ${projectState.exportedFilePath}",
                        fontSize = 10.sp,
                        color = NovaMint
                    )
                }
            }
        },
        confirmButton = {
            if (projectState.exportedFilePath != null) {
                Button(
                    onClick = {
                        val uri = Uri.parse(projectState.exportedFilePath)
                        onShare(uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPink, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Video")
                }
            } else {
                Button(
                    onClick = { onExport(selectedRes) },
                    enabled = !projectState.isExporting,
                    colors = ButtonDefaults.buttonColors(containerColor = NovaMint, contentColor = NovaBackground)
                ) {
                    Text("Export $selectedRes")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// 4. CHARACTER CONSISTENCY DIALOG
@Composable
private fun CharacterConsistencyDialog(
    initial: com.example.videomaker.model.CharacterConsistency,
    onSave: (com.example.videomaker.model.CharacterConsistency) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var face by remember { mutableStateOf(initial.faceFeatures) }
    var hair by remember { mutableStateOf(initial.hairStyle) }
    var clothes by remember { mutableStateOf(initial.clothes) }
    var age by remember { mutableStateOf(initial.age) }
    var proportions by remember { mutableStateOf(initial.bodyProportions) }
    var colors by remember { mutableStateOf(initial.colors) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NovaSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = NovaPink)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔒 Character Consistency Engine")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Locks the character's facial structure, hair, clothes, and colors across all generated scenes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Character Name / Identity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = face,
                    onValueChange = { face = it },
                    label = { Text("Face Features & Eyes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = hair,
                    onValueChange = { hair = it },
                    label = { Text("Hair Style & Color") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = clothes,
                    onValueChange = { clothes = it },
                    label = { Text("Signature Clothing / Attire") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age & Body Proportions") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = colors,
                    onValueChange = { colors = it },
                    label = { Text("Color Palette (e.g. Teal & Crimson)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initial.copy(
                            name = name,
                            faceFeatures = face,
                            hairStyle = hair,
                            clothes = clothes,
                            age = age,
                            bodyProportions = proportions,
                            colors = colors
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPink, contentColor = Color.White)
            ) {
                Text("Lock Character")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
