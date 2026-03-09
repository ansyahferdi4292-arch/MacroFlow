package com.macroflow.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.macroflow.data.database.MacroDatabase
import com.macroflow.data.model.MacroRecording
import com.macroflow.data.repository.MacroRepository
import kotlinx.coroutines.launch

class MacroListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MacroRepository

    val recordings: LiveData<List<MacroRecording>>

    init {
        val dao = MacroDatabase.getInstance(application).macroDao()
        repository = MacroRepository(dao)
        recordings = repository.allRecordings.asLiveData()
    }

    fun deleteRecording(recording: MacroRecording) {
        viewModelScope.launch {
            repository.delete(recording)
        }
    }
}
