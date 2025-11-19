package com.vika.sdk.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.ln
import kotlin.math.sin

/**
 * Animated waveform visualization component that responds to audio amplitude.
 *
 * @param amplitude Current audio amplitude (0-32767 for MediaRecorder)
 * @param isActive Whether the waveform should be animating (recording/playing)
 * @param modifier Compose modifier
 * @param lineSegments Number of line segments in the waveform
 * @param maxAmplitude Maximum expected amplitude value for normalization
 * @param strokeWidth Width of the waveform lines
 * @param color Color of the waveform
 */
@Composable
internal fun WaveformView(
    amplitude: Int,
    isActive: Boolean = true,
    modifier: Modifier = Modifier,
    lineSegments: Int = 60,
    maxAmplitude: Float = 32767f,
    strokeWidth: Float = 5f,
    color: Color = Color.White
) {
    var amplitudeList by remember { mutableStateOf(FloatArray(lineSegments) { 0f }) }
    var phase by remember { mutableFloatStateOf(0f) }

    // Update amplitude list when amplitude changes
    LaunchedEffect(amplitude, isActive) {
        if (!isActive) {
            // Reset amplitude list when inactive
            amplitudeList = FloatArray(lineSegments) { 0f }
            return@LaunchedEffect
        }

        // Normalize amplitude with logarithmic scaling for better voice perception
        val normalizedAmplitude = if (amplitude > 0) {
            val logAmp = ln(amplitude.toFloat() + 1) / ln(maxAmplitude + 1)
            logAmp.coerceIn(0f, 1f)
        } else {
            0f
        }

        // Update amplitude list with smooth interpolation
        amplitudeList = amplitudeList.mapIndexed { index, currentAmp ->
            // Create wave-like distribution across segments
            val segmentFactor = sin(Math.PI * index / lineSegments).toFloat()
            val targetAmp = normalizedAmplitude * segmentFactor

            // Smooth interpolation: rise fast, fall gradually
            if (targetAmp > currentAmp) {
                currentAmp + (targetAmp - currentAmp) * 0.6f
            } else {
                currentAmp * 0.85f
            }
        }.toFloatArray()
    }

    // Animation loop for phase - only when active
    LaunchedEffect(isActive) {
        if (!isActive) {
            phase = 0f
            return@LaunchedEffect
        }

        while (isActive) {
            withFrameMillis {
                phase += 0.1f
                if (phase > 2 * Math.PI) {
                    phase -= (2 * Math.PI).toFloat()
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val widthPerSegment = size.width / lineSegments
        var startX = 0f

        for (i in 0 until lineSegments) {
            val amp = amplitudeList[i]
            val adjustedAmplitude = sin(phase + Math.PI * i / lineSegments * 2).toFloat() * amp
            val startY = size.height / 2 + adjustedAmplitude * size.height / 2f * 0.9f
            val stopX = startX + widthPerSegment
            val stopY = size.height / 2 - adjustedAmplitude * size.height / 2f * 0.9f

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(stopX, stopY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            startX = stopX
        }
    }
}

@Preview
@Composable
fun WaveformExample() {
    var amplitude by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        WaveformView(
            amplitude = amplitude,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black)
        )

        Slider(
            value = amplitude.toFloat(),
            onValueChange = { amplitude = it.toInt() },
            valueRange = 0f..32767f,
            modifier = Modifier.padding(16.dp)
        )
    }
}