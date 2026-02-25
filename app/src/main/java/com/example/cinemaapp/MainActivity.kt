package com.example.cinemaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.*

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
    val showtimes: List<String>,
    val imageResource: Int,
    val reviewScore: Double,
    val streamingPlatform: String
)

val sampleMovies = listOf(
    Movie(
        id = "1",
        title = "Avatar: Fire and Ash",
        description = "Jake Sully and Neytiri face a new threat: the 'Ash People,' a clan of Na'vi who utilize fire and reject the pacifist ways of Eywa.",
        rating = "PG-13",
        showtimes = listOf("12pm", "4pm", "8pm"),
        imageResource = R.drawable.avatar,
        reviewScore = 7.4,
        streamingPlatform = "Disney+"
    ),
    Movie(
        id = "2",
        title = "Wuthering Heights",
        description = "A bold new adaptation of the classic romance starring Margot Robbie and Jacob Elordi. A story of passion and revenge on the moors.",
        rating = "R",
        showtimes = listOf("12pm", "1pm", "3pm", "4pm", "6pm", "7pm", "9pm", "10pm"),
        imageResource = R.drawable.wuthering_heights,
        reviewScore = 6.3,
        streamingPlatform = "Max"
    ),
    Movie(
        id = "3",
        title = "Goat",
        description = "An animated sports comedy featuring the voices of Steph Curry and David Harbour about a literal goat trying to make it in the big leagues.",
        rating = "PG",
        showtimes = listOf("12pm", "1pm", "3pm", "6pm", "7pm", "9pm"),
        imageResource = R.drawable.goat,
        reviewScore = 6.9,
        streamingPlatform = "Netflix"
    ),
    Movie(
        id = "4",
        title = "Mercy",
        description = "Sci-fi thriller starring Chris Pratt. A detective is accused of a violent crime and must prove his innocence in a future where capital crime has increased.",
        rating = "PG-13",
        showtimes = listOf("1pm", "4pm", "8pm"),
        imageResource = R.drawable.mercy,
        reviewScore = 6.2,
        streamingPlatform = "Amazon Prime Video"
    ),
    Movie(
        id = "5",
        title = "Scream 7",
        description = "COMING SOON (Feb 27). The saga continues as Ghostface returns to terrorize a new generation of victims.",
        rating = "R",
        showtimes = listOf("N/A"),
        imageResource = R.drawable.scream_7,
        reviewScore = 0.0,
        streamingPlatform = "Paramount+"
    ),
    Movie(
        id = "6",
        title = "Iron Lung",
        description = "Survivors of the apocalypse send a convict in a small submarine to explore a desolate moon that's an ocean of blood.",
        rating = "R",
        showtimes = listOf("6pm"),
        imageResource = R.drawable.iron_lung,
        reviewScore = 8.0,
        streamingPlatform = "N/A"
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
            MovieDetailScreen(movieId = movieId, onBack = {navController.popBackStack()})
        }
    }
}

// ==========================================
// UI SCREENS
// ==========================================

@Composable
fun MovieRow(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Image(
            painter =  painterResource(movie.imageResource),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    movies: List<Movie>,
    onMovieClick: (String) -> Unit
) {
    var searchText by remember {mutableStateOf("")}

    val filteredMovies = movies.filter {
        it.title.contains(searchText, ignoreCase = true)
    }

    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            MediumTopAppBar(
                title = {
                    Text("Cinema App")
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseSurface,
                    titleContentColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {searchText = it},
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                singleLine = true
            )
            if(filteredMovies.isEmpty()) {
                Text(
                    text = "No movies found.",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                LazyColumn {
                    items(filteredMovies) { movie ->
                        MovieRow(
                            movie = movie,
                            onClick = {onMovieClick(movie.id)}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String?,
    onBack: () -> Unit
) {

    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val scrollState = rememberScrollState()

    val movie = sampleMovies.find { it.id == movieId }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(movie?.title ?: "Movie") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseSurface,
                    titleContentColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (movie != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Image(
                        painter =  painterResource(movie.imageResource),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        text = "Description:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Review Score:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = movie.reviewScore.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Streaming Platform:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = movie.streamingPlatform,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Local Showtimes:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column (
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Ada",
                                style = MaterialTheme.typography.titleMedium
                            )
                            movie.showtimes.forEach {
                                Text(text = "-") //placeholder
                            }
                        }
                        Column (
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Lima",
                                style = MaterialTheme.typography.titleMedium
                            )
                            movie.showtimes.forEach {
                                Text(text = it)
                            }
                        }
                        Column (
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Findlay",
                                style = MaterialTheme.typography.titleMedium
                            )
                            movie.showtimes.forEach {
                                Text(text = "-") //placeholder
                            }
                        }
                    }
                }
            }
        }
    }
}