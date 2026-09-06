package com.example.data.model

import androidx.annotation.DrawableRes

enum class PipelineStageId(val stepNumber: Int, val title: String, val shortTitle: String) {
    EXTRACT_AUDIO(1, "Video → Audio Extraction", "1. Extract Audio"),
    SPEECH_RECOGNITION(2, "Audio → Text (Speech Recognition)", "2. Speech Recognition"),
    LANGUAGE_DETECTION(3, "Detect Original Language", "3. Detect Language"),
    AI_TRANSLATION(4, "AI Translation", "4. AI Translation"),
    ADAPT_TRANSLATION(5, "Context & Pacing Adaptation", "5. Adapt Script"),
    VOICE_GENERATION(6, "AI Voice Generation (Dubbing)", "6. AI Voice Dubbing"),
    TIMING_SYNC(7, "Voice & Timing Synchronization", "7. Timing Sync"),
    STEM_SEPARATION(8, "Stem Separation & Audio Mixing", "8. Music & SFX Mix"),
    CREATE_SUBTITLES(9, "Create Subtitles (.srt / .vtt)", "9. Subtitles"),
    RECOMBINE(10, "Recombine Translated Video", "10. Final Video")
}

enum class StageStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    ERROR
}

data class PipelineStageState(
    val id: PipelineStageId,
    val status: StageStatus = StageStatus.PENDING,
    val progress: Float = 0f, // 0.0 to 1.0
    val summary: String = "",
    val details: List<String> = emptyList(),
    val durationMs: Long = 0L
)

data class TranscriptSegment(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val speaker: String = "Speaker 1",
    val originalText: String,
    val originalLanguage: String = "English",
    val translatedText: String = "",
    val adaptedText: String = "",
    val targetLanguage: String = "Hindi",
    val syncSpeedMultiplier: Float = 1.0f,
    val audioDurationMs: Long = 0L,
    val pitchShift: Float = 1.0f
) {
    val durationFormatted: String
        get() {
            val startSec = startMs / 1000f
            val endSec = endMs / 1000f
            return String.format("%.1fs - %.1fs", startSec, endSec)
        }

    fun toSrtEntry(index: Int): String {
        return "$index\n${formatSrtTime(startMs)} --> ${formatSrtTime(endMs)}\n${if (adaptedText.isNotBlank()) adaptedText else translatedText.ifBlank { originalText }}\n"
    }

    fun toVttEntry(index: Int): String {
        return "$index\n${formatVttTime(startMs)} --> ${formatVttTime(endMs)}\n${if (adaptedText.isNotBlank()) adaptedText else translatedText.ifBlank { originalText }}\n"
    }

    private fun formatSrtTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    private fun formatVttTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }
}

data class AudioStemConfig(
    val dialogueVolume: Float = 0.0f, // Muted original speech when dubbed
    val dubbedVoiceVolume: Float = 1.0f, // Active AI translated voice
    val musicVolume: Float = 0.45f, // Background music bed preserved
    val sfxVolume: Float = 0.65f, // Ambient & sound effects preserved
    val duckingEnabled: Boolean = true,
    val duckingAmountDb: Float = -18f
)

data class SubtitleConfig(
    val enabled: Boolean = true,
    val format: SubtitleFormat = SubtitleFormat.BURNED_IN,
    val fontSizeSp: Int = 16,
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#CC000000",
    val showBox: Boolean = true
)

enum class SubtitleFormat {
    BURNED_IN,
    SRT,
    VTT
}

data class VoiceProfile(
    val id: String,
    val name: String,
    val gender: String,
    val tone: String,
    val sampleLanguage: String,
    val pitch: Float = 1.0f,
    val rate: Float = 1.0f
)

data class SupportedLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String
)

data class VideoProject(
    val id: String,
    val title: String,
    val description: String,
    val durationMs: Long,
    @DrawableRes val thumbnailRes: Int = 0,
    val thumbnailUrl: String? = null,
    val videoUri: String? = null,
    val isYoutube: Boolean = false,
    val youtubeVideoId: String? = null,
    val youtubeUrl: String? = null,
    val channelOrAuthor: String = "Creator",
    val sourceLanguage: SupportedLanguage,
    val targetLanguage: SupportedLanguage,
    val selectedVoice: VoiceProfile,
    val segments: List<TranscriptSegment>,
    val stems: AudioStemConfig = AudioStemConfig(),
    val subtitles: SubtitleConfig = SubtitleConfig(),
    val stages: Map<PipelineStageId, PipelineStageState> = emptyMap(),
    val currentStage: PipelineStageId = PipelineStageId.EXTRACT_AUDIO,
    val isPipelineRunning: Boolean = false,
    val isPipelineComplete: Boolean = false
)
