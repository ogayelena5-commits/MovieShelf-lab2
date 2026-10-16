package uz.ttpu.movieshelf.data.mapper

import uz.ttpu.movieshelf.data.remote.MovieDto
import uz.ttpu.movieshelf.domain.model.Movie

// The mapper lives in the DATA layer: it must know MovieDto (a detail of the
// server/JSON format), and the domain must not know about DTOs. The dependency
// points data -> domain, never domain -> data.
fun MovieDto.toDomain(isFavorite: Boolean): Movie = Movie(
    id = id,
    title = title,
    year = releaseYear,
    rating = score,
    isFavorite = isFavorite,
)
