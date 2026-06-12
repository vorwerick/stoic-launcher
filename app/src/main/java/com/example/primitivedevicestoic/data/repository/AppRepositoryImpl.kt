package com.example.primitivedevicestoic.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.example.primitivedevicestoic.domain.model.AppInfo
import com.example.primitivedevicestoic.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppRepositoryImpl(
    private val context: Context,
    private val prefs: SharedPreferences
) : AppRepository {

    private val _selectedApps = MutableStateFlow<List<String>>(emptyList())
    
    init {
        _selectedApps.value = loadSelectedAppsFromPrefs()
    }

    override fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        return pm.queryIntentActivities(mainIntent, 0)
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null
                
                AppInfo(
                    packageName = packageName,
                    label = resolveInfo.loadLabel(pm).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    override fun getSelectedApps(): Flow<List<String>> = _selectedApps.asStateFlow()

    override fun getSelectedAppsList(): List<String> = _selectedApps.value

    override suspend fun saveSelectedApps(packageNames: List<String>) {
        val uniqueApps = packageNames.distinct()
        _selectedApps.value = uniqueApps
        prefs.edit().putString("selected_apps", uniqueApps.joinToString(",")).apply()
    }

    private fun loadSelectedAppsFromPrefs(): List<String> {
        val saved = prefs.getString("selected_apps", "") ?: ""
        return if (saved.isNotEmpty()) {
            saved.split(",").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }
}
