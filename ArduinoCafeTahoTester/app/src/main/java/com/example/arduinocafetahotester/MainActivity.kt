package com.example.arduinocafetahotester

import android.content.Context
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arduinocafetahotester.ui.theme.ArduinoCafeTahoTesterTheme
import com.example.arduinocafetahotester.ui.theme.composables.ComposableLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aflak.arduino.Arduino
import me.aflak.arduino.ArduinoListener
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(baseContext, "onCreate", Toast.LENGTH_SHORT).show()


        setContent {
            ArduinoCafeTahoTesterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,

                    ) {
                    Content()
                }
            }
        }
    }
}


@Composable
fun Content() {
    val viewModel = getViewModel<MainActivityViewModel>(parameters = { parametersOf() })

    val state = viewModel.state.collectAsStateWithLifecycle().value

    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            viewModel.onDestroy()
        }
    }

    when (state) {
        MainActivityState.INITIALIZING -> {
            val context = LocalContext.current
            viewModel.initialize(context)
        }

        MainActivityState.WAITING_FOR_DEVICE -> {
            InitScreen(anim = true)
        }

        MainActivityState.CONNECTING -> {
            InitScreen(anim = false)
        }

        MainActivityState.RUNNING -> {
            Running(viewModel)
        }
    }
}

@Composable
private fun InitScreen(anim: Boolean) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
    ) {
        val (
            textLabel, board, usb
        ) = createRefs()
        Text(
            modifier = Modifier
                .padding(top = 20.dp)
                .constrainAs(textLabel) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    linkTo(parent.top, parent.bottom, bias = 0.4f)
                },
            text = "Connect Device",
            color = Color.Black
        )

        val infiniteTransition = rememberInfiniteTransition()
        val transY by infiniteTransition.animateFloat(
            initialValue = 52.0f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        Image(
            painterResource(R.drawable.usb),
            modifier = Modifier
                .width(20.dp)
                .constrainAs(usb) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(board.bottom, if (anim) transY.dp else (-12).dp)
                },
            contentDescription = "",
            contentScale = ContentScale.Fit,
        )

        Image(
            painterResource(R.drawable.board),
            modifier = Modifier
                .width(58.dp)
                .padding(top = 20.dp)
                .constrainAs(board) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(textLabel.bottom, 24.dp)
                },
            contentDescription = "",
            contentScale = ContentScale.Fit,
        )
    }
}


//protokuł "aINTbINTcINT\n"
//          aSPEEDbTAHOcMILLE\n

@Composable
fun Running(
    viewModel: MainActivityViewModel
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (
            sliderKmh, sliderRPM, gaugeKmhText, gaugeRpmText, gaugeKmh, gaugeRpm, messageText, milleButton, plusRPM, minusRPM, plusKMH, minusKMH
        ) = createRefs()

        var positionKmh by remember { mutableStateOf(0f) }
        var positionRpm by remember { mutableStateOf(0f) }

        var msg by remember { mutableStateOf("a0b0c0") }

        Text(
            modifier = Modifier
                .constrainAs(gaugeKmhText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top, 24.dp)
                },
            text = "" + positionKmh + " km/h"
        )
        Button(
            onClick = {
                positionKmh -= 5.0f
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 0
                viewModel.send(msg)
            },
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(minusKMH) {
                    start.linkTo(parent.start)
                    end.linkTo(plusKMH.start, 8.dp)
                    top.linkTo(gaugeKmhText.bottom, 24.dp)
                    width = Dimension.fillToConstraints
                },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Red
            ),
        ) { Text(text = "-") }
        Button(
            onClick = {
                positionKmh += 5.0f
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 0
                viewModel.send(msg)
            },
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(plusKMH) {
                    start.linkTo(minusKMH.end, 8.dp)
                    end.linkTo(parent.end)
                    top.linkTo(gaugeKmhText.bottom, 24.dp)
                    width = Dimension.fillToConstraints
                },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Green
            ),
        ) { Text(text = "+") }
        Slider(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .constrainAs(sliderKmh) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(gaugeKmhText.bottom)
                },
            value = positionKmh,
            onValueChange = {
                positionKmh = it
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 0
            },
            valueRange = 0f..255f,
            onValueChangeFinished = { viewModel.send(msg) },
            steps = 254,
        )

        Text(
            modifier = Modifier
                .constrainAs(gaugeRpmText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(sliderKmh.bottom, 24.dp)
                },
            text = "" + positionRpm + " rpm"
        )
        Button(
            onClick = {
                positionRpm -= 5.0f
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 0
                viewModel.send(msg)
            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Red
            ),
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(minusRPM) {
                    start.linkTo(parent.start)
                    end.linkTo(plusRPM.start, 8.dp)
                    top.linkTo(gaugeRpmText.bottom, 24.dp)
                    width = Dimension.fillToConstraints
                },
        ) { Text(text = "-") }
        Button(
            onClick = {
                positionRpm += 5.0f
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 0
                viewModel.send(msg)
            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Green
            ),
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(plusRPM) {
                    start.linkTo(minusRPM.end, 8.dp)
                    end.linkTo(parent.end)
                    top.linkTo(gaugeRpmText.bottom, 24.dp)
                    width = Dimension.fillToConstraints
                },
        ) { Text(text = "+") }
        Slider(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .constrainAs(sliderRPM) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(plusRPM.bottom)
                },
            value = positionRpm,
            onValueChange = {
                positionRpm = it
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 0
            },
            valueRange = 0f..255f,
            onValueChangeFinished = { viewModel.send(msg) },
            steps = 254,
        )


        GaugeIndicator(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(gaugeKmh) {
                    start.linkTo(parent.start)
                    end.linkTo(gaugeRpm.start)
                    top.linkTo(sliderRPM.bottom)
                    width = Dimension.fillToConstraints
                },
            UiState(255f, positionRpm / 255f, "rpm")
        )

        GaugeIndicator(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(gaugeRpm) {
                    start.linkTo(gaugeKmh.end)
                    end.linkTo(parent.end)
                    top.linkTo(sliderRPM.bottom)
                    width = Dimension.fillToConstraints
                },
            UiState(255f, positionKmh / 255f, "km/h")
        )

        TextButton(
            onClick = {
                msg = "a" + positionKmh.roundToInt() + "b" + positionRpm.roundToInt() + "c" + 110
                viewModel.send(msg)
            },
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(milleButton) {
                    start.linkTo(gaugeKmh.end)
                    end.linkTo(parent.end)
                    top.linkTo(gaugeRpm.bottom, 24.dp)
                    width = Dimension.fillToConstraints
                },
        ) {
            Text(
                text = "MILLE"
            )
        }


        val sendedMsg = viewModel.sendedMsg.collectAsStateWithLifecycle().value
        val msgFromArduino = viewModel.msgFromArduino.collectAsStateWithLifecycle().value

        Text(
            modifier = Modifier
                .constrainAs(messageText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(milleButton.bottom, 24.dp)
                },
            text = sendedMsg + "\n" + msgFromArduino
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ArduinoCafeTahoTesterTheme {
        InitScreen(true)
    }
}
