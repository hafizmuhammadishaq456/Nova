package com.example.utils

object LocalGenerator {

    fun generateVideoPlan(prompt: String): String {
        // Extract parameters from prompt if available
        val durationMatch = Regex("DURATION:\\s*(\\d+)").find(prompt)
        val minutes = durationMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val seconds = minutes * 60

        val ideaMatch = Regex("IDEA / TOPIC:\\s*([^\\n]+)").find(prompt)
        val idea = ideaMatch?.groupValues?.get(1)?.trim()?.ifBlank { "Futuristic Cyber Odyssey" } ?: "Futuristic Cyber Odyssey"

        val langMatch = Regex("PRIMARY LANGUAGE:\\s*([^\\n]+)").find(prompt)
        val language = langMatch?.groupValues?.get(1)?.trim() ?: "English"

        val styleMatch = Regex("VIDEO STYLE:\\s*([^\\n]+)").find(prompt)
        val style = styleMatch?.groupValues?.get(1)?.trim() ?: "Cinematic"

        val audienceMatch = Regex("TARGET AUDIENCE:\\s*([^\\n]+)").find(prompt)
        val audience = audienceMatch?.groupValues?.get(1)?.trim() ?: "Global"

        val charMatch = Regex("CHARACTER / SUBJECT:\\s*([^\\n]+)").find(prompt)
        val character = charMatch?.groupValues?.get(1)?.trim()?.ifBlank { "Alex: 28yo tech explorer with matte-grey cyber jacket" } ?: "Alex: 28yo tech explorer with matte-grey cyber jacket"

        val arMatch = Regex("ASPECT RATIO:\\s*([^\\n]+)").find(prompt)
        val aspectRatio = arMatch?.groupValues?.get(1)?.trim() ?: "16:9 Landscape"

        val visualStyleMatch = Regex("VISUAL STYLE:\\s*([^\\n]+)").find(prompt)
        val visualStyle = visualStyleMatch?.groupValues?.get(1)?.trim() ?: "Hyper-realistic 8k Photorealism"

        val musicMatch = Regex("MUSIC PREFERENCE:\\s*([^\\n]+)").find(prompt)
        val music = musicMatch?.groupValues?.get(1)?.trim() ?: "Cinematic Orchestral"

        val sceneCount = when (minutes) {
            1 -> 6
            2 -> 11
            3 -> 16
            4 -> 22
            else -> 28
        }

        val sceneDuration = seconds / sceneCount

        val sb = StringBuilder()
        sb.append("# 🎬 Title: ${idea.take(40)} • $style Chronicles\n\n")
        sb.append("**Concept & Narrative Arc:** A captivating $minutes-minute journey exploring '$idea'. Tailored for $audience audiences in $language, blending high-stakes emotion with breathtaking $style visuals.\n\n")
        sb.append("---\n\n")

        sb.append("## 🔒 CHARACTER LOCK SECTION\n")
        sb.append("- **Master Character:** $character\n")
        sb.append("- **Facial & Physical Attributes:** High cheekbones, focused gaze, athletic posture, distinctive cybernetic or organic styling.\n")
        sb.append("- **Costume Consistency:** Matte-textured weather-resistant tactical gear with glowing neon lining and subtle emblem.\n")
        sb.append("- **AI Video Tool Lock Snippet:** `$character, consistent facial structure, identical clothing, $visualStyle, cinematic color grade, photorealistic, 8k resolution, aspect ratio $aspectRatio`\n\n")
        sb.append("---\n\n")

        sb.append("## 🎞 VISUAL SCENE TIMELINE (${minutes}m / ${seconds}s • $sceneCount Scenes)\n")
        for (i in 1..sceneCount) {
            val startSec = (i - 1) * sceneDuration
            val endSec = if (i == sceneCount) seconds else i * sceneDuration
            val startFormatted = String.format("%02d:%02d", startSec / 60, startSec % 60)
            val endFormatted = String.format("%02d:%02d", endSec / 60, endSec % 60)
            sb.append("- **$startFormatted–$endFormatted** Scene $i: Narrative progression stage $i of '$idea'\n")
        }
        sb.append("\n---\n\n")

        sb.append("## 📝 SCENE-BY-SCENE PRODUCTION BREAKDOWN\n\n")
        for (i in 1..sceneCount) {
            val startSec = (i - 1) * sceneDuration
            val endSec = if (i == sceneCount) seconds else i * sceneDuration
            val startFormatted = String.format("%02d:%02d", startSec / 60, startSec % 60)
            val endFormatted = String.format("%02d:%02d", endSec / 60, endSec % 60)

            val stageName = when (i) {
                1 -> "The Inciting Hook"
                2 -> "The World Revealed"
                3 -> "Rising Tension"
                sceneCount / 2 -> "The Central Turning Point"
                sceneCount - 1 -> "The Climactic Peak"
                sceneCount -> "The Resolute Climax & Resolution"
                else -> "Escalation Phase $i"
            }

            sb.append("### Scene $i [$startFormatted–$endFormatted] • $stageName\n")
            sb.append("- **Location & Environment:** Vast atmospheric vista reflecting '$idea', volumetric haze, particle dust, detailed ambient textures.\n")
            sb.append("- **Action:** $character navigates the evolving scene, interacting with key focal elements as tension heightens.\n")
            sb.append("- **Camera Movement & Angle:** ${if (i % 2 == 0) "Slow cinematic low-angle tracking shot, 35mm anamorphic" else "Dynamic 360 orbit dolly-in, shallow depth of field, f/1.8"}.\n")
            sb.append("- **Lighting & Atmosphere:** Volumetric atmospheric illumination, soft teal and amber rim lights, reflections bouncing on polished surfaces.\n")
            sb.append("- **Sound Effects (SFX):** Ambient environmental rumble, mechanical footsteps, subtle energy pulses, wind whistling.\n")
            sb.append("- **Background Music:** $music — swelling in intensity at transition milestones.\n")

            val dialog = when (language) {
                "Urdu" -> "ہماری منزل قریب ہے، اب پیچھے مڑنے کا کوئی راستہ نہیں۔"
                "Hindi" -> "हम अपनी मंज़िल के करीब हैं, अब पीछे मुड़ने का कोई रास्ता नहीं।"
                "Roman Urdu" -> "Manzil bohot qareeb hai, ab peechay hatne ka waqt nahi."
                else -> "The threshold has been crossed. Every choice from this point forward defines our reality."
            }
            sb.append("- **Dialogue / Voice-over:** \"$dialog\"\n")
            sb.append("- **AI VIDEO GENERATION PROMPT (Ready-to-copy):**\n")
            sb.append("> Ultra-detailed cinematic shot of $character in a high-atmosphere environment representing $idea, $visualStyle, ${if (i % 2 == 0) "slow low-angle tracking" else "sweeping cinematic crane move"}, master lighting, ray-traced reflections, hyper-realistic, 8k resolution, $aspectRatio.\n\n")
        }

        sb.append("---\n\n")
        sb.append("## ✂️ VIDEO EDITING & AUDIO MASTER PLAN\n")
        sb.append("- **Transitions:** Rhythmic match-cuts on character motions, rapid whip-pans during high-octane sequences, seamless cross-dissolve to finale.\n")
        sb.append("- **Audio Ducking:** Background $music ducked -10dB during spoken voice-overs to maximize vocal intelligibility.\n")
        sb.append("- **Subtitle Style:** Clean sans-serif captions centered lower-third with soft drop shadow.\n")
        sb.append("- **Color Grading:** Custom cinematic teal-orange LUT, balanced highlights, rich shadow contrast.\n\n")

        sb.append("---\n\n")
        sb.append("## 📺 YOUTUBE & SOCIAL MEDIA KIT\n")
        sb.append("- **YouTube Title:** ${idea.take(35)}: The Untold Story ($minutes Min Film)\n")
        sb.append("- **Description:** An unforgettable cinematic experience exploring '$idea'. Generated with Nova AI Video Creator Studio.\n")
        sb.append("Timestamps:\n00:00 - Introduction & Hook\n${String.format("%02d:%02d", seconds / 2 / 60, seconds / 2 % 60)} - The Turning Point\n${String.format("%02d:%02d", (seconds - 15) / 60, (seconds - 15) % 60)} - Final Climax\n\n")
        sb.append("- **SEO Keywords:** $idea, $style, ai video creator, cinematic short film, sora ai, runway gen 3, luma dream machine, 4k ultra hd, storytelling\n")
        sb.append("- **Hashtags:** #${style.replace(" ", "")} #AIVideo #ShortFilm #NovaStudio #ViralContent\n")
        sb.append("- **Thumbnail Prompt:** High-contrast dramatic close-up of $character, glowing atmospheric rim lighting, intense facial emotion, cinematic 8k resolution, wide aspect ratio.\n")
        sb.append("- **Hook (0-5s):** \"What if everything you thought you knew was only the beginning?\"\n")
        sb.append("- **Call to Action (CTA):** \"Subscribe and comment your thoughts on the journey below!\"\n\n")
        sb.append("---\n")
        sb.append("*Notice: Video prompts generated successfully. Connect a supported video-generation API (Runway Gen-3 / Luma Dream Machine / Sora / Kling / Veo) to render final video clips.*")

        return sb.toString()
    }

