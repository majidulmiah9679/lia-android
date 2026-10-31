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
                            val lowerName = name.lowercase()
                            val isNonTextOrDeprecated = lowerName.contains("-tts") ||
                                    lowerName.contains("-image") ||
                                    lowerName.contains("preview-image") ||
                                    lowerName.contains("embedding") ||
                                    lowerName.contains("transcribe") ||
                                    lowerName.contains("lyria") ||
                                    lowerName.contains("banana") ||
                                    lowerName.contains("er-2") ||
                                    lowerName.contains("computer-use") ||
                                    lowerName.contains("deep-research") ||
                                    lowerName.contains("aqa") ||
                                    lowerName.contains("2.5-flash") ||
                                    lowerName.contains("2.5-pro") ||
                                    lowerName.contains("2.0-flash") ||
                                    lowerName.contains("1.5-flash")

                            if (methods.contains("generateContent") && !isNonTextOrDeprecated) {
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
            val scoreModel: (ModelCandidate) -> Int = { candidate ->
                val n = candidate.name.lowercase()
                when {
                    n.contains("flash-lite-latest") -> 100
                    n.contains("3.5-flash-lite") -> 95
                    n.contains("3.6-flash") -> 90
                    n.contains("3-flash-preview") -> 85
                    n.contains("3.8-flash") -> 80
                    n.contains("flash-latest") -> 75
                    n.contains("gemma-4") -> 70
                    n.contains("flash-lite") -> 65
                    n.contains("flash") -> 60
                    n.contains("pro") -> 50
                    else -> 10
                }
            }
            val sorted = discoveredCandidates.sortedWith(
                compareByDescending(scoreModel)
                    .thenByDescending { it.apiVersion == "v1beta" }
            )
            Log.i(TAG, "Dynamically discovered ${sorted.size} models: ${sorted.map { "${it.apiVersion}/${it.name}" }}")
            return sorted
        }

        // 2. Fallback static candidate list if ListModels is unavailable
        Log.i(TAG, "ListModels returned no candidates, using comprehensive static fallback list")
        return listOf(
            ModelCandidate("v1beta", "models/gemini-flash-lite-latest"),
            ModelCandidate("v1beta", "models/gemini-3.5-flash-lite"),
            ModelCandidate("v1beta", "models/gemini-3.6-flash"),
            ModelCandidate("v1beta", "models/gemini-3-flash-preview"),
            ModelCandidate("v1beta", "models/gemini-3.8-flash"),
            ModelCandidate("v1beta", "models/gemini-flash-latest"),
            ModelCandidate("v1beta", "models/gemma-4-26b-a4b-it")
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

        val fallback = "models/gemini-2.5-flash-native-audio-preview-12-2025"
        cachedLiveModelName = fallback
        return fallback
    }
}
