package com.gemini.multiverse.core.actions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.core.agent.LiaVisualAgentService
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URLEncoder

/**
 * Handles device actions invoked by Gemini Live and Chat Tool Calls:
 * 1. Mobile Screen Control A to Z: Click, Scroll, Swipe, Gestures, Back, Home, Recents, Type Text
 * 2. Screen Inspector: Reads all UI text, buttons, and switches on screen
 * 3. Settings Control: Opens and toggles Wi-Fi, Bluetooth, Sound, Display, etc.
 * 4. Permanent Memory: Remembers facts and user details permanently
 * 5. App Launch, Calls, WhatsApp, Flashlight, 3D Forge
 */
class PhoneActionBridge(
    private val context: Context,
    private val onForgeTriggered: ((prompt: String, title: String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "PhoneActionBridge"
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val memoryRepo get() = GeminiMultiverseApp.instance.container.jarvisMemoryRepository

    suspend fun executeTool(name: String, args: JsonObject): JsonObject {
        Log.d(TAG, "Executing tool: $name with args: $args")
        return when (name) {
            "control_screen" -> {
                val action = args["action"]?.toString()?.replace("\"", "") ?: "click"
                val target = args["targetText"]?.toString()?.replace("\"", "") ?: ""
                val textToType = args["textToType"]?.toString()?.replace("\"", "") ?: ""
                val x = args["x"]?.toString()?.replace("\"", "")?.toFloatOrNull() ?: -1f
                val y = args["y"]?.toString()?.replace("\"", "")?.toFloatOrNull() ?: -1f
                controlScreen(action, target, textToType, x, y)
            }
            "inspect_screen" -> {
                inspectScreen()
            }
            "change_setting" -> {
                val settingName = args["settingName"]?.toString()?.replace("\"", "") ?: "settings"
                val toggle = args["toggleAction"]?.toString()?.replace("\"", "") ?: "open"
                changeSetting(settingName, toggle)
            }
            "remember_fact" -> {
                val key = args["key"]?.toString()?.replace("\"", "") ?: "note"
                val value = args["value"]?.toString()?.replace("\"", "") ?: ""
                rememberFact(key, value)
            }
            "recall_memories" -> {
                recallMemories()
            }
            "open_app" -> {
                val appName = args["appName"]?.toString()?.replace("\"", "") ?: ""
                openApp(appName)
            }
            "make_call" -> {
                val target = args["contactNameOrNumber"]?.toString()?.replace("\"", "") ?: ""
                makeCall(target)
            }
            "send_whatsapp" -> {
                val contact = args["contactName"]?.toString()?.replace("\"", "") ?: ""
                val message = args["message"]?.toString()?.replace("\"", "") ?: ""
                sendWhatsAppMessage(contact, message)
            }
            "toggle_flashlight" -> {
                val enabled = args["enabled"]?.toString()?.replace("\"", "")?.toBoolean() ?: true
                toggleFlashlight(enabled)
            }
            "forge_3d_website" -> {
                val prompt = args["prompt"]?.toString()?.replace("\"", "") ?: ""
                val title = args["title"]?.toString()?.replace("\"", "") ?: "3D Website"
                forgeWebsite(prompt, title)
            }
            else -> {
                buildJsonObject {
                    put("status", "unsupported")
                    put("message", "Tool $name is not supported on this device.")
                }
            }
        }
    }

    // ==========================================
    // SCREEN CONTROL (CLICK, SCROLL, NAV, TYPE)
    // ==========================================

    private fun controlScreen(
        action: String,
        target: String,
        textToType: String,
        x: Float,
        y: Float
    ): JsonObject {
        val agent = LiaVisualAgentService.instance
        if (agent == null) {
            return buildJsonObject {
                put("status", "permission_needed")
                put("message", "Accessibility Service চালু নেই। দয়া করে সেটিংস থেকে Lia/Jarvis Accessibility অন করুন।")
            }
        }

        val success: Boolean
        val message: String

        when (action.lowercase().trim()) {
            "back", "home", "recents", "notifications", "quick_settings", "lock", "screenshot" -> {
                success = agent.performSystemNav(action)
                message = if (success) "System navigation '$action' সফলভাবে সম্পন্ন হয়েছে।" else "System navigation '$action' ব্যর্থ হয়েছে।"
            }
            "click", "click_text" -> {
                success = if (x >= 0 && y >= 0) {
                    agent.clickAtCoordinates(x, y)
                } else {
                    agent.clickButtonByText(target)
                }
                message = if (success) "'$target' বাটনে ক্লিক করা হয়েছে।" else "'$target' বাটন স্ক্রিনে খুঁজে পাওয়া যায়নি।"
            }
            "scroll_down", "down", "forward" -> {
                success = agent.scroll("down")
                message = if (success) "স্ক্রল ডাউন করা হয়েছে।" else "স্ক্রল করা যায়নি।"
            }
            "scroll_up", "up", "backward" -> {
                success = agent.scroll("up")
                message = if (success) "স্ক্রল আপ করা হয়েছে।" else "স্ক্রল করা যায়নি।"
            }
            "scroll_left", "left" -> {
                success = agent.scroll("left")
                message = if (success) "বামে স্ক্রল করা হয়েছে।" else "বামে স্ক্রল করা যায়নি।"
            }
            "scroll_right", "right" -> {
                success = agent.scroll("right")
                message = if (success) "ডানে স্ক্রল করা হয়েছে।" else "ডানে স্ক্রল করা যায়নি।"
            }
            "type", "input" -> {
                success = agent.setInputText(textToType.ifBlank { target })
                message = if (success) "টেক্সট টাইপ করা হয়েছে: '$textToType'" else "কোনো টেক্সট ইনপুট বক্স পাওয়া যায়নি।"
            }
            else -> {
                success = false
                message = "অজানা অ্যাকশন: $action"
            }
        }

        return buildJsonObject {
            put("status", if (success) "success" else "failed")
            put("message", message)
        }
    }

    private fun inspectScreen(): JsonObject {
        val agent = LiaVisualAgentService.instance
        if (agent == null) {
            return buildJsonObject {
                put("status", "permission_needed")
                put("message", "Accessibility Service সক্রিয় নেই।")
            }
        }
        val screenContent = agent.inspectCurrentScreen()
        return buildJsonObject {
            put("status", "success")
            put("screen_elements", screenContent)
        }
    }

    private fun changeSetting(settingName: String, toggleAction: String): JsonObject {
        val agent = LiaVisualAgentService.instance
        if (agent != null && toggleAction.equals("toggle", true)) {
            val toggled = agent.toggleSettingSwitch(settingName)
            if (toggled) {
                return buildJsonObject {
                    put("status", "success")
                    put("message", "$settingName সেটিং টগল করা হয়েছে।")
                }
            }
        }

        // Open setting page
        val opened = agent?.openSystemSettings(settingName) ?: openSettingFallback(settingName)
        return buildJsonObject {
            put("status", if (opened) "success" else "failed")
            put("message", if (opened) "$settingName সেটিংস স্ক্রিন খোলা হয়েছে।" else "$settingName সেটিংস খোলা যায়নি।")
        }
    }

    private fun openSettingFallback(settingName: String): Boolean {
        return try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // PERMANENT MEMORY VAULT
    // ==========================================

    private fun rememberFact(key: String, value: String): JsonObject {
        memoryRepo.saveMemory(key, value)
        return buildJsonObject {
            put("status", "success")
            put("message", "মনে রেখেছি! '$key': '$value'")
        }
    }

    private fun recallMemories(): JsonObject {
        val prompt = memoryRepo.compileMemoryPrompt()
        return buildJsonObject {
            put("status", "success")
            put("memories", prompt)
        }
    }

    // ==========================================
    // APP & PHONE ACTIONS
    // ==========================================

    private fun openApp(appName: String): JsonObject {
        val pm = context.packageManager
        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "chrome" to "com.android.chrome",
            "camera" to "com.android.camera",
            "maps" to "com.google.android.apps.maps",
            "settings" to "com.android.settings",
            "spotify" to "com.spotify.music",
            "facebook" to "com.facebook.katana",
            "telegram" to "org.telegram.messenger",
            "gmail" to "com.google.android.gm"
        )

        val cleanName = appName.lowercase().trim()
        val pkg = knownPackages[cleanName] ?: findPackageByName(pm, cleanName)

        return if (pkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                buildJsonObject {
                    put("status", "success")
                    put("message", "$appName অ্যাপ খোলা হয়েছে।")
                }
            } else {
                buildJsonObject {
                    put("status", "failed")
                    put("message", "$appName লঞ্চ করা সম্ভব হয়নি।")
                }
            }
        } else {
            buildJsonObject {
                put("status", "not_found")
                put("message", "$appName ফোনে ইনস্টল পাওয়া যায়নি।")
            }
        }
    }

    private fun findPackageByName(pm: PackageManager, name: String): String? {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in apps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(name) || name.contains(label)) {
                return app.packageName
            }
        }
        return null
    }

    @SuppressLint("Range")
    private fun makeCall(target: String): JsonObject {
        var phoneNumber = target.filter { it.isDigit() || it == '+' }
        if (phoneNumber.length < 5) {
            phoneNumber = lookupContactPhoneNumber(target) ?: ""
        }

        if (phoneNumber.isBlank()) {
            return buildJsonObject {
                put("status", "contact_not_found")
                put("message", "$target এর ফোন নম্বর পাওয়া যায়নি।")
            }
        }

        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(dialIntent)

        return buildJsonObject {
            put("status", "success")
            put("phoneNumber", phoneNumber)
            put("message", "$target ($phoneNumber) নম্বরে কল দেওয়া হচ্ছে।")
        }
    }

    private fun lookupContactPhoneNumber(name: String): String? {
        val contentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                return number
            }
        }
        return null
    }

    private fun sendWhatsAppMessage(contactName: String, message: String): JsonObject {
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            return buildJsonObject {
                put("status", "success")
                put("message", "$contactName এর জন্য WhatsApp মেসেজ প্রস্তুত করা হয়েছে।")
            }
        } catch (e: Exception) {
            return buildJsonObject {
                put("status", "failed")
                put("message", "WhatsApp খুলতে সমস্যা: ${e.message}")
            }
        }
    }

    private fun toggleFlashlight(enabled: Boolean): JsonObject {
        try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                cameraManager?.setTorchMode(cameraId, enabled)
                return buildJsonObject {
                    put("status", "success")
                    put("message", if (enabled) "ফ্ল্যাশলাইট অন করা হয়েছে।" else "ফ্ল্যাশলাইট অফ করা হয়েছে।")
                }
            }
            return buildJsonObject {
                put("status", "failed")
                put("message", "ফ্ল্যাশলাইট সেন্সর পাওয়া যায়নি।")
            }
        } catch (e: Exception) {
            return buildJsonObject {
                put("status", "failed")
                put("message", "ফ্ল্যাশলাইট টগল এরর: ${e.message}")
            }
        }
    }

    private fun forgeWebsite(prompt: String, title: String): JsonObject {
        onForgeTriggered?.invoke(prompt, title)
        return buildJsonObject {
            put("status", "success")
            put("message", "3D Website Forge তৈরি শুরু হয়েছে: '$prompt'")
        }
    }
}
