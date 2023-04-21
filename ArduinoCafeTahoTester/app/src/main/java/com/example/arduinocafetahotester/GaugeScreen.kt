package com.example.arduinocafetahotester

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arduinocafetahotester.theme.ComposeSpeedTestTheme
import com.example.arduinocafetahotester.theme.DarkGradient
import com.example.arduinocafetahotester.theme.Green200
import com.example.arduinocafetahotester.theme.Green500
import com.example.arduinocafetahotester.theme.GreenGradient
import com.example.arduinocafetahotester.theme.LightColor
import kotlin.math.floor

class UiState(
    val maxSpeed: Float = 0f,
    val arcValue: Float = 0f,
    val unit: String
)


suspend fun startAnimation(animation: Animatable<Float, AnimationVector1D>) {
    animation.animateTo(0.84f, keyframes {
        durationMillis = 9000
        0f at 0 with CubicBezierEasing(0f, 1.5f, 0.8f, 1f)
        0.72f at 1000 with CubicBezierEasing(0.2f, -1.5f, 0f, 1f)
        0.76f at 2000 with CubicBezierEasing(0.2f, -2f, 0f, 1f)
        0.78f at 3000 with CubicBezierEasing(0.2f, -1.5f, 0f, 1f)
        0.82f at 4000 with CubicBezierEasing(0.2f, -2f, 0f, 1f)
        0.85f at 5000 with CubicBezierEasing(0.2f, -2f, 0f, 1f)
        0.89f at 6000 with CubicBezierEasing(0.2f, -1.2f, 0f, 1f)
        0.82f at 7500 with LinearOutSlowInEasing
    })
}

fun Animatable<Float, AnimationVector1D>.toUiState(maxSpeed: Float, unit: String) = UiState(
    arcValue = value,
    maxSpeed = maxSpeed,
    unit = unit
)


@Composable
private fun SpeedTestScreen(state: UiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        GaugeIndicator(Modifier, state = state)
    }
}

@Composable
fun Header() {
    Text(
        text = "SPEEDTEST",
        modifier = Modifier.padding(top = 52.dp, bottom = 16.dp),
        style = MaterialTheme.typography.h6
    )
}

@Composable
fun GaugeIndicator(modifier: Modifier, state: UiState) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(state.arcValue, 240f)
        SpeedValue( String.format("%.0f", (state.maxSpeed * state.arcValue)) + " " + state.unit)
    }
}

@Composable
fun SpeedValue(value: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun AdditionalInfo(ping: String, maxSpeed: String) {

    @Composable
    fun RowScope.InfoColumn(title: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(title)
            Text(
                value,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        InfoColumn(title = "PING", value = ping)
        VerticalDivider()
        InfoColumn(title = "MAX SPEED", value = maxSpeed)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF414D66))
            .width(1.dp)
    )
}


@Composable
fun CircularSpeedIndicator(value: Float, angle: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        drawLines(value, angle)
        drawArcs(value, angle)
    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress

    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    fun drawBlur() {
        for (i in 0..20) {
            drawArc(
                color = Green200.copy(alpha = i / 900f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)
            )
        }
    }

    fun drawStroke() {
        drawArc(
            color = Green500,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )
    }

    fun drawGradient() {
        drawArc(
            brush = GreenGradient,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )
    }

    drawBlur()
    drawStroke()
    drawGradient()
}

fun DrawScope.drawLines(progress: Float, maxValue: Float, numberOfLines: Int = 40) {
    val oneRotation = maxValue / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt() + 1

    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                LightColor,
                Offset(if (i % 5 == 0) 80f else 30f, size.height / 2),
                Offset(0f, size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
fun GaugePreview() {
    ComposeSpeedTestTheme {
        Surface() {
            SpeedTestScreen(
                UiState(
                    maxSpeed = 220.0f,
                    arcValue = 0.23f,
                    unit = "km/h"
                )
            )
        }
    }
}