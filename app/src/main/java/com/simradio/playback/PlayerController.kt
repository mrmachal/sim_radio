package com.simradio.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.simradio.data.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** 播放控制器：连接前台服务并把播放状态暴露成可观察的流。 */
class PlayerController(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var controller: MediaController? = null

    private var sleepJob: Job? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _sleepMinutes = MutableStateFlow(0)

    /** 定时关闭的剩余分钟，0 表示未开启。 */
    val sleepMinutes: StateFlow<Int> = _sleepMinutes.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish(player)
        }
    }

    suspend fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future: ListenableFuture<MediaController> = MediaController.Builder(context, token).buildAsync()
        val built = suspendCancellableCoroutine { cont ->
            future.addListener(
                { cont.resume(runCatching { future.get() }.getOrNull()) },
                ContextCompat.getMainExecutor(context),
            )
            cont.invokeOnCancellation { MediaController.releaseFuture(future) }
        }
        if (built != null) {
            controller = built
            built.addListener(listener)
            publish(built)
        }
    }

    /** 播放整列电台中的某一台。 */
    fun play(stations: List<Station>, index: Int) {
        val target = controller ?: return
        if (stations.isEmpty()) return
        target.setMediaItems(stations.map { it.toMediaItem() }, index.coerceIn(stations.indices), 0L)
        target.prepare()
        target.play()
    }

    fun togglePlayPause() {
        val target = controller ?: return
        if (target.isPlaying) target.pause() else target.play()
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    /** 设定若干分钟后暂停播放，0 表示取消。 */
    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        if (minutes <= 0) {
            _sleepMinutes.value = 0
            return
        }
        _sleepMinutes.value = minutes
        sleepJob = scope.launch {
            repeat(minutes) { elapsed ->
                delay(60_000)
                _sleepMinutes.value = minutes - elapsed - 1
            }
            controller?.pause()
            _sleepMinutes.value = 0
        }
    }

    fun release() {
        sleepJob?.cancel()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    private fun publish(player: Player) {
        val item: MediaItem? = player.currentMediaItem
        _state.value = PlayerState(
            station = item?.toStationOrNull(),
            isPlaying = player.isPlaying,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            hasError = player.playerError != null,
            hasNext = player.hasNextMediaItem(),
            hasPrevious = player.hasPreviousMediaItem(),
        )
    }
}

/** 播放器对外输出的状态。 */
data class PlayerState(
    val station: Station? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val hasError: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
)