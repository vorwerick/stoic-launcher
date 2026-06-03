package com.example.primitivedevicestoic.domain.repository

import com.example.primitivedevicestoic.domain.model.AppInfo
import com.example.primitivedevicestoic.domain.model.UnlockEvent
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getUnlockEvents(): Flow<List<UnlockEvent>>
    suspend fun saveUnlockEvent(event: UnlockEvent)
    fun getInstalledApps(): List<AppInfo>
    fun getScreenTimeMinutes(): Long
    fun getSelectedApps(): Flow<List<String>>
    suspend fun saveSelectedApps(packageNames: List<String>)
    fun isOfflineMode(): Boolean
    suspend fun setOfflineMode(enabled: Boolean)
    fun getBirthDate(): Long?
    suspend fun saveBirthDate(timestamp: Long)
    fun getIntention(): String
    suspend fun saveIntention(intention: String)
    fun getSleepTime(): String
    suspend fun saveSleepTime(time: String)
    fun isEditorTipShown(): Boolean
    suspend fun setEditorTipShown(shown: Boolean)
    fun isDarkMode(): Boolean
    suspend fun setDarkMode(enabled: Boolean)
}
