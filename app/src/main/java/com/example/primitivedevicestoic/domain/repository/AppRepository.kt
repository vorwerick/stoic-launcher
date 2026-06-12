package com.example.primitivedevicestoic.domain.repository

import com.example.primitivedevicestoic.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): List<AppInfo>
    fun getSelectedApps(): Flow<List<String>>
    fun getSelectedAppsList(): List<String>
    suspend fun saveSelectedApps(packageNames: List<String>)
}
