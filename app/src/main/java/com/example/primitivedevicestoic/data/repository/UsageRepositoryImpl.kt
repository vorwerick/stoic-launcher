package com.example.primitivedevicestoic.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.primitivedevicestoic.domain.model.AppInfo
import com.example.primitivedevicestoic.domain.model.UnlockEvent
import com.example.primitivedevicestoic.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class UsageRepositoryImpl(
    private val context: Context,
    private val prefs: SharedPreferences
) : UsageRepository {

    private val _unlockEvents = MutableStateFlow<List<UnlockEvent>>(emptyList())
    private val _selectedApps = MutableStateFlow<List<String>>(emptyList())
    
    init {
        loadUnlockEvents()
        loadSelectedApps()
    }

    override fun getUnlockEvents(): Flow<List<UnlockEvent>> = _unlockEvents.asStateFlow()
    override fun getSelectedApps(): Flow<List<String>> = _selectedApps.asStateFlow()

    override suspend fun saveSelectedApps(packageNames: List<String>) {
        val limited = packageNames.take(10)
        _selectedApps.value = limited
        prefs.edit().putString("selected_apps", limited.joinToString(",")).apply()
    }
    
    override fun isOfflineMode(): Boolean = prefs.getBoolean("offline_mode", false)

    override suspend fun setOfflineMode(enabled: Boolean) {
        prefs.edit().putBoolean("offline_mode", enabled).apply()
    }

    override fun getBirthDate(): Long? {
        val ts = prefs.getLong("birth_date", -1L)
        return if (ts == -1L) null else ts
    }

    override suspend fun saveBirthDate(timestamp: Long) {
        prefs.edit().putLong("birth_date", timestamp).apply()
    }

    override fun getIntention(): String = prefs.getString("today_intention", "") ?: ""

    override suspend fun saveIntention(intention: String) {
        prefs.edit().putString("today_intention", intention).apply()
    }

    override fun getSleepTime(): String = prefs.getString("sleep_time", "22:00") ?: "22:00"

    override suspend fun saveSleepTime(time: String) {
        prefs.edit().putString("sleep_time", time).apply()
    }

    private fun loadSelectedApps() {
        val saved = prefs.getString("selected_apps", "") ?: ""
        if (saved.isNotEmpty()) {
            _selectedApps.value = saved.split(",")
        }
    }

    override suspend fun saveUnlockEvent(event: UnlockEvent) {
        val current = _unlockEvents.value.toMutableList()
        current.add(0, event)
        
        // Vyfiltrujeme pouze dnešní eventy pro počítání, ale uložíme posledních 50 celkově
        val limited = current.take(50)
        _unlockEvents.value = limited
        
        val timestamps = limited.joinToString(",") { it.timestamp.toString() }
        prefs.edit().putString("unlock_timestamps", timestamps).apply()
    }

    private fun loadUnlockEvents() {
        val saved = prefs.getString("unlock_timestamps", "") ?: ""
        if (saved.isNotEmpty()) {
            val events = saved.split(",").mapNotNull { 
                it.toLongOrNull()?.let { ts -> UnlockEvent(ts) }
            }
            _unlockEvents.value = events
        }
    }

    override fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, 0).map {
            AppInfo(
                packageName = it.activityInfo.packageName,
                label = it.loadLabel(pm).toString(),
                icon = it.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }

    override fun getScreenTimeMinutes(): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        // Použijeme queryAndAggregateUsageStats pro lepší přesnost u denního využití
        val stats = usageStatsManager.queryAndAggregateUsageStats(
            startTime,
            endTime
        )
        
        val totalTime = stats?.values?.sumOf { it.totalTimeInForeground } ?: 0L
        return totalTime / (1000 * 60)
    }
}
