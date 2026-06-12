package com.example.primitivedevicestoic.data.repository

import android.content.SharedPreferences
import com.example.primitivedevicestoic.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val prefs: SharedPreferences
) : SettingsRepository {

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
}
