package com.simradio.ui.discover

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simradio.R
import com.simradio.ui.components.Hairline
import com.simradio.ui.components.ListFooter
import com.simradio.ui.components.LoadingState
import com.simradio.ui.components.MessageState
import com.simradio.ui.components.StationRow
import com.simradio.ui.discoverModel
import com.simradio.simRadioApp

/** 发现页：范围切换、主题分组与电台长列表。 */
@Composable
fun DiscoverScreen() {
    val model = discoverModel()
    val state by model.state.collectAsStateWithLifecycle()
    val app = simRadioApp()
    val player by app.playerController.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.stations.size - 6
        }
    }
    LaunchedEffect(shouldLoadMore, state.stations.size) {
        if (shouldLoadMore) model.loadMore()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DiscoverHeader(
            scope = state.scope,
            onScope = model::selectScope,
            onRefresh = model::refresh,
            refreshing = state.loading,
        )
        GroupRail(selected = state.group, scope = state.scope, onSelect = model::selectGroup)
        Hairline(modifier = Modifier.padding(horizontal = 16.dp), alpha = 0.4f)

        when {
            state.loading && state.stations.isEmpty() ->
                LoadingState(stringResource(R.string.list_loading))

            state.error && state.stations.isEmpty() -> MessageState(
                title = stringResource(R.string.error_load),
                hint = stringResource(R.string.error_network),
                action = stringResource(R.string.action_retry),
                onAction = model::refresh,
            )

            state.stations.isEmpty() -> MessageState(
                title = stringResource(R.string.list_empty),
                hint = stringResource(R.string.search_empty_hint),
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(state.stations, key = { _, item -> item.id }) { index, station ->
                    StationRow(
                        station = station,
                        playing = player.station?.id == station.id,
                        onClick = { model.play(index) },
                    )
                }
                item(key = "footer") {
                    when {
                        state.loadingMore -> ListFooter(
                            text = stringResource(R.string.list_load_more),
                            loading = true,
                        )
                        state.endReached -> ListFooter(
                            text = stringResource(R.string.list_end),
                            loading = false,
                        )
                        else -> Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverHeader(
    scope: DiscoverScope,
    onScope: (DiscoverScope) -> Unit,
    onRefresh: () -> Unit,
    refreshing: Boolean,
) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (scope == DiscoverScope.China) R.string.scope_cn else R.string.scope_world,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        if (scope == DiscoverScope.China) R.string.scope_cn_hint else R.string.scope_world_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            IconButton(onClick = onRefresh, enabled = !refreshing, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.action_refresh),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
            DiscoverScope.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = entry == scope,
                    onClick = { onScope(entry) },
                    shape = SegmentedButtonDefaults.itemShape(index, DiscoverScope.entries.size),
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
                            if (entry == DiscoverScope.China) R.string.scope_cn else R.string.scope_world,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupRail(
    selected: DiscoverGroup,
    scope: DiscoverScope,
    onSelect: (DiscoverGroup) -> Unit,
) {
    val groups = when (scope) {
        DiscoverScope.China -> listOf(
            DiscoverGroup.All, DiscoverGroup.News, DiscoverGroup.Music, DiscoverGroup.Traffic,
            DiscoverGroup.Economics, DiscoverGroup.Literature, DiscoverGroup.Storytelling, DiscoverGroup.Sport,
        )
        DiscoverScope.World -> listOf(
            DiscoverGroup.All, DiscoverGroup.Music, DiscoverGroup.News, DiscoverGroup.Storytelling,
            DiscoverGroup.Literature, DiscoverGroup.Sport,
        )
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(groups.size) { index ->
            val group = groups[index]
            FilterChip(
                selected = group == selected,
                onClick = { onSelect(group) },
                label = { Text(groupLabel(group)) },
                shape = CircleShape,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = group == selected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun groupLabel(group: DiscoverGroup): String = stringResource(
    when (group) {
        DiscoverGroup.All -> R.string.group_all
        DiscoverGroup.News -> R.string.group_news
        DiscoverGroup.Music -> R.string.group_music
        DiscoverGroup.Traffic -> R.string.group_traffic
        DiscoverGroup.Economics -> R.string.group_economics
        DiscoverGroup.Literature -> R.string.group_literature
        DiscoverGroup.Storytelling -> R.string.group_storytelling
        DiscoverGroup.Sport -> R.string.group_sport
    },
)