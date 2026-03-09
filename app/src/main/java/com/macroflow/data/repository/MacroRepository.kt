package com.macroflow.data.repository

import com.macroflow.data.database.MacroDao
import com.macroflow.data.model.MacroRecording
import kotlinx.coroutines.flow.Flow

class MacroRepository(private val dao: MacroDao) {

    val allRecordings: Flow<List<MacroRecording>> = dao.getAllRecordings()

    suspend fun insert(recording: MacroRecording) {
        dao.insertRecording(recording)
    }

    suspend fun delete(recording: MacroRecording) {
        dao.deleteRecording(recording)
    }

    suspend fun getById(id: Int): MacroRecording? {
        return dao.getRecordingById(id)
    }
}
