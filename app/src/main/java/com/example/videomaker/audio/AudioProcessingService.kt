package com.example.videomaker.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.videomaker.model.AudioMix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioProcessingService {

    private var musicTrack: AudioTrack? = null
    private var sfxTrack: AudioTrack? = null
    private var musicJob: Job? = null
    private var currentMix = AudioMix()

    private val sampleRate = 44100
    private var isMusicPlaying = false

    fun updateMix(newMix: AudioMix) {
        currentMix = newMix
        try {
            musicTrack?.setVolume(currentMix.musicVolume)
            sfxTrack?.setVolume(currentMix.sfxVolume)
        } catch (e: Exception) {
            Log.w("AudioProc", "Error setting volume: ${e.message}")
        }
    }

    fun startBackgroundMusic(scope: CoroutineScope, mood: String = "Cinematic Ambient") {
        stopBackgroundMusic()
        isMusicPlaying = true

        musicJob = scope.launch(Dispatchers.Default) {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            musicTrack = track
            track.setVolume(currentMix.musicVolume)
            track.play()

            // Generate harmonious cinematic ambient chords (Root, 5th, Octave + slow tremolo)
            val baseFreq = when {
                mood.contains("Epic", true) || mood.contains("Action", true) -> 130.81 // C3
                mood.contains("Mystery", true) || mood.contains("Horror", true) -> 110.0 // A2
                else -> 146.83 // D3
            }

            val buffer = ShortArray(bufferSize)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var tremoloPhase = 0.0

            while (isActive && isMusicPlaying) {
                for (i in buffer.indices) {
                    val tremolo = 0.6 + 0.4 * sin(tremoloPhase)
                    val s1 = sin(phase1)
                    val s2 = 0.6 * sin(phase2)
                    val s3 = 0.4 * sin(phase3)
                    val sample = ((s1 + s2 + s3) / 2.0 * tremolo * 16000.0).toInt().coerceIn(-32767, 32767)
                    buffer[i] = sample.toShort()

                    phase1 += 2.0 * Math.PI * baseFreq / sampleRate
                    phase2 += 2.0 * Math.PI * (baseFreq * 1.4983) / sampleRate // Perfect 5th
                    phase3 += 2.0 * Math.PI * (baseFreq * 2.0) / sampleRate // Octave
                    tremoloPhase += 2.0 * Math.PI * 0.25 / sampleRate // Slow 0.25Hz pulse
                }
                track.write(buffer, 0, buffer.size)
            }

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun playSfxChime(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            track.setVolume(currentMix.sfxVolume)
            track.play()

            val numSamples = (sampleRate * 0.4).toInt() // 400ms chime
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            val freq = 880.0 // A5 pleasant chime

            for (i in 0 until numSamples) {
                val envelope = (1.0 - (i.toDouble() / numSamples)) // decay
                val s = sin(phase) * envelope * 20000.0
                buffer[i] = s.toInt().toShort()
                phase += 2.0 * Math.PI * freq / sampleRate
            }

            track.write(buffer, 0, numSamples)
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun stopBackgroundMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
        try {
            musicTrack?.stop()
            musicTrack?.release()
        } catch (_: Exception) {}
        musicTrack = null
    }

    fun release() {
        stopBackgroundMusic()
    }
}
