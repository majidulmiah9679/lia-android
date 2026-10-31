package com.gemini.multiverse.core.agent

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Jarvis / Lia Visual Screen Agent:
 * Complete on-screen mobile controller via Android AccessibilityService:
 * - A to Z app interaction: Click, Scroll, Swipe, Gestures, Type Text
 * - System navigation: Back, Home, Recents, Notifications, Quick Settings
 * - Screen Inspector: Reads visible screen hierarchy, buttons, toggles, text
 * - System Settings automation: Navigates and toggles phone settings
 */
class LiaVisualAgentService : AccessibilityService() {

    companion object {
        private const val TAG = "JarvisVisualAgent"
        var instance: LiaVisualAgentService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Jarvis Visual Screen Agent connected and active.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitors window state and transitions
    }

    override fun onInterrupt() {
        Log.w(TAG, "Jarvis Visual Screen Agent interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    // ==========================================
    // 1. GLOBAL SYSTEM NAVIGATION (Back, Home, Recents...)
    // ==========================================

    fun performSystemNav(action: String): Boolean {
        return when (action.lowercase().trim()) {
            "back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            "recents", "recent_apps" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            "notifications", "notification_shade" -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            "quick_settings" -> performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            "lock", "lock_screen" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } else {
                    false
                }
            }
            "power", "power_dialog" -> performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            "screenshot" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                } else {
                    false
                }
            }
            else -> false
        }
    }

    // ==========================================
    // 2. CLICK AUTOMATION (Text, ID, Coordinates)
    // ==========================================

    fun clickButtonByText(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val cleanTarget = targetText.trim().lowercase()

        // 1. Try finding nodes by text directly
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        for (node in nodes) {
            if (tryClickNodeOrParent(node)) {
                Log.d(TAG, "Clicked node matching text: $targetText")
                return true
            }
        }

        // 2. Fuzzy recursive search by text or contentDescription
        val matchedNode = findNodeFuzzy(rootNode, cleanTarget)
        if (matchedNode != null && tryClickNodeOrParent(matchedNode)) {
            Log.d(TAG, "Clicked fuzzy matched node for: $targetText")
            return true
        }

        return false
    }

    fun clickById(viewId: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
        for (node in nodes) {
            if (tryClickNodeOrParent(node)) {
                Log.d(TAG, "Clicked node by ID: $viewId")
                return true
            }
        }
        return false
    }

    private fun tryClickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true
        }
        var current: AccessibilityNodeInfo? = node.parent
        var depth = 0
        while (current != null && depth < 4) {
            if (current.isClickable && current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
            current = current.parent
            depth++
        }
        // If not clickable via action, click its center coordinates via gesture!
        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.width() > 0 && rect.height() > 0) {
            return clickAtCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
        }
        return false
    }

    private fun findNodeFuzzy(node: AccessibilityNodeInfo, targetText: String): AccessibilityNodeInfo? {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        if (text.contains(targetText) || desc.contains(targetText)) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeFuzzy(child, targetText)
            if (found != null) return found
        }
        return null
    }

    fun clickAtCoordinates(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        var completed = false
        val dispatched = dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                completed = true
                Log.d(TAG, "Gesture click completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Gesture click cancelled at ($x, $y)")
            }
        }, null)
        return dispatched
    }

    // ==========================================
    // 3. SCROLL & SWIPE GESTURES
    // ==========================================

    fun scroll(direction: String): Boolean {
        val rootNode = rootInActiveWindow
        val isDown = direction.equals("down", true) || direction.equals("forward", true)
        val isUp = direction.equals("up", true) || direction.equals("backward", true)

        // Try standard scroll action on scrollable container first
        if (rootNode != null) {
            val scrollableNode = findScrollableNode(rootNode)
            if (scrollableNode != null) {
                val action = if (isDown) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                if (scrollableNode.performAction(action)) {
                    Log.d(TAG, "Scrolled container via accessibility action: $direction")
                    return true
                }
            }
        }

        // Gesture fallback: Swipe up/down across screen center
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()
        val midX = width / 2f

        return when {
            isDown -> swipe(midX, height * 0.75f, midX, height * 0.25f, 350) // scroll down = drag up
            isUp -> swipe(midX, height * 0.25f, midX, height * 0.75f, 350)   // scroll up = drag down
            direction.equals("left", true) -> swipe(width * 0.85f, height * 0.5f, width * 0.15f, height * 0.5f, 350)
            direction.equals("right", true) -> swipe(width * 0.15f, height * 0.5f, width * 0.85f, height * 0.5f, 350)
            else -> false
        }
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }

    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    // ==========================================
    // 4. TEXT ENTRY
    // ==========================================

    fun setInputText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focused = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }

        // If no focus, find first editable node
        val editableNode = findEditableNode(rootNode)
        if (editableNode != null) {
            editableNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    // ==========================================
    // 5. SCREEN INSPECTOR (Screen Understanding)
    // ==========================================

    fun inspectCurrentScreen(): String {
        val rootNode = rootInActiveWindow ?: return "Screen is currently unavailable or empty."
        val elements = mutableListOf<String>()
        extractScreenElements(rootNode, elements, 0, 40)
        return if (elements.isEmpty()) {
            "Screen active, but no readable text or buttons were detected."
        } else {
            "CURRENT SCREEN ELEMENTS:\n" + elements.joinToString("\n")
        }
    }

    private fun extractScreenElements(
        node: AccessibilityNodeInfo,
        outList: MutableList<String>,
        depth: Int,
        maxCount: Int
    ) {
        if (outList.size >= maxCount) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        val viewId = node.viewIdResourceName?.substringAfter(":id/")

        val label = when {
            !text.isNullOrBlank() -> text
            !desc.isNullOrBlank() -> "[$desc]"
            else -> null
        }

        if (label != null) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            val typeStr = when {
                node.isCheckable -> "Switch(checked=${node.isChecked})"
                node.isClickable -> "Button"
                node.isEditable -> "InputField"
                else -> "Text"
            }
            val idInfo = if (viewId != null) " id='$viewId'" else ""
            outList.add("- $typeStr: \"$label\"$idInfo pos=(${rect.centerX()},${rect.centerY()})")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractScreenElements(child, outList, depth + 1, maxCount)
        }
    }

    // ==========================================
    // 6. SETTINGS & APP CONTROL
    // ==========================================

    fun openSystemSettings(category: String): Boolean {
        val intentAction = when (category.lowercase().trim()) {
            "wifi", "wi-fi", "internet" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound", "volume", "audio" -> Settings.ACTION_SOUND_SETTINGS
            "display", "brightness", "screen" -> Settings.ACTION_DISPLAY_SETTINGS
            "apps", "applications" -> Settings.ACTION_APPLICATION_SETTINGS
            "battery", "power" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "date", "time", "clock" -> Settings.ACTION_DATE_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "location", "gps" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(intentAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings: $category", e)
            false
        }
    }

    fun toggleSettingSwitch(settingName: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val cleanName = settingName.lowercase().trim()

        // 1. Look for text with setting name
        val matchedNode = findNodeFuzzy(rootNode, cleanName)
        if (matchedNode != null) {
            // Check if node itself is checkable or clickable
            if (matchedNode.isCheckable || matchedNode.isClickable) {
                return matchedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }

            // Check parent container for a Switch or Checkable child
            val parent = matchedNode.parent
            if (parent != null) {
                for (i in 0 until parent.childCount) {
                    val sibling = parent.getChild(i) ?: continue
                    if (sibling.isCheckable) {
                        return sibling.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                if (parent.isClickable) {
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }
}
