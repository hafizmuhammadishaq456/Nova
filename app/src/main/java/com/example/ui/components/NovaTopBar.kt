package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import com.example.viewmodel.AssistantState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaTopBar(
    isOnline: Boolean,
    isVoiceEnabled: Boolean,
    assistantState: AssistantState,
    onMenuClick: () -> Unit,
    onToggleVoice: () -> Unit,
    onClearChat: () -> Unit,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("nova_top_bar"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Mini Orb Logo
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NovaCyan.copy(alpha = 0.2f))
                        .border(1.dp, NovaCyan.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        color = NovaMint,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nova",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // Status pill: Online / Offline mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOnline) StatusOnline.copy(alpha = 0.15f) else StatusOffline.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isOnline) StatusOnline.copy(alpha = 0.5f) else StatusOffline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) StatusOnline else StatusOffline)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "Online" else "Offline mode",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isOnline) StatusOnline else StatusOffline
                                )
                            }
                        }
                    }

                    val stateLabel = when (assistantState) {
                        AssistantState.LISTENING -> "Listening for voice..."
                        AssistantState.SPEAKING -> "Speaking..."
                        AssistantState.THINKING -> "Thinking..."
                        AssistantState.IDLE -> "Personal AI Assistant"
                    }
                    Text(
                        text = stateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (assistantState != AssistantState.IDLE) NovaMint else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("nav_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer",
                    tint = Color.White
                )
            }
        },
        actions = {
            // Voice on/off button
            IconButton(
                onClick = onToggleVoice,
                modifier = Modifier.testTag("voice_toggle_button")
            ) {
                Icon(
                    imageVector = if (isVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (isVoiceEnabled) "Voice enabled" else "Voice muted",
                    tint = if (isVoiceEnabled) NovaCyan else Color.Gray
                )
            }

            // Clear conversation button
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("clear_conversation_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear conversation",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NovaSurface
        )
    )
}
