package uz.ttpu.movieshelf.data.remote

data class MovieDto(
    val id: Int,
    val title: String,
    val releaseYear: Int,
    val score: Double,
)

// suspend: network/disk I/O must not block the main thread; the caller decides
// the dispatcher/scope, and the function can suspend while waiting.
interface MovieRemoteDataSource {
    suspend fun fetchMovies(): List<MovieDto>
}
