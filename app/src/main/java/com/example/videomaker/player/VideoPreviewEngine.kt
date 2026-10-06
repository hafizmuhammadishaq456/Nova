package com.example.videomaker.player

import android.content.Context
import com.example.videomaker.audio.AudioProcessingService
import com.example.videomaker.audio.VoiceGenerationService
import com.example.videomaker.model.AudioMix
import com.example.videomaker.model.SceneData
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoPreviewEngine(
    context: Context,
    private val scope: CoroutineScope
) {
    val voiceService = VoiceGenerationService(context)
    val audioService = AudioProcessingService()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeSec = MutableStateFlow(0f)
    val currentTimeSec: StateFlow<Float> = _currentTimeSec.asStateFlow()

    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _totalDurationSec = MutableStateFlow(60)
    val totalDurationSec: StateFlow<Int> = _totalDurationSec.asStateFlow()

    private val _currentScene = MutableStateFlow<SceneData?>(null)
    val currentScene: StateFlow<SceneData?> = _currentScene.asStateFlow()

    private var playbackJob: Job? = null
    private var scenes: List<SceneData> = emptyList()
    private var voiceLang: VoiceLanguage = VoiceLanguage.ENGLISH
    private var voiceGender: VoiceGender = VoiceGender.MALE
    private var audioMix = AudioMix()

    private var lastSpokenSceneIndex = -1

    fun loadProject(
        sceneList: List<SceneData>,
        durationMinutes: Int,
        language: VoiceLanguage,
        gender: VoiceGender,
        mix: AudioMix
    ) {
        stop()
        scenes = sceneList.sortedBy { it.sceneNumber }
        _totalDurationSec.value = durationMinutes * 60
        _currentTimeSec.value = 0f
        _currentSceneIndex.value = 0
        _currentScene.value = scenes.firstOrNull()
        voiceLang = language
        voiceGender = gender
        audioMix = mix
        audioService.updateMix(mix)
        lastSpokenSceneIndex = -1
    }

    fun updateMix(mix: AudioMix) {
        audioMix = mix
        audioService.updateMix(mix)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (scenes.isEmpty()) return
        _isPlaying.value = true
        audioService.startBackgroundMusic(scope, _currentScene.value?.backgroundMusicMood ?: "Cinematic")

        playbackJob?.cancel()
        playbackJob = scope.launch {
            val tickMs = 50L
            while (isActive && _isPlaying.value) {
                delay(tickMs)
                val newTime = _currentTimeSec.value + (tickMs / 1000f)
                if (newTime >= _totalDurationSec.value) {
                    _currentTimeSec.value = 0f
                    _currentSceneIndex.value = 0
                    _currentScene.value = scenes.firstOrNull()
                    lastSpokenSceneIndex = -1
                    pause()
                    break
                } else {
                    _currentTimeSec.value = newTime
                    updateActiveScene(newTime)
                }
            }
        }
    }

    private fun updateActiveScene(timeSec: Float) {
        val idx = scenes.indexOfFirst { timeSec >= it.startSec && timeSec < it.endSec }
        val activeIdx = if (idx != -1) idx else (_currentSceneIndex.value.coerceIn(0, scenes.lastIndex))

        if (activeIdx != _currentSceneIndex.value) {
            _currentSceneIndex.value = activeIdx
            _currentScene.value = scenes.getOrNull(activeIdx)
            // Trigger transition sound chime
            audioService.playSfxChime(scope)
        }

        // Trigger spoken dialogue once when scene starts
        val scene = scenes.getOrNull(activeIdx)
        if (scene != null && activeIdx != lastSpokenSceneIndex) {
            lastSpokenSceneIndex = activeIdx
            if (audioMix.voiceVolume > 0.05f && scene.dialogue.isNotBlank()) {
                voiceService.speakDialogue(
                    text = scene.dialogue,
                    language = voiceLang,
                    gender = voiceGender,
                    volume = audioMix.voiceVolume
                )
            }
        }

        // Update real-time lip sync
        if (scene != null) {
            val sceneDuration = (scene.endSec - scene.startSec).coerceAtLeast(1)
            val progressInScene = (timeSec - scene.startSec) / sceneDuration
            voiceService.updateLipSyncFrame(progressInScene, _isPlaying.value && voiceService.isSpeaking.value)
        }
    }

    fun seekTo(seconds: Float) {
        _currentTimeSec.value = seconds.coerceIn(0f, _totalDurationSec.value.toFloat())
        updateActiveScene(_currentTimeSec.value)
    }

    fun seekToScene(sceneIndex: Int) {
        if (sceneIndex in scenes.indices) {
            val targetSec = scenes[sceneIndex].startSec.toFloat()
            seekTo(targetSec)
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        audioService.stopBackgroundMusic()
        voiceService.stop()
    }

    fun stop() {
        pause()
        _currentTimeSec.value = 0f
        _currentSceneIndex.value = 0
        _currentScene.value = scenes.firstOrNull()
        lastSpokenSceneIndex = -1
    }

    fun release() {
        stop()
        voiceService.release()
        audioService.release()
    }
}
