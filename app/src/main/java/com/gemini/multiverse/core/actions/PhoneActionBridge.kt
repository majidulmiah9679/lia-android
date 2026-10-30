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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URLEncoder

/**
 * Handles device actions invoked by Gemini Live Tool Calls.
 * Supports: 'YouTube kholo', 'Mummy ko call karo', 'Rahul ko WhatsApp karo', flashlight, etc.
 */
class PhoneActionBridge(
    private val context: Context,
    private val onForgeTriggered: ((prompt: String, title: String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "PhoneActionBridge"
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    suspend fun executeTool(name: String, args: JsonObject): JsonObject {
        Log.d(TAG, "Executing tool: $name with args: $args")
        return when (name) {
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
            "spotify" to "com.spotify.music"
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
                    put("message", "$appName khol diya gaya hai.")
                }
            } else {
                buildJsonObject {
                    put("status", "failed")
                    put("message", "$appName ko launch nahi kiya ja saka.")
                }
            }
        } else {
            buildJsonObject {
                put("status", "not_found")
                put("message", "$appName phone mein installed nahi mila.")
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
            // Target is likely a contact name (e.g. "Mummy", "Rahul")
            phoneNumber = lookupContactPhoneNumber(target) ?: ""
        }

        if (phoneNumber.isBlank()) {
            return buildJsonObject {
                put("status", "contact_not_found")
                put("message", "$target ka phone number contact list mein nahi mila.")
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
            put("message", "$target ($phoneNumber) ko call lagayi ja rahi hai.")
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
                put("message", "$contactName ke liye WhatsApp message tayar kar diya gaya hai.")
            }
        } catch (e: Exception) {
            return buildJsonObject {
                put("status", "failed")
                put("message", "WhatsApp launch karne mein error: ${e.message}")
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
                    put("message", if (enabled) "Flashlight on kar di gayi hai." else "Flashlight off kar di gayi hai.")
                }
            }
            return buildJsonObject {
                put("status", "failed")
                put("message", "Flashlight camera sensor nahi mila.")
            }
        } catch (e: Exception) {
            return buildJsonObject {
                put("status", "failed")
                put("message", "Flashlight toggle error: ${e.message}")
            }
        }
    }

    private fun forgeWebsite(prompt: String, title: String): JsonObject {
        onForgeTriggered?.invoke(prompt, title)
        return buildJsonObject {
            put("status", "success")
            put("message", "3D Website Forge shuru kiya gaya hai prompt: '$prompt' ke sath.")
        }
    }
}
