package com.example.primitivedevicestoic.domain.model

data class UnlockEvent(
    val timestamp: Long
)

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable? = null
)
