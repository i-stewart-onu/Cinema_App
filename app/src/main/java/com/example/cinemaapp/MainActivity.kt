package com.example.cinemaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                CinemaAppNavigation()
            }
        }
    }
}

// ==========================================
// DATA MODEL & SAMPLE DATA (Feb 2026)
// ==========================================

data class Movie(
    val id: String,
    val title: String,
    val description: String,
    val rating: String,
    val showtimes: String
)

val sampleMovies = listOf(
    Movie(
        id = "1",
        title = "Avatar: Fire and Ash",
        description = "Jake Sully and Neytiri face a new threat: the 'Ash People,' a clan of Na'vi who utilize fire and reject the pacifist ways of Eywa.",
        rating = "PG-13",
        showtimes = "12:00 PM, 3:30 PM, 7:00 PM, 10:45 PM"
    ),
    Movie(
        id = "2",
        title = "Wuthering Heights",
        description = "A bold new adaptation of the classic romance starring Margot Robbie and Jacob Elordi. A story of passion and revenge on the moors.",
        rating = "R",
        showtimes = "4:15 PM, 6:50 PM, 9:30 PM"
    ),
    Movie(
        id = "3",
        title = "Goat",
        description = "An animated sports comedy featuring the voices of Steph Curry and David Harbour about a literal goat trying to make it in the big leagues.",
        rating = "PG",
        showtimes = "10:00 AM, 12:30 PM, 2:45 PM"
    ),
    Movie(
        id = "4",
        title = "Mercy",
        description = "Sci-fi thriller starring Chris Pratt. A detective is accused of a violent crime and must prove his innocence in a future where capital crime has increased.",
        rating = "PG-13",
        showtimes = "1:15 PM, 4:00 PM, 7:30 PM"
    ),
    Movie(
        id = "5",
        title = "Scream 7",
        description = "COMING SOON (Feb 27). The saga continues as Ghostface returns to terrorize a new generation of victims.",
        rating = "R",
        showtimes = "Advance Screening: Feb 26 @ 8:00 PM"
    )
)

// ==========================================
// NAVIGATION LOGIC
// ==========================================

@Composable
fun CinemaAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "movie_list") {

        // Screen 1: List
        composable("movie_list") {
            MovieListScreen(
                movies = sampleMovies,
                onMovieClick = { movieId ->
                    navController.navigate("movie_details/$movieId")
                }
            )
        }

        // Screen 2: Details
        composable(
            route = "movie_details/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            MovieDetailScreen(movieId = movieId)
        }
    }
}

// ==========================================
// UI SCREENS
// ==========================================

@Composable
fun MovieListScreen(
    movies: List<Movie>,
    onMovieClick: (String) -> Unit
) {
    LazyColumn {
        items(movies) { movie ->
            MovieRow(movie = movie, onClick = { onMovieClick(movie.id) })
        }
    }
}

@Composable
fun MovieRow(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun MovieDetailScreen(movieId: String?) {
    val movie = sampleMovies.find { it.id == movieId }

    if (movie != null) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Rated: ${movie.rating}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )
            Text(
                text = "Plot Summary:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Text(
                text = "Local Showtimes:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = movie.showtimes,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Text(text = "Error: Movie not found")
    }
}