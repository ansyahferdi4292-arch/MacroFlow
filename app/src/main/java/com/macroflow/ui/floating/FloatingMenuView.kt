package com.macroflow.ui.floating

import android.app.AlertDialog
import android.content.Context
import android.graphics.PixelFormat
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import com.macroflow.R
import com.macroflow.databinding.LayoutFloatingMenuBinding
import com.macroflow.service.MacroAccessibilityService
import com.macroflow.ui.list.MacroListBottomSheet

class FloatingMenuView(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onDismiss: () -> Unit
) {

    private var rootView: View? = null
    private lateinit var binding: LayoutFloatingMenuBinding
    private var macroListSheet: MacroListBottomSheet? = null
    private var isRecording = false

    fun show(iconX: Int, iconY: Int) {
        val themedContext = ContextThemeWrapper(context, R.style.Theme_MacroFlow)
        val inflater = LayoutInflater.from(themedContext)
        binding = LayoutFloatingMenuBinding.inflate(inflater)
        rootView = binding.root

        updateRecordButtonState()

        binding.btnStartRecord.setOnClickListener {
            if (MacroAccessibilityService.isRecording) {
                stopRecording()
            } else {
                showNameInputDialog()
            }
        }

        binding.btnListRecordings.setOnClickListener {
            dismiss()
            showMacroList()
        }

        binding.btnCloseMenu.setOnClickListener {
            dismiss()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = iconX + 70
            y = iconY
        }

        windowManager.addView(rootView, params)
    }

    private fun updateRecordButtonState() {
        if (MacroAccessibilityService.isRecording) {
            binding.btnStartRecord.text = context.getString(R.string.btn_stop_record)
            binding.btnStartRecord.backgroundTintList =
                android.content.res.ColorStateList.valueOf(context.getColor(R.color.color_status_inactive))
        } else {
            binding.btnStartRecord.text = context.getString(R.string.btn_start_record)
            binding.btnStartRecord.backgroundTintList =
                android.content.res.ColorStateList.valueOf(context.getColor(R.color.color_record_active))
        }
    }

    private fun showNameInputDialog() {
        val themedContext = ContextThemeWrapper(context, R.style.Theme_MacroFlow)
        val editText = EditText(themedContext).apply {
            hint = context.getString(R.string.hint_recording_name)
            setPadding(40, 20, 40, 20)
        }

        val dialogView = android.widget.LinearLayout(themedContext).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
            addView(editText)
        }

        val dialog = AlertDialog.Builder(themedContext)
            .setTitle(context.getString(R.string.dialog_record_title))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.btn_ok)) { _, _ ->
                val name = editText.text.toString().trim().ifEmpty {
                    "Rekaman ${System.currentTimeMillis()}"
                }
                startRecording(name)
            }
            .setNegativeButton(context.getString(R.string.btn_cancel), null)
            .create()

        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        dialog.show()
    }

    private fun startRecording(name: String) {
        MacroAccessibilityService.startRecording(name)
        dismiss()
    }

    private fun stopRecording() {
        MacroAccessibilityService.stopRecording(context)
        dismiss()
    }

    private fun showMacroList() {
        macroListSheet = MacroListBottomSheet(context, windowManager)
        macroListSheet?.show()
    }

    fun dismiss() {
        rootView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        rootView = null
        onDismiss()
    }
}
