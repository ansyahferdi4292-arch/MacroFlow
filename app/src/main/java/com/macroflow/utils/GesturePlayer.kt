package com.macroflow.utils

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import com.macroflow.data.model.MacroAction
import com.macroflow.data.model.MacroRecording
import com.macroflow.service.MacroAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object GesturePlayer {

    private const val TAG = "GesturePlayer"
    private const val GESTURE_DURATION_MS = 100L
    private const val SCROLL_DURATION_MS = 300L

    fun play(recording: MacroRecording, scope: CoroutineScope = CoroutineScope(Dispatchers.Main)) {
        val service = MacroAccessibilityService.instance
        if (service == null) {
            Log.e(TAG, "AccessibilityService not available")
            return
        }

        scope.launch {
            Log.d(TAG, "Playing recording: ${recording.name} with ${recording.actions.size} actions")
            for (action in recording.actions) {
                if (action.delayAfter > 0) {
                    delay(action.delayAfter)
                }
                performAction(service, action)
            }
            Log.d(TAG, "Playback finished: ${recording.name}")
        }
    }

    private fun performAction(service: MacroAccessibilityService, action: MacroAction) {
        try {
            when (action.type) {
                MacroAccessibilityService.TYPE_CLICK -> performClick(service, action.x, action.y)
                MacroAccessibilityService.TYPE_SCROLL -> performScroll(
                    service,
                    action.x,
                    action.y,
                    action.scrollDirection,
                    action.scrollDistance ?: 300
                )
                else -> Log.w(TAG, "Unknown action type: ${action.type}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing action: ${action.type}", e)
        }
    }

    private fun performClick(service: MacroAccessibilityService, x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, GESTURE_DURATION_MS)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        service.dispatchGesture(gesture, null, null)
        Log.d(TAG, "Dispatched CLICK at ($x, $y)")
    }

    private fun performScroll(
        service: MacroAccessibilityService,
        x: Float,
        y: Float,
        direction: String?,
        distance: Int
    ) {
        val path = Path()
        when (direction) {
            MacroAccessibilityService.SCROLL_UP -> {
                path.moveTo(x, y - distance / 2f)
                path.lineTo(x, y + distance / 2f)
            }
            MacroAccessibilityService.SCROLL_DOWN -> {
                path.moveTo(x, y + distance / 2f)
                path.lineTo(x, y - distance / 2f)
            }
            else -> {
                Log.w(TAG, "Unknown scroll direction: $direction")
                return
            }
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, SCROLL_DURATION_MS)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        service.dispatchGesture(gesture, null, null)
        Log.d(TAG, "Dispatched SCROLL $direction dist=$distance")
    }
}
