package com.gemini.multiverse.core.gemini

import android.util.Log
import com.gemini.multiverse.core.audio.AudioEngine
import com.gemini.multiverse.domain.personality.Personality
import com.gemini.multiverse.domain.polyglot.PolyglotBrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

enum class LiveSessionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    LISTENING,
    SPEAKING,
    ERROR
}

class GeminiLiveClient(
    private val audioEngine: AudioEngine,
    private val onToolCallRequest: suspend (name: String, args: JsonObject) -> JsonObject
) {
    companion object {
        private const val TAG = "GeminiLiveClient"
        private const val HOST = "generativelanguage.googleapis.com"
        // v1beta is the active endpoint for BidiGenerateContent
        private const val WS_PATH = "/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(0, TimeUnit.SECONDS) // Disable OkHttp ping to prevent killing connection if server doesn't echo raw ping frames
        .build()

    private var webSocket: WebSocket? = null
    private var activeScope: CoroutineScope? = null
    private var activePersonality: Personality? = null
    private var activeEchoCancellation: Boolean = true

    private val _sessionState = MutableStateFlow(LiveSessionState.IDLE)
    val sessionState: StateFlow<LiveSessionState> = _sessionState.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow("")
    val lastErrorMessage: StateFlow<String> = _lastErrorMessage.asStateFlow()

    fun connect(
        scope: CoroutineScope,
        apiKey: String,
        personality: Personality,
        languageMode: String,
        useEchoCancellation: Boolean
    ) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            _lastErrorMessage.value = "API Key is empty. Please enter it in Settings."
            _sessionState.value = LiveSessionState.ERROR
            return
        }

        disconnect()
        activeScope = scope
        activePersonality = personality
        activeEchoCancellation = useEchoCancellation
        _lastErrorMessage.value = ""
        _sessionState.value = LiveSessionState.CONNECTING

        val url = "wss://$HOST$WS_PATH?key=$cleanKey"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Gemini Live WebSocket opened. Sending setup payload.")
                sendSetupMessage(webSocket, personality, languageMode)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch(Dispatchers.IO) {
                    handleServerMessage(text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val respMsg = response?.let { "HTTP ${it.code}: ${it.message}" } ?: ""
                val errorText = "${t.message ?: "Connection failed"} $respMsg".trim()
                Log.e(TAG, "WebSocket failure: $errorText", t)
                _lastErrorMessage.value = errorText
                _sessionState.value = LiveSessionState.ERROR
                audioEngine.stopRecording()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason ($code)")
                _sessionState.value = LiveSessionState.IDLE
                audioEngine.stopRecording()
                activeScope = null
                activePersonality = null
            }
        })
    }

    private fun sendSetupMessage(
        ws: WebSocket,
        personality: Personality,
        languageMode: String
    ) {
        val memoryContext = try {
            com.gemini.multiverse.GeminiMultiverseApp.instance.container.jarvisMemoryRepository.compileMemoryPrompt()
        } catch (e: Exception) { "" }
        val systemInstructionText = PolyglotBrain.compileSystemInstruction(personality, languageMode, memoryContext)
        val liveModel = "models/gemini-2.0-flash-exp"
        Log.i(TAG, "Configuring Gemini Live session with model: $liveModel")

        val setupPayload = buildJsonObject {
            putJsonObject("setup") {
                put("model", liveModel)
                putJsonObject("generationConfig") {
                    putJsonArray("responseModalities") {
                        add("AUDIO")
                    }
                    putJsonObject("speechConfig") {
                        putJsonObject("voiceConfig") {
                            putJsonObject("prebuiltVoiceConfig") {
                                put("voiceName", personality.voiceName)
                            }
                        }
                    }
                }
                putJsonObject("systemInstruction") {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", systemInstructionText)
                        }
                    }
                }
                putJsonArray("tools") {
                    addJsonObject {
                        putJsonArray("functionDeclarations") {
                            // Mobile Screen Control (A to Z)
                            add(createFunctionSchema("control_screen", "Click, scroll, swipe, type, or navigate system back/home/recents/notifications on mobile screen", listOf("action"), listOf("targetText", "textToType")))
                            add(createFunctionSchema("inspect_screen", "Inspect the current screen to read all visible buttons, texts, switches, and input fields", emptyList()))
                            add(createFunctionSchema("change_setting", "Open or toggle an Android system setting (wifi, bluetooth, sound, display, apps, battery)", listOf("settingName"), listOf("toggleAction")))
                            // Memory Vault
                            add(createFunctionSchema("remember_fact", "Permanently save a user fact, preference, or detail to long-term memory", listOf("key", "value")))
                            add(createFunctionSchema("recall_memories", "Recall all stored long-term memories about the user", emptyList()))
                            // Phone Action Tools
                            add(createFunctionSchema("open_app", "Open any installed application on the phone", listOf("appName")))
                            add(createFunctionSchema("make_call", "Place a phone call to a contact or phone number", listOf("contactNameOrNumber")))
                            add(createFunctionSchema("send_whatsapp", "Send a WhatsApp message to a contact", listOf("contactName", "message")))
                            add(createFunctionSchema("toggle_flashlight", "Turn device flashlight on or off", listOf("enabled")))
                            // 3D Website Forge Tool
                            add(createFunctionSchema("forge_3d_website", "Generate and launch an interactive 3D WebGL website in the Forge", listOf("prompt", "title")))
                        }
                    }
                }
            }
        }

        ws.send(setupPayload.toString())
    }

    private fun sendInitialGreeting(ws: WebSocket, personality: Personality) {
        val greetingPayload = buildJsonObject {
            putJsonObject("clientContent") {
                putJsonArray("turns") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            addJsonObject {
                                put("text", "Introduce yourself warmly out loud: ${personality.sampleGreeting}")
                            }
                        }
                    }
                }
                put("turnComplete", true)
            }
        }
        ws.send(greetingPayload.toString())
    }

    private fun createFunctionSchema(
        name: String,
        description: String,
        requiredParams: List<String>,
        optionalParams: List<String> = emptyList()
    ): JsonObject {
        val allParams = (requiredParams + optionalParams).distinct()
        return buildJsonObject {
            put("name", name)
            put("description", description)
            putJsonObject("parameters") {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    allParams.forEach { param ->
                        putJsonObject(param) {
                            put("type", "STRING")
                            put("description", "Value for $param")
                        }
                    }
                }
                if (requiredParams.isNotEmpty()) {
                    putJsonArray("required") {
                        requiredParams.forEach { add(it) }
                    }
                }
            }
        }
    }

    private fun sendRealtimeAudioChunk(base64Pcm: String) {
        val payload = buildJsonObject {
            putJsonObject("realtimeInput") {
                putJsonArray("mediaChunks") {
                    addJsonObject {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", base64Pcm)
                    }
                }
            }
        }
        webSocket?.send(payload.toString())
    }

    private suspend fun handleServerMessage(text: String) {
        try {
            val root = json.parseToJsonElement(text).jsonObject

            // 0a. Check for setup acknowledgment from Gemini Live server
            if (root.containsKey("setupComplete")) {
                Log.d(TAG, "Gemini Live setupComplete received from server!")
                _sessionState.value = LiveSessionState.CONNECTED
                val p = activePersonality
                val s = activeScope
                val ws = webSocket
                if (ws != null && p != null && s != null) {
                    sendInitialGreeting(ws, p)
                    audioEngine.startRecording(s, activeEchoCancellation) { base64Chunk ->
                        sendRealtimeAudioChunk(base64Chunk)
                    }
                }
                return
            }

            // 0b. Check for error payload from Gemini Live server
            val errorObj = root["error"]?.jsonObject
            if (errorObj != null) {
                val code = errorObj["code"]?.jsonPrimitive?.content ?: ""
                val msg = errorObj["message"]?.jsonPrimitive?.content ?: "Server error"
                val fullErr = if (code.isNotBlank()) "Error $code: $msg" else "Error: $msg"
                Log.e(TAG, "Gemini Live error: $fullErr")
                _lastErrorMessage.value = fullErr
                _sessionState.value = LiveSessionState.ERROR
                audioEngine.stopRecording()
                return
            }

            // 1. Check for barge-in / user interruption
            val serverContent = root["serverContent"]?.jsonObject
            if (serverContent != null) {
                val interrupted = serverContent["interrupted"]?.jsonPrimitive?.content?.toBoolean() ?: false
                if (interrupted) {
                    Log.d(TAG, "Barge-in detected: User interrupted AI speech.")
                    audioEngine.stopPlaybackAndFlush()
                    _sessionState.value = LiveSessionState.LISTENING
                    return
                }

                // 2. Play incoming audio chunks
                val modelTurn = serverContent["modelTurn"]?.jsonObject
                val parts = modelTurn?.get("parts")?.jsonArray
                parts?.forEach { partElement ->
                    val inlineData = partElement.jsonObject["inlineData"]?.jsonObject
                    if (inlineData != null) {
                        val base64Audio = inlineData["data"]?.jsonPrimitive?.content ?: ""
                        if (base64Audio.isNotEmpty()) {
                            _sessionState.value = LiveSessionState.SPEAKING
                            audioEngine.playAudioChunk(base64Audio)
                        }
                    }

                    // Optional text transcript
                    val transcript = partElement.jsonObject["text"]?.jsonPrimitive?.content
                    if (!transcript.isNullOrBlank()) {
                        _lastRecognizedText.value = transcript
                    }
                }

                val turnComplete = serverContent["turnComplete"]?.jsonPrimitive?.content?.toBoolean() ?: false
                if (turnComplete) {
                    _sessionState.value = LiveSessionState.LISTENING
                }
            }

            // 3. Handle Tool / Function Calls
            val toolCall = root["toolCall"]?.jsonObject
            if (toolCall != null) {
                val functionCalls = toolCall["functionCalls"]?.jsonArray
                functionCalls?.forEach { callElement ->
                    val callObj = callElement.jsonObject
                    val callId = callObj["id"]?.jsonPrimitive?.content ?: "call_1"
                    val functionName = callObj["name"]?.jsonPrimitive?.content ?: ""
                    val args = callObj["args"]?.jsonObject ?: JsonObject(emptyMap())

                    Log.d(TAG, "Executing function call: $functionName with args $args")
                    val result = onToolCallRequest(functionName, args)

                    sendToolResponse(callId, functionName, result)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing server message: ${e.message}", e)
        }
    }

    private fun sendToolResponse(callId: String, name: String, responseData: JsonObject) {
        val payload = buildJsonObject {
            putJsonObject("toolResponse") {
                putJsonArray("functionResponses") {
                    addJsonObject {
                        put("id", callId)
                        put("name", name)
                        putJsonObject("response") {
                            put("result", responseData)
                        }
                    }
                }
            }
        }
        webSocket?.send(payload.toString())
    }

    fun disconnect() {
        audioEngine.stopRecording()
        audioEngine.releasePlayback()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        activeScope = null
        activePersonality = null
        _sessionState.value = LiveSessionState.IDLE
    }
}
