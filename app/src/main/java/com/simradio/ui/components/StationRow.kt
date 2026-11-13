package com.simradio.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simradio.data.Station
import com.simradio.ui.theme.SignalGreen

/** 电台列表行。 */
@Composable
fun StationRow(
    station: Station,
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClickLabel = station.name, indication = ripple(), interactionSource = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StationMark(station = station, size = 46)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = station.metaLine(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (playing) {
            Spacer(Modifier.width(12.dp))
            PlayingBars()
        }
    }
}

/** 正在播放的三条起伏音量柱。 */
@Composable
fun PlayingBars(
    modifier: Modifier = Modifier,
    barWidth: Int = 3,
    height: Int = 16,
) {
    val levels = rememberLevels()
    Row(modifier = modifier.height(height.dp), verticalAlignment = Alignment.CenterVertically) {
        levels.forEach { level ->
            Box(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .width(barWidth.dp)
                    .height((height * level).dp.coerceAtLeast(2.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(SignalGreen),
            )
        }
    }
}

@Composable
private fun rememberLevels(): List<Float> {
    val transition = rememberInfiniteTransition(label = "levels")
    return listOf(0, 1, 2).map { index ->
        val max = if (index == 1) 1f else 0.7f
        transition.animateFloat(
            initialValue = max * 0.32f,
            targetValue = max,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 520 + index * 180, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "level$index",
        ).value
    }
}