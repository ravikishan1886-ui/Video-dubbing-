package com.example.data.sample

import com.example.R
import com.example.data.model.PipelineStageId
import com.example.data.model.PipelineStageState
import com.example.data.model.StageStatus
import com.example.data.model.SupportedLanguage
import com.example.data.model.TranscriptSegment
import com.example.data.model.VideoProject
import com.example.data.model.VoiceProfile

object SampleData {
    val languages = listOf(
        SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
        SupportedLanguage("en", "English", "English", "🇺🇸"),
        SupportedLanguage("es", "Spanish", "Español", "🇪🇸"),
        SupportedLanguage("ja", "Japanese", "日本語", "🇯🇵"),
        SupportedLanguage("fr", "French", "Français", "🇫🇷"),
        SupportedLanguage("de", "German", "Deutsch", "🇩🇪"),
        SupportedLanguage("ar", "Arabic", "العربية", "🇸🇦"),
        SupportedLanguage("pt", "Portuguese", "Português", "🇧🇷"),
        SupportedLanguage("zh", "Chinese", "中文", "🇨🇳")
    )

    val voiceProfiles = listOf(
        VoiceProfile("kore", "Kore (Dynamic Studio)", "Female", "Enthusiastic & Clear", "en", pitch = 1.05f, rate = 1.0f),
        VoiceProfile("fenrir", "Fenrir (Keynote Speaker)", "Male", "Deep, Authoritative & Warm", "en", pitch = 0.95f, rate = 0.98f),
        VoiceProfile("aoede", "Aoede (Narrator)", "Female", "Smooth, Documentary Style", "fr", pitch = 1.0f, rate = 0.92f),
        VoiceProfile("puck", "Puck (Casual Creator)", "Male", "Energetic & Friendly", "es", pitch = 1.1f, rate = 1.05f),
        VoiceProfile("charon", "Charon (Broadcast)", "Male", "Professional News Anchor", "hi", pitch = 0.9f, rate = 1.0f)
    )

    fun createDefaultStages(): Map<PipelineStageId, PipelineStageState> {
        return PipelineStageId.values().associateWith { stageId ->
            PipelineStageState(
                id = stageId,
                status = StageStatus.PENDING,
                progress = 0f,
                summary = "Ready to process",
                details = emptyList()
            )
        }
    }

    val sampleProjects = listOf(
        VideoProject(
            id = "proj_keynote_01",
            title = "Keynote: Next-Gen AI Silicon",
            description = "English tech announcement keynote with dialogue, presentation music, and product audio.",
            durationMs = 15000L,
            thumbnailRes = R.drawable.sample_keynote_1788693040282,
            sourceLanguage = SupportedLanguage("en", "English", "English", "🇺🇸"),
            targetLanguage = SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
            selectedVoice = voiceProfiles[1],
            segments = listOf(
                TranscriptSegment(
                    id = "seg_1",
                    startMs = 500L,
                    endMs = 3800L,
                    speaker = "Speaker 1 (Host)",
                    originalText = "Welcome to our channel. Today we're thrilled to introduce our newest breakthrough.",
                    originalLanguage = "English",
                    translatedText = "हमारे चैनल में आपका स्वागत है। आज हम अपनी सबसे नई सफलता पेश करते हुए रोमांचित हैं।",
                    adaptedText = "हमारे चैनल में आपका स्वागत है! आज हम अगली पीढ़ी की तकनीक पेश करने जा रहे हैं।",
                    targetLanguage = "Hindi",
                    syncSpeedMultiplier = 1.02f
                ),
                TranscriptSegment(
                    id = "seg_2",
                    startMs = 4200L,
                    endMs = 8600L,
                    speaker = "Speaker 1 (Host)",
                    originalText = "Notice how the voice timing automatically synchronizes with lip movements.",
                    originalLanguage = "English",
                    translatedText = "ध्यान दें कि आवाज का समय स्वचालित रूप से होंठों की हरकतों के साथ कैसे मेल खाता है।",
                    adaptedText = "देखें कि कैसे अनुवादित आवाज़ और होठों की हरकतें सटीक रूप से तालमेल बिठाती हैं।",
                    targetLanguage = "Hindi",
                    syncSpeedMultiplier = 0.98f
                ),
                TranscriptSegment(
                    id = "seg_3",
                    startMs = 9100L,
                    endMs = 14200L,
                    speaker = "Speaker 1 (Host)",
                    originalText = "All background music and sound effects are seamlessly preserved in the final video.",
                    originalLanguage = "English",
                    translatedText = "अंतिम वीडियो में सभी बैकग्राउंड संगीत और ध्वनि प्रभाव मूल रूप से संरक्षित हैं।",
                    adaptedText = "बैकग्राउंड संगीत और प्रभाव पूरी तरह सुरक्षित रहते हैं, ताकि अनुभव वास्तविक लगे।",
                    targetLanguage = "Hindi",
                    syncSpeedMultiplier = 1.04f
                )
            ),
            stages = createDefaultStages()
        ),
        VideoProject(
            id = "proj_nature_02",
            title = "Expedition: Wilderness Echoes",
            description = "French nature documentary with ambient wilderness wind, bird calls, and narration.",
            durationMs = 16000L,
            thumbnailRes = R.drawable.sample_nature_1788693059568,
            sourceLanguage = SupportedLanguage("fr", "French", "Français", "🇫🇷"),
            targetLanguage = SupportedLanguage("es", "Spanish", "Español", "🇪🇸"),
            selectedVoice = voiceProfiles[2],
            segments = listOf(
                TranscriptSegment(
                    id = "seg_nat_1",
                    startMs = 800L,
                    endMs = 4500L,
                    speaker = "Narrator",
                    originalText = "Bienvenue dans les forêts tropicales de Madagascar, un écosystème d'une beauté rare.",
                    originalLanguage = "French",
                    translatedText = "Bienvenidos a las selvas tropicales de Madagascar, un ecosistema de rara belleza.",
                    adaptedText = "¡Bienvenidos a las selvas de Madagascar, un ecosistema asombroso y único!",
                    targetLanguage = "Spanish",
                    syncSpeedMultiplier = 1.0f
                ),
                TranscriptSegment(
                    id = "seg_nat_2",
                    startMs = 5000L,
                    endMs = 9800L,
                    speaker = "Narrator",
                    originalText = "Écoutez les échos de la faune sauvage se réveillant au premier rayon du soleil.",
                    originalLanguage = "French",
                    translatedText = "Escucha los ecos de la vida salvaje que despierta con el primer rayo de sol.",
                    adaptedText = "Siente el despertar de la naturaleza con los primeros rayos del amanecer.",
                    targetLanguage = "Spanish",
                    syncSpeedMultiplier = 0.96f
                ),
                TranscriptSegment(
                    id = "seg_nat_3",
                    startMs = 10400L,
                    endMs = 15200L,
                    speaker = "Narrator",
                    originalText = "Chaque son raconte une histoire millénaire sculptée par le vent et la terre.",
                    originalLanguage = "French",
                    translatedText = "Cada sonido cuenta una historia milenaria esculpida por el viento y la tierra.",
                    adaptedText = "Cada susurro cuenta una historia eterna forjada entre el viento y la montaña.",
                    targetLanguage = "Spanish",
                    syncSpeedMultiplier = 1.02f
                )
            ),
            stages = createDefaultStages()
        )
    )
}
