package uz.ttpu.movieshelf.data.repository

import uz.ttpu.movieshelf.data.local.MovieLocalDataSource
import uz.ttpu.movieshelf.data.mapper.toDomain
import uz.ttpu.movieshelf.data.remote.MovieRemoteDataSource
import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.repository.MovieRepository
import java.io.IOException

class MovieRepositoryImpl(
    private val remote: MovieRemoteDataSource,
    private val local: MovieLocalDataSource,
) : MovieRepository {

    override suspend fun getMovies(): MoviesResult {
        // Local storage is the source of truth for favorites.
        val favoriteIds = local.getFavoriteIds()
        return try {
            val dtos = remote.fetchMovies()          // fresh remote data wins
            local.saveMovies(dtos)
            MoviesResult(
                movies = dtos.map { it.toDomain(it.id in favoriteIds) },
                isFromCache = false,
            )
        } catch (e: IOException) {                   // only IOException; CancellationException passes through
            val cached = local.getCachedMovies() ?: throw e
            MoviesResult(
                movies = cached.map { it.toDomain(it.id in favoriteIds) },
                isFromCache = true,
            )
        }
    }

    override suspend fun toggleFavorite(movieId: Int): Boolean {
        val newValue = movieId !in local.getFavoriteIds()
        local.setFavorite(movieId, newValue)
        return newValue
    }
}
