package com.example.cinemaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import java.time.LocalDate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.SubcomposeAsyncImage
import com.example.cinemaapp.ui.theme.CinemaAppTheme
import com.example.cinemaapp.ui.viewmodel.CinemaUiState
import com.example.cinemaapp.ui.viewmodel.MovieViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CinemaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CinemaAppNavigation()
                }
            }
        }
    }
}

data class Movie(
    val id: String,
    val title: String,
    val description: String,
    val rating: String,
    val showtimes: List<Pair<String, List<String>>>,
    val imageResource: Int? = null,
    val imageUrl: String? = null,
    val reviewScore: Double,
    val streamingPlatform: String,
    val releaseDate: String = "",
    val genres: List<String> = emptyList()
)

data class TheaterMovie(
    val movieTitle: String,
    val times: List<String>
)

data class Theater(
    val name: String,
    val movies: List<TheaterMovie>,
    val address: String = ""
)

enum class SortOption(val label: String) {
    TITLE("Title (A-Z)"),
    REVIEW_SCORE("Review Score"),
    RATING("Rating"),
    RELEASE_DATE("Release Date"),
    GENRE("Genre"),
    STREAMING("Streaming")
}

fun sortComparator(opt: SortOption): Comparator<Movie> = when (opt) {
    SortOption.TITLE        -> compareBy { it.title.lowercase() }
    SortOption.REVIEW_SCORE -> compareByDescending { it.reviewScore }
    SortOption.RATING       -> compareBy { it.rating }
    SortOption.RELEASE_DATE -> compareByDescending { it.releaseDate }
    SortOption.GENRE        -> compareBy { it.genres.firstOrNull()?.lowercase() ?: "" }
    SortOption.STREAMING    -> compareBy { it.streamingPlatform }
}

enum class MainTab(val route: String, val label: String) {
    NOW_PLAYING("now_playing", "NOW PLAYING"),
    COMING_SOON("coming_soon", "COMING SOON"),
    THEATERS("theaters", "THEATERS"),
    BROWSE("browse", "ALL MOVIES")
}

