package com.example.arduinocafetahotester

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arduinocafetahotester.ui.theme.ArduinoCafeTahoTesterTheme

import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import org.koin.androidx.compose.getViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

lateinit var port: UsbSerialPort


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ArduinoCafeTahoTesterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Content(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        port.close()
    }
}


@Composable
fun Content(context: Context) {
    val viewModel = getViewModel<MainActivityViewModel>()

    val state = viewModel.state.collectAsStateWithLifecycle().value.state

    when (state) {
        "start" -> {
            TextButton(
                onClick = {
                    initDevice(context)
                    viewModel.initialized()
                },
            ) {
                Text(text = "CONNECT", color = Color.Black)
            }
        }

        "running" -> {
            Running(true, onClick = {}, rpmMode = true, rpmChange = {})
        }

        "readyToStart" -> {
            Running(false,
                onClick = { viewModel.start() },
                rpmMode = viewModel.state.collectAsStateWithLifecycle().value.rpmMode,
                rpmChange = { viewModel.toggleRpmMode(it) })
        }
    }
}


@Composable
fun Running(
    started: Boolean,
    rpmMode: Boolean,
    onClick: () -> Unit,
    rpmChange: (isRpm: Boolean) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (
            slider, logText, gaugeText, gauge, rpmSwitch, rpmSwitchText
        ) = createRefs()

        TextButton(
            onClick = onClick,
            modifier = Modifier
                .constrainAs(logText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
        ) {
            Text(text = if (started) "running" else "START", color = Color.Black)
        }

        var position by remember { mutableStateOf(0f) }

        Text(
            modifier = Modifier
                .constrainAs(gaugeText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(logText.bottom, 24.dp)
                },
            text = if(rpmMode) ""+position*1300+" rpm" else ""+position*22 +" km/h"
        )
        Slider(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .constrainAs(slider) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(gaugeText.bottom)
                },
            value = position,
            onValueChange = {
                if (started) {
                    val data = String.format("%d/n", it.toInt()).toByteArray()
                    port.write(data, 5000)
                }
                position = it
            },
            valueRange = 0f..9f,
            onValueChangeFinished = {},
            steps = 10,
        )


        GaugeIndicator(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(gauge) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(slider.bottom)
                    width = Dimension.fillToConstraints
                },
            state = if(rpmMode) {
                UiState(13000f, position*1300 / 13000, "rpm")
            } else {
                UiState(220f, position*22 / 220f, "km/h")
            }

        )

        val checkedState = remember { mutableStateOf(true) }
        Switch(
            modifier = Modifier
                .padding(top = 8.dp)
                .constrainAs(rpmSwitch) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(gauge.bottom)
                    width = Dimension.fillToConstraints
                },
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
                rpmChange(it)
            },
        )
        Text(
            modifier = Modifier
                .constrainAs(rpmSwitchText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(rpmSwitch.bottom)
                },
            text = "RPM mode")
    }
}


fun initDevice(context: Context) {
    // Find all available drivers from attached devices.
    // Find all available drivers from attached devices.
    val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    val availableDrivers: List<UsbSerialDriver> =
        UsbSerialProber.getDefaultProber().findAllDrivers(manager)
    if (availableDrivers.isEmpty()) {
        return
    }

    // Open a connection to the first available driver.
    // Open a connection to the first available driver.
    val driver: UsbSerialDriver = availableDrivers[0]

    //manager.requestPermission(driver.getDevice(), mPermissionIntent);

    val connection = manager.openDevice(driver.device)
        ?: // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
        return

    port = driver.ports[0] // Most devices have just one port (port 0)

    port.open(connection)
    port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)


}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArduinoCafeTahoTesterTheme {
        Running(true, true, {}, {})
    }
}