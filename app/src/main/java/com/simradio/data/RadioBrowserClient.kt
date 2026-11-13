package com.simradio.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** radio-browser 社区开放目录客户端，内置镜像故障转移。 */
class RadioBrowserClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Volatile
    private var mirrors: List<String> = DEFAULT_MIRRORS

    @Volatile
    private var mirrorCheckedAt = 0L

    /** 探测可用镜像，按顺序取首个返回 200 的。 */
    suspend fun pickMirror(force: Boolean = false): String = withContext(Dispatchers.IO) {
        if (!force && mirrorCheckedAt != 0L && System.currentTimeMillis() - mirrorCheckedAt < MIRROR_TTL_MS) {
            return@withContext mirrors.first()
        }
        val candidates = mirrors
        val winner = coroutineScope {
            candidates.map { base ->
                async { base to probe(base) }
            }.awaitAll().firstOrNull { it.second }
        }
        if (winner != null) {
            mirrors = listOf(winner.first) + candidates.filter { it != winner.first }
        }
        mirrorCheckedAt = System.currentTimeMillis()
        mirrors.first()
    }

    private suspend fun probe(base: String): Boolean = withContext(Dispatchers.IO) {
        withTimeoutOrNull(6_000) {
            runCatching {
                val request = Request.Builder().url("$base/json/stats").header("User-Agent", USER_AGENT).build()
                client.newCall(request).execute().use { it.isSuccessful }
            }.getOrDefault(false)
        } ?: false
    }

    /** 按关键词搜索电台。 */
    suspend fun search(
        name: String = "",
        countryCode: String = "",
        tag: String = "",
        language: String = "",
        limit: Int = 40,
        offset: Int = 0,
    ): List<Station> = page(
        path = "stations/search",
        params = listOf(
            "hidebroken" to "true",
            "order" to "votes",
            "reverse" to "true",
            "limit" to limit.coerceIn(1, 200).toString(),
            "offset" to offset.coerceAtLeast(0).toString(),
            "name" to name,
            "countrycode" to countryCode,
            "tag" to tag,
            "language" to language,
        ),
        limit = limit,
    )

    /** 拉取有电台的国家，按电台数量降序。 */
    suspend fun countries(): List<CountryDto> = withContext(Dispatchers.IO) {
        val data = get("countries", listOf("order" to "name", "hidebroken" to "true")) ?: return@withContext emptyList()
        runCatching { json.decodeFromString<List<CountryDto>>(data) }.getOrDefault(emptyList())
            .filter { it.name.isNotBlank() && it.stationcount > 0 }
            .sortedByDescending { it.stationcount }
    }

    /** 拉取热门标签，按电台数量降序。 */
    suspend fun tags(): List<TagDto> = withContext(Dispatchers.IO) {
        val data = get("tags", listOf("order" to "stationcount", "reverse" to "true", "hidebroken" to "true"))
            ?: return@withContext emptyList()
        runCatching { json.decodeFromString<List<TagDto>>(data) }.getOrDefault(emptyList())
            .filter { it.name.isNotBlank() && it.stationcount > 0 }
            .take(60)
    }

    private suspend fun page(path: String, params: List<Pair<String, String>>, limit: Int): List<Station> =
        withContext(Dispatchers.IO) {
            val want = limit.coerceIn(1, 200)
            val buffer = minOf(200 - want, BUFFER_ROWS)
            val data = get(path, params.map { (k, v) -> k to if (k == "limit") (want + buffer).toString() else v })
                ?: return@withContext emptyList()
            runCatching { json.decodeFromString<List<StationDto>>(data) }.getOrDefault(emptyList())
                .mapNotNull { it.toStation() }
                .filterNot { NoiseFilter.isNoise(it.name) }
                .take(want)
        }

    /** 逐个镜像尝试，首个成功即返回原始 JSON。 */
    private suspend fun get(path: String, params: List<Pair<String, String>>): String? {
        val query = params
            .filter { it.second.isNotBlank() }
            .joinToString("&") { "${it.first}=${URLEncoder.encode(it.second, "UTF-8")}" }
        val suffix = "$path${if (query.isEmpty()) "" else "?$query"}"
        pickMirror()
        for (base in mirrors) {
            val body = withTimeoutOrNull(12_000) {
                runCatching {
                    val request = Request.Builder().url("$base/json/$suffix").header("User-Agent", USER_AGENT).build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) response.body?.string() else null
                    }
                }.getOrNull()
            }
            if (body != null) return body
        }
        return null
    }

    companion object {
        const val USER_AGENT = "SimRadio/1.0 (Android; radio-browser client)"
        private const val MIRROR_TTL_MS = 24 * 60 * 60 * 1000L
        private const val BUFFER_ROWS = 12

        val DEFAULT_MIRRORS = listOf(
            "https://de1.api.radio-browser.info",
            "https://si1.api.radio-browser.info",
            "https://fr1.api.radio-browser.info",
            "https://fi1.api.radio-browser.info",
        )
    }
}