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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
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
fun CodingStudioScreen(
    isGenerating: Boolean,
    codeResult: String?,
    isOnline: Boolean,
    onGenerateCode: (language: String, mode: String, taskDescription: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var selectedMode by remember { mutableStateOf("Code generation") }
    var taskDescription by remember { mutableStateOf("") }

    val languages = listOf("Kotlin", "Python", "JavaScript", "TypeScript", "Java", "C++", "Rust", "SQL", "Go")
    val modes = listOf("Code generation", "Code explanation", "Debugging", "Code improvement")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("coding_studio_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(NovaCyan.copy(alpha = 0.2f))
                    .border(1.dp, NovaCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Code Assistant",
                    tint = NovaMint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Coding Studio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Production code, debugging & explanations by Nova",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mode Selector
        Text(
            text = "Coding Task",
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
            for (m in modes) {
                FilterChip(
                    selected = selectedMode == m,
                    onClick = { selectedMode = m },
                    label = { Text(m, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyan,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurface,
                        labelColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // Language Selector
        Text(
            text = "Programming Language",
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
                        selectedContainerColor = NovaMint,
                        selectedLabelColor = NovaBackground,
                        containerColor = NovaSurface,
                        labelColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // Prompt input
        Text(
            text = "Requirement / Code to review",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            placeholder = {
                Text(
                    "Describe what code you need or paste snippet to debug/improve (e.g. 'Room KSP entity with relations', 'FastAPI OAuth2 JWT auth', 'Binary Search Tree traversal')...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            minLines = 3,
            maxLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("code_task_input"),
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

        // Action Button
        Button(
            onClick = {
                onGenerateCode(selectedLanguage, selectedMode, taskDescription)
            },
            enabled = taskDescription.isNotBlank() && !isGenerating && isOnline,
            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = NovaBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("code_generate_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = NovaBackground,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova is writing code...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isOnline) "Run with Nova" else "Offline (Online required)", fontWeight = FontWeight.Bold)
            }
        }

        // Result Card
        if (!codeResult.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("code_result_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Result ($selectedLanguage • $selectedMode)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaMint
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Code", codeResult)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied code to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("code_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = NovaCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MessageContentRenderer(content = codeResult)
                }
            }
        }
    }
}
