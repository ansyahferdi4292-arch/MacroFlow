package com.macroflow.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.macroflow.R
import com.macroflow.databinding.ActivityMainBinding
import com.macroflow.service.FloatingWindowService
import com.macroflow.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStatus()
    }

    private fun setupObservers() {
        viewModel.isAccessibilityEnabled.observe(this) { isEnabled ->
            if (isEnabled) {
                binding.tvAccessibilityStatus.text = getString(R.string.status_active)
                binding.tvAccessibilityStatus.setTextColor(getColor(R.color.color_status_active))
            } else {
                binding.tvAccessibilityStatus.text = getString(R.string.status_inactive)
                binding.tvAccessibilityStatus.setTextColor(getColor(R.color.color_status_inactive))
            }
        }

        viewModel.isFloatingServiceRunning.observe(this) { isGranted ->
            if (isGranted) {
                binding.tvFloatingStatus.text = getString(R.string.status_active)
                binding.tvFloatingStatus.setTextColor(getColor(R.color.color_status_active))
            } else {
                binding.tvFloatingStatus.text = getString(R.string.status_inactive)
                binding.tvFloatingStatus.setTextColor(getColor(R.color.color_status_inactive))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnActivate.setOnClickListener {
            handleActivateButton()
        }
    }

    private fun handleActivateButton() {
        // Step 1: Check overlay permission
        if (!PermissionHelper.isOverlayPermissionGranted(this)) {
            PermissionHelper.openOverlaySettings(this)
            return
        }

        // Step 2: Check accessibility service
        if (!PermissionHelper.isAccessibilityServiceEnabled(this)) {
            PermissionHelper.openAccessibilitySettings(this)
            return
        }

        // Step 3: Start floating service
        val intent = Intent(this, FloatingWindowService::class.java)
        startForegroundService(intent)
    }
}
