package com.example.blood_donor.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.blood_donor.MainActivity
import com.example.blood_donor.data.AlertDto
import com.example.blood_donor.data.dataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

object NotificationHelper {
    private const val CHANNEL_CRITICAL_ID = "critical_sos_alerts"
    private const val CHANNEL_STANDARD_ID = "standard_sos_alerts"

    fun createNotificationChannels(context: Context) {
        val lang = runBlocking {
            val key = androidx.datastore.preferences.core.stringPreferencesKey("preferred_language")
            context.dataStore.data.firstOrNull()?.get(key) ?: "en"
        }
        fun t(key: String): String {
            return com.example.blood_donor.ui.utils.LocalizedStrings.get(key, lang)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Critical Channel (High Importance, Bypasses DND, Vibrate, Sound)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ID,
                t("critical_sos_alerts_channel"),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = t("critical_sos_desc")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000) // Aggressive vibration
                setBypassDnd(true)
            }

            // Standard Channel (Default Importance)
            val standardChannel = NotificationChannel(
                CHANNEL_STANDARD_ID,
                t("standard_sos_alerts_channel"),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = t("standard_sos_desc")
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(standardChannel)
        }
    }

    fun showEmergencyNotification(context: Context, alert: AlertDto) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val lang = runBlocking {
            val key = androidx.datastore.preferences.core.stringPreferencesKey("preferred_language")
            context.dataStore.data.firstOrNull()?.get(key) ?: "en"
        }
        fun t(key: String): String {
            return com.example.blood_donor.ui.utils.LocalizedStrings.get(key, lang)
        }

        val isCritical = alert.urgency.equals("Critical", ignoreCase = true)
        val channelId = if (isCritical) CHANNEL_CRITICAL_ID else CHANNEL_STANDARD_ID

        // Intent to open the app (specifically the Alerts Tab if we pass extras, but for now just open app)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            alert.id ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isCritical) {
            String.format(t("urgent_blood_needed"), alert.bloodGroup)
        } else {
            String.format(t("blood_request_title"), alert.bloodGroup)
        }
        val contentText = String.format(t("units_needed_at_hospital"), alert.unitsRequired, alert.hospitalName)

        val builder = NotificationCompat.Builder(context, channelId)
            // Ideally use an actual drawable here, using a generic system icon as fallback
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        if (isCritical) {
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
        }

        notificationManager.notify(alert.id ?: System.currentTimeMillis().toInt(), builder.build())
    }
}
