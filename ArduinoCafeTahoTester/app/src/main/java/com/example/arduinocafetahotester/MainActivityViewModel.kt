package com.example.arduinocafetahotester

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainActivityState("start"))
    val state = _state.asStateFlow()

    fun initialized() {
        _state.value = MainActivityState("readyToStart")
    }

    fun start(){
        _state.value = MainActivityState("running")
    }

    fun toggleRpmMode(mode: Boolean){
        _state.value = MainActivityState(_state.value.state, rpmMode = mode)
    }
}

class MainActivityState(
    val state: String,
    val rpmMode: Boolean = true
)