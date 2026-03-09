package com.macroflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macro_recordings")
data class MacroRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val actions: List<MacroAction> = emptyList(),
    val totalDuration: Long = 0L
)
