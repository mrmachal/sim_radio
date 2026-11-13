package com.simradio.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** 调谐刻度盘：左右拖动超过半格切换电台，松手回弹。 */
@Composable
fun TuningDial(
    hasPrevious: Boolean,
    hasNext: Boolean,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var drag by remember { mutableFloatStateOf(0f) }
    val settled by animateFloatAsState(drag, spring(stiffness = Spring.StiffnessLow), label = "dial")
    val settleTarget by rememberUpdatedState({ drag = 0f })
    val active = MaterialTheme.colorScheme.primary
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    val fading = MaterialTheme.colorScheme.surface

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = constraints.maxWidth.toFloat()
        val step = widthPx / TICKS_VISIBLE

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .pointerInput(hasPrevious, hasNext, step) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val ratio = drag / step
                            if (ratio < -0.45f && hasNext) onNext()
                            if (ratio > 0.45f && hasPrevious) onPrevious()
                            settleTarget()
                        },
                        onDragCancel = { settleTarget() },
                    ) { _, delta ->
                        drag = (drag + delta).coerceIn(-step * 1.7f, step * 1.7f)
                    }
                },
        ) {
            val centerY = size.height * 0.5f
            val half = TICKS_VISIBLE / 2
            for (index in 0..TICKS_VISIBLE) {
                val tick = index - half
                val x = size.width / 2f + tick * step + settled
                if (x < -step || x > size.width + step) continue
                val distance = abs(tick)
                val major = tick % 5 == 0
                val lit = distance == 0
                val length = when {
                    lit -> size.height * 0.62f
                    major -> size.height * 0.42f
                    else -> size.height * 0.24f
                }
                val color: Color = when {
                    lit -> active
                    distance == 1 && isPlaying -> active.copy(alpha = 0.62f)
                    major -> quiet.copy(alpha = 0.8f)
                    else -> quiet.copy(alpha = 0.42f)
                }
                drawLine(
                    color = color,
                    start = Offset(x, centerY - length / 2f),
                    end = Offset(x, centerY + length / 2f),
                    strokeWidth = if (lit) 3.dp.toPx() else if (major) 1.6.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            val needle = Path().apply {
                val base = size.width / 2f + settled
                moveTo(base, size.height * 0.06f)
                lineTo(base - 5.dp.toPx(), 0f)
                lineTo(base + 5.dp.toPx(), 0f)
                close()
            }
            drawPath(needle, quiet.copy(alpha = 0.75f))

            // 两端渐隐遮罩。
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to fading,
                    0.16f to Color.Transparent,
                    0.84f to Color.Transparent,
                    1f to fading,
                ),
            )
        }
    }
}

private const val TICKS_VISIBLE = 24