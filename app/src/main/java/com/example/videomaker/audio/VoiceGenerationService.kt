package com.example.videomaker.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.videomaker.model.VoiceGender
import com.example.videomaker.model.VoiceLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceGenerationService(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentPhoneme = MutableStateFlow("REST")
    val currentPhoneme: StateFlow<String> = _currentPhoneme.asStateFlow()

    private val _lipOpenness = MutableStateFlow(0f)
    val lipOpenness: StateFlow<Float> = _lipOpenness.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _lipOpenness.value = 0f
                        _currentPhoneme.value = "REST"
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _lipOpenness.value = 0f
                    }
                })
            } else {
                Log.w("VoiceGen", "TTS initialization failed status=$status")
            }
        }
    }

    fun speakDialogue(
        text: String,
        language: VoiceLanguage,
        gender: VoiceGender,
        volume: Float = 1.0f
    ) {
        if (!isInitialized || tts == null || text.isBlank()) return

        val locale = when (language) {
            VoiceLanguage.URDU -> Locale("ur", "PK")
            VoiceLanguage.HINDI -> Locale("hi", "IN")
            VoiceLanguage.ENGLISH -> Locale.US
        }

        try {
            tts?.language = locale
            tts?.setPitch(gender.pitchFactor)
            tts?.setSpeechRate(gender.rateFactor)

            val params = android.os.Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "vid_dialogue_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("VoiceGen", "Error speaking dialogue", e)
        }
    }

    fun updateLipSyncFrame(progressInDialogue: Float, isPlaying: Boolean) {
        if (!isPlaying) {
            _lipOpenness.value = 0f
            _currentPhoneme.value = "REST"
            return
        }
        // Realistic simulated vowel/consonant mouth cycle
        val cycle = (progressInDialogue * 24f) % 4f
        val openness = when (cycle.toInt()) {
            0 -> 0.85f // A, O
            1 -> 0.45f // E, I
            2 -> 0.70f // U
            else -> 0.15f // Consonants M, B, P
        }
        _lipOpenness.value = openness
        _currentPhoneme.value = when (cycle.toInt()) {
            0 -> "AA"
            1 -> "EE"
            2 -> "OO"
            else -> "MM"
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _lipOpenness.value = 0f
        _currentPhoneme.value = "REST"
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
