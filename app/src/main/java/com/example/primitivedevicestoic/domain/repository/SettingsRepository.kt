package com.example.primitivedevicestoic.domain.repository

interface SettingsRepository {
    fun isOfflineMode(): Boolean
    suspend fun setOfflineMode(enabled: Boolean)
    fun getBirthDate(): Long?
    suspend fun saveBirthDate(timestamp: Long)
    fun getIntention(): String
    suspend fun saveIntention(intention: String)
    fun getSleepTime(): String?
    suspend fun saveSleepTime(time: String?)
    fun isEditorTipShown(): Boolean
    suspend fun setEditorTipShown(shown: Boolean)
    fun isDarkMode(): Boolean
    suspend fun setDarkMode(enabled: Boolean)
    fun isSystemBarHidden(): Boolean
    suspend fun setSystemBarHidden(enabled: Boolean)
    fun isCallsEnabled(): Boolean
    suspend fun setCallsEnabled(enabled: Boolean)
    fun isMessagesEnabled(): Boolean
    suspend fun setMessagesEnabled(enabled: Boolean)
    fun isCameraEnabled(): Boolean
    suspend fun setCameraEnabled(enabled: Boolean)
    fun isSettingsEnabled(): Boolean
    suspend fun setSettingsEnabled(enabled: Boolean)
}
