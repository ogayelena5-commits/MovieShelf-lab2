package uz.ttpu.movieshelf.domain.usecase

import uz.ttpu.movieshelf.domain.model.MoviesResult
import uz.ttpu.movieshelf.domain.repository.MovieRepository

// Task 5 (before the fix in Task 6): the use case depended on MovieRepositoryImpl,
// i.e. the domain (inner layer) imported a class from the data layer (outer layer):
//   domain -> data. The Dependency Rule says source-code dependencies must point
// inward only (data -> domain), so this arrow is reversed. Task 6 fixes it:
// the use case now depends on the MovieRepository interface owned by the domain.
class GetMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): MoviesResult {
        val result = repository.getMovies()
        return result.copy(
            movies = result.movies.sortedWith(
                compareByDescending<uz.ttpu.movieshelf.domain.model.Movie> { it.rating }
                    .thenBy { it.title }
            )
        )
    }
}
