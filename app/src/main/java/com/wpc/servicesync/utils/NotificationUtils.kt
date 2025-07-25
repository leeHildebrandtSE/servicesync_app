package com.wpc.servicesync.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wpc.servicesync.R
import com.wpc.servicesync.activities.MainActivity
import com.wpc.servicesync.models.ServiceSession

object NotificationUtils {

    private const val CHANNEL_ID_SERVICE = "service_notifications"
    private const val CHANNEL_ID_ALERTS = "alert_notifications"
    private const val CHANNEL_ID_PROGRESS = "progress_notifications"

    private const val NOTIFICATION_ID_SERVICE_ACTIVE = 1001
    private const val NOTIFICATION_ID_NURSE_ALERT = 1002
    private const val NOTIFICATION_ID_SERVICE_COMPLETE = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Service Status",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about meal delivery service status"
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Urgent Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important alerts and notifications"
            }

            val progressChannel = NotificationChannel(
                CHANNEL_ID_PROGRESS,
                "Service Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progress updates during meal service"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(progressChannel)
        }
    }

    fun showServiceActiveNotification(context: Context, session: ServiceSession) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("ServiceSync Active")
            .setContentText("Meal delivery in progress - Ward ${session.wardNumber}")
            .setSubText("${session.mealsServed}/${session.mealCount} meals served")
            .setProgress(session.mealCount, session.mealsServed, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SERVICE_ACTIVE, notification)
    }

    fun showNurseAlertNotification(context: Context, session: ServiceSession) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Nurse Alert Sent")
            .setContentText("Diet nurse notified for Ward ${session.wardNumber}")
            .setSubText("${session.mealCount} ${session.mealType.displayName} meals")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NURSE_ALERT, notification)
    }

    fun showServiceCompleteNotification(context: Context, session: ServiceSession) {
        val completionRate = session.getCompletionRate()
        val duration = TimerUtils.formatTime(session.getElapsedTime())

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Service Complete")
            .setContentText("Ward ${session.wardNumber} - ${session.mealsServed} meals served")
            .setSubText("Duration: $duration | Completion: ${String.format("%.1f", completionRate)}%")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SERVICE_COMPLETE, notification)

        // Clear the ongoing service notification
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_SERVICE_ACTIVE)
    }

    fun updateServiceProgressNotification(context: Context, session: ServiceSession) {
        if (session.isActive && !session.isCompleted) {
            showServiceActiveNotification(context, session)
        }
    }

    fun clearAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun showWarningNotification(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    fun showSessionWarnings(context: Context, session: ServiceSession) {
        val warnings = session.getWarnings()
        if (warnings.isNotEmpty()) {
            val warningText = warnings.joinToString(", ")
            showWarningNotification(
                context,
                "Service Warnings",
                "Ward ${session.wardNumber}: $warningText"
            )
        }
    }

    // Check if notifications are enabled
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // Request notification permission (Android 13+)
    fun shouldRequestNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !areNotificationsEnabled(context)
    }
}