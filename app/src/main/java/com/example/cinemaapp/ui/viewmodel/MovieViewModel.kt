package com.example.cinemaapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemaapp.Movie
import com.example.cinemaapp.Theater
import com.example.cinemaapp.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CinemaUiState(
    val nowPlaying: List<Movie> = emptyList(),
    val upcoming: List<Movie> = emptyList(),
    val theaters: List<Theater> = emptyList(),
    val browseCatalog: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)

    private val _uiState = MutableStateFlow(CinemaUiState())
    val uiState: StateFlow<CinemaUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // Show cached data immediately if available
            val cached = repository.loadCache()
            if (cached != null) {
                _uiState.value = CinemaUiState(
                    nowPlaying = cached.nowPlaying,
                    upcoming = cached.upcoming,
                    theaters = cached.theaters,
                    browseCatalog = cached.browseCatalog,
                    isLoading = false
                )
            } else {
                _uiState.value = CinemaUiState(isLoading = true)
            }

            // Always refresh from network
            val result = repository.fetchAll()
            if (result.isSuccess) {
                val data = result.getOrThrow()
                repository.saveCache(data)
                _uiState.value = CinemaUiState(
                    nowPlaying = data.nowPlaying,
                    upcoming = data.upcoming,
                    theaters = data.theaters,
                    browseCatalog = data.browseCatalog,
                    isLoading = false
                )
            } else if (cached == null) {
                _uiState.value = CinemaUiState(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error loading data."
                )
            }
            // If network fails but cache exists, keep showing cached data silently
        }
    }
}
