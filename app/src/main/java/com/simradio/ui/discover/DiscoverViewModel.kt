package com.simradio.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simradio.SimRadioApp
import com.simradio.data.Station
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 发现页的主题分组，key 与 radio-browser 的 tag 对应。 */
enum class DiscoverGroup(val tag: String) {
    All(""),
    News("news"),
    Music("music"),
    Traffic("traffic"),
    Economics("economics"),
    Literature("literature"),
    Storytelling("storytelling"),
    Sport("sport"),
}

/** 目录范围：中文电台按国家过滤，全球热门按票数排。 */
enum class DiscoverScope {
    China,
    World,
}

data class DiscoverUiState(
    val scope: DiscoverScope = DiscoverScope.China,
    val group: DiscoverGroup = DiscoverGroup.All,
    val stations: List<Station> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: Boolean = false,
)

class DiscoverViewModel(private val app: SimRadioApp) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun selectScope(scope: DiscoverScope) {
        if (scope == _state.value.scope) return
        _state.update { it.copy(scope = scope) }
        refresh()
    }

    fun selectGroup(group: DiscoverGroup) {
        if (group == _state.value.group) return
        _state.update { it.copy(group = group) }
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        _state.update { it.copy(loading = true, error = false, endReached = false) }
        loadJob = viewModelScope.launch {
            val current = _state.value
            val result = runCatching {
                coroutineScope {
                    val mirrorJob = async { app.radioClient.pickMirror() }
                    val listJob = async { fetch(current.scope, current.group, PAGE_SIZE, 0) }
                    mirrorJob.await()
                    listJob.await()
                }
            }
            result.onSuccess { list ->
                _state.update {
                    it.copy(
                        stations = list,
                        loading = false,
                        error = false,
                        endReached = list.size < PAGE_SIZE,
                    )
                }
            }.onFailure {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || current.endReached) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val result = runCatching {
                fetch(current.scope, current.group, PAGE_SIZE, current.stations.size)
            }
            result.onSuccess { list ->
                _state.update {
                    it.copy(
                        stations = it.stations + list.filterNot { item -> it.stations.any { s -> s.id == item.id } },
                        loadingMore = false,
                        endReached = list.size < PAGE_SIZE,
                    )
                }
            }.onFailure {
                _state.update { it.copy(loadingMore = false, endReached = true) }
            }
        }
    }

    fun play(index: Int) {
        app.play(_state.value.stations, index)
    }

    private suspend fun fetch(
        scope: DiscoverScope,
        group: DiscoverGroup,
        limit: Int,
        offset: Int,
    ): List<Station> = app.radioClient.search(
        countryCode = if (scope == DiscoverScope.China) "CN" else "",
        tag = group.tag,
        limit = limit,
        offset = offset,
    )

    private companion object {
        const val PAGE_SIZE = 40
    }
}