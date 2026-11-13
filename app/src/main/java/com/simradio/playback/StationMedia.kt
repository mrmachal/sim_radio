package com.simradio.playback

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.simradio.data.Station
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

private const val KEY_STATION = "station"

/** 电台与 MediaItem 互转，队列里带着完整条目以免丢失来源信息。 */
fun Station.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id)
    .setUri(url)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(name)
            .setArtist(listOfNotNull(country.ifBlank { null }, tags.firstOrNull()).joinToString(" · "))
            .setArtworkUri(favicon.takeIf { it.startsWith("http") }?.toUriOrNull())
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(bundleOf(KEY_STATION to json.encodeToString(Station.serializer(), this)))
            .build(),
    )
    .build()

fun MediaItem.toStationOrNull(): Station? {
    val raw = mediaMetadata.extras?.getString(KEY_STATION) ?: return null
    return runCatching { json.decodeFromString(Station.serializer(), raw) }.getOrNull()
}

private fun String.toUriOrNull(): android.net.Uri? = runCatching { android.net.Uri.parse(this) }.getOrNull()