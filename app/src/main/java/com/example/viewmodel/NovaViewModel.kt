package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.NovaDatabase
import com.example.model.ConversationEntity
import com.example.model.MessageEntity
import com.example.model.TodoEntity
import com.example.model.UserMemoryEntity
import com.example.model.VideoProjectEntity
import com.example.network.ApiClient
import com.example.network.NetworkMonitor
import com.example.repository.NovaRepository
import com.example.speech.SpeechRecognitionManager
import com.example.speech.TextToSpeechManager
import com.example.wakeword.WakeWordManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantTab {
    CHAT,
    VIDEO,
    YOUTUBE,
    CODE,
    MEMORY,
    SETTINGS
}

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class NovaUiState(
    val currentTab: AssistantTab = AssistantTab.CHAT,
    val currentConversationId: Long = 1,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val assistantState: AssistantState = AssistantState.IDLE,
    val isVoiceEnabled: Boolean = true,
    val isWakeWordEnabled: Boolean = true,
    val selectedLanguage: String = "auto", // "auto", "ur", "hi", "roman_ur", "en"
    val backendUrl: String = ApiClient.DEFAULT_BASE_URL,
    val isBackendHealthy: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NovaDatabase.getDatabase(application, viewModelScope)
    private val networkMonitor = NetworkMonitor(application)
    val repository = NovaRepository(database, networkMonitor)

    val speechManager = SpeechRecognitionManager(application)
    val ttsManager = TextToSpeechManager(application)
    val wakeWordManager = WakeWordManager(application, viewModelScope)

    private val _uiState = MutableStateFlow(NovaUiState())
    val uiState: StateFlow<NovaUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = repository.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<UserMemoryEntity>> = repository.memories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todos: StateFlow<List<TodoEntity>> = repository.todos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoProjects: StateFlow<List<VideoProjectEntity>> = repository.videoProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<Long>(1)
    val messages: StateFlow<List<MessageEntity>> = _currentConversationId
        .flatMapLatest { id -> repository.getMessages(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Studio tool states
    private val _youtubeResult = MutableStateFlow<String?>(null)
    val youtubeResult: StateFlow<String?> = _youtubeResult.asStateFlow()
    private val _isGeneratingYoutube = MutableStateFlow(false)
    val isGeneratingYoutube: StateFlow<Boolean> = _isGeneratingYoutube.asStateFlow()

    private val _codeResult = MutableStateFlow<String?>(null)
    val codeResult: StateFlow<String?> = _codeResult.asStateFlow()
    private val _isGeneratingCode = MutableStateFlow(false)
    val isGeneratingCode: StateFlow<Boolean> = _isGeneratingCode.asStateFlow()

    // AI Video Creator Studio States
    private val _videoDurationMinutes = MutableStateFlow(1)
    val videoDurationMinutes: StateFlow<Int> = _videoDurationMinutes.asStateFlow()

    private val _isGeneratingVideo = MutableStateFlow(false)
    val isGeneratingVideo: StateFlow<Boolean> = _isGeneratingVideo.asStateFlow()

    private val _generatedVideoContent = MutableStateFlow<String?>(null)
    val generatedVideoContent: StateFlow<String?> = _generatedVideoContent.asStateFlow()

    private val _currentVideoProject = MutableStateFlow<VideoProjectEntity?>(null)
    val currentVideoProject: StateFlow<VideoProjectEntity?> = _currentVideoProject.asStateFlow()

    private val _transferredYouTubeTopic = MutableStateFlow<String?>(null)
    val transferredYouTubeTopic: StateFlow<String?> = _transferredYouTubeTopic.asStateFlow()

    init {
        viewModelScope.launch {
            // Setup wake word listener
            wakeWordManager.setOnWakeWordDetectedListener {
                startVoiceInput()
            }
            if (_uiState.value.isWakeWordEnabled) {
                wakeWordManager.startListening()
            }

            // Sync conversation ID if database populated
            repository.conversations.collect { list ->
                if (list.isNotEmpty() && _uiState.value.currentConversationId == 1L && list.none { it.id == 1L }) {
                    _currentConversationId.value = list.first().id
                    _uiState.value = _uiState.value.copy(currentConversationId = list.first().id)
                }
            }
        }

        // Monitor speech and TTS states for animated orb state
        viewModelScope.launch {
            speechManager.isListening.collect { listening ->
                updateAssistantState()
            }
        }
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                updateAssistantState()
            }
        }

        checkBackendHealth()
    }

    private fun updateAssistantState() {
        val listening = speechManager.isListening.value
        val speaking = ttsManager.isSpeaking.value
        val loading = _uiState.value.isLoading

        val newState = when {
            listening -> AssistantState.LISTENING
            speaking -> AssistantState.SPEAKING
            loading -> AssistantState.THINKING
            else -> AssistantState.IDLE
        }
        _uiState.value = _uiState.value.copy(assistantState = newState)
    }

    fun setTab(tab: AssistantTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun selectConversation(id: Long) {
        _currentConversationId.value = id
        _uiState.value = _uiState.value.copy(currentConversationId = id)
    }

    fun createNewConversation() {
        viewModelScope.launch {
            val newId = repository.createConversation("New Conversation")
            _currentConversationId.value = newId
            _uiState.value = _uiState.value.copy(currentConversationId = newId, currentTab = AssistantTab.CHAT)
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.clearAllConversations()
            createNewConversation()
        }
    }

    fun sendMessage(textOverride: String? = null, isVoice: Boolean = false) {
        val text = (textOverride ?: _uiState.value.inputText).trim()
        if (text.isBlank()) return

        val convId = _currentConversationId.value
        _uiState.value = _uiState.value.copy(inputText = "", isLoading = true, errorMessage = null)
        updateAssistantState()

        viewModelScope.launch {
            val result = repository.sendMessage(
                conversationId = convId,
                text = text,
                isVoice = isVoice,
                backendUrl = _uiState.value.backendUrl
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
            updateAssistantState()

            if (result.isSuccess) {
                val assistantMsg = result.getOrNull()
                if (assistantMsg != null && (_uiState.value.isVoiceEnabled || isVoice)) {
                    ttsManager.speak(assistantMsg.content, assistantMsg.language)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to get response"
                )
            }
        }
    }

    fun startVoiceInput() {
        wakeWordManager.resetTrigger()
        ttsManager.stop()
        speechManager.startListening(
            languageCode = _uiState.value.selectedLanguage,
            onResult = { text ->
                if (text.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(inputText = text)
                    sendMessage(textOverride = text, isVoice = true)
                }
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error)
                updateAssistantState()
            }
        )
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
        updateAssistantState()
    }

    fun toggleVoiceEnabled() {
        val newVoice = !_uiState.value.isVoiceEnabled
        _uiState.value = _uiState.value.copy(isVoiceEnabled = newVoice)
        if (!newVoice) {
            ttsManager.stop()
        }
    }

    fun toggleWakeWord(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isWakeWordEnabled = enabled)
        wakeWordManager.setEnabled(enabled)
    }

    fun setSelectedLanguage(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun updateBackendUrl(url: String) {
        ApiClient.updateBaseUrl(url)
        _uiState.value = _uiState.value.copy(backendUrl = url)
        checkBackendHealth()
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            val result = repository.checkBackendHealth(_uiState.value.backendUrl)
            _uiState.value = _uiState.value.copy(isBackendHealthy = result.getOrDefault(false))
        }
    }

    fun speakText(text: String, language: String = "auto") {
        ttsManager.speak(text, language)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    // AI Video Creator Studio Methods
    fun setVideoDuration(minutes: Int) {
        _videoDurationMinutes.value = minutes.coerceIn(1, 5)
    }

    fun generateAIVideo(
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
    ) {
        if (idea.isBlank()) return
        _isGeneratingVideo.value = true
        _generatedVideoContent.value = null

        val seconds = durationMinutes * 60
        val targetScenes = when (durationMinutes) {
            1 -> "6 to 8 scenes (approx 8–10 seconds per scene, total 60s)"
            2 -> "10 to 14 scenes (approx 8–12 seconds per scene, total 120s)"
            3 -> "15 to 20 scenes (approx 9–12 seconds per scene, total 180s)"
            4 -> "20 to 26 scenes (approx 9–12 seconds per scene, total 240s)"
            else -> "25 to 35 scenes (approx 8–12 seconds per scene, total 300s)"
        }

        val prompt = """
Act as Nova, World-Class AI Video Director and Producer.

Create a complete production-ready AI Video Package based on the following specifications:
- DURATION: $durationMinutes Minute(s) (~$seconds seconds total)
- SCENE REQUIREMENT: Exactly $targetScenes
- IDEA / TOPIC: $idea
- PRIMARY LANGUAGE: $language (If Urdu: use natural Urdu script; if Hindi: Devanagari script; if English: American English; if Roman Urdu: Roman Urdu).
- VIDEO STYLE: $style
- TARGET AUDIENCE: $audience
- CHARACTER / SUBJECT: $characterSubject
- VOICE PREFERENCE: $voicePref (Synchronized voice-over / dialogue)
- ASPECT RATIO: $aspectRatio
- VISUAL STYLE: $visualStyle
- MUSIC PREFERENCE: $musicPref

OUTPUT STRUCTURE (STRICT):

# 🎬 TITLE & CORE CONCEPT
Provide a high-concept title, core premise, hook, and narrative arc.

---

## 🔒 CHARACTER LOCK SECTION
If characters exist in the story, define the strict master visual reference:
- Exact face structure, skin tone, eye color
- Age and body proportions
- Exact clothing and color palette
- Hairstyle and accessories
- Strict consistency prompt snippet to include in every scene prompt.

---

## 🎞 VISUAL SCENE TIMELINE
List the exact visual timestamp timeline for all $targetScenes:
00:00–00:XX Scene 1: [Brief summary]
00:XX–00:XX Scene 2: [Brief summary]
... up to the end of the $durationMinutes minute ($seconds seconds) mark.

---

## 📝 SCENE-BY-SCENE PRODUCTION BREAKDOWN
For every scene from 1 to the end, provide:
### Scene X [Start - End] • Scene Title
- **Location & Environment:**
- **Action:**
- **Camera Movement & Angle:** (e.g. Low-angle tracking shot, slow dolly-in, drone aerial pan)
- **Lighting & Atmosphere:** (e.g. Volumetric golden hour, moody cyber neon, high-key studio)
- **Sound Effects (SFX):**
- **Music Timing & Mood:**
- **Dialogue / Voice-over ($voicePref):** (Exact spoken script synchronized to scene duration)
- **AI VIDEO GENERATION PROMPT (Ready-to-copy):**
> (Create an ultra-detailed, photorealistic AI video prompt compatible with Runway Gen-3, Luma Dream Machine, Sora, Pika, Kling, or Veo, including subject, action, camera movement, lighting, mood, aspect ratio $aspectRatio, and character consistency lock).

---

## ✂️ VIDEO EDITING & AUDIO MASTER PLAN
- Scene sequencing & transition types (Hard cut, cross-dissolve, match cut, whip pan)
- Background music cue points, swells, and ducking for voice-over
- Sound effects timing table
- Caption / Subtitle placement suggestions
- Final Call to Action (CTA)

---

## 📺 YOUTUBE & SOCIAL MEDIA KIT
- **YouTube Title:** High CTR, curiosity-inducing
- **Description:** Optimized with synopsis, timestamps, and links
- **SEO Keywords:** 15 high-volume search tags
- **Hashtags:** 5 viral tags
- **Thumbnail Prompt:** Detailed prompt to generate the high-CTR video thumbnail
- **Hook (0-5s):**
- **Call to Action (CTA):**

---
*Notice: Video prompts generated successfully. Connect a supported video-generation API (Runway/Luma/Veo) to render the final video.*
        """.trimIndent()

        viewModelScope.launch {
            val result = repository.executeStudioPrompt(prompt, _uiState.value.backendUrl)
            _isGeneratingVideo.value = false
            if (result.isSuccess) {
                val content = result.getOrNull().orEmpty()
                _generatedVideoContent.value = content

                // Extract title candidate
                val extractedTitle = content.lines()
                    .firstOrNull { it.startsWith("#") || it.contains("TITLE:", ignoreCase = true) }
                    ?.replace("#", "")?.replace("TITLE:", "")?.trim()
                    ?.ifBlank { "Video Project: $idea" } ?: "Video Project: $idea"

                val project = VideoProjectEntity(
                    title = extractedTitle.take(50),
                    durationMinutes = durationMinutes,
                    idea = idea,
                    style = style,
                    audience = audience,
                    language = language,
                    aspectRatio = aspectRatio,
                    characterLockDescription = characterSubject,
                    fullGeneratedContent = content
                )
                val newId = repository.saveVideoProject(project)
                _currentVideoProject.value = project.copy(id = newId)
            } else {
                _generatedVideoContent.value = "Error generating video plan: " +
                        (result.exceptionOrNull()?.localizedMessage ?: "Connection failure")
            }
        }
    }

    fun loadVideoProject(project: VideoProjectEntity) {
        _currentVideoProject.value = project
        _videoDurationMinutes.value = project.durationMinutes
        _generatedVideoContent.value = project.fullGeneratedContent
    }

    fun deleteVideoProject(id: Long) {
        viewModelScope.launch {
            repository.deleteVideoProject(id)
            if (_currentVideoProject.value?.id == id) {
                _currentVideoProject.value = null
                _generatedVideoContent.value = null
            }
        }
    }

    fun duplicateVideoProject(id: Long) {
        viewModelScope.launch {
            repository.duplicateVideoProject(id)
        }
    }

    fun sendVideoToYouTubeStudio(
        title: String,
        description: String,
        keywords: String,
        hashtags: String,
        script: String,
        thumbnailPrompt: String
    ) {
        val bundle = """
TOPIC: $title
DESCRIPTION: $description
TAGS: $keywords $hashtags
SCRIPT REFERENCE:
$script
THUMBNAIL CONCEPT:
$thumbnailPrompt
        """.trimIndent()

        _transferredYouTubeTopic.value = title
        _youtubeResult.value = bundle
        _uiState.value = _uiState.value.copy(currentTab = AssistantTab.YOUTUBE)
    }

    // YouTube Studio
    fun generateYouTubeContent(
        topic: String,
        type: String,
        audience: String,
        language: String
    ) {
        if (topic.isBlank()) return
        _isGeneratingYoutube.value = true
        _youtubeResult.value = null

        val prompt = """
Act as Nova, expert YouTube strategist and creative assistant.
TASK TYPE: $type
TOPIC: $topic
TARGET AUDIENCE: $audience
LANGUAGE: $language

Instructions:
1. Provide actionable, high-performing YouTube content.
2. For scripts: Hook, Intro, Key Body Points with visual cues, Call to Action, Outro.
3. For titles & tags: High CTR titles (under 60 characters) and 15 targeted SEO tags.
4. For thumbnails: Eye-catching concept, text overlay, colors, expressions.
5. Provide clean formatting ready to copy and paste.
        """.trimIndent()

        viewModelScope.launch {
            val result = repository.executeStudioPrompt(prompt, _uiState.value.backendUrl)
            _isGeneratingYoutube.value = false
            if (result.isSuccess) {
                _youtubeResult.value = result.getOrNull()
            } else {
                _youtubeResult.value = "Error: " + (result.exceptionOrNull()?.localizedMessage ?: "Failed to generate content")
            }
        }
    }

    // Coding Studio
    fun generateCode(
        language: String,
        mode: String,
        taskDescription: String
    ) {
        if (taskDescription.isBlank()) return
        _isGeneratingCode.value = true
        _codeResult.value = null

        val prompt = """
Act as Nova, expert software engineer and programming assistant.
LANGUAGE/FRAMEWORK: $language
MODE: $mode (Generation, Explanation, Debugging, or Improvement)
REQUEST: $taskDescription

Instructions:
1. Provide complete, working, production-grade code.
2. Clearly explain where the code should be placed.
3. Keep code clean, modern, and well-commented.
4. Include caveats and edge-case handling where applicable.
        """.trimIndent()

        viewModelScope.launch {
            val result = repository.executeStudioPrompt(prompt, _uiState.value.backendUrl)
            _isGeneratingCode.value = false
            if (result.isSuccess) {
                _codeResult.value = result.getOrNull()
            } else {
                _codeResult.value = "Error: " + (result.exceptionOrNull()?.localizedMessage ?: "Failed to generate code")
            }
        }
    }

    // Memories
    fun addMemory(key: String, value: String, category: String = "preference") {
        viewModelScope.launch {
            repository.addMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // Todos
    fun addTodo(title: String, notes: String = "", priority: String = "normal") {
        viewModelScope.launch {
            repository.addTodo(title, notes, priority)
        }
    }

    fun toggleTodo(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTodoCompleted(id, isCompleted)
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    fun clearCompletedTodos() {
        viewModelScope.launch {
            repository.clearCompletedTodos()
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
        wakeWordManager.stopListening()
    }
}
