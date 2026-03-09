package com.macroflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.macroflow.R
import com.macroflow.ui.floating.FloatingMenuView

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingIconView: View? = null
    private var floatingMenuView: FloatingMenuView? = null
    private var isMenuVisible = false

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        showFloatingIcon()
    }

    private fun showFloatingIcon() {
        val themedContext = ContextThemeWrapper(this, R.style.Theme_MacroFlow)
        val inflater = LayoutInflater.from(themedContext)
        floatingIconView = inflater.inflate(R.layout.layout_floating_icon, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        var isDragging = false

        floatingIconView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (!isDragging && (Math.abs(dx) > 5 || Math.abs(dy) > 5)) {
                        isDragging = true
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(floatingIconView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        if (isMenuVisible) {
                            hideMenu()
                        } else {
                            showMenu(params.x, params.y)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingIconView, params)
    }

    private fun showMenu(iconX: Int, iconY: Int) {
        if (isMenuVisible) return
        isMenuVisible = true

        floatingMenuView = FloatingMenuView(this, windowManager) {
            isMenuVisible = false
        }
        floatingMenuView?.show(iconX, iconY)
    }

    private fun hideMenu() {
        floatingMenuView?.dismiss()
        floatingMenuView = null
        isMenuVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingIconView?.let {
            windowManager.removeView(it)
        }
        floatingMenuView?.dismiss()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
