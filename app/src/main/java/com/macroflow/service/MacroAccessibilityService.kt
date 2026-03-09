package com.macroflow.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.macroflow.data.database.MacroDatabase
import com.macroflow.data.model.MacroAction
import com.macroflow.data.model.MacroRecording
import com.macroflow.data.repository.MacroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MacroAccessibilityService : AccessibilityService() {

    // Tracks the last known scroll Y per view key to detect direction on API < 28
    private val scrollPositions = HashMap<String, Int>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "MacroAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isRecording || event == null) return

        val now = System.currentTimeMillis()
        val delay = if (lastEventTime == 0L) 0L else now - lastEventTime
        lastEventTime = now

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val source = event.source ?: return
                val rect = android.graphics.Rect()
                source.getBoundsInScreen(rect)
                val x = rect.exactCenterX()
                val y = rect.exactCenterY()
                source.recycle()

                if (x > 0 && y > 0) {
                    recordedActions.add(
                        MacroAction(
                            id = recordedActions.size,
                            type = TYPE_CLICK,
                            x = x,
                            y = y,
                            delayAfter = delay
                        )
                    )
                    Log.d(TAG, "Recorded CLICK at ($x, $y) delay=$delay")
                }
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                val direction: String
                val distance: Int

                // On API 28+, use scrollDeltaY for precise delta. If it is 0 (e.g. overscroll
                // bounce), fall through to position-tracking as well.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && event.scrollDeltaY != 0) {
                    direction = if (event.scrollDeltaY > 0) SCROLL_DOWN else SCROLL_UP
                    distance = kotlin.math.abs(event.scrollDeltaY) * SCROLL_MULTIPLIER
                } else {
                    // Resolve source before accessing scrollY, then recycle immediately
                    val source = event.source
                    val currentScrollY: Int
                    val windowId: Int
                    if (source != null) {
                        currentScrollY = source.scrollY
                        windowId = source.windowId
                        source.recycle()
                    } else {
                        currentScrollY = -1
                        windowId = -1
                    }

                    // Include window ID to distinguish same-class views in different windows/screens
                    val key = "${event.packageName}/${event.className}/$windowId"
                    val prevScrollY = scrollPositions[key] ?: -1
                    scrollPositions[key] = currentScrollY

                    if (prevScrollY < 0 || currentScrollY < 0 || currentScrollY == prevScrollY) {
                        return
                    }
                    direction = if (currentScrollY > prevScrollY) SCROLL_DOWN else SCROLL_UP
                    distance = kotlin.math.abs(currentScrollY - prevScrollY) * SCROLL_MULTIPLIER
                }

                // Use display center as approximate scroll position
                val displayMetrics = resources.displayMetrics
                val cx = displayMetrics.widthPixels / 2f
                val cy = displayMetrics.heightPixels / 2f

                recordedActions.add(
                    MacroAction(
                        id = recordedActions.size,
                        type = TYPE_SCROLL,
                        x = cx,
                        y = cy,
                        scrollDirection = direction,
                        scrollDistance = distance,
                        delayAfter = delay
                    )
                )
                Log.d(TAG, "Recorded SCROLL $direction dist=$distance delay=$delay")
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "MacroAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollPositions.clear()
        instance = null
    }

    companion object {
        private const val TAG = "MacroAccessibility"
        const val TYPE_CLICK = "CLICK"
        const val TYPE_SCROLL = "SCROLL"
        const val SCROLL_UP = "UP"
        const val SCROLL_DOWN = "DOWN"
        private const val SCROLL_MULTIPLIER = 5

        @Volatile
        var instance: MacroAccessibilityService? = null
            private set

        var isRecording = false
            private set

        private val recordedActions = mutableListOf<MacroAction>()
        private var recordingName = ""
        private var recordingStartTime = 0L
        private var lastEventTime = 0L

        fun startRecording(name: String) {
            recordedActions.clear()
            recordingName = name
            recordingStartTime = System.currentTimeMillis()
            lastEventTime = 0L
            isRecording = true
            Log.d(TAG, "Recording started: $name")
        }

        fun stopRecording(context: Context) {
            if (!isRecording) return
            isRecording = false
            val totalDuration = System.currentTimeMillis() - recordingStartTime
            val actions = recordedActions.toList()
            val name = recordingName

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = MacroDatabase.getInstance(context)
                    val repo = MacroRepository(db.macroDao())
                    repo.insert(
                        MacroRecording(
                            name = name,
                            createdAt = System.currentTimeMillis(),
                            actions = actions,
                            totalDuration = totalDuration
                        )
                    )
                    Log.d(TAG, "Recording saved: $name with ${actions.size} actions")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save recording", e)
                }
            }

            recordedActions.clear()
            recordingName = ""
            lastEventTime = 0L
        }
    }
}
