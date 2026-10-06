package com.example.videomaker.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.NovaDatabase
import com.example.model.VideoProjectEntity
import com.example.network.ApiClient
import com.example.network.NetworkMonitor
import com.example.repository.NovaRepository
import com.example.videomaker.engine.ImageToVideoService
import com.example.videomaker.engine.PromptProcessor
import com.example.videomaker.engine.StoryToVideoEngine
import com.example.videomaker.engine.TextToVideoService
import com.example.videomaker.export.VideoExportSystem
import com.example.videomaker.model.AudioMix
import com.example.videomaker.model.CharacterConsistency
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VideoAspectRatio
import com.example.videomaker.model.VideoMakerMode
import com.example.videomaker.model.VideoProjectState
import com.example.videomaker.model.VideoStyle
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage
import com.example.videomaker.player.VideoPreviewEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiVideoMakerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NovaDatabase.getDatabase(application, viewModelScope)
    private val networkMonitor = NetworkMonitor(application)
    val repository = NovaRepository(database, networkMonitor)

    val previewEngine = VideoPreviewEngine(application, viewModelScope)

    private val textToVideoService = TextToVideoService(repository)
    private val imageToVideoService = ImageToVideoService(repository)
    private val storyToVideoEngine = StoryToVideoEngine(repository)

    private val _projectState = MutableStateFlow(VideoProjectState())
    val projectState: StateFlow<VideoProjectState> = _projectState.asStateFlow()

    private val _backendUrl = MutableStateFlow(ApiClient.DEFAULT_BASE_URL)
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    fun setMode(mode: VideoMakerMode) {
        _projectState.value = _projectState.value.copy(mode = mode)
    }

    fun setPrompt(text: String) {
        _projectState.value = _projectState.value.copy(prompt = text)
    }

    fun setStoryText(text: String) {
        _projectState.value = _projectState.value.copy(storyText = text)
    }

    fun setDurationMinutes(minutes: Int) {
        _projectState.value = _projectState.value.copy(durationMinutes = minutes.coerceIn(1, 5))
    }

    fun setAspectRatio(ratio: VideoAspectRatio) {
        _projectState.value = _projectState.value.copy(aspectRatio = ratio)
    }

    fun setStyle(style: VideoStyle) {
        _projectState.value = _projectState.value.copy(style = style)
    }

    fun setVoiceLanguage(language: VoiceLanguage) {
        _projectState.value = _projectState.value.copy(voiceLanguage = language)
    }

    fun setVoiceGender(gender: VoiceGender) {
        _projectState.value = _projectState.value.copy(voiceGender = gender)
    }

    fun setUploadedImageUri(uri: Uri?) {
        _projectState.value = _projectState.value.copy(uploadedImageUri = uri)
    }

    fun updateCharacter(character: CharacterConsistency) {
        _projectState.value = _projectState.value.copy(character = character)
    }

    fun updateAudioMix(mix: AudioMix) {
        _projectState.value = _projectState.value.copy(audioMix = mix)
        previewEngine.updateMix(mix)
    }

    fun updateScene(index: Int, updatedScene: SceneData) {
        val currentScenes = _projectState.value.scenes.toMutableList()
        if (index in currentScenes.indices) {
            currentScenes[index] = updatedScene
            _projectState.value = _projectState.value.copy(scenes = currentScenes)
            previewEngine.loadProject(
                sceneList = currentScenes,
                durationMinutes = _projectState.value.durationMinutes,
                language = _projectState.value.voiceLanguage,
                gender = _projectState.value.voiceGender,
                mix = _projectState.value.audioMix
            )
        }
    }

    fun generateVideo() {
        val state = _projectState.value
        val input = when (state.mode) {
            VideoMakerMode.STORY_TO_VIDEO -> state.storyText.ifBlank { state.prompt }
            else -> state.prompt
        }
        if (input.isBlank() && state.uploadedImageUri == null) return

        _projectState.value = _projectState.value.copy(
            isGenerating = true,
            generationProgress = 0.05f,
            generationStepText = "Preparing video pipeline...",
            errorMessage = null,
            isGenerated = false
        )

        viewModelScope.launch {
            try {
                val (scenes, parsedChar) = when (state.mode) {
                    VideoMakerMode.TEXT_TO_VIDEO -> {
                        textToVideoService.generateVideo(
                            prompt = input,
                            durationMinutes = state.durationMinutes,
                            aspectRatio = state.aspectRatio,
                            style = state.style,
                            language = state.voiceLanguage,
                            gender = state.voiceGender,
                            character = state.character,
                            backendUrl = _backendUrl.value,
                            onProgress = { p, msg ->
                                _projectState.value = _projectState.value.copy(
                                    generationProgress = p,
                                    generationStepText = msg
                                )
                            }
                        )
                    }
                    VideoMakerMode.IMAGE_TO_VIDEO -> {
                        imageToVideoService.generateFromImage(
                            imageUri = state.uploadedImageUri,
                            prompt = input.ifBlank { "Animate this image with cinematic camera motion" },
                            durationMinutes = state.durationMinutes,
                            aspectRatio = state.aspectRatio,
                            style = state.style,
                            language = state.voiceLanguage,
                            gender = state.voiceGender,
                            character = state.character,
                            backendUrl = _backendUrl.value,
                            onProgress = { p, msg ->
                                _projectState.value = _projectState.value.copy(
                                    generationProgress = p,
                                    generationStepText = msg
                                )
                            }
                        )
                    }
                    VideoMakerMode.STORY_TO_VIDEO -> {
                        storyToVideoEngine.generateFromStory(
                            storyText = input,
                            durationMinutes = state.durationMinutes,
                            aspectRatio = state.aspectRatio,
                            style = state.style,
                            language = state.voiceLanguage,
                            gender = state.voiceGender,
                            character = state.character,
                            backendUrl = _backendUrl.value,
                            onProgress = { p, msg ->
                                _projectState.value = _projectState.value.copy(
                                    generationProgress = p,
                                    generationStepText = msg
                                )
                            }
                        )
                    }
                }

                val titleCandidate = input.take(35).trim().ifBlank { "Cinematic Project" }
                _projectState.value = _projectState.value.copy(
                    title = titleCandidate,
                    scenes = scenes,
                    character = parsedChar,
                    isGenerating = false,
                    generationProgress = 1.0f,
                    generationStepText = "Video Ready for Preview!",
                    isGenerated = true
                )

                // Load scenes directly into interactive Preview Engine
                previewEngine.loadProject(
                    sceneList = scenes,
                    durationMinutes = state.durationMinutes,
                    language = state.voiceLanguage,
                    gender = state.voiceGender,
                    mix = state.audioMix
                )

                // Save to Room database
                val entity = VideoProjectEntity(
                    title = titleCandidate,
                    durationMinutes = state.durationMinutes,
                    idea = input,
                    style = state.style.displayName,
                    audience = "Global",
                    language = state.voiceLanguage.displayName,
                    aspectRatio = state.aspectRatio.ratioLabel,
                    characterLockDescription = parsedChar.toPromptSnippet(),
                    fullGeneratedContent = "Generated ${scenes.size} scenes for $titleCandidate"
                )
                repository.saveVideoProject(entity)

            } catch (e: Exception) {
                _projectState.value = _projectState.value.copy(
                    isGenerating = false,
                    errorMessage = e.localizedMessage ?: "Failed to generate video project"
                )
            }
        }
    }

    fun exportVideo(resolution: String, onFinished: (Uri) -> Unit) {
        val state = _projectState.value
        if (state.scenes.isEmpty()) return

        _projectState.value = _projectState.value.copy(
            isExporting = true,
            exportResolution = resolution
        )

        viewModelScope.launch {
            val result = VideoExportSystem.exportVideoProject(
                context = getApplication(),
                project = state,
                resolution = resolution,
                onProgress = { _, _ -> }
            )
            _projectState.value = _projectState.value.copy(isExporting = false)
            result.onSuccess { uri ->
                _projectState.value = _projectState.value.copy(exportedFilePath = uri.toString())
                onFinished(uri)
            }.onFailure { err ->
                _projectState.value = _projectState.value.copy(errorMessage = "Export failed: ${err.message}")
            }
        }
    }

    fun resetProject() {
        previewEngine.stop()
        _projectState.value = VideoProjectState()
    }

    override fun onCleared() {
        super.onCleared()
        previewEngine.release()
    }
}
