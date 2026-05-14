package com.example.primitivedevicestoic.di

import android.content.Context
import com.example.primitivedevicestoic.data.repository.UsageRepositoryImpl
import com.example.primitivedevicestoic.domain.repository.UsageRepository
import com.example.primitivedevicestoic.presentation.welcome.WelcomeViewModel
import com.example.primitivedevicestoic.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { androidContext().getSharedPreferences("stoic_prefs", Context.MODE_PRIVATE) }
    single<UsageRepository> { UsageRepositoryImpl(androidContext(), get()) }
    
    viewModel { WelcomeViewModel(androidContext()) }
    viewModel { HomeViewModel(get<UsageRepository>(), androidContext()) }
}
