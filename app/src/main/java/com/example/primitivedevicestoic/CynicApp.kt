package com.example.primitivedevicestoic

import android.app.Application
import com.example.primitivedevicestoic.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CynicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CynicApp)
            modules(appModule)
        }
    }
}
