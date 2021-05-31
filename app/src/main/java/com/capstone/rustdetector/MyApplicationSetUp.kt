package com.capstone.rustdetector

import android.app.Application
import com.capstone.rustdetector.di.AppModule.viewModelModule
import com.capstone.rustdetector.di.CoreModule
import com.capstone.rustdetector.di.CoreModule.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplicationSetUp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplicationSetUp)
            modules(
                listOf(
                    repositoryModule,
                    viewModelModule
                )
            )
        }
    }
}