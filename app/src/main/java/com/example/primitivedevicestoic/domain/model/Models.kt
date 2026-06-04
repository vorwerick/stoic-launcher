package com.example.primitivedevicestoic.domain.model

import androidx.compose.runtime.Immutable

data class UnlockEvent(
    val timestamp: Long
)

@Immutable
data class AppInfo(
    val packageName: String,
    val label: String
)
