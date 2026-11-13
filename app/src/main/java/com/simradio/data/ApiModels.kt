package com.simradio.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** radio-browser 原始条目，字段全部可缺省。 */
@Serializable
data class StationDto(
    val stationuuid: String = "",
    val name: String = "",
    val url: String = "",
    @SerialName("url_resolved") val urlResolved: String = "",
    val homepage: String = "",
    val favicon: String = "",
    val tags: String = "",
    val country: String = "",
    val countrycode: String = "",
    val state: String = "",
    val language: String = "",
    val codec: String = "",
    val bitrate: Int = 0,
    val hls: Int = 0,
    val votes: Int = 0,
    val clickcount: Int = 0,
    val lastcheckok: Int = 0,
)

@Serializable
data class CountryDto(
    val name: String = "",
    @SerialName("iso_3166_1") val iso: String = "",
    val stationcount: Int = 0,
)

@Serializable
data class TagDto(
    val name: String = "",
    val stationcount: Int = 0,
)

fun StationDto.toStation(): Station? {
    val streamUrl = urlResolved.ifBlank { url }.trim()
    if (!streamUrl.startsWith("http://") && !streamUrl.startsWith("https://")) return null
    return Station(
        id = stationuuid.ifBlank { streamUrl },
        name = name.trim(),
        url = streamUrl,
        homepage = homepage.trim(),
        favicon = favicon.trim(),
        country = country.trim(),
        countryCode = countrycode.trim().uppercase(),
        state = state.trim(),
        language = language.trim(),
        tags = tags.split(',').map { it.trim() }.filter { it.isNotEmpty() }.take(6),
        codec = codec.trim().uppercase().takeIf { it != "UNKNOWN" } ?: "",
        bitrate = bitrate.coerceAtLeast(0),
        hls = hls == 1,
    )
}