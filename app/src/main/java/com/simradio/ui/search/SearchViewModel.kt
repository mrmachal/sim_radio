package com.simradio.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simradio.SimRadioApp
import com.simradio.data.Station
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 当前检索的维度：关键词、标签或国家。 */
sealed interface SearchQuery {
    data object None : SearchQuery
    data class ByName(val name: String) : SearchQuery
    data class ByTag(val tag: String) : SearchQuery
    data class ByCountry(val code: String, val label: String) : SearchQuery
}

data class SearchUiState(
    val input: String = "",
    val query: SearchQuery = SearchQuery.None,
    val stations: List<Station> = emptyList(),
    val tags: List<String> = emptyList(),
    val countries: List<Pair<String, String>> = emptyList(),
    val loadingMore: Boolean = false,
    val searching: Boolean = false,
    val error: Boolean = false,
    val page: Int = 0,
    val endReached: Boolean = false,
) {
    val showSuggestions: Boolean get() = query is SearchQuery.None && !searching
    val isEmptyResult: Boolean get() = query !is SearchQuery.None && !searching && !error && stations.isEmpty()
}

class SearchViewModel(private val app: SimRadioApp) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSuggestions()
    }

    fun updateInput(input: String) {
        _state.update { it.copy(input = input) }
        searchJob?.cancel()
        if (input.isBlank()) {
            _state.update {
                it.copy(query = SearchQuery.None, stations = emptyList(), searching = false, error = false)
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(320)
            runQuery(SearchQuery.ByName(input))
        }
    }

    fun searchByTag(tag: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runQuery(SearchQuery.ByTag(tag)) }
    }

    fun searchByCountry(code: String, label: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runQuery(SearchQuery.ByCountry(code, label)) }
    }

    fun clear() {
        searchJob?.cancel()
        _state.update {
            it.copy(input = "", query = SearchQuery.None, stations = emptyList(), searching = false, error = false)
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.searching || current.loadingMore || current.endReached) return
        if (current.query is SearchQuery.None) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val next = current.page + 1
            runCatching { fetch(current.query, next * PAGE_SIZE) }
                .onSuccess { list ->
                    _state.update {
                        it.copy(
                            stations = it.stations + list.filterNot { item -> it.stations.any { s -> s.id == item.id } },
                            loadingMore = false,
                            page = next,
                            endReached = list.size < PAGE_SIZE,
                        )
                    }
                }
                .onFailure { _state.update { s -> s.copy(loadingMore = false, endReached = true) } }
        }
    }

    fun retry() {
        val current = _state.value.query
        if (current is SearchQuery.None) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runQuery(current) }
    }

    fun play(index: Int) {
        app.play(_state.value.stations, index)
    }

    private suspend fun runQuery(query: SearchQuery) {
        _state.update {
            it.copy(
                query = query,
                stations = if (it.query == query) it.stations else emptyList(),
                searching = true,
                error = false,
                page = 0,
                endReached = false,
            )
        }
        runCatching { fetch(query, 0) }
            .onSuccess { list ->
                _state.update {
                    it.copy(stations = list, searching = false, error = false, endReached = list.size < PAGE_SIZE)
                }
            }
            .onFailure { _state.update { s -> s.copy(searching = false, error = true) } }
    }

    private suspend fun fetch(query: SearchQuery, offset: Int): List<Station> = when (query) {
        is SearchQuery.None -> emptyList()
        is SearchQuery.ByName -> app.radioClient.search(name = query.name, limit = PAGE_SIZE, offset = offset)
        is SearchQuery.ByTag -> app.radioClient.search(tag = query.tag, limit = PAGE_SIZE, offset = offset)
        is SearchQuery.ByCountry ->
            app.radioClient.search(countryCode = query.code, limit = PAGE_SIZE, offset = offset)
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            val tags = runCatching { app.radioClient.tags() }.getOrDefault(emptyList())
                .map { it.name }
                .filter { it.length in 3..12 && it.all { ch -> ch.isLetterOrDigit() || ch == ' ' } }
                .take(16)
            _state.update { it.copy(tags = tags) }
        }
        viewModelScope.launch {
            val countries = runCatching { app.radioClient.countries() }.getOrDefault(emptyList())
                .take(18)
                .map { it.name to it.iso }
            _state.update { it.copy(countries = countries) }
        }
    }

    private companion object {
        const val PAGE_SIZE = 40
    }
}