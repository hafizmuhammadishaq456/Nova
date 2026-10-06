package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusOnline

@Composable
fun SettingsScreen(
    isVoiceEnabled: Boolean,
    isWakeWordEnabled: Boolean,
    selectedLanguage: String,
    backendUrl: String,
    isBackendHealthy: Boolean,
    onVoiceToggle: () -> Unit,
    onWakeWordToggle: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBackendUrlChange: (String) -> Unit,
    onCheckHealth: () -> Unit,
    onClearChatHistory: () -> Unit,
    onClearMemory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tempUrl by remember { mutableStateOf(backendUrl) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearMemoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen_root")
    ) {
        // Assistant Identity Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NovaCyan.copy(alpha = 0.2f))
                        .border(1.5.dp, NovaCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nova",
                        fontWeight = FontWeight.Bold,
                        color = NovaMint,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Nova AI Assistant",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Secure OpenAI Backend • Voice • Offline Memory",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice & Audio Configuration
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Voice Output", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Speak responses aloud via Android TTS", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isVoiceEnabled,
                        onCheckedChange = { onVoiceToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaBackground,
                            checkedTrackColor = NovaCyan
                        ),
                        modifier = Modifier.testTag("voice_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Hearing, contentDescription = null, tint = NovaMint, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Wake Word ('Hey Nova')", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("On-device offline keyword listening", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isWakeWordEnabled,
                        onCheckedChange = onWakeWordToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaBackground,
                            checkedTrackColor = NovaMint
                        ),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Selector
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Selected Language", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Nova fluently supports Urdu, Hindi, Roman Urdu, and English.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                val languages = listOf(
                    "auto" to "Auto-Detect Language",
                    "ur" to "Urdu (اردو)",
                    "hi" to "Hindi (हिन्दी)",
                    "roman_ur" to "Roman Urdu",
                    "en" to "English"
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for ((code, name) in languages) {
                        FilterChip(
                            selected = selectedLanguage == code,
                            onClick = { onLanguageChange(code) },
                            label = { Text(name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaCyan,
                                selectedLabelColor = NovaBackground,
                                containerColor = NovaBackground,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backend & Security Connection
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NovaMint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Backend & OpenAI Security", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isBackendHealthy) StatusOnline else StatusError)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBackendHealthy) "Backend Connected (Port 3000)" else "Backend Disconnected / Checking...",
                        fontSize = 12.sp,
                        color = if (isBackendHealthy) StatusOnline else StatusError
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "OpenAI API keys are kept safely on the Node.js backend environment and never exposed in the Android app.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    label = { Text("Backend URL Endpoint") },
                    placeholder = { Text("http://10.0.2.2:3000/") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backend_url_input"),
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onBackendUrlChange(tempUrl)
                            Toast.makeText(context, "Backend URL updated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = NovaBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save URL")
                    }

                    Button(
                        onClick = onCheckHealth,
                        colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceElevated, contentColor = NovaMint),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Check Connection")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Storage & Clear Actions
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Data & Memory Management", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showClearHistoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Chats", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showClearMemoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Memory", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Nova
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NovaSurface.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About Nova", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nova is an on-device enabled Personal AI Assistant powered by OpenAI via a secure server, featuring local Room database memory, wake word ('Hey Nova') architecture, and multilingual conversation in English, Urdu, and Hindi.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Chat History?") },
            text = { Text("All conversation history will be permanently deleted from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChatHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearMemoryDialog = false },
            title = { Text("Clear Assistant Memories?") },
            text = { Text("All user preferences and remembered facts will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearMemory()
                        showClearMemoryDialog = false
                        Toast.makeText(context, "Memories cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showClearMemoryDialog = false }) { Text("Cancel") }
            }
        )
    }
}
