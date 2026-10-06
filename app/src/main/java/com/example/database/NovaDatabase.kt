package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.ConversationEntity
import com.example.model.MessageEntity
import com.example.model.TodoEntity
import com.example.model.UserMemoryEntity
import com.example.model.VideoProjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        UserMemoryEntity::class,
        TodoEntity::class,
        VideoProjectEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun userMemoryDao(): UserMemoryDao
    abstract fun todoDao(): TodoDao
    abstract fun videoProjectDao(): VideoProjectDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_assistant_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(NovaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class NovaDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: NovaDatabase) {
                val memoryDao = database.userMemoryDao()
                memoryDao.insertMemory(
                    UserMemoryEntity(
                        key = "Assistant Identity",
                        value = "Name is Nova. Calm, respectful, intelligent, fast, and multi-lingual (Urdu, Hindi, English).",
                        category = "core"
                    )
                )
                memoryDao.insertMemory(
                    UserMemoryEntity(
                        key = "Wake Word",
                        value = "Responds to 'Hey Nova'",
                        category = "core"
                    )
                )

                val todoDao = database.todoDao()
                todoDao.insertTodo(
                    TodoEntity(
                        title = "Welcome to Nova AI",
                        notes = "Try speaking 'Hey Nova' or use voice input in English, Urdu or Hindi.",
                        priority = "high"
                    )
                )

                val convDao = database.conversationDao()
                val convId = convDao.insertConversation(
                    ConversationEntity(title = "Welcome to Nova")
                )
                database.messageDao().insertMessage(
                    MessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = "Hello! I am **Nova**, your personal AI assistant. I can converse in English, Urdu (اردو), Hindi (हिन्दी), or Roman Urdu. Say **'Hey Nova'** or tap the microphone to speak!"
                    )
                )

                // Sample starter video project in AI Video Creator Studio
                val videoProjectDao = database.videoProjectDao()
                videoProjectDao.insertProject(
                    VideoProjectEntity(
                        title = "The Secret of the Deep Ocean",
                        durationMinutes = 1,
                        idea = "An autonomous exploration drone discovers a glowing bioluminescent city in the Mariana Trench.",
                        style = "Cinematic",
                        audience = "Global",
                        language = "English",
                        aspectRatio = "16:9 Landscape",
                        characterLockDescription = "Drone Unit 'Aero-7': Sleek titanium sphere with an azure optical eye sensor and thruster fins.",
                        fullGeneratedContent = """
# TITLE: The Secret of the Deep Ocean
**Duration:** 1 Minute (~60s) • **Aspect Ratio:** 16:9 Landscape • **Style:** Cinematic

---

## 🎬 CONCEPT & HOOK
An autonomous research drone descends beyond the photic zone into the Mariana Trench, capturing footage of a sprawling, pulsating bioluminescent crystalline ecosystem never seen by humankind.

---

## 🔒 CHARACTER LOCK
- **Subject:** Exploration Sub-Drone 'Aero-7'
- **Appearance:** Spherical brushed dark titanium body, 40cm diameter, central glowing azure optical lens with aperture rings, 4 micro-hydro thrusters emitting subtle blue cavitation rings.
- **Consistency Rule:** Maintain exact metallic reflection, azure eye glow, and hull scratches across all scenes.

---

## 🎞 SCENE TIMELINE (1 MINUTE • 6 SCENES)

### Scene 1 [00:00 - 00:10] • The Abyss Descending
- **Location:** Deep ocean twilight zone (3,000 meters down). Pitch black water with floating marine snow particles.
- **Action:** Drone Aero-7 tilts downward, twin headlights cutting through murky blue-black water.
- **Camera:** Slow tilt-down, tracking behind the drone.
- **Lighting:** Dual xenon searchlights piercing deep volumetric dark water.
- **Audio/SFX:** Deep sub-bass hydrophone hum, muffled mechanical thruster whine.
- **Voice-over:** "Three thousand meters below the surface, sunlight ceases to exist."
- **AI Video Prompt:**
> Cinematic 8k macro underwater shot of spherical dark titanium drone with glowing azure lens descending into abyss, volumetric light beams slicing through marine snow particles, highly detailed, Unreal Engine 5 render style, photorealistic, 16:9.

---

### Scene 2 [00:10 - 00:20] • The Trench Wall
- **Location:** Sheer basalt tectonic cliff wall at 6,000m.
- **Action:** Drone sweeps light across an ancient basalt wall covered in strange crystalline veins that shimmer.
- **Camera:** Lateral tracking shot along basalt rock face.
- **Lighting:** Harsh directional beam casting sharp shadows across sharp jagged rock ridges.
- **Audio/SFX:** Distant tectonic rumble, sonar ping echoing.
- **Voice-over:** "At these crushing depths, human instruments should encounter only barren rock."
- **AI Video Prompt:**
> Cinematic tracking shot along ancient underwater basalt sea cliff, titanium drone Aero-7 with azure glowing eye scanning jagged rock face with spotlight, glistening crystalline mineral veins, photorealistic 8k, moody atmosphere.

---

### Scene 3 [00:20 - 00:30] • The First Glow
- **Location:** Trench floor threshold.
- **Action:** In the distance, an ethereal neon-cyan bloom begins to emerge beneath a dark rocky arch.
- **Camera:** Slow push-in over drone's shoulder.
- **Lighting:** Transition from searchlight cone to soft ambient cyan bioluminescence.
- **Audio/SFX:** Low frequency crystalline chime, swelling synth pad.
- **Voice-over:** "Until today."
- **AI Video Prompt:**
> First-person drone view passing through deep ocean rock arch, discovering glowing cyan and violet bioluminescent flora on seabed, breathtaking discovery, cinematic lighting, ultra-realistic, 16:9.

---

### Scene 4 [00:30 - 00:40] • The Spire Garden
- **Location:** Sprawling underwater geothermal plain.
- **Action:** Drone rises to reveal towering spiraling geothermal towers pulsing with rhythmic bio-electric light waves.
- **Camera:** Wide crane/pedestal ascent showing scale.
- **Lighting:** Bioluminescent turquoise and magenta volumetric glow lighting the seabed.
- **Audio/SFX:** Harmonic acoustic resonance, bubble venting.
- **Voice-over:** "A sanctuary thriving without the sun, sustained by the earth's core."
- **AI Video Prompt:**
> Wide cinematic master shot of underwater bioluminescent spire towers on ocean floor, glowing turquoise and purple light pulses, small titanium drone hovering in foreground, grand scale, Avatar style underwater world, 8k.

---

### Scene 5 [00:40 - 00:50] • The Entity Awakes
- **Location:** Core of the largest central spire.
- **Action:** A massive translucent manta-like organism made of living light glides peacefully overhead.
- **Camera:** Low angle looking up at the creature's bioluminescent wingspan.
- **Lighting:** Internal creature luminescence illuminating the drone's hull reflections.
- **Audio/SFX:** Deep majestic whale-like resonance.
- **Voice-over:** "Life didn't just survive here... it evolved beyond our comprehension."
- **AI Video Prompt:**
> Low-angle cinematic shot of a majestic giant translucent ethereal manta creature glowing with internal azure ribbons gliding above the seabed, titanium drone Aero-7 observing below, awe-inspiring, hyper-realistic 8k.

---

### Scene 6 [00:50 - 01:00] • Outro & Transmission
- **Location:** Overview of the trench disappearing into misty glow.
- **Action:** Drone transmits telemetry data, optical lens pulsing as camera pulls back into darkness.
- **Camera:** Dramatic pull-back into black abyss with spires in distance.
- **Lighting:** Fading glow, red HUD telemetry graphics blinking.
- **Audio/SFX:** Data transmission burst, concluding orchestral swell.
- **Voice-over:** "What else waits in the depths? Subscribe to explore the uncharted frontier."
- **AI Video Prompt:**
> Cinematic slow pull-back from underwater glowing sanctuary into deep darkness, titanium drone transmitting cyan data beam upward, dramatic sci-fi ending, photorealistic, 16:9.

---

## 📺 YOUTUBE & SOCIAL METADATA
- **Title:** We Found What's Hiding at the Bottom of the Mariana Trench...
- **Description:** Join the autonomous exploration drone Aero-7 as it plunges 6,000 meters into the abyss and makes an unbelievable discovery. Subscribe for more deep sea AI cinematic documentaries!
- **Keywords:** Mariana trench discovery, deep ocean mystery, underwater documentary, deep sea creatures, AI cinematic video, ocean abyss
- **Hashtags:** #OceanMysteries #DeepSea #SciFi #CinematicAI #Documentary
- **Thumbnail Prompt:** High-contrast dramatic shot of glowing azure drone Aero-7 facing a gigantic glowing bioluminescent underwater leviathan in the dark abyss, hyper-detailed, bold lighting.
                        """.trimIndent()
                    )
                )
            }
        }
    }
}
