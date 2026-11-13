package com.simradio.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simradio.R
import com.simradio.ui.components.MessageState
import com.simradio.ui.components.StationRow
import com.simradio.ui.libraryModel
import com.simradio.simRadioApp

/** 收藏页：收藏与最近两张列表。 */
@Composable
fun LibraryScreen() {
    val model = libraryModel()
    val tab by model.tab.collectAsStateWithLifecycle()
    val favorites by model.favorites.collectAsStateWithLifecycle()
    val recents by model.recents.collectAsStateWithLifecycle()
    val app = simRadioApp()
    val player by app.playerController.state.collectAsStateWithLifecycle()

    val list = if (tab == LibraryTab.Favorites) favorites else recents

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    if (tab == LibraryTab.Favorites) R.string.lib_favorites else R.string.lib_recents,
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (tab == LibraryTab.Recents && recents.isNotEmpty()) {
                IconButton(onClick = model::clearRecents, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = stringResource(R.string.action_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            LibraryTab.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = entry == tab,
                    onClick = { model.selectTab(entry) },
                    shape = SegmentedButtonDefaults.itemShape(index, LibraryTab.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        activeBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (entry == LibraryTab.Favorites) R.string.lib_favorites else R.string.lib_recents,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        if (list.isEmpty()) {
            MessageState(
                title = stringResource(
                    if (tab == LibraryTab.Favorites) {
                        R.string.lib_favorites_empty
                    } else {
                        R.string.lib_recents_empty
                    },
                ),
                hint = stringResource(
                    if (tab == LibraryTab.Favorites) {
                        R.string.lib_favorites_empty_hint
                    } else {
                        R.string.lib_recents_empty_hint
                    },
                ),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
            ) {
                itemsIndexed(list, key = { _, item -> item.id }) { index, station ->
                    StationRow(
                        station = station,
                        playing = player.station?.id == station.id,
                        onClick = { model.play(list, index) },
                    )
                }
                item(key = "tail") { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}