@Composable
fun CinemaAppNavigation() {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val movieViewModel: MovieViewModel = viewModel()
    val uiState by movieViewModel.uiState.collectAsState()

    fun findMovie(movieId: String?): Movie? =
        uiState.nowPlaying.find { it.id == movieId }
            ?: uiState.upcoming.find { it.id == movieId }
            ?: uiState.browseCatalog.find { it.id == movieId }

    Scaffold(
        bottomBar = {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                MainTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        text = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.NOW_PLAYING.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.NOW_PLAYING.route) {
                MovieListScreen(
                    movies = uiState.nowPlaying,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    emptyMessage = "No movies currently in theaters.",
                    onMovieClick = { navController.navigate("movie_details/$it") },
                    onRefresh = { movieViewModel.loadData() }
                )
            }
            composable(MainTab.COMING_SOON.route) {
                MovieListScreen(
                    movies = uiState.upcoming,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    emptyMessage = "No upcoming movies found.",
                    onMovieClick = { navController.navigate("movie_details/$it") },
                    onRefresh = { movieViewModel.loadData() }
                )
            }
            composable(
                route = "movie_details/{movieId}",
                arguments = listOf(navArgument("movieId") { type = NavType.StringType })
            ) { backStackEntry ->
                MovieDetailScreen(
                    movie = findMovie(backStackEntry.arguments?.getString("movieId")),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(MainTab.THEATERS.route) {
                TheaterListScreen(
                    theaters = uiState.theaters,
                    isLoading = uiState.isLoading,
                    onTheaterClick = { navController.navigate("theater_details/$it") }
                )
            }
            composable(MainTab.BROWSE.route) {
                MovieListScreen(
                    movies = uiState.browseCatalog,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    emptyMessage = "No movies found.",
                    onMovieClick = { navController.navigate("movie_details/$it") },
                    onRefresh = { movieViewModel.loadData() }
                )
            }
            composable(
                route = "theater_details/{theaterName}",
                arguments = listOf(navArgument("theaterName") { type = NavType.StringType })
            ) { backStackEntry ->
                TheaterDetailScreen(
                    theater = uiState.theaters.find {
                        it.name == backStackEntry.arguments?.getString("theaterName")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MoviePoster(movie: Movie, modifier: Modifier = Modifier) {
    if (movie.imageUrl != null) {
        SubcomposeAsyncImage(
            model = movie.imageUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.FillWidth,
            modifier = modifier.fillMaxWidth(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        )
    } else if (movie.imageResource != null) {
        Image(
            painter = painterResource(movie.imageResource),
            contentDescription = movie.title,
            contentScale = ContentScale.FillWidth,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
fun MovieRow(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        MoviePoster(movie = movie)
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Box {
            Text(
                text = selected,
                modifier = Modifier.clickable { expanded = true }.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    movies: List<Movie>,
    isLoading: Boolean,
    error: String?,
    emptyMessage: String,
    onMovieClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var sortPrimary by remember { mutableStateOf(SortOption.TITLE) }
    var sortSecondary by remember { mutableStateOf<SortOption?>(null) }
    var selectedRating by remember { mutableStateOf("All") }
    var selectedGenre by remember { mutableStateOf("All") }
    var selectedPlatform by remember { mutableStateOf("All") }

    val availableRatings = remember(movies) {
        listOf("All") + movies.map { it.rating }.filter { it != "N/A" && it.isNotBlank() }.distinct().sorted()
    }
    val availableGenres = remember(movies) {
        listOf("All") + movies.flatMap { it.genres }.distinct().sorted()
    }
    val availablePlatforms = remember(movies) {
        listOf("All") + movies
            .map { it.streamingPlatform }
            .filter { it != "N/A" && it.isNotBlank() }
            .flatMap { it.split(", ") }
            .distinct()
            .sorted()
    }

    val comparator = sortComparator(sortPrimary).let { primary ->
        sortSecondary?.let { primary.then(sortComparator(it)) } ?: primary
    }

    val filteredMovies = movies
        .filter { it.title.contains(searchText, ignoreCase = true) }
        .filter { selectedRating == "All" || it.rating == selectedRating }
        .filter { selectedGenre == "All" || it.genres.contains(selectedGenre) }
        .filter { selectedPlatform == "All" || it.streamingPlatform.split(", ").contains(selectedPlatform) }
        .sortedWith(comparator)

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("SPOTLIGHT LIVE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown(
                    label = "Sort",
                    selected = sortPrimary.label,
                    options = SortOption.entries.map { it.label },
                    onSelect = { label -> sortPrimary = SortOption.entries.first { it.label == label } }
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterDropdown(
                    label = "Then",
                    selected = sortSecondary?.label ?: "None",
                    options = listOf("None") + SortOption.entries.map { it.label },
                    onSelect = { label -> sortSecondary = SortOption.entries.firstOrNull { it.label == label } }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown(
                    label = "Rating",
                    selected = selectedRating,
                    options = availableRatings,
                    onSelect = { selectedRating = it }
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterDropdown(
                    label = "Genre",
                    selected = selectedGenre,
                    options = availableGenres,
                    onSelect = { selectedGenre = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown(
                    label = "Platform",
                    selected = selectedPlatform,
                    options = availablePlatforms,
                    onSelect = { selectedPlatform = it }
                )
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) { Text("Retry") }
                        }
                    }
                }
                filteredMovies.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        items(filteredMovies) { movie ->
                            MovieRow(movie = movie, onClick = { onMovieClick(movie.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie?,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "SPOTLIGHT LIVE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.surface
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
                MoviePoster(
                    movie = movie,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                )
                Text(text = movie.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = movie.description, style = MaterialTheme.typography.titleMedium)
                if (movie.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = movie.genres.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Text(text = "Rated: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = movie.rating, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Text(text = "Review Score: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (movie.reviewScore > 0) movie.reviewScore.toString() else "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Text(text = "Streaming Platform: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = movie.streamingPlatform, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when {
                        movie.showtimes.isNotEmpty() -> "Local Showtimes:"
                        movie.releaseDate.isNotEmpty() && runCatching {
                            LocalDate.parse(movie.releaseDate).isBefore(LocalDate.now())
                        }.getOrDefault(false) -> "Not in theaters."
                        else -> "Not yet in theaters."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                movie.showtimes.forEach { (theater, times) ->
                    ExpandableTheater(theater, times)
                }
            }
        }
    }
}

@Composable
fun ExpandableTheater(theater: String, times: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = theater,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "▲" else "▼",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (expanded) {
            Column {
                times.forEach { time ->
                    Text(text = "⬤ $time", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheaterListScreen(
    theaters: List<Theater>,
    isLoading: Boolean,
    onTheaterClick: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "SPOTLIGHT LIVE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(10.dp)) {
                items(theaters) { theater ->
                    TheaterCard(theater = theater, onClick = { onTheaterClick(theater.name) })
                }
            }
        }
    }
}

@Composable
fun TheaterCard(theater: Theater, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = theater.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheaterDetailScreen(
    theater: Theater?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "SPOTLIGHT LIVE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (theater != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = theater.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = theater.address.ifEmpty { "Address not available" },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                theater.movies.forEach { movie ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = movie.movieTitle, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            movie.times.forEach { time ->
                                Text(text = "⬤ $time")
                            }
                        }
                    }
                }
            }
        }
    }
}
