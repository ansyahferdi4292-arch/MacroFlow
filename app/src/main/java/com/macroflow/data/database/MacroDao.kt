package com.macroflow.data.database

import androidx.room.*
import com.macroflow.data.model.MacroRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: MacroRecording)

    @Delete
    suspend fun deleteRecording(recording: MacroRecording)

    @Query("SELECT * FROM macro_recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<MacroRecording>>

    @Query("SELECT * FROM macro_recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingById(id: Int): MacroRecording?
}
