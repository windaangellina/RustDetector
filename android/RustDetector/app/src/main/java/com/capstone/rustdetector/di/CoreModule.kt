package com.capstone.rustdetector.di

import com.capstone.rustdetector.source.remote.RemoteDataSource
import com.capstone.rustdetector.source.RustDetectorRepository
import org.koin.dsl.module

object CoreModule {
    val repositoryModule = module {
        single { RemoteDataSource() }
        single { RustDetectorRepository(get()) }
    }
}