package com.macroflow.ui.list

import android.content.Context
import android.graphics.PixelFormat
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.macroflow.R
import com.macroflow.data.database.MacroDatabase
import com.macroflow.data.model.MacroRecording
import com.macroflow.data.repository.MacroRepository
import com.macroflow.databinding.LayoutBottomSheetMacrolistBinding
import com.macroflow.utils.GesturePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MacroListBottomSheet(
    private val context: Context,
    private val windowManager: WindowManager
) {

    private var rootView: View? = null
    private lateinit var binding: LayoutBottomSheetMacrolistBinding
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val adapter = MacroListAdapter(
        onPlay = { recording ->
            GesturePlayer.play(recording, CoroutineScope(Dispatchers.Main))
            dismiss()
        },
        onDelete = { recording ->
            deleteRecording(recording)
        }
    )

    fun show() {
        val themedContext = ContextThemeWrapper(context, R.style.Theme_MacroFlow)
        val inflater = LayoutInflater.from(themedContext)
        binding = LayoutBottomSheetMacrolistBinding.inflate(inflater)
        rootView = binding.root

        binding.rvRecordings.adapter = adapter

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        rootView?.setOnTouchListener { _, _ -> false }

        windowManager.addView(rootView, params)

        observeRecordings()
    }

    private fun observeRecordings() {
        val db = MacroDatabase.getInstance(context)
        val repo = MacroRepository(db.macroDao())

        scope.launch {
            repo.allRecordings.collect { recordings ->
                adapter.submitList(recordings)
                binding.tvEmptyRecordings.visibility =
                    if (recordings.isEmpty()) View.VISIBLE else View.GONE
                binding.rvRecordings.visibility =
                    if (recordings.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun deleteRecording(recording: MacroRecording) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = MacroDatabase.getInstance(context)
            val repo = MacroRepository(db.macroDao())
            repo.delete(recording)
        }
    }

    fun dismiss() {
        scope.cancel()
        rootView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        rootView = null
    }
}
