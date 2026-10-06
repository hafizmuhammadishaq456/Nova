package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaTopBar
import com.example.ui.screens.CodingStudioScreen
import com.example.ui.screens.MainChatScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideoCreatorStudioScreen
import com.example.ui.screens.YouTubeStudioScreen
import com.example.videomaker.ui.AiVideoMakerScreen
import com.example.videomaker.ui.AiVideoMakerViewModel
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.viewmodel.AssistantTab
import com.example.viewmodel.NovaViewModel
import kotlinx.coroutines.launch

@Composable
fun NovaAppScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val todos by viewModel.todos.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    val youtubeResult by viewModel.youtubeResult.collectAsState()
    val isGeneratingYoutube by viewModel.isGeneratingYoutube.collectAsState()

    val codeResult by viewModel.codeResult.collectAsState()
    val isGeneratingCode by viewModel.isGeneratingCode.collectAsState()

    val durationMinutes by viewModel.videoDurationMinutes.collectAsState()
    val isGeneratingVideo by viewModel.isGeneratingVideo.collectAsState()
    val generatedVideoContent by viewModel.generatedVideoContent.collectAsState()
    val currentVideoProject by viewModel.currentVideoProject.collectAsState()
    val savedVideoProjects by viewModel.videoProjects.collectAsState()
    val transferredYouTubeTopic by viewModel.transferredYouTubeTopic.collectAsState()

    val aiVideoMakerViewModel: AiVideoMakerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen || uiState.currentTab != AssistantTab.CHAT) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.setTab(AssistantTab.CHAT)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NovaSurface,
                drawerContentColor = Color.White,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NovaCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = NovaMint,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Nova",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaMint
                            )
                            Text(
                                text = "AI Assistant & Video Studio",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = NovaBorder, modifier = Modifier.padding(vertical = 8.dp))

                    // Studio Shortcuts in Drawer
                    Text(
                        text = "Studios & Modes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Movie, contentDescription = null, tint = NovaMint) },
                        label = { Text("🎬 AI Video Maker", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        selected = uiState.currentTab == AssistantTab.VIDEO,
                        onClick = {
                            viewModel.setTab(AssistantTab.VIDEO)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NovaCyan.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color(0xFFFF4D4D)) },
                        label = { Text("📺 YouTube Creator Studio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        selected = uiState.currentTab == AssistantTab.YOUTUBE,
                        onClick = {
                            viewModel.setTab(AssistantTab.YOUTUBE)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NovaCyan.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = NovaCyan) },
                        label = { Text("💻 Coding Studio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        selected = uiState.currentTab == AssistantTab.CODE,
                        onClick = {
                            viewModel.setTab(AssistantTab.CODE)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NovaCyan.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    HorizontalDivider(color = NovaBorder, modifier = Modifier.padding(vertical = 8.dp))

                    // New Chat Item
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = NovaCyan) },
                        label = { Text("New Conversation", fontWeight = FontWeight.SemiBold, color = Color.White) },
                        selected = false,
                        onClick = {
                            viewModel.createNewConversation()
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = NovaBackground
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Recent Chats",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    // Conversations
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        for (conv in conversations) {
                            val isSelected = conv.id == uiState.currentConversationId && uiState.currentTab == AssistantTab.CHAT
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NovaCyan.copy(alpha = 0.15f) else Color.Transparent)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            Icons.Default.ChatBubble,
                                            contentDescription = null,
                                            tint = if (isSelected) NovaCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = conv.title,
                                            maxLines = 1,
                                            fontSize = 12.sp,
                                            color = if (isSelected) NovaMint else Color.White
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.selectConversation(conv.id)
                                        viewModel.setTab(AssistantTab.CHAT)
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = Color.Transparent,
                                        unselectedContainerColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                if (conversations.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.deleteConversation(conv.id) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete chat",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = NovaBorder, modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Projects: ${savedVideoProjects.size}  •  Memories: ${memories.size}  •  Tasks: ${todos.count { !it.isCompleted }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                NovaTopBar(
                    isOnline = isOnline,
                    isVoiceEnabled = uiState.isVoiceEnabled,
                    assistantState = uiState.assistantState,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onToggleVoice = { viewModel.toggleVoiceEnabled() },
                    onClearChat = { viewModel.clearAllConversations() },
                    onOrbClick = { viewModel.startVoiceInput() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = NovaSurface,
                    contentColor = Color.White,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    // 1. Chat
                    NavigationBarItem(
                        selected = uiState.currentTab == AssistantTab.CHAT,
                        onClick = { viewModel.setTab(AssistantTab.CHAT) },
                        icon = {
                            Icon(
                                if (uiState.currentTab == AssistantTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Chat", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaBackground,
                            selectedTextColor = NovaMint,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 2. AI Video Maker
                    NavigationBarItem(
                        selected = uiState.currentTab == AssistantTab.VIDEO,
                        onClick = { viewModel.setTab(AssistantTab.VIDEO) },
                        icon = {
                            Icon(
                                if (uiState.currentTab == AssistantTab.VIDEO) Icons.Filled.Movie else Icons.Outlined.Movie,
                                contentDescription = "AI Video Maker",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Video Maker", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaBackground,
                            selectedTextColor = NovaMint,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 3. YouTube Studio
                    NavigationBarItem(
                        selected = uiState.currentTab == AssistantTab.YOUTUBE,
                        onClick = { viewModel.setTab(AssistantTab.YOUTUBE) },
                        icon = {
                            Icon(
                                if (uiState.currentTab == AssistantTab.YOUTUBE) Icons.Filled.SmartDisplay else Icons.Outlined.SmartDisplay,
                                contentDescription = "YouTube",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("YouTube", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaBackground,
                            selectedTextColor = NovaMint,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 4. Code Studio
                    NavigationBarItem(
                        selected = uiState.currentTab == AssistantTab.CODE,
                        onClick = { viewModel.setTab(AssistantTab.CODE) },
                        icon = {
                            Icon(
                                if (uiState.currentTab == AssistantTab.CODE) Icons.Filled.Terminal else Icons.Outlined.Terminal,
                                contentDescription = "Code",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Code", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaBackground,
                            selectedTextColor = NovaMint,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 5. Memory
                    NavigationBarItem(
                        selected = uiState.currentTab == AssistantTab.MEMORY,
                        onClick = { viewModel.setTab(AssistantTab.MEMORY) },
                        icon = {
                            Icon(
                                if (uiState.currentTab == AssistantTab.MEMORY) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                                contentDescription = "Memory",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Memory", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaBackground,
                            selectedTextColor = NovaMint,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 6. Settings
                    NavigationBarItem(
                        selected = uiState.currentTab == AssistantTab.SETTINGS,
                        onClick = { viewModel.setTab(AssistantTab.SETTINGS) },
                        icon = {
                            Icon(
                                if (uiState.currentTab == AssistantTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Settings", fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaBackground,
                            selectedTextColor = NovaMint,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    AssistantTab.CHAT -> {
                        MainChatScreen(
                            messages = messages,
                            inputText = uiState.inputText,
                            isLoading = uiState.isLoading,
                            isOnline = isOnline,
                            errorMessage = uiState.errorMessage,
                            assistantState = uiState.assistantState,
                            isSpeaking = isSpeaking,
                            onInputTextChange = { viewModel.setInputText(it) },
                            onSendMessage = { text, isVoice -> viewModel.sendMessage(text, isVoice) },
                            onStartVoice = { viewModel.startVoiceInput() },
                            onStopVoice = { viewModel.stopVoiceInput() },
                            onSpeakMessage = { text, lang -> viewModel.speakText(text, lang) },
                            onStopSpeaking = { viewModel.stopSpeaking() },
                            onDismissError = { viewModel.dismissError() }
                        )
                    }
                    AssistantTab.VIDEO -> {
                        AiVideoMakerScreen(
                            viewModel = aiVideoMakerViewModel
                        )
                    }
                    AssistantTab.YOUTUBE -> {
                        YouTubeStudioScreen(
                            isGenerating = isGeneratingYoutube,
                            generatedResult = youtubeResult,
                            isOnline = isOnline,
                            prefilledTopic = transferredYouTubeTopic,
                            onGenerate = { topic, type, audience, language ->
                                viewModel.generateYouTubeContent(topic, type, audience, language)
                            }
                        )
                    }
                    AssistantTab.CODE -> {
                        CodingStudioScreen(
                            isGenerating = isGeneratingCode,
                            codeResult = codeResult,
                            isOnline = isOnline,
                            onGenerateCode = { language, mode, taskDescription ->
                                viewModel.generateCode(language, mode, taskDescription)
                            }
                        )
                    }
                    AssistantTab.MEMORY -> {
                        MemoryScreen(
                            memories = memories,
                            todos = todos,
                            onAddMemory = { k, v, c -> viewModel.addMemory(k, v, c) },
                            onDeleteMemory = { viewModel.deleteMemory(it) },
                            onAddTodo = { t, n, p -> viewModel.addTodo(t, n, p) },
                            onToggleTodo = { id, done -> viewModel.toggleTodo(id, done) },
                            onDeleteTodo = { viewModel.deleteTodo(it) }
                        )
                    }
                    AssistantTab.SETTINGS -> {
                        SettingsScreen(
                            isVoiceEnabled = uiState.isVoiceEnabled,
                            isWakeWordEnabled = uiState.isWakeWordEnabled,
                            selectedLanguage = uiState.selectedLanguage,
                            backendUrl = uiState.backendUrl,
                            isBackendHealthy = uiState.isBackendHealthy,
                            onVoiceToggle = { viewModel.toggleVoiceEnabled() },
                            onWakeWordToggle = { viewModel.toggleWakeWord(it) },
                            onLanguageChange = { viewModel.setSelectedLanguage(it) },
                            onBackendUrlChange = { viewModel.updateBackendUrl(it) },
                            onCheckHealth = { viewModel.checkBackendHealth() },
                            onClearChatHistory = { viewModel.clearAllConversations() },
                            onClearMemory = { viewModel.clearAllMemories() }
                        )
                    }
                }
            }
        }
    }
}
