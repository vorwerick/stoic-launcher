package com.example.primitivedevicestoic.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.primitivedevicestoic.domain.model.AppInfo
import com.example.primitivedevicestoic.domain.model.UnlockEvent
import com.example.primitivedevicestoic.domain.repository.UsageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.util.Calendar

class UsageRepositoryImpl(
    private val context: Context,
    private val prefs: SharedPreferences
) : UsageRepository {

    private val _unlockEvents = MutableStateFlow<List<UnlockEvent>>(emptyList())
    
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "unlock_timestamps" -> loadUnlockEvents()
        }
    }
    
    init {
        loadUnlockEvents()
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun getUnlockEvents(): Flow<List<UnlockEvent>> = _unlockEvents.asStateFlow()
    
    override fun getSelectedApps(): Flow<List<String>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "selected_apps") {
                trySend(getSelectedAppsList())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart { emit(getSelectedAppsList()) }

    override fun getSelectedAppsList(): List<String> {
        val saved = prefs.getString("selected_apps", "") ?: ""
        return if (saved.isNotEmpty()) {
            saved.split(",")
        } else {
            emptyList()
        }
    }

    override suspend fun saveSelectedApps(packageNames: List<String>) {
        val limited = packageNames
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

    override fun getSleepTime(): String? = prefs.getString("sleep_time", null)

    override suspend fun saveSleepTime(time: String?) {
        if (time == null) {
            prefs.edit().remove("sleep_time").apply()
        } else {
            prefs.edit().putString("sleep_time", time).apply()
        }
    }

    override fun isEditorTipShown(): Boolean = prefs.getBoolean("editor_tip_shown", true)

    override suspend fun setEditorTipShown(shown: Boolean) {
        prefs.edit().putBoolean("editor_tip_shown", shown).apply()
    }

    override fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)

    override suspend fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    override fun isMottoEnabled(): Boolean = prefs.getBoolean("motto_enabled", false)

    override suspend fun setMottoEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("motto_enabled", enabled).apply()
    }

    override fun isSystemBarHidden(): Boolean = prefs.getBoolean("system_bar_hidden", false)

    override suspend fun setSystemBarHidden(enabled: Boolean) {
        prefs.edit().putBoolean("system_bar_hidden", enabled).apply()
    }

    override fun isCallsEnabled(): Boolean = prefs.getBoolean("calls_enabled", true)

    override suspend fun setCallsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("calls_enabled", enabled).apply()
    }

    override fun isMessagesEnabled(): Boolean = prefs.getBoolean("messages_enabled", true)

    override suspend fun setMessagesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("messages_enabled", enabled).apply()
    }

    override fun isCameraEnabled(): Boolean = prefs.getBoolean("camera_enabled", true)

    override suspend fun setCameraEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("camera_enabled", enabled).apply()
    }

    override fun isSettingsEnabled(): Boolean = prefs.getBoolean("settings_enabled", true)

    override suspend fun setSettingsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("settings_enabled", enabled).apply()
    }

    override fun getUsedMottoIndices(): Set<Int> {
        val saved = prefs.getString("used_motto_indices", "") ?: ""
        if (saved.isEmpty()) return emptySet()
        return saved.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    override suspend fun saveUsedMottoIndices(indices: Set<Int>) {
        prefs.edit().putString("used_motto_indices", indices.joinToString(",")).apply()
    }


    override suspend fun saveUnlockEvent(event: UnlockEvent) {
        val current = _unlockEvents.value.toMutableList()
        current.add(0, event)
        
        // Vyfiltrujeme pouze dnešní eventy pro počítání, ale uložíme posledních 50 celkově
        val limited = current.take(50)
        
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
        return pm.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
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
