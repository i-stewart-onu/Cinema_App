package com.example.cinemaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
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

class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()

    private val _uiState = MutableStateFlow(CinemaUiState())
    val uiState: StateFlow<CinemaUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CinemaUiState(isLoading = true)
            val result = repository.fetchAll()
            _uiState.value = if (result.isSuccess) {
                val data = result.getOrThrow()
                CinemaUiState(
                    nowPlaying = data.nowPlaying,
                    upcoming = data.upcoming,
                    theaters = data.theaters,
                    browseCatalog = data.browseCatalog,
                    isLoading = false
                )
            } else {
                CinemaUiState(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error loading data."
                )
            }
        }
    }
}
