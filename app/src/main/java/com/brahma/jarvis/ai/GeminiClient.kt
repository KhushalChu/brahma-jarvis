package com.brahma.jarvis.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Minimal client for the Gemini generateContent REST endpoint.
 * Docs: https://ai.google.dev/api/generate-content
 */
class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    sealed class Result {
        data class Success(val text: String) : Result()
        data class Failure(val message: String) : Result()
    }

    /**
     * Sends the full conversation history so the model has context.
     * [systemPrompt] sets the assistant's persona/behavior.
     */
    suspend fun sendMessage(
        apiKey: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): Result = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.Failure("Gemini API key set nahi hai. Settings mein jaake add karo.")
        }

        try {
            val contents = JSONArray()
            for (msg in history) {
                if (msg.sender == Sender.SYSTEM) continue
                val role = if (msg.sender == Sender.USER) "user" else "model"
                val part = JSONObject().put("text", msg.text)
                val entry = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(part))
                contents.put(entry)
            }

            val body = JSONObject().apply {
                put("contents", contents)
                put(
                    "systemInstruction",
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", systemPrompt))
                    )
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 1024)
                    }
                )
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                "gemini-flash-latest:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errMsg = try {
                        JSONObject(raw).optJSONObject("error")?.optString("message")
                    } catch (e: Exception) {
                        null
                    } ?: "Gemini request failed (HTTP ${response.code})"
                    return@withContext Result.Failure(errMsg)
                }

                val json = JSONObject(raw)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (text.isNullOrBlank()) {
                    Result.Failure("Gemini se khaali response mila.")
                } else {
                    Result.Success(text.trim())
                }
            }
        } catch (e: Exception) {
            Result.Failure("Network/parse error: ${e.message}")
        }
    }
}