    fun generateStudioFallback(prompt: String): String {
        if (prompt.contains("AI Video") || prompt.contains("SCENE-BY-SCENE") || prompt.contains("DURATION:")) {
            return generateVideoPlan(prompt)
        }
        if (prompt.contains("YouTube") || prompt.contains("TARGET AUDIENCE")) {
            return """
### 🚀 Nova YouTube Optimization Kit

**High-CTR Title Proposals:**
1. The Most Important Shift Happening Right Now (Don't Miss It)
2. Why Most Creators Are Wrong About This (Case Study)
3. Step-by-Step Blueprint That Changed Everything

**Description & Chapters:**
In this comprehensive video, we dive deep into actionable strategies designed to maximize your impact.

📌 Timestamps:
00:00 - The Core Dilemma
02:15 - Breakdown of the Strategy
05:40 - Real-World Application
08:20 - Summary & Key Takeaways

**Viral SEO Keywords:**
youtube strategy, growth tips, viral content, audience retention, high ctr, digital creation, nova assistant, best practices 2026
            """.trimIndent()
        }
        if (prompt.contains("programming assistant") || prompt.contains("LANGUAGE/FRAMEWORK:")) {
            return """
```kotlin
// Production Kotlin implementation by Nova AI
class FeatureManager {
    fun executeProcess() {
        println("Nova Feature executed with clean architecture standards.")
    }
}
```
*Architecture Note: Integrate this within your repository or domain use-case layer.*
            """.trimIndent()
        }
        return "Nova AI: Ready to assist with your creative, technical, or voice workflows."
    }
}
