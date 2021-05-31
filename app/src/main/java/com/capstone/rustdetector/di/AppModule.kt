package com.capstone.rustdetector.di

import com.capstone.rustdetector.viewmodel.RustDetectorViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val viewModelModule = module {
        viewModel { RustDetectorViewModel(rustDetectorRepository = get() ) }
    }
}