package uz.ttpu.movieshelf.presentation.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.ttpu.movieshelf.domain.model.Movie
import uz.ttpu.movieshelf.ui.theme.MovieShelfTheme

@Composable
fun MovieListScreen(
    state: MovieListUiState,
    onFavoriteClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoritesOnlyChange: (Boolean) -> Unit = {},
) {
    when (state) {
        MovieListUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is MovieListUiState.Error -> Column(
            modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(state.message)
            Button(onClick = onRefresh, modifier = Modifier.padding(top = 12.dp)) { Text("Retry") }
        }

        is MovieListUiState.Success -> Column(modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FilterChip(
                    selected = state.favoritesOnly,
                    onClick = { onFavoritesOnlyChange(!state.favoritesOnly) },
                    label = { Text("Favorites only") },
                )
                TextButton(onClick = onRefresh) { Text("Refresh") }
            }
            if (state.isFromCache) {
                Text(
                    "Offline — showing saved data",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.movies, key = { it.id }) { movie ->
                    MovieItem(movie = movie, onFavoriteClick = { onFavoriteClick(movie.id) })
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, onFavoriteClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(movie.title, fontWeight = FontWeight.Bold)
            Text("${movie.year} · Rating ${movie.rating}")
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (movie.isFavorite) "Remove ${movie.title} from favorites"
                else "Add ${movie.title} to favorites",
            )
        }
    }
}

private val sample = listOf(
    Movie(6, "Echoes of Khiva", 2022, 9.0, isFavorite = true),
    Movie(3, "Registan Rhapsody", 2018, 8.4),
    Movie(1, "The Silk Road Express", 2019, 8.4),
)

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() = MovieShelfTheme {
    MovieListScreen(MovieListUiState.Loading, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun SuccessPreview() = MovieShelfTheme {
    MovieListScreen(MovieListUiState.Success(sample, isFromCache = false), {}, {})
}

@Preview(showBackground = true)
@Composable
private fun CachedPreview() = MovieShelfTheme {
    MovieListScreen(MovieListUiState.Success(sample, isFromCache = true), {}, {})
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() = MovieShelfTheme {
    MovieListScreen(
        MovieListUiState.Error("Can't load movies. Check your connection and try again."), {}, {},
    )
}
