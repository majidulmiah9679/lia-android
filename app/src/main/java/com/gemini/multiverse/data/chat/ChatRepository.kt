package com.gemini.multiverse.data.chat

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
                text = "Namaste! Main Lia hoon. Aap mujhse type karke ya voice orb se baat kar sakte hain. Aaj kya plan hai?"
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
        val apiKey = settings.apiKey
        if (apiKey.isBlank()) {
            updateLiaMessage(liaPlaceholder.id, "Kripya Settings mein jakar Gemini API Key enter karein.")
            return@withContext
        }

        val persona = PersonalitiesCatalog.getById(settings.selectedPersonalityId)
        val systemInstruction = PolyglotBrain.compileSystemInstruction(persona, settings.selectedLanguage)

        try {
            val payload = buildJsonObject {
                putJsonObject("system_instruction") {
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
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                updateLiaMessage(liaPlaceholder.id, "Error: ${response.code} - ${response.message}")
                return@withContext
            }

            val responseBody = response.body?.string() ?: ""
            val parsed = json.parseToJsonElement(responseBody).jsonObject
            val candidateText = parsed["candidates"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.content
                ?: "Khushi se baat karne ke liye taiyar hoon!"

            updateLiaMessage(liaPlaceholder.id, candidateText)
        } catch (e: Exception) {
            updateLiaMessage(liaPlaceholder.id, "Connection error: ${e.message}")
        }
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
