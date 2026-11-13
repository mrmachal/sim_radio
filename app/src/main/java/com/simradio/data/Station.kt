package com.simradio.data

import kotlinx.serialization.Serializable

/** 电台条目：目录接口归一化后的稳定结构。 */
@Serializable
data class Station(
    val id: String,
    val name: String,
    val url: String,
    val homepage: String = "",
    val favicon: String = "",
    val country: String = "",
    val countryCode: String = "",
    val state: String = "",
    val language: String = "",
    val tags: List<String> = emptyList(),
    val codec: String = "",
    val bitrate: Int = 0,
    val hls: Boolean = false,
)