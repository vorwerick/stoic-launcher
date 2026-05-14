package com.example.primitivedevicestoic.presentation.welcome

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Intent

class WelcomeViewModel(private val context: Context) : ViewModel() {
    private val _isDefaultLauncher = MutableStateFlow(isDefaultLauncher(context))
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher

    fun updateDefaultLauncherStatus() {
        _isDefaultLauncher.value = isDefaultLauncher(context)
    }

    private fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val res = context.packageManager.resolveActivity(intent, 0)
        return res?.activityInfo?.packageName == context.packageName
    }
}
