package com.gemini.multiverse.core.gemini

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class ModelCandidate(
    val apiVersion: String,
    val name: String // e.g. "models/gemini-2.0-flash"
) {
    fun generateContentUrl(apiKey: String): String {
        return "https://generativelanguage.googleapis.com/$apiVersion/$name:generateContent?key=$apiKey"
    }
}

object GeminiModelResolver {
    private const val TAG = "GeminiModelResolver"
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedChatCandidate: ModelCandidate? = null

    @Volatile
    private var cachedLiveModelName: String? = null

    /**
     * Resolves an ordered list of candidate models for chat generateContent.
     * Queries ModelService.ListModels dynamically from Google to inspect permitted models.
     */
    fun resolveChatCandidates(apiKey: String): List<ModelCandidate> {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) return emptyList()

        // If we already know which model works for this key, return it as primary
        cachedChatCandidate?.let {
            return listOf(it)
        }

        val discoveredCandidates = mutableListOf<ModelCandidate>()

        // 1. Query ListModels on v1beta and v1
        for (version in listOf("v1beta", "v1")) {
            try {
                val url = "https://generativelanguage.googleapis.com/$version/models?key=$cleanKey"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val root = json.parseToJsonElement(body).jsonObject
                    val modelsList = root["models"]?.jsonArray
                    if (modelsList != null) {
                        for (element in modelsList) {
                            val modelObj = element.jsonObject
                            val name = modelObj["name"]?.jsonPrimitive?.content ?: continue
                            val methods = modelObj["supportedGenerationMethods"]?.jsonArray
                                ?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
                            // Exclude TTS-only models — they only support AUDIO, not TEXT
                            val isTtsModel = name.contains("-tts", ignoreCase = true)
                            if (methods.contains("generateContent") && !isTtsModel) {
                                discoveredCandidates.add(ModelCandidate(version, name))
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "ListModels failed on $version: HTTP ${response.code} - $body")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception querying ListModels on $version: ${e.message}")
            }
        }

        if (discoveredCandidates.isNotEmpty()) {
            // Sort to prioritize faster/modern flash models first
            val sorted = discoveredCandidates.sortedWith(
                compareByDescending<ModelCandidate> { it.name.contains("2.0-flash") || it.name.contains("2.5-flash") }
                    .thenByDescending { it.name.contains("flash") }
                    .thenByDescending { it.apiVersion == "v1beta" }
                    .thenByDescending { it.name.contains("pro") }
            )
            Log.i(TAG, "Dynamically discovered ${sorted.size} models: ${sorted.map { "${it.apiVersion}/${it.name}" }}")
            return sorted
        }

        // 2. Fallback static candidate list if ListModels is unavailable
        Log.i(TAG, "ListModels returned no candidates, using comprehensive static fallback list")
        return listOf(
            ModelCandidate("v1beta", "models/gemini-2.0-flash"),
            ModelCandidate("v1beta", "models/gemini-2.0-flash-001"),
            ModelCandidate("v1beta", "models/gemini-2.0-flash-lite"),
            ModelCandidate("v1beta", "models/gemini-2.0-flash-exp"),
            ModelCandidate("v1beta", "models/gemini-1.5-flash-latest"),
            ModelCandidate("v1beta", "models/gemini-1.5-pro-latest"),
            ModelCandidate("v1beta", "models/gemini-1.5-flash-002"),
            ModelCandidate("v1beta", "models/gemini-1.5-flash-001"),
            ModelCandidate("v1", "models/gemini-1.5-flash"),
            ModelCandidate("v1", "models/gemini-1.5-pro"),
            ModelCandidate("v1", "models/gemini-2.0-flash"),
            ModelCandidate("v1", "models/gemini-pro")
        )
    }

    fun markChatModelSuccess(candidate: ModelCandidate) {
        cachedChatCandidate = candidate
        Log.i(TAG, "Cached working chat model: ${candidate.apiVersion}/${candidate.name}")
    }

    fun invalidateChatCache() {
        cachedChatCandidate = null
    }

    /**
     * Resolves the live bidirectional voice model name (e.g. models/gemini-2.0-flash or models/gemini-2.0-flash-exp).
     */
    fun resolveLiveModel(apiKey: String): String {
        cachedLiveModelName?.let { return it }

        val cleanKey = apiKey.trim()
        if (cleanKey.isNotBlank()) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val root = json.parseToJsonElement(body).jsonObject
                    val modelsList = root["models"]?.jsonArray
                    if (modelsList != null) {
                        for (element in modelsList) {
                            val modelObj = element.jsonObject
                            val name = modelObj["name"]?.jsonPrimitive?.content ?: continue
                            val methods = modelObj["supportedGenerationMethods"]?.jsonArray
                                ?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
                            if (methods.contains("bidiGenerateContent")) {
                                Log.i(TAG, "Discovered live model via ListModels: $name")
                                cachedLiveModelName = name
                                return name
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception resolving live model: ${e.message}")
            }
        }

        val fallback = "models/gemini-2.0-flash-exp"
        cachedLiveModelName = fallback
        return fallback
    }
}
