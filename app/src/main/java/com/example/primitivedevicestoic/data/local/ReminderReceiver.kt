package com.example.primitivedevicestoic.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.primitivedevicestoic.domain.repository.UsageRepository
import com.example.primitivedevicestoic.util.NotificationHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: UsageRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleReminder(context)
            return
        }

        if (repository.getReminderEnabled()) {
            val notificationHelper = NotificationHelper(context)
            val screenTime = repository.getScreenTimeMinutes()
            notificationHelper.showEveningReminder(screenTime)
            
            // Re-schedule for next day
            scheduleReminder(context)
        } else {
            // I když není zapnutá "stará" upomínka, nové zadání říká "hodinu před spánkem se zobrazí připomínka"
            // bez explicitní zmínky o toggle v nastavení, ale dává smysl to mít spojené nebo samostatné.
            // Původní toggle budeme ignorovat pro retrospektivu hodinu před spánkem, nebo ho použijeme.
            // Zadání: "hodinu před spánkem se zobrazí připomínka na retrospektivu a pošle se notifikace"
            // Implementujeme to tak, že se pošle vždy, pokud je nastaven čas spánku.
            val notificationHelper = NotificationHelper(context)
            val screenTime = repository.getScreenTimeMinutes()
            notificationHelper.showEveningReminder(screenTime)
            
            scheduleReminder(context)
        }
    }

    fun scheduleReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sleepTimeStr = repository.getSleepTime()
        val parts = sleepTimeStr.split(":")
        val sleepHour = parts.getOrNull(0)?.toIntOrNull() ?: 23
        val sleepMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, sleepHour)
            set(Calendar.MINUTE, sleepMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.HOUR_OF_DAY, -1) // Hodinu před spánkem

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback to non-exact alarm if permission not granted
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
