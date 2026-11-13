package com.simradio.data

/** 剔除目录里明显不是电台直播的条目（白噪音、有声书、带时间戳的重复台）。 */
object NoiseFilter {
    private val NOISE = Regex(
        "白噪音|雨声|睡眠|大自然的声音|休息音乐|相声合集|德云社|asmr|white\\s?noise|【\\d{4}|_?\\d{4}[-.]\\d{1,2}",
        RegexOption.IGNORE_CASE,
    )

    fun isNoise(name: String): Boolean = NOISE.containsMatchIn(name)
}