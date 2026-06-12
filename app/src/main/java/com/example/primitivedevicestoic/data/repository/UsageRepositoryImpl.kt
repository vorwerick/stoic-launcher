package com.example.primitivedevicestoic.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
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
    
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "unlock_timestamps") {
            loadUnlockEvents()
        }
    }
    
    init {
        loadUnlockEvents()
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun getUnlockEvents(): Flow<List<UnlockEvent>> = _unlockEvents.asStateFlow()

    override suspend fun saveUnlockEvent(event: UnlockEvent) {
        val current = _unlockEvents.value.toMutableList()
        current.add(0, event)
        
        // Uložíme posledních 50 událostí
        val limited = current.take(50)
        
        val timestamps = limited.joinToString(",") { it.timestamp.toString() }
        prefs.edit().putString("unlock_timestamps", timestamps).apply()
        _unlockEvents.value = limited
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

    override fun getScreenTimeMinutes(): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        val stats = usageStatsManager.queryAndAggregateUsageStats(
            startTime,
            endTime
        )
        
        val totalTime = stats?.values?.sumOf { it.totalTimeInForeground } ?: 0L
        return totalTime / (1000 * 60)
    }
}
