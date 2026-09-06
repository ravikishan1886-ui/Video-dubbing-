package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val isApiKeyConfigured: Boolean
        get() = try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Throwable) {
            false
        }

    /**
     * Translates and adapts transcript segments using Gemini 3.5 Flash.
     */
    suspend fun translateAndAdapt(
        segmentsText: List<String>,
        sourceLang: String,
        targetLang: String,
        tone: String = "natural, matching the original speaker's tempo and emotion"
    ): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Secrets panel."))
        }

        try {
            val prompt = """
                You are an advanced AI Video Dubbing & Translation engine.
                Translate the following video transcript segments from $sourceLang to $targetLang.
                
                For EACH segment, provide:
                1. "translation": Direct high-quality translation.
                2. "adapted": Context-adapted translation for lip-sync & dubbing.
                   (Preserve speaker tone ($tone), adjust syllable length to match spoken timing, keep proper names intact, and make it sound natural when spoken).
                
                Input Segments:
                ${segmentsText.mapIndexed { idx, s -> "[$idx] $s" }.joinToString("\n")}
                
                Return ONLY a valid JSON array of objects with keys "index", "translation", and "adapted".
                Example:
                [
                  {"index": 0, "translation": "...", "adapted": "..."}
                ]
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ${response.code}: $responseBody")
                return@withContext Result.failure(Exception("API returned code ${response.code}"))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val text = firstCandidate?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val parsedArray = JSONArray(text)
            val result = mutableListOf<Pair<String, String>>()
            for (i in 0 until parsedArray.length()) {
                val item = parsedArray.getJSONObject(i)
                val trans = item.optString("translation", "")
                val adapt = item.optString("adapted", trans)
                result.add(Pair(trans, adapt))
            }

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini translation", e)
            Result.failure(e)
        }
    }
}
