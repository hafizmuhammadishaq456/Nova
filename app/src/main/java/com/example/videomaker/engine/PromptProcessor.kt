package com.example.videomaker.engine

import com.example.videomaker.model.CharacterConsistency
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VideoAspectRatio
import com.example.videomaker.model.VideoMakerMode
import com.example.videomaker.model.VideoStyle
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage

object PromptProcessor {

    fun calculateSceneCount(durationMinutes: Int): Int {
        return when (durationMinutes) {
            1 -> 6
            2 -> 12
            3 -> 18
            4 -> 24
            else -> 30
        }
    }

    fun buildSystemPrompt(
        mode: VideoMakerMode,
        durationMinutes: Int,
        aspectRatio: VideoAspectRatio,
        style: VideoStyle,
        language: VoiceLanguage,
        gender: VoiceGender,
        character: CharacterConsistency,
        hasImage: Boolean
    ): String {
        val totalSeconds = durationMinutes * 60
        val sceneCount = calculateSceneCount(durationMinutes)
        val consistencyLockSnippet = character.toPromptSnippet()

        return """
Act as an elite AI Video Director, Screenwriter, and Production Engine (similar to Google Flow / Runway / Sora studio systems).

TASK:
Generate a complete, high-end AI Video Production package with exactly $sceneCount scenes, covering a total duration of $durationMinutes Minute(s) ($totalSeconds seconds).

SPECIFICATIONS:
- GENERATION MODE: ${mode.label}
- TOTAL DURATION: $durationMinutes Minute(s) ($totalSeconds seconds total)
- SCENE COUNT: Exactly $sceneCount scenes (approx ${totalSeconds / sceneCount} seconds per scene)
- ASPECT RATIO: ${aspectRatio.ratioLabel}
- VISUAL STYLE: ${style.displayName} (${style.promptEnhancer})
- DIALOGUE/VOICE LANGUAGE: ${language.displayName} (${language.code})
- VOICE STYLE: ${gender.displayName} with natural lip-sync timing
${if (hasImage) "- SOURCE IMAGE PROVIDED: Animate source image into the primary keyframes and scene continuity." else ""}
${if (consistencyLockSnippet.isNotBlank()) "- MASTER CHARACTER CONSISTENCY: $consistencyLockSnippet" else "- CHARACTER CONSISTENCY: Maintain strict identity (face, hair, attire, proportions, colors) across all scenes."}

OUTPUT FORMAT (Follow this strict Markdown structure):

# 🎬 TITLE: [Curiosity-Driven Cinematic Title]
**Concept & Hook:** [2-3 sentence overview of the narrative arc and visual journey]

---

## 🔒 CHARACTER CONSISTENCY MASTER LOCK
- **Name:** [Character Name or 'Protagonist']
- **Age & Build:** [Age, body proportions, height]
- **Face & Eyes:** [Distinctive facial features, eye shape & color]
- **Hair:** [Exact hairstyle, color, texture]
- **Attire & Palette:** [Exact clothes, fabric textures, color scheme]
- **Consistency Lock Prompt:** [Snippet to prepend in every AI video generator]

---

## 🎞 SCENE-BY-SCENE PRODUCTION BREAKDOWN
${(1..sceneCount).joinToString("\n\n") { i ->
    val start = (i - 1) * (totalSeconds / sceneCount)
    val end = if (i == sceneCount) totalSeconds else i * (totalSeconds / sceneCount)
    val startStr = String.format("%02d:%02d", start / 60, start % 60)
    val endStr = String.format("%02d:%02d", end / 60, end % 60)
"""### Scene $i [$startStr - $endStr] • [Scene Title]
- **Description:** [What happens visually in this scene]
- **Camera:** [Camera movement: dolly, pan, orbit, crane, tracking shot, lens focal length]
- **Lighting:** [Lighting style, atmosphere, color grade]
- **Sound Effects (SFX):** [Detailed ambient and action audio effects]
- **Music Mood:** [Musical progression, tempo, emotion]
- **Speaker:** [Character Name or 'Narrator']
- **Dialogue (${language.displayName}):** "[Exact spoken sentence with natural conversational phrasing and lip-sync cues]"
- **AI Video Prompt:**
> [Ultra-detailed generative prompt including character, action, camera movement, style ${style.displayName}, lighting, and aspect ratio ${aspectRatio.ratioLabel}]"""
}}

---

## ✂️ AUDIO & EDITING PLAN
- **Transitions:** Match cuts, subtle speed ramps, crossfades
- **Music Ducking:** -12dB under dialogue
- **Subtitles:** Clean lower-third centered captions
        """.trimIndent()
    }

