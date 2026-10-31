package com.gemini.multiverse.data.chat

import com.gemini.multiverse.core.gemini.GeminiModelResolver
import com.gemini.multiverse.data.settings.SettingsRepository
import com.gemini.multiverse.domain.chat.ChatMessage
import com.gemini.multiverse.domain.chat.MessageSender
import com.gemini.multiverse.domain.chat.MessageStatus
import com.gemini.multiverse.domain.personality.PersonalitiesCatalog
import com.gemini.multiverse.domain.polyglot.PolyglotBrain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class ChatRepository(
    private val settingsRepository: SettingsRepository
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.LIA,
                text = "নমস্কার! আমি Jarvis। আপনি আমাকে টাইপ করে বা ভয়েস অর্ব দিয়ে কথা বলতে পারেন। আজকে কীভাবে সাহায্য করতে পারি?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    suspend fun sendMessage(userText: String): Unit = withContext(Dispatchers.IO) {
        if (userText.isBlank()) return@withContext

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userText.trim(),
            status = MessageStatus.SENT
        )

        val liaPlaceholder = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.LIA,
            text = "...",
            status = MessageStatus.SENDING
        )

        _messages.value = _messages.value + userMessage + liaPlaceholder

        val settings = settingsRepository.settingsState.value
        val apiKey = settings.apiKey.trim()
        if (apiKey.isBlank()) {
            updateLiaMessage(liaPlaceholder.id, "অনুগ্রহ করে Settings-এ গিয়ে Gemini API Key দিন।")
            return@withContext
        }

        val persona = PersonalitiesCatalog.getById(settings.selectedPersonalityId)
        val systemInstruction = PolyglotBrain.compileSystemInstruction(persona, settings.selectedLanguage)

        val payload = buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    addJsonObject { put("text", systemInstruction) }
                }
            }
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        addJsonObject { put("text", userText) }
                    }
                }
            }
        }

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())

        var lastError = ""
        val candidates = GeminiModelResolver.resolveChatCandidates(apiKey)
        if (candidates.isEmpty()) {
            updateLiaMessage(liaPlaceholder.id, "Error: No compatible Gemini models found. Please check your API key in Settings.")
            return@withContext
        }

        // Try discovered candidate models in order until one succeeds
        for (candidate in candidates) {
            try {
                val url = candidate.generateContentUrl(apiKey)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val parsed = json.parseToJsonElement(responseBody).jsonObject
                    val candidateText = parsed["candidates"]?.jsonArray?.firstOrNull()
                        ?.jsonObject?.get("content")?.jsonObject
                        ?.get("parts")?.jsonArray?.firstOrNull()
                        ?.jsonObject?.get("text")?.jsonPrimitive?.content

                    if (!candidateText.isNullOrBlank()) {
                        GeminiModelResolver.markChatModelSuccess(candidate)
                        updateLiaMessage(liaPlaceholder.id, candidateText)
                        return@withContext
                    }
                } else {
                    lastError = "Model ${candidate.apiVersion}/${candidate.name} returned HTTP ${response.code}: $responseBody"
                    // Only break if the key itself is unauthorized (401/403).
                    // For model-specific quota (429), busy (503), not found (404), or arg error (400), try next candidate model!
                    if (response.code == 401 || response.code == 403) {
                        break
                    }
                }
            } catch (e: Exception) {
                lastError = "Connection error: ${e.message}"
            }
        }

        updateLiaMessage(liaPlaceholder.id, "Error: $lastError")
    }

    private fun updateLiaMessage(id: String, newText: String) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == id) {
                msg.copy(text = newText, status = MessageStatus.SENT)
            } else msg
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}
