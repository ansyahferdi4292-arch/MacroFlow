package com.macroflow.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macroflow.utils.PermissionHelper

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _isAccessibilityEnabled = MutableLiveData<Boolean>()
    val isAccessibilityEnabled: LiveData<Boolean> = _isAccessibilityEnabled

    private val _isFloatingServiceRunning = MutableLiveData<Boolean>()
    val isFloatingServiceRunning: LiveData<Boolean> = _isFloatingServiceRunning

    fun refreshStatus() {
        val context: Context = getApplication()
        _isAccessibilityEnabled.value = PermissionHelper.isAccessibilityServiceEnabled(context)
        _isFloatingServiceRunning.value = PermissionHelper.isOverlayPermissionGranted(context)
    }
}
