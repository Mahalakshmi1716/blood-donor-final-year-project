package com.example.blood_donor

import android.app.Application
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure osmdroid before any MapView is created
        val config = org.osmdroid.config.Configuration.getInstance()

        // Load osmdroid preferences first
        config.load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        // Then set our application's User-Agent
        config.userAgentValue =
            "SmartBloodDonorFinder/1.0 (Android Native; com.example.blood_donor)"

        // Crash logging
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(
                "BLOOD_DONOR_CRASH",
                "FATAL CRASH on thread ${thread.name}",
                throwable
            )

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}