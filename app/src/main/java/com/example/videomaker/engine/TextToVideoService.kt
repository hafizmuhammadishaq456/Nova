package com.example.videomaker.engine

import com.example.repository.NovaRepository
import com.example.videomaker.model.CharacterConsistency
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VideoAspectRatio
import com.example.videomaker.model.VideoMakerMode
import com.example.videomaker.model.VideoStyle
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage

class TextToVideoService(private val repository: NovaRepository) {

    suspend fun generateVideo(
        prompt: String,
        durationMinutes: Int,
        aspectRatio: VideoAspectRatio,
        style: VideoStyle,
        language: VoiceLanguage,
        gender: VoiceGender,
        character: CharacterConsistency,
        backendUrl: String? = null,
        onProgress: (Float, String) -> Unit
    ): Pair<List<SceneData>, CharacterConsistency> {
        onProgress(0.15f, "Analyzing prompt & generating cinematic storyboard...")

        val fullPrompt = PromptProcessor.buildSystemPrompt(
            mode = VideoMakerMode.TEXT_TO_VIDEO,
            durationMinutes = durationMinutes,
            aspectRatio = aspectRatio,
            style = style,
            language = language,
            gender = gender,
            character = character,
            hasImage = false
        ) + "\n\nUSER PROMPT: $prompt"

        onProgress(0.35f, "Composing $durationMinutes-minute multi-scene breakdown...")
        val result = repository.executeStudioPrompt(fullPrompt, backendUrl)
        val content = result.getOrNull().orEmpty()

        onProgress(0.70f, "Locking character consistency & visual prompts...")
        val parsed = PromptProcessor.parseScenesFromResponse(
            rawContent = content,
            durationMinutes = durationMinutes,
            defaultStyle = style,
            aspectRatio = aspectRatio
        )

        onProgress(0.95f, "Finalizing audio synchronization & scene cues...")
        return parsed
    }
}
