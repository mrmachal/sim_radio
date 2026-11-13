package com.simradio.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.simradio.SimRadioApp
import com.simradio.ui.discover.DiscoverViewModel
import com.simradio.ui.library.LibraryViewModel
import com.simradio.ui.search.SearchViewModel

/** 按 ViewModel 类型注入应用容器的工厂。 */
object AppViewModels {
    private fun app(extras: androidx.lifecycle.viewmodel.CreationExtras): SimRadioApp =
        extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SimRadioApp

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { DiscoverViewModel(app(this)) }
        initializer { SearchViewModel(app(this)) }
        initializer { LibraryViewModel(app(this)) }
    }
}

@androidx.compose.runtime.Composable
fun discoverModel(): DiscoverViewModel = viewModel(factory = AppViewModels.Factory)

@androidx.compose.runtime.Composable
fun searchModel(): SearchViewModel = viewModel(factory = AppViewModels.Factory)

@androidx.compose.runtime.Composable
fun libraryModel(): LibraryViewModel = viewModel(factory = AppViewModels.Factory)