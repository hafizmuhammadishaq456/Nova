package com.example.videomaker.model

import android.net.Uri

enum class VideoMakerMode(val label: String, val iconEmoji: String, val description: String) {
    TEXT_TO_VIDEO("Text to Video", "📝", "Generate a complete multi-scene video from a text prompt"),
    IMAGE_TO_VIDEO("Image to Video", "🖼", "Upload an image and animate it into a cinematic video"),
    STORY_TO_VIDEO("Story to Video", "📖", "Enter a full story and automatically divide it into scenes with consistent characters")
}

enum class VideoAspectRatio(val ratioLabel: String, val widthFactor: Float, val heightFactor: Float) {
    RATIO_9_16("9:16 Vertical", 9f, 16f),
    RATIO_16_9("16:9 Landscape", 16f, 9f),
    RATIO_1_1("1:1 Square", 1f, 1f)
}

enum class VideoStyle(val displayName: String, val promptEnhancer: String) {
    REALISTIC("Realistic", "hyper-realistic 8k raw footage, natural lighting, shot on 35mm lens, photorealistic textures"),
    CINEMATIC("Cinematic", "cinematic anamorphic lens, volumetric lighting, Arri Alexa color grade, dramatic atmosphere, 8k"),
    THREE_D("3D", "Unreal Engine 5 render, raytraced shadows, subsurface scattering, 3D CGI masterwork, octane render"),
    CARTOON("Cartoon", "vibrant 2D cartoon style, dynamic outlines, bold colors, whimsical animation, playful expressions"),
    ANIME("Anime", "Makoto Shinkai anime aesthetic, beautiful sky gradients, detailed hand-drawn anime keyframes, Studio Ghibli touch"),
    PIXAR("Pixar-style", "Pixar Disney 3D animation style, expressive eyes, warm soft lighting, highly detailed stylized textures"),
    FANTASY("Fantasy", "epic high fantasy, mystical ethereal glow, magical particles, grand scale, mythical wonder"),
    ACTION("Action", "high-octane action thriller, rapid camera motion, lens flares, kinetic energy, dramatic contrast"),
    COMEDY("Comedy", "bright vibrant comedy style, exaggerated comedic expressions, sunny warm color palette, energetic pacing")
}

enum class VoiceLanguage(val code: String, val displayName: String, val sampleGreeting: String) {
    ENGLISH("en", "English", "Welcome to this cinematic story."),
    URDU("ur", "Urdu", "اس دلکش کہانی میں خوش آمدید۔"),
    HINDI("hi", "Hindi", "इस रोमांचक कहानी में आपका स्वागत है।")
}

enum class VoiceGender(val displayName: String, val pitchFactor: Float, val rateFactor: Float) {
    MALE("Male Voice", 0.9f, 1.0f),
    FEMALE("Female Voice", 1.2f, 1.05f)
}

data class AudioMix(
    val voiceVolume: Float = 0.9f,
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.7f,
    val musicTrack: String = "Cinematic Ambient",
    val isLipSyncEnabled: Boolean = true
)

data class CharacterConsistency(
    val name: String = "",
    val faceFeatures: String = "",
    val hairStyle: String = "",
    val clothes: String = "",
    val bodyProportions: String = "",
    val age: String = "",
    val colors: String = "",
    val designSummary: String = ""
) {
    fun toPromptSnippet(): String {
        val parts = mutableListOf<String>()
        if (name.isNotBlank()) parts.add("Character $name")
        if (age.isNotBlank()) parts.add("age $age")
        if (faceFeatures.isNotBlank()) parts.add("face: $faceFeatures")
        if (hairStyle.isNotBlank()) parts.add("hair: $hairStyle")
        if (clothes.isNotBlank()) parts.add("attire: $clothes")
        if (bodyProportions.isNotBlank()) parts.add("body: $bodyProportions")
        if (colors.isNotBlank()) parts.add("palette: $colors")
        return if (parts.isNotEmpty()) "character consistency lock (${parts.joinToString(", ")})" else ""
    }
}

data class SceneData(
    val sceneNumber: Int,
    val startSec: Int,
    val endSec: Int,
    val title: String,
    val description: String,
    val dialogue: String,
    val characterSpeaker: String,
    val cameraDirection: String,
    val lightingInstructions: String,
    val soundEffects: String,
    val backgroundMusicMood: String,
    val aiVideoPrompt: String,
    val imageUri: String? = null
)

data class VideoProjectState(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "Untitled Video",
    val mode: VideoMakerMode = VideoMakerMode.TEXT_TO_VIDEO,
    val prompt: String = "",
    val storyText: String = "",
    val uploadedImageUri: Uri? = null,
    val durationMinutes: Int = 1,
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.RATIO_16_9,
    val style: VideoStyle = VideoStyle.REALISTIC,
    val voiceLanguage: VoiceLanguage = VoiceLanguage.ENGLISH,
    val voiceGender: VoiceGender = VoiceGender.MALE,
    val character: CharacterConsistency = CharacterConsistency(),
    val audioMix: AudioMix = AudioMix(),
    val scenes: List<SceneData> = emptyList(),
    val isGenerating: Boolean = false,
    val generationProgress: Float = 0f,
    val generationStepText: String = "",
    val isGenerated: Boolean = false,
    val fullMarkdownPlan: String = "",
    val exportResolution: String = "1080p",
    val isExporting: Boolean = false,
    val exportedFilePath: String? = null,
    val errorMessage: String? = null
)
