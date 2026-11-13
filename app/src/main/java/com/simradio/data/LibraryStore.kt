package com.simradio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "simradio")

/** 收藏与最近收听的本地存储。 */
class LibraryStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(Station.serializer())

    private val favoritesKey = stringPreferencesKey("favorites")
    private val recentsKey = stringPreferencesKey("recents")

    val favorites: Flow<List<Station>> = context.dataStore.data.map { prefs ->
        decode(prefs[favoritesKey])
    }

    val recents: Flow<List<Station>> = context.dataStore.data.map { prefs ->
        decode(prefs[recentsKey])
    }

    suspend fun toggleFavorite(station: Station) {
        context.dataStore.edit { prefs ->
            val current = decode(prefs[favoritesKey])
            val next = if (current.any { it.id == station.id }) {
                current.filterNot { it.id == station.id }
            } else {
                (listOf(station) + current).take(MAX_FAVORITES)
            }
            prefs[favoritesKey] = encode(next)
        }
    }

    suspend fun pushRecent(station: Station) {
        context.dataStore.edit { prefs ->
            val current = decode(prefs[recentsKey])
            val next = (listOf(station) + current.filterNot { it.id == station.id }).take(MAX_RECENTS)
            prefs[recentsKey] = encode(next)
        }
    }

    suspend fun clearRecents() {
        context.dataStore.edit { prefs -> prefs.remove(recentsKey) }
    }

    private fun decode(raw: String?): List<Station> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString(listSerializer, raw) }.getOrDefault(emptyList())
    }

    private fun encode(list: List<Station>): String = json.encodeToString(listSerializer, list)

    private companion object {
        const val MAX_FAVORITES = 200
        const val MAX_RECENTS = 30
    }
}