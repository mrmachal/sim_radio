package com.simradio

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.simradio.data.LibraryStore
import com.simradio.data.RadioBrowserClient
import com.simradio.data.Station
import com.simradio.playback.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** 应用级依赖容器。 */
class SimRadioApp : Application() {

    val radioClient: RadioBrowserClient by lazy { RadioBrowserClient() }

    val libraryStore: LibraryStore by lazy { LibraryStore(this) }

    val playerController: PlayerController by lazy { PlayerController(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { playerController.connect() }
    }

    /** 播放整列电台中的某一台，并记入最近收听。 */
    fun play(stations: List<Station>, index: Int) {
        if (stations.isEmpty()) return
        val safeIndex = index.coerceIn(stations.indices)
        appScope.launch {
            playerController.connect()
            playerController.play(stations, safeIndex)
            libraryStore.pushRecent(stations[safeIndex])
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()

    fun next() = playerController.next()

    fun previous() = playerController.previous()

    fun toggleFavorite(station: Station) {
        appScope.launch { libraryStore.toggleFavorite(station) }
    }
}

val Context.simRadioApp: SimRadioApp
    get() = applicationContext as SimRadioApp

/** 组合内取应用容器。 */
@Composable
@ReadOnlyComposable
fun simRadioApp(): SimRadioApp = LocalContext.current.applicationContext as SimRadioApp