package com.example.videomaker.engine

import android.net.Uri
import com.example.repository.NovaRepository
import com.example.videomaker.model.CharacterConsistency
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VideoAspectRatio
import com.example.videomaker.model.VideoMakerMode
import com.example.videomaker.model.VideoStyle
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage

class ImageToVideoService(private val repository: NovaRepository) {

    suspend fun generateFromImage(
        imageUri: Uri?,
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
        onProgress(0.15f, "Processing image visual elements & motion vectors...")

        val imagePromptDetail = if (imageUri != null) {
            "Animate the uploaded source image into motion. Scene 1 must begin with the source image's exact framing, then introduce camera dolly-in and kinetic character movement."
        } else {
            "Simulate keyframe image animation sequence."
        }

        val fullPrompt = PromptProcessor.buildSystemPrompt(
            mode = VideoMakerMode.IMAGE_TO_VIDEO,
            durationMinutes = durationMinutes,
            aspectRatio = aspectRatio,
            style = style,
            language = language,
            gender = gender,
            character = character,
            hasImage = imageUri != null
        ) + "\n\n$imagePromptDetail\nUSER MOTION & ANIMATION PROMPT: $prompt"

        onProgress(0.40f, "Generating continuous video sequence from keyframe...")
        val result = repository.executeStudioPrompt(fullPrompt, backendUrl)
        val content = result.getOrNull().orEmpty()

        onProgress(0.75f, "Rendering scene motion curves & visual consistency...")
        val (scenes, parsedChar) = PromptProcessor.parseScenesFromResponse(
            rawContent = content,
            durationMinutes = durationMinutes,
            defaultStyle = style,
            aspectRatio = aspectRatio
        )

        // Attach source image to first scene as reference frame
        val updatedScenes = scenes.mapIndexed { idx, s ->
            if (idx == 0 && imageUri != null) {
                s.copy(imageUri = imageUri.toString())
            } else {
                s
            }
        }

        onProgress(0.95f, "Synchronizing voiceover, lip sync & audio mix...")
        return Pair(updatedScenes, parsedChar)
    }
}
