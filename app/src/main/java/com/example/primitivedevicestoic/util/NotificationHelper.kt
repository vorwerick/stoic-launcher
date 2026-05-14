package com.example.primitivedevicestoic.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.primitivedevicestoic.MainActivity
import com.example.primitivedevicestoic.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "evening_reminder_channel"
        private const val NOTIFICATION_ID = 1001
    }

    fun showEveningReminder(screenTimeMinutes: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Evening Reflection",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to reflect on your day and phone usage"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val hours = screenTimeMinutes / 60
        val minutes = screenTimeMinutes % 60
        val screenTimeText = if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using system icon for now
            .setContentTitle("Večerní retrospektiva")
            .setContentText("Dnes jsi na telefonu strávil $screenTimeText. Čas na reflexi.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