    /**
     * Parses the generated response into structured SceneData objects and CharacterConsistency
     */
    fun parseScenesFromResponse(
        rawContent: String,
        durationMinutes: Int,
        defaultStyle: VideoStyle,
        aspectRatio: VideoAspectRatio
    ): Pair<List<SceneData>, CharacterConsistency> {
        val sceneCount = calculateSceneCount(durationMinutes)
        val totalSeconds = durationMinutes * 60
        val secPerScene = totalSeconds / sceneCount
        val scenes = mutableListOf<SceneData>()

        // Extract Character Consistency Lock if present
        var charName = ""
        var charFace = ""
        var charHair = ""
        var charClothes = ""
        var charAge = ""
        var charPalette = ""

        val lines = rawContent.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("- **Name:**", ignoreCase = true) ->
                    charName = trimmed.substringAfter("Name:**").trim()
                trimmed.startsWith("- **Face & Eyes:**", ignoreCase = true) || trimmed.startsWith("- **Face:**", ignoreCase = true) ->
                    charFace = trimmed.substringAfter(":**").trim()
                trimmed.startsWith("- **Hair:**", ignoreCase = true) ->
                    charHair = trimmed.substringAfter("Hair:**").trim()
                trimmed.startsWith("- **Attire & Palette:**", ignoreCase = true) || trimmed.startsWith("- **Attire:**", ignoreCase = true) ->
                    charClothes = trimmed.substringAfter(":**").trim()
                trimmed.startsWith("- **Age & Build:**", ignoreCase = true) || trimmed.startsWith("- **Age:**", ignoreCase = true) ->
                    charAge = trimmed.substringAfter(":**").trim()
            }
        }

        val character = CharacterConsistency(
            name = charName.ifBlank { "Hero" },
            faceFeatures = charFace.ifBlank { "Sharp gaze, expressive eyes, defined facial features" },
            hairStyle = charHair.ifBlank { "Dark textured hair" },
            clothes = charClothes.ifBlank { "Modern sleek attire with signature accent colors" },
            age = charAge.ifBlank { "25-30" },
            colors = charPalette.ifBlank { "Teal and ember tones" }
        )

        // Parse individual scenes using Regex
        val sceneRegex = Regex("###\\s*Scene\\s*(\\d+)[^\\n]*", RegexOption.IGNORE_CASE)
        val sceneBlocks = rawContent.split(Regex("(?=###\\s*Scene\\s*\\d+)", RegexOption.IGNORE_CASE))

        for (block in sceneBlocks) {
            val match = sceneRegex.find(block) ?: continue
            val num = match.groupValues[1].toIntOrNull() ?: continue
            if (num > sceneCount) continue

            val start = (num - 1) * secPerScene
            val end = if (num == sceneCount) totalSeconds else num * secPerScene

            var title = "Scene $num"
            val titleLine = block.lines().firstOrNull { it.contains("Scene $num", ignoreCase = true) }
            if (titleLine != null && titleLine.contains("•")) {
                title = titleLine.substringAfter("•").trim()
            }

            fun extractField(prefix: String, default: String): String {
                return block.lines().firstOrNull { it.trim().startsWith(prefix, ignoreCase = true) }
                    ?.substringAfter(":**")?.replace("*", "")?.trim() ?: default
            }

            val desc = extractField("- **Description", "Visually captivating scene transition")
            val camera = extractField("- **Camera", "Cinematic 35mm slow dolly tracking")
            val lighting = extractField("- **Lighting", "Volumetric atmospheric light")
            val sfx = extractField("- **Sound Effects", "Subtle ambient room tone and motion sound")
            val music = extractField("- **Music Mood", "Swelling orchestral cinematic tone")
            val speaker = extractField("- **Speaker", character.name)

            // Extract Dialogue
            var dialogue = block.lines().firstOrNull { it.contains("Dialogue", ignoreCase = true) }
                ?.substringAfter(":**")?.replace("*", "")?.replace("\"", "")?.trim() ?: ""
            if (dialogue.isBlank()) {
                dialogue = "The journey continues beyond the horizon."
            }

            // Extract Prompt
            val promptLine = block.lines().firstOrNull { it.trim().startsWith(">") }
                ?.removePrefix(">")?.trim() ?: "${defaultStyle.promptEnhancer}, $desc, $camera, $lighting, aspect ratio ${aspectRatio.ratioLabel}"

            scenes.add(
                SceneData(
                    sceneNumber = num,
                    startSec = start,
                    endSec = end,
                    title = title,
                    description = desc,
                    dialogue = dialogue,
                    characterSpeaker = speaker,
                    cameraDirection = camera,
                    lightingInstructions = lighting,
                    soundEffects = sfx,
                    backgroundMusicMood = music,
                    aiVideoPrompt = promptLine
                )
            )
        }

        // If block parsing didn't find all scenes, fill the rest cleanly
        if (scenes.size < sceneCount) {
            for (i in (scenes.size + 1)..sceneCount) {
                val start = (i - 1) * secPerScene
                val end = if (i == sceneCount) totalSeconds else i * secPerScene
                scenes.add(
                    SceneData(
                        sceneNumber = i,
                        startSec = start,
                        endSec = end,
                        title = "Cinematic Phase $i",
                        description = "Key visual progression stage $i",
                        dialogue = if (i == sceneCount) "And this is only the beginning." else "Moving forward into the unfolding story.",
                        characterSpeaker = character.name,
                        cameraDirection = "Cinematic tracking shot",
                        lightingInstructions = "Dramatic volumetric lighting",
                        soundEffects = "Atmospheric swell, ambient movement",
                        backgroundMusicMood = "Cinematic crescendo",
                        aiVideoPrompt = "${defaultStyle.promptEnhancer}, key scene $i, ${character.toPromptSnippet()}, 8k resolution, ${aspectRatio.ratioLabel}"
                    )
                )
            }
        }

        return Pair(scenes.sortedBy { it.sceneNumber }, character)
    }
}
