package com.gemini.multiverse.core.agent

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Lia Visual Agent (Direct Tier):
 * Reads on-screen accessibility tree and executes automated button presses and text entry
 * across Instagram, WhatsApp, Facebook, and system apps.
 */
class LiaVisualAgentService : AccessibilityService() {

    companion object {
        private const val TAG = "LiaVisualAgent"
        var instance: LiaVisualAgentService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Lia Visual Agent connected and ready for on-screen actions.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitors app transitions and active window updates
    }

    override fun onInterrupt() {
        Log.w(TAG, "Lia Visual Agent interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Finds and clicks a button by visible text across the on-screen app.
     */
    fun clickButtonByText(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        for (node in nodes) {
            if (node.isClickable) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Clicked node by text '$targetText': $success")
                return success
            }
            // Check parent if container is clickable
            node.parent?.let { parent ->
                if (parent.isClickable) {
                    val success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "Clicked parent of '$targetText': $success")
                    return success
                }
            }
        }
        return false
    }

    /**
     * Types text into the focused or first editable input field.
     */
    fun setInputText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focused = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }
}
