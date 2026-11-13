package com.simradio.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simradio.SimRadioApp
import com.simradio.data.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val app: SimRadioApp) : ViewModel() {

    val favorites: StateFlow<List<Station>> = app.libraryStore.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recents: StateFlow<List<Station>> = app.libraryStore.recents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _tab = MutableStateFlow(LibraryTab.Favorites)
    val tab: StateFlow<LibraryTab> = _tab

    fun selectTab(tab: LibraryTab) {
        _tab.value = tab
    }

    fun clearRecents() {
        viewModelScope.launch { app.libraryStore.clearRecents() }
    }

    fun play(list: List<Station>, index: Int) {
        app.play(list, index)
    }

    fun removeFavorite(station: Station) {
        app.toggleFavorite(station)
    }
}

enum class LibraryTab {
    Favorites,
    Recents,
}