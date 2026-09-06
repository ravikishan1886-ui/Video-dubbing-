package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class DubbingEngine(private val context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "DubbingEngine"
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // Synthetic audio track for background music and ambient sounds
    private var musicAudioTrack: AudioTrack? = null
    private var isMusicPlaying = false
    private var musicJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.0f)
            Log.d(TAG, "TTS initialized successfully")
        } else {
            isTtsReady = false
            Log.e(TAG, "TTS init failed with status $status")
        }
    }

    fun setLanguage(langCode: String) {
        if (!isTtsReady) return
        val locale = when (langCode.lowercase()) {
            "hi", "hin" -> Locale("hi", "IN")
            "es", "spa" -> Locale("es", "ES")
            "ja", "jpn" -> Locale.JAPANESE
            "fr", "fra" -> Locale.FRENCH
            "de", "deu" -> Locale.GERMAN
            "zh", "zho" -> Locale.SIMPLIFIED_CHINESE
            "pt", "por" -> Locale("pt", "BR")
            "ar", "ara" -> Locale("ar")
            "it", "ita" -> Locale.ITALIAN
            else -> Locale.ENGLISH
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language $locale is not supported or missing data, falling back to English")
            tts?.setLanguage(Locale.ENGLISH)
        }
    }

    fun speak(text: String, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isTtsReady || text.isBlank()) return
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        tts?.setPitch(pitch.coerceIn(0.5f, 1.8f))
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dubbing_utterance_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        if (isTtsReady) {
            tts?.stop()
        }
    }

    /**
     * Synthesizes warm background music chords using AudioTrack in real-time,
     * allowing the user to experience the stem mixing stage (Dialogue vs Music vs SFX).
     */
    fun startBackgroundMusic(volume: Float = 0.45f) {
        if (isMusicPlaying) {
            updateMusicVolume(volume)
            return
        }
        isMusicPlaying = true
        musicJob = audioScope.launch {
            try {
                val sampleRate = 22050
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                musicAudioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                musicAudioTrack?.play()
                musicAudioTrack?.setVolume(volume.coerceIn(0f, 1f))

                // Chord progression: C - Am - F - G ambient warm pads
                val chords = listOf(
                    listOf(261.63, 329.63, 392.00), // C major (C4, E4, G4)
                    listOf(220.00, 261.63, 329.63), // A minor (A3, C4, E4)
                    listOf(174.61, 220.00, 261.63), // F major (F3, A3, C4)
                    listOf(196.00, 246.94, 293.66)  // G major (G3, B3, D4)
                )

                val buffer = ShortArray(bufferSize)
                var chordIndex = 0
                var sampleIndex = 0

                while (isActive && isMusicPlaying) {
                    val currentChord = chords[chordIndex % chords.size]
                    val samplesPerChord = sampleRate * 2 // 2 seconds per chord

                    for (i in buffer.indices) {
                        val t = (sampleIndex + i).toDouble() / sampleRate
                        // Superposition of chord frequencies + warm overtone
                        var wave = 0.0
                        for (freq in currentChord) {
                            wave += sin(2.0 * Math.PI * freq * t) * 0.25
                        }
                        // Gentle envelope decay
                        val sampleInChord = (sampleIndex + i) % samplesPerChord
                        val env = sin(Math.PI * (sampleInChord.toDouble() / samplesPerChord)).coerceIn(0.1, 1.0)
                        val sampleVal = (wave * env * 14000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        buffer[i] = sampleVal.toShort()
                    }

                    musicAudioTrack?.write(buffer, 0, buffer.size)
                    sampleIndex += buffer.size
                    if (sampleIndex >= samplesPerChord) {
                        sampleIndex = 0
                        chordIndex++
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in music synthesis", e)
            }
        }
    }

    fun updateMusicVolume(volume: Float) {
        try {
            musicAudioTrack?.setVolume(volume.coerceIn(0f, 1f))
        } catch (e: Exception) {
            Log.e(TAG, "Error setting music volume", e)
        }
    }

    fun stopBackgroundMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
        try {
            musicAudioTrack?.stop()
            musicAudioTrack?.release()
            musicAudioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio track", e)
        }
    }

    fun release() {
        stopBackgroundMusic()
        stopSpeaking()
        try {
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
}
