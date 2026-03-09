package com.macroflow.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.macroflow.data.model.MacroRecording
import com.macroflow.databinding.ItemMacroRecordingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MacroListAdapter(
    private val onPlay: (MacroRecording) -> Unit,
    private val onDelete: (MacroRecording) -> Unit
) : ListAdapter<MacroRecording, MacroListAdapter.RecordingViewHolder>(DiffCallback) {

    inner class RecordingViewHolder(private val binding: ItemMacroRecordingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recording: MacroRecording) {
            binding.tvRecordingName.text = recording.name

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(recording.createdAt))
            val durationStr = formatDuration(recording.totalDuration)
            binding.tvRecordingInfo.text = "$dateStr • $durationStr • ${recording.actions.size} aksi"

            binding.btnPlay.setOnClickListener { onPlay(recording) }
            binding.btnDelete.setOnClickListener { onDelete(recording) }
        }

        private fun formatDuration(ms: Long): String {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
            return if (seconds >= 60) {
                "${seconds / 60}m ${seconds % 60}s"
            } else {
                "${seconds}s"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding = ItemMacroRecordingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<MacroRecording>() {
        override fun areItemsTheSame(oldItem: MacroRecording, newItem: MacroRecording) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MacroRecording, newItem: MacroRecording) =
            oldItem == newItem
    }
}
