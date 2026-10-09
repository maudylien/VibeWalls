package com.example.data.api

import android.graphics.Bitmap
import com.example.BuildConfig
import com.example.util.WallpaperUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiImageService(
    private var customApiKey: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun setCustomApiKey(key: String) {
        customApiKey = key.trim()
    }

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank()) return custom
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun hasApiKey(): Boolean = getEffectiveApiKey().isNotBlank()

    /**
     * Generates a single image with Gemini REST API.
     * Returns the Bitmap and Base64 string pair.
     */
    suspend fun generateSingleImage(
        prompt: String,
        referenceImageBase64: String? = null,
        model: String = "gemini-nano-banana-2.1",
        aspectRatio: String = "9:16",
        imageSize: String = "1K"
    ): Result<Pair<Bitmap, String>> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set GEMINI_API_KEY in the Secrets panel or API key settings.")
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Prompt text part
                            put(JSONObject().put("text", prompt))

                            // Reference image part if provided (for remixing)
                            if (!referenceImageBase64.isNullOrBlank()) {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", referenceImageBase64)
                                }
                                put(JSONObject().put("inlineData", inlineData))
                            }
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    val imageConfig = JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", imageSize)
                    }
                    put("imageConfig", imageConfig)

                    val responseModalities = JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    }
                    put("responseModalities", responseModalities)
                }
                put("generationConfig", generationConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody ?: "{}")
                    errJson.optJSONObject("error")?.optString("message")
                        ?: "HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            if (responseBody.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty response received from Gemini API"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No image candidates returned from model"))
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts == null || parts.length() == 0) {
                return@withContext Result.failure(Exception("No parts returned in candidate"))
            }

            var base64Data: String? = null
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val inlineData = part.optJSONObject("inlineData")
                if (inlineData != null) {
                    val data = inlineData.optString("data")
                    if (data.isNotBlank()) {
                        base64Data = data
                        break
                    }
                }
            }

            if (base64Data == null) {
                // If model returned text instead of image, extract text
                val textPart = parts.getJSONObject(0).optString("text", "No image data")
                return@withContext Result.failure(Exception("Model returned message instead of image: $textPart"))
            }

            val bitmap = WallpaperUtils.base64ToBitmap(base64Data)
                ?: return@withContext Result.failure(Exception("Failed to decode image bitmap from response"))

            Result.success(Pair(bitmap, base64Data))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
