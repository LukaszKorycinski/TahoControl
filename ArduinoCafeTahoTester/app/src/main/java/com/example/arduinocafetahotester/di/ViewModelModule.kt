package com.example.arduinocafetahotester.di

import com.example.arduinocafetahotester.MainActivityViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { MainActivityViewModel() }
}