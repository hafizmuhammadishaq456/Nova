package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.utils.LanguageDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "TextToSpeechMgr"

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    fun speak(text: String, languageCode: String = "auto") {
        if (_isMuted.value || !isInitialized || tts == null) return

        // Clean formatting symbols that sound unnatural when spoken
        val cleanedText = text
            .replace(Regex("```[a-zA-Z0-9_]*\\n[\\s\\S]*?```"), "Code snippet omitted.")
            .replace(Regex("[*#_`~>|\\[\\]()]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanedText.isBlank()) return

        val targetLocale = LanguageDetector.getLocaleForLanguage(languageCode, cleanedText)

        try {
            val availability = tts?.isLanguageAvailable(targetLocale)
            if (availability == TextToSpeech.LANG_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            ) {
                tts?.language = targetLocale
            } else {
                tts?.language = Locale.US
            }
        } catch (e: Exception) {
            tts?.language = Locale.US
        }

        tts?.speak(
            cleanedText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "nova_utterance_${System.currentTimeMillis()}"
        )
    }

    fun toggleMute(): Boolean {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        if (newMuted) {
            stop()
        }
        return newMuted
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stop()
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
