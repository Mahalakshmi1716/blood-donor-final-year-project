import os

app_path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\MyApplication.kt"

content = """package com.example.blood_donor

import android.app.Application
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("BLOOD_DONOR_CRASH", "FATAL CRASH on thread ${thread.name}", throwable)
            // Call default to let it actually crash
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
"""

with open(app_path, "w", encoding="utf-8") as f:
    f.write(content)

manifest_path = r"d:\blood_donor\blood_donor_android\app\src\main\AndroidManifest.xml"
with open(manifest_path, "r", encoding="utf-8") as f:
    manifest = f.read()

if "android:name=\".MyApplication\"" not in manifest:
    manifest = manifest.replace("<application", "<application android:name=\".MyApplication\"")
    with open(manifest_path, "w", encoding="utf-8") as f:
        f.write(manifest)
