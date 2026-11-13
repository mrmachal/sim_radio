package com.simradio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simradio.R
import com.simradio.SimRadioApp
import com.simradio.ui.discover.DiscoverScreen
import com.simradio.ui.library.LibraryScreen
import com.simradio.ui.player.MiniPlayer
import com.simradio.ui.player.NowPlayingScreen
import com.simradio.ui.search.SearchScreen

private enum class Destination(
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Discover(R.string.tab_discover, Icons.Outlined.Explore, Icons.Rounded.Explore),
    Search(R.string.tab_search, Icons.Outlined.Search, Icons.Rounded.Search),
    Library(R.string.tab_library, Icons.Outlined.FavoriteBorder, Icons.Rounded.Favorite),
}

@Composable
fun SimRadioRoot() {
    val app = LocalContext.current.applicationContext as SimRadioApp
    var destination by rememberSaveable { mutableStateOf(Destination.Discover) }
    var playerOpen by rememberSaveable { mutableStateOf(false) }

    val playerState by app.playerController.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column {
                    if (playerState.station != null) {
                        MiniPlayer(
                            state = playerState,
                            onOpen = { playerOpen = true },
                            onToggle = app::togglePlayPause,
                            onNext = app::next,
                        )
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified,
                        windowInsets = WindowInsets.navigationBars,
                    ) {
                        Destination.entries.forEach { item ->
                            val selected = destination == item
                            NavigationBarItem(
                                selected = selected,
                                onClick = { destination = item },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.icon,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                when (destination) {
                    Destination.Discover -> DiscoverScreen()
                    Destination.Search -> SearchScreen()
                    Destination.Library -> LibraryScreen()
                }
            }
        }

        AnimatedVisibility(
            visible = playerOpen && playerState.station != null,
            enter = slideInVertically(tween(320)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(280)) { it } + fadeOut(tween(180)),
        ) {
            NowPlayingScreen(state = playerState, onClose = { playerOpen = false })
        }
    }
}