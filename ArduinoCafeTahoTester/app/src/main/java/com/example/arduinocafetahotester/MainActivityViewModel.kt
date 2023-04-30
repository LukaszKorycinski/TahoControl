package com.example.arduinocafetahotester

import android.content.Context
import android.hardware.usb.UsbDevice
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.aflak.arduino.Arduino
import me.aflak.arduino.ArduinoListener
import timber.log.Timber

class MainActivityViewModel() : ViewModel() {
    private val _state = MutableStateFlow(MainActivityState.INITIALIZING)
    val state = _state.asStateFlow()

    private val _sendedMsg = MutableStateFlow("a0b0c0")
    val sendedMsg = _sendedMsg.asStateFlow()

    private val _msgFromArduino = MutableStateFlow("")
    val msgFromArduino = _msgFromArduino.asStateFlow()

    lateinit var arduino: Arduino
    fun initialize(context: Context){
        arduino = Arduino(context)
        vendorIds.forEach { arduino.addVendorId(it) }

        arduino.setArduinoListener(object : ArduinoListener {
            override fun onArduinoAttached(device: UsbDevice) {
                arduino.open(device)
                Timber.e("onArduinoAttached")
                _state.value = MainActivityState.CONNECTING
            }

            override fun onArduinoDetached() {
                // arduino detached from phone
                Timber.e("onArduinoDetached")
                _state.value = MainActivityState.WAITING_FOR_DEVICE
            }

            override fun onArduinoMessage(bytes: ByteArray) {
                val message = bytes.toString()
                _msgFromArduino.value = message

                Timber.e("onArduinoMessage")
            }

            override fun onArduinoOpened() {
                Timber.e("onArduinoOpened")
                _state.value = MainActivityState.RUNNING
                // you can start the communication
//                val str = "Hello Arduino !"
//                arduino.send(str.toByteArray())
            }

            override fun onUsbPermissionDenied() {
                // Permission denied, display popup then
                Timber.e("onUsbPermissionDenied")
                arduino.reopen()
            }
        })

        _state.value = MainActivityState.WAITING_FOR_DEVICE
    }

    fun onDestroy() {
        arduino.unsetArduinoListener()
        arduino.close()
    }

    fun send(msg: String) {
        arduino.send(msg.toByteArray())
        _sendedMsg.value = msg
    }
}



enum class MainActivityState {
    INITIALIZING,//1przebieg
    WAITING_FOR_DEVICE,//podłącz
    CONNECTING,//podłączone inicjalizuje się
    RUNNING,//podłączone i działające
}