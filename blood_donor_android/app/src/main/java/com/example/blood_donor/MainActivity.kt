package com.example.blood_donor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.blood_donor.data.TokenManager
import com.example.blood_donor.network.RetrofitClient
import com.example.blood_donor.ui.navigation.AppNavigation
import com.example.blood_donor.ui.theme.BloodDonorAndroidTheme
import com.example.blood_donor.utils.NotificationHelper

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Notifications
        NotificationHelper.createNotificationChannels(this)
        askNotificationPermission()
        
        // Initialize Networking and DataStore
        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.initialize(tokenManager)
        
        enableEdgeToEdge()
        setContent {
            BloodDonorAndroidTheme {
                AppNavigation()
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}