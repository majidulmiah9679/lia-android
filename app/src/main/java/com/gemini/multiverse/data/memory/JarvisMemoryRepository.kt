package com.gemini.multiverse.data.memory

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Jarvis Persistent Memory Vault:
 * Stores long-term user facts, preferences, past requests, habits, and relationship notes.
 * Automatically formatted and injected into Gemini Live & Chat session system instructions.
 */
class JarvisMemoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _memoriesFlow = MutableStateFlow<Map<String, String>>(loadAllMemories())
    val memoriesFlow: StateFlow<Map<String, String>> = _memoriesFlow.asStateFlow()

    init {
        // Initialize default core profile memory if empty
        if (!prefs.contains(KEY_MEMORY_JSON)) {
            val defaults = mapOf(
                "user_name" to "Mike Wheeler",
                "assistant_role" to "Jarvis female real-time companion and screen controller",
                "preferred_language" to "Bengali and English (bilingual, natural human flow)",
                "voice_preference" to "Female melodious voice",
                "screen_control" to "Full permission to click, scroll, navigate apps, and adjust settings"
            )
            saveMemoriesMap(defaults)
        }
    }

    private fun loadAllMemories(): Map<String, String> {
        val jsonStr = prefs.getString(KEY_MEMORY_JSON, null) ?: return emptyMap()
        return try {
            val json = JSONObject(jsonStr)
            val result = mutableMapOf<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = json.getString(k)
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveMemoriesMap(map: Map<String, String>) {
        try {
            val json = JSONObject()
            for ((k, v) in map) {
                json.put(k, v)
            }
            prefs.edit().putString(KEY_MEMORY_JSON, json.toString()).apply()
            _memoriesFlow.value = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveMemory(key: String, value: String) {
        val current = _memoriesFlow.value.toMutableMap()
        current[key.trim()] = value.trim()
        saveMemoriesMap(current)
    }

    fun removeMemory(key: String) {
        val current = _memoriesFlow.value.toMutableMap()
        current.remove(key.trim())
        saveMemoriesMap(current)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_MEMORY_JSON).apply()
        _memoriesFlow.value = emptyMap()
    }

    /**
     * Compiles all long-term memories into a prompt block for Gemini.
     */
    fun compileMemoryPrompt(): String {
        val memories = _memoriesFlow.value
        if (memories.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("\n=== JARVIS PERMANENT MEMORY VAULT ===\n")
        sb.append("You remember everything the user has ever shared with you:\n")
        for ((k, v) in memories) {
            sb.append("- ").append(k.replace('_', ' ').replaceFirstChar { it.uppercase() }).append(": ").append(v).append("\n")
        }
        sb.append("Always honor and apply these personal memories naturally in your conversations.\n")
        return sb.toString()
    }

    companion object {
        private const val PREFS_NAME = "jarvis_longterm_memory_prefs"
        private const val KEY_MEMORY_JSON = "key_jarvis_memories_json"
    }
}
