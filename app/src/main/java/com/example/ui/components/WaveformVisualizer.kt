package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.TranscriptSegment
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.StudioSurfaceVariant
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    durationMs: Long,
    currentTimeMs: Long,
    segments: List<TranscriptSegment>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate stable pseudo-random waveform bars
    val barHeights = remember(durationMs) {
        val count = 72
        val rng = Random(42)
        FloatArray(count) { rng.nextFloat().coerceIn(0.15f, 0.95f) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioSurfaceVariant.copy(alpha = 0.6f))
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((fraction * durationMs).toLong())
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val count = barHeights.size
            val barSpacing = width / count
            val barWidth = barSpacing * 0.65f

            // 1. Draw segment speech zones in background
            segments.forEach { seg ->
                val startX = (seg.startMs.toFloat() / durationMs) * width
                val endX = (seg.endMs.toFloat() / durationMs) * width
                drawRect(
                    color = NeonIndigo.copy(alpha = 0.18f),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, height)
                )
            }

            // 2. Draw waveform vertical bars
            val progressFraction = if (durationMs > 0) currentTimeMs.toFloat() / durationMs else 0f
            val playheadX = progressFraction * width

            for (i in 0 until count) {
                val x = i * barSpacing + (barSpacing - barWidth) / 2f
                val barH = barHeights[i] * (height * 0.85f)
                val topY = centerY - barH / 2f

                // Is this bar in an active speech segment?
                val timeAtBar = ((i.toFloat() / count) * durationMs).toLong()
                val isInSpeech = segments.any { timeAtBar in it.startMs..it.endMs }

                val barColor = when {
                    x <= playheadX && isInSpeech -> NeonCyan
                    x <= playheadX -> NeonIndigo
                    isInSpeech -> Color(0xFF64748B)
                    else -> Color(0xFF334155)
                }

                drawRect(
                    color = barColor,
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, barH)
                )
            }

            // 3. Draw red/cyan playhead line
            drawLine(
                color = NeonAmber,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, height),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}
