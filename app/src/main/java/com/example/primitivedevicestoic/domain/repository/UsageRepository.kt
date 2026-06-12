package com.example.primitivedevicestoic.domain.repository

import com.example.primitivedevicestoic.domain.model.UnlockEvent
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getUnlockEvents(): Flow<List<UnlockEvent>>
    suspend fun saveUnlockEvent(event: UnlockEvent)
    fun getScreenTimeMinutes(): Long
}
