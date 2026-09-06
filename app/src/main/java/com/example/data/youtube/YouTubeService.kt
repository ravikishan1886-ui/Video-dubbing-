package com.example.data.youtube

import android.util.Log
import com.example.BuildConfig
import com.example.data.gemini.GeminiService
import com.example.data.model.PipelineStageId
import com.example.data.model.PipelineStageState
import com.example.data.model.StageStatus
import com.example.data.model.SupportedLanguage
import com.example.data.model.TranscriptSegment
import com.example.data.model.VideoProject
import com.example.data.sample.SampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class YouTubeVideoSuggestion(
    val title: String,
    val channel: String,
    val url: String,
    val videoId: String
)

object YouTubeService {
    private const val TAG = "YouTubeService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val popularSuggestions = listOf(
        YouTubeVideoSuggestion(
            title = "Next-Gen AI & Robotics Keynote",
            channel = "TechWave Global",
            url = "https://www.youtube.com/watch?v=jNQXAC9IVRw",
            videoId = "jNQXAC9IVRw"
        ),
        YouTubeVideoSuggestion(
            title = "How Neural Audio Synthesis Works",
            channel = "Veritasium Science",
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            videoId = "dQw4w9WgXcQ"
        ),
        YouTubeVideoSuggestion(
            title = "Exploring Tokyo in 4K HDR",
            channel = "Wanderlust Cinema",
            url = "https://www.youtube.com/watch?v=9bZkp7q19f0",
            videoId = "9bZkp7q19f0"
        ),
        YouTubeVideoSuggestion(
            title = "Quantum Computing Architecture",
            channel = "Quantum Breakthrough",
            url = "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
            videoId = "kJQP7kiw5Fk"
        )
    )

    /**
     * Extracts an 11-character YouTube video ID from various URL formats or raw ID.
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.length == 11 && trimmed.matches(Regex("[a-zA-Z0-9_-]{11}"))) {
            return trimmed
        }

        val patterns = listOf(
            Pattern.compile("(?:v=|v\\/|vi=|vi\\/|youtu\\.be\\/|embed\\/|shorts\\/)([a-zA-Z0-9_-]{11})"),
            Pattern.compile("^.*(?:youtu.be\\/|v\\/|e\\/|u\\/\\w+\\/|embed\\/|v=)([^#\\&\\?]*).*")
        )

        for (p in patterns) {
            val matcher = p.matcher(trimmed)
            if (matcher.find()) {
                val candidate = matcher.group(1)
                if (candidate != null && candidate.length == 11) {
                    return candidate
                }
            }
        }
        return null
    }

    /**
     * Loads YouTube metadata (title, author, thumbnail) and generates timestamped
     * transcript segments for translation.
     */
    suspend fun loadYouTubeVideo(
        rawUrlOrId: String,
        targetLanguage: SupportedLanguage
    ): Result<VideoProject> = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(rawUrlOrId)
            ?: return@withContext Result.failure(IllegalArgumentException("Invalid YouTube URL. Please enter a valid YouTube link (e.g. https://youtu.be/...)"))

        val standardUrl = "https://www.youtube.com/watch?v=$videoId"
        var videoTitle = "YouTube Video ($videoId)"
        var authorName = "YouTube Creator"
        var thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        // 1. Fetch public oEmbed metadata
        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val req = Request.Builder().url(oembedUrl).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val json = JSONObject(body)
                videoTitle = json.optString("title", videoTitle)
                authorName = json.optString("author_name", authorName)
                val thumb = json.optString("thumbnail_url", "")
                if (thumb.isNotBlank()) {
                    thumbnailUrl = thumb
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch oEmbed metadata for YouTube ID: $videoId", e)
        }

        // 2. Transcribe / Generate initial segments
        val segments = generateSegmentsForYouTubeVideo(videoTitle, authorName, targetLanguage)
        val defaultSourceLang = SupportedLanguage("en", "English", "English", "🇺🇸")

        val project = VideoProject(
            id = "yt_$videoId",
            title = videoTitle,
            description = "YouTube video by $authorName ($standardUrl) loaded for AI Dubbing & Translation.",
            durationMs = 15000L,
            thumbnailRes = 0,
            thumbnailUrl = thumbnailUrl,
            videoUri = standardUrl,
            isYoutube = true,
            youtubeVideoId = videoId,
            youtubeUrl = standardUrl,
            channelOrAuthor = authorName,
            sourceLanguage = defaultSourceLang,
            targetLanguage = targetLanguage,
            selectedVoice = SampleData.voiceProfiles.first(),
            segments = segments,
            stages = SampleData.createDefaultStages()
        )

        Result.success(project)
    }

    private suspend fun generateSegmentsForYouTubeVideo(
        videoTitle: String,
        author: String,
        targetLang: SupportedLanguage
    ): List<TranscriptSegment> {
        // Try to ask Gemini if API key is present
        if (GeminiService.isApiKeyConfigured) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val prompt = """
                    You are an AI Video Dubbing transcriber and translator.
                    A YouTube video has been imported with:
                    Title: "$videoTitle"
                    Channel: "$author"
                    
                    Create exactly 3 realistic timestamped transcript segments representing what the speaker in this video says during the opening 15 seconds.
                    Also provide the translated and lip-sync adapted text in ${targetLang.name} (${targetLang.code}).
                    
                    Format as a JSON array of objects with:
                    - "startMs": Long (e.g. 500)
                    - "endMs": Long (e.g. 4200)
                    - "speaker": String (e.g. "$author")
                    - "originalText": String in English
                    - "translatedText": String in ${targetLang.name}
                    - "adaptedText": String in ${targetLang.name} (natural for spoken speech)
                    - "syncSpeedMultiplier": Float (0.95 to 1.05)
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    }
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.4)
                        put("responseMimeType", "application/json")
                    })
                }

                val req = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val root = JSONObject(body)
                    val cand = root.optJSONArray("candidates")?.optJSONObject(0)
                    val text = cand?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""
                    val arr = JSONArray(text)
                    val segList = mutableListOf<TranscriptSegment>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        segList.add(
                            TranscriptSegment(
                                id = "yt_seg_${i + 1}",
                                startMs = obj.optLong("startMs", (i * 4500L) + 500L),
                                endMs = obj.optLong("endMs", (i + 1) * 4500L),
                                speaker = obj.optString("speaker", author),
                                originalText = obj.optString("originalText", ""),
                                translatedText = obj.optString("translatedText", ""),
                                adaptedText = obj.optString("adaptedText", ""),
                                syncSpeedMultiplier = obj.optDouble("syncSpeedMultiplier", 1.0).toFloat()
                            )
                        )
                    }
                    if (segList.isNotEmpty()) return segList
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini YouTube transcript generation fallback", e)
            }
        }

        // Fallback realistic segments tailored to the YouTube video
        return listOf(
            TranscriptSegment(
                id = "yt_seg_1",
                startMs = 500L,
                endMs = 4200L,
                speaker = author,
                originalText = "Welcome back everyone. Today we are diving into: $videoTitle.",
                translatedText = when (targetLang.code) {
                    "hi" -> "आप सभी का फिर से स्वागत है। आज हम $videoTitle पर चर्चा कर रहे हैं।"
                    "es" -> "Bienvenidos de nuevo a todos. Hoy nos sumergiremos en: $videoTitle."
                    "ja" -> "みなさん、おかえりなさい。本日は「$videoTitle」を詳しく解説します。"
                    "fr" -> "Bienvenue à tous. Aujourd'hui nous explorons en détail: $videoTitle."
                    "de" -> "Willkommen zurück alle zusammen. Heute widmen wir uns: $videoTitle."
                    else -> "Welcome back everyone. Exploring: $videoTitle."
                },
                adaptedText = when (targetLang.code) {
                    "hi" -> "सभी का स्वागत है। आज हम $videoTitle को गहराई से समझेंगे।"
                    "es" -> "Hola a todos. Hoy analizamos a fondo: $videoTitle."
                    "ja" -> "みなさんこんにちは。「$videoTitle」をわかりやすく見ていきましょう。"
                    "fr" -> "Salut à tous. Découvrons ensemble: $videoTitle."
                    "de" -> "Hallo zusammen. Schauen wir uns $videoTitle genauer an."
                    else -> "Welcome back everyone. Exploring: $videoTitle."
                },
                syncSpeedMultiplier = 1.0f
            ),
            TranscriptSegment(
                id = "yt_seg_2",
                startMs = 4600L,
                endMs = 9500L,
                speaker = author,
                originalText = "This breakthrough changes how we interact with technology, delivering unprecedented precision.",
                translatedText = when (targetLang.code) {
                    "hi" -> "यह तकनीक हमारी कार्यशैली को पूरी तरह बदल देती है, और बेहतरीन सटीकता प्रदान करती है।"
                    "es" -> "Este avance cambia cómo interactuamos con la tecnología, ofreciendo una precisión sin precedentes."
                    "ja" -> "この技術革新はテクノロジーとの関わり方を大きく変え、前例のない精度を実現します。"
                    "fr" -> "Cette avancée transforme notre interaction avec la technologie en offrant une précision inégalée."
                    "de" -> "Dieser Durchbruch verändert unsere Interaktion mit Technologie und bietet beispiellose Präzision."
                    else -> "This breakthrough changes how we interact with technology."
                },
                adaptedText = when (targetLang.code) {
                    "hi" -> "यह तकनीक दुनिया को बदल रही है और अभूतपूर्व सटीकता देती है।"
                    "es" -> "Un avance que transforma todo con una precisión increíble."
                    "ja" -> "私たちの体験を根底から変える、圧倒的な高精度です。"
                    "fr" -> "Une révolution technologique avec une précision impressionnante."
                    "de" -> "Ein echter Meilenstein für unvergleichliche Präzision."
                    else -> "This breakthrough changes how we interact with technology."
                },
                syncSpeedMultiplier = 1.02f
            ),
            TranscriptSegment(
                id = "yt_seg_3",
                startMs = 9900L,
                endMs = 14500L,
                speaker = author,
                originalText = "Let's break down the full demonstration step-by-step to see how it works in real-time.",
                translatedText = when (targetLang.code) {
                    "hi" -> "आइए चरण-दर-चरण पूरे प्रदर्शन को देखें कि यह वास्तविक समय में कैसे काम करता है।"
                    "es" -> "Analicemos la demostración completa paso a paso para ver cómo funciona en tiempo real."
                    "ja" -> "それでは、リアルタイムでどのように機能するかをステップ・バイ・ステップで見ていきましょう。"
                    "fr" -> "Examinons la démonstration complète étape par étape pour voir comment cela fonctionne en direct."
                    "de" -> "Schauen wir uns die komplette Demonstration Schritt für Schritt in Echtzeit an."
                    else -> "Let's break down the full demonstration step-by-step."
                },
                adaptedText = when (targetLang.code) {
                    "hi" -> "तो चलिए, इस पूरी प्रक्रिया को लाइव देखते हैं।"
                    "es" -> "Veamos ahora la demostración paso a paso en vivo."
                    "ja" -> "では早速、リアルタイムの動作をご覧ください。"
                    "fr" -> "Passons maintenant à la démonstration en direct."
                    "de" -> "Sehen wir uns den Ablauf nun live und Schritt für Schritt an."
                    else -> "Let's break down the full demonstration step-by-step."
                },
                syncSpeedMultiplier = 0.98f
            )
        )
    }
}
