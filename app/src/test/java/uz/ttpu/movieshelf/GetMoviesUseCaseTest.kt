package uz.ttpu.movieshelf

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.ttpu.movieshelf.domain.model.Movie
import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.repository.MovieRepository
import uz.ttpu.movieshelf.domain.usecase.GetFavoriteMoviesUseCase
import uz.ttpu.movieshelf.domain.usecase.GetMoviesUseCase

class FakeMovieRepository(
    private val result: MoviesResult,
) : MovieRepository {
    override suspend fun getMovies() = result
    override suspend fun toggleFavorite(movieId: Int) = true
}

class GetMoviesUseCaseTest {

    @Test
    fun movies_are_sorted_by_rating_desc_then_title() = runBlocking<Unit> {
        val repo = FakeMovieRepository(
            MoviesResult(
                listOf(Movie(1, "B", 2020, 8.0), Movie(2, "A", 2021, 8.0), Movie(3, "C", 2022, 9.1)),
                isFromCache = false,
            )
        )
        val result = GetMoviesUseCase(repo)()
        assertEquals(listOf("C", "A", "B"), result.movies.map { it.title })
    }

    @Test
    fun is_from_cache_flag_is_passed_through() = runBlocking<Unit> {
        val movies = listOf(Movie(1, "A", 2020, 8.0))
        assertTrue(GetMoviesUseCase(FakeMovieRepository(MoviesResult(movies, true)))().isFromCache)
        assertFalse(GetMoviesUseCase(FakeMovieRepository(MoviesResult(movies, false)))().isFromCache)
    }

    @Test
    fun favorites_use_case_keeps_only_favorites() = runBlocking<Unit> {
        val repo = FakeMovieRepository(
            MoviesResult(
                listOf(
                    Movie(1, "A", 2020, 8.0, isFavorite = true),
                    Movie(2, "B", 2021, 9.0),
                    Movie(3, "C", 2022, 7.0, isFavorite = true),
                ),
                isFromCache = true,
            )
        )
        val result = GetFavoriteMoviesUseCase(GetMoviesUseCase(repo))()
        assertEquals(listOf("A", "C"), result.movies.map { it.title })
        assertTrue(result.isFromCache)
    }
}
