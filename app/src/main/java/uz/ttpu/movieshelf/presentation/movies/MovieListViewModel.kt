package uz.ttpu.movieshelf.presentation.movies

/*
 * Task 15 — analysis
 *
 * (1) Order of calls (control flow, inward):
 *     MovieItem (heart click) -> MovieListViewModel.onFavoriteClick
 *       -> ToggleFavoriteUseCase.invoke -> MovieRepositoryImpl.toggleFavorite
 *       -> SharedPrefsMovieLocalDataSource.setFavorite
 *     and then the Boolean result travels back out the same way.
 *
 * (2) Source-code dependencies between the same five classes:
 *     MovieItem/ViewModel -> ToggleFavoriteUseCase            (presentation -> domain)
 *     ToggleFavoriteUseCase -> MovieRepository (interface)    (domain -> domain)
 *     MovieRepositoryImpl  -> MovieRepository                 (data -> domain)   <== opposite to the call
 *     MovieRepositoryImpl  -> MovieLocalDataSource            (data -> data)
 *     SharedPrefsMovieLocalDataSource -> MovieLocalDataSource (data -> data)
 *     At run time the use case calls the repository implementation, but in the source
 *     code the implementation depends on the use case's interface, not the other way
 *     round. That is Dependency Inversion: the domain owns the interface, the data
 *     layer implements it, so every source-code arrow points inward.
 *
 * (3) The use case lives in a pure Kotlin module (no Android on the classpath), so
 *     android.util.Log does not exist there. The clean way: declare an interface
 *     Logger in the domain, let the use case depend on it, implement it in an outer
 *     layer (e.g. AndroidLogger using Log.d) and pass it in from the composition
 *     root — Dependency Inversion again.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.ttpu.movieshelf.domain.model.Movie
import uz.ttpu.movieshelf.domain.usecase.GetFavoriteMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.GetMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.ToggleFavoriteUseCase
import java.io.IOException

sealed interface MovieListUiState {
    data object Loading : MovieListUiState
    data class Success(
        val movies: List<Movie>,
        val isFromCache: Boolean,
        val favoritesOnly: Boolean = false,
    ) : MovieListUiState
    data class Error(val message: String) : MovieListUiState
}

class MovieListViewModel(
    private val getMovies: GetMoviesUseCase,
    private val getFavoriteMovies: GetFavoriteMoviesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    val state: StateFlow<MovieListUiState> = _state.asStateFlow()

    private var favoritesOnly = false
    private var loadJob: Job? = null

    init { load() }

    fun onRefresh() = load()

    fun onFavoritesOnlyChange(enabled: Boolean) {
        favoritesOnly = enabled
        load()
    }

    fun onFavoriteClick(movieId: Int) {
        viewModelScope.launch {
            val nowFavorite = toggleFavorite(movieId)
            val current = _state.value as? MovieListUiState.Success ?: return@launch
            val updated = current.movies.map {
                if (it.id == movieId) it.copy(isFavorite = nowFavorite) else it
            }
            _state.value = current.copy(
                // in the Favorites view an un-favorited movie disappears immediately
                movies = if (current.favoritesOnly) updated.filter { it.isFavorite } else updated,
            )
        }
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = MovieListUiState.Loading
        val onlyFavorites = favoritesOnly
        loadJob = viewModelScope.launch {
            try {
                val result = if (onlyFavorites) getFavoriteMovies() else getMovies()
                _state.value = MovieListUiState.Success(
                    movies = result.movies,
                    isFromCache = result.isFromCache,
                    favoritesOnly = onlyFavorites,
                )
            } catch (e: IOException) {
                _state.value = MovieListUiState.Error(
                    "Can't load movies. Check your connection and try again."
                )
            }
        }
    }
}
