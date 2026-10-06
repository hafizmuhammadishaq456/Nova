package com.example.videomaker.engine

import com.example.repository.NovaRepository
import com.example.videomaker.model.CharacterConsistency
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VideoAspectRatio
import com.example.videomaker.model.VideoMakerMode
import com.example.videomaker.model.VideoStyle
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage

class StoryToVideoEngine(private val repository: NovaRepository) {

    suspend fun generateFromStory(
        storyText: String,
        durationMinutes: Int,
        aspectRatio: VideoAspectRatio,
        style: VideoStyle,
        language: VoiceLanguage,
        gender: VoiceGender,
        character: CharacterConsistency,
        backendUrl: String? = null,
        onProgress: (Float, String) -> Unit
    ): Pair<List<SceneData>, CharacterConsistency> {
        onProgress(0.10f, "Deconstructing narrative arc & character entities...")

        val sceneTarget = PromptProcessor.calculateSceneCount(durationMinutes)

        val fullPrompt = PromptProcessor.buildSystemPrompt(
            mode = VideoMakerMode.STORY_TO_VIDEO,
            durationMinutes = durationMinutes,
            aspectRatio = aspectRatio,
            style = style,
            language = language,
            gender = gender,
            character = character,
            hasImage = false
        ) + """
            
FULL USER STORY:
$storyText

INSTRUCTIONS FOR STORY-TO-VIDEO:
1. Divide this full story into exactly $sceneTarget logical, exciting visual scenes.
2. Maintain strong narrative tension: Beginning Hook -> Rising Action -> Climax -> Resolution.
3. Keep character identity completely consistent (same face, hair, outfit, age, proportions).
4. Generate natural, emotionally engaging spoken dialogue and voice narration in ${language.displayName}.
5. Design custom SFX and background music swells for each story beat.
        """.trimIndent()

        onProgress(0.40f, "Partitioning story into $sceneTarget rhythmic cinematic scenes...")
        val result = repository.executeStudioPrompt(fullPrompt, backendUrl)
        val content = result.getOrNull().orEmpty()

        onProgress(0.70f, "Aligning character consistency lock & camera blocking...")
        val parsed = PromptProcessor.parseScenesFromResponse(
            rawContent = content,
            durationMinutes = durationMinutes,
            defaultStyle = style,
            aspectRatio = aspectRatio
        )

        onProgress(0.95f, "Composing dialogue timing, SFX layers & background score...")
        return parsed
    }
}
