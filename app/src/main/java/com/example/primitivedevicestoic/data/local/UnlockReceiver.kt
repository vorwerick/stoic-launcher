package com.example.primitivedevicestoic.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.primitivedevicestoic.domain.model.UnlockEvent
import com.example.primitivedevicestoic.domain.repository.UsageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class UnlockReceiver : BroadcastReceiver() {
    private val repository: UsageRepository by inject(UsageRepository::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            scope.launch {
                repository.saveUnlockEvent(UnlockEvent(System.currentTimeMillis()))
            }
        }
    }
}
