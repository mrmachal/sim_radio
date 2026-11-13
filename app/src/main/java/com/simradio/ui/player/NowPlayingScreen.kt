package com.simradio.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simradio.R
import com.simradio.data.Station
import com.simradio.playback.PlayerState
import com.simradio.simRadioApp
import com.simradio.ui.components.StationMark

/** 全屏播放页：刻度盘是主视觉，控制与元信息围绕它排布。 */
@Composable
fun NowPlayingScreen(
    state: PlayerState,
    onClose: () -> Unit,
) {
    val app = simRadioApp()
    val station = state.station ?: return
    val sleepMinutes by app.playerController.sleepMinutes.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            PlayerTopBar(onClose = onClose, sleepMinutes = sleepMinutes)

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val minContentHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = minContentHeight)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    StationHeader(station)
                    DialBlock(state)
                    ControlBlock(station, sleepMinutes)
                }
            }
        }
    }
}

@Composable
private fun StationHeader(station: Station) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        StationMark(
            station = station,
            size = 128,
            modifier = Modifier.clip(RoundedCornerShape(32.dp)),
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = station.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        MetaStrip(station)
    }
}

@Composable
private fun DialBlock(state: PlayerState) {
    val app = simRadioApp()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = when {
                state.hasError -> stringResource(R.string.state_error)
                state.isBuffering -> stringResource(R.string.state_buffering)
                !state.isPlaying -> stringResource(R.string.state_paused)
                else -> stringResource(R.string.player_tuning_hint)
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (state.hasError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        TuningDial(
            hasPrevious = state.hasPrevious,
            hasNext = state.hasNext,
            isPlaying = state.isPlaying,
            onPrevious = app::previous,
            onNext = app::next,
        )
        Spacer(Modifier.height(4.dp))
        TransportRow(
            state = state,
            onPrevious = app::previous,
            onToggle = app::togglePlayPause,
            onNext = app::next,
        )
    }
}

@Composable
private fun ControlBlock(station: Station, sleepMinutes: Int) {
    val app = simRadioApp()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        FavoriteRow(station)
        SleepTimerRow(current = sleepMinutes, onChange = app.playerController::setSleepTimer)
    }
}

@Composable
private fun PlayerTopBar(onClose: () -> Unit, sleepMinutes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.action_collapse),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = stringResource(R.string.now_playing),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (sleepMinutes > 0) {
                Text(
                    text = stringResource(R.string.sleep_timer_active, sleepMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun MetaStrip(station: Station) {
    val parts = buildList {
        if (station.country.isNotBlank()) add(station.country)
        station.tags.take(2).forEach { add(it) }
        station.codec.takeIf { it.isNotBlank() && it != "UNKNOWN" }?.let { add(it) }
        if (station.bitrate > 0) add("${station.bitrate} kbps")
    }
    if (parts.isEmpty()) return
    Text(
        text = parts.distinctBy { it.lowercase() }.joinToString("  ·  "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TransportRow(
    state: PlayerState,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, enabled = state.hasPrevious, modifier = Modifier.size(56.dp)) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = stringResource(R.string.action_previous),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (state.hasPrevious) 1f else 0.3f),
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        FilledIconButton(
            onClick = onToggle,
            modifier = Modifier.size(76.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(
                    if (state.isPlaying) R.string.action_pause else R.string.action_play,
                ),
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        IconButton(onClick = onNext, enabled = state.hasNext, modifier = Modifier.size(56.dp)) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.action_next),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (state.hasNext) 1f else 0.3f),
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun FavoriteRow(station: Station) {
    val app = simRadioApp()
    val favorites by app.libraryStore.favorites.collectAsStateWithLifecycle(emptyList())
    val isFavorite = favorites.any { it.id == station.id }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = { app.toggleFavorite(station) }, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.player_favorite_remove else R.string.player_favorite_add,
                ),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SleepTimerRow(current: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SLEEP_OPTIONS.forEach { minutes ->
            TextButton(onClick = { onChange(minutes) }) {
                Text(
                    text = if (minutes == 0) {
                        stringResource(R.string.sleep_timer_off)
                    } else {
                        stringResource(R.string.sleep_timer_minutes, minutes)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (current == minutes) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

private val SLEEP_OPTIONS = listOf(0, 15, 30, 60)