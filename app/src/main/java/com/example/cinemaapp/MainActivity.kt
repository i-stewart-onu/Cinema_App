package com.example.cinemaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cinemaapp.ui.theme.CinemaAppTheme

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
    val imageResource: Int,
    val reviewScore: Double,
    val streamingPlatform: String
)

enum class SortOption {
    TITLE,
    REVIEW_SCORE,
    RATING,
    STREAMING
}

enum class MainTab(val route: String, val label: String) {
    MOVIES("movie_list", "MOVIES"),
    THEATERS("theaters", "THEATERS")
}

val sampleMovies = listOf(
    Movie("1","Avatar: Fire and Ash","Jake Sully and Neytiri face a new threat: the 'Ash People,' a clan of Na'vi who utilize fire and reject the pacifist ways of Eywa.","PG-13",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("3:45PM","8:00PM")),R.drawable.avatar,7.4,"N/A"),
    Movie("2","Wuthering Heights","A bold new adaptation of the classic romance starring Margot Robbie and Jacob Elordi. A story of passion and revenge on the moors.","R",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.wuthering_heights,6.3,"N/A"),
    Movie("3","Goat","An animated sports comedy featuring the voices of Steph Curry and David Harbour about a literal goat trying to make it in the big leagues.","PG",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.goat,6.9,"N/A"),
    Movie("4","Mercy","Sci-fi thriller starring Chris Pratt. A detective is accused of a violent crime and must prove his innocence in a future where capital crime has increased.","PG-13",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.mercy,6.2,"Amazon Prime Video"),
    Movie("5","Scream 7","COMING SOON (Feb 27). The saga continues as Ghostface returns to terrorize a new generation of victims.","R",listOf("Ada Theatre, Ada" to listOf("Not Showing"),"Regal American Mall, Lima" to listOf("12:10PM","12:40PM","3:20PM"),"AMC, Findlay" to listOf("3:15PM","4:20PM","6:15PM")),R.drawable.scream_7,6.1,"Paramount+ (COMING SOON)"),
    Movie("6","Iron Lung","Survivors of the apocalypse send a convict in a small submarine to explore a desolate moon that's an ocean of blood.","R",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.iron_lung,6.2,"N/A"),
    Movie("7","Project Hail Mary","Science teacher Ryland Grace wakes up on a spaceship with no recollection of who he is or how he got there.","PG-13",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.hail_mary,0.0,"N/A"),
    Movie("8","Hoppers","When scientists discover a way to transform human consciousness into robotic animals, Mabel uses the new technology to uncover mysteries.","PG",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.hoppers,0.0,"N/A"),
    Movie("9","Interstellar","When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot is tasked to pilot a spacecraft.","PG-13",listOf("Ada Theatre, Ada" to listOf("N/A"),"Regal American Mall, Lima" to listOf("N/A"),"AMC, Findlay" to listOf("N/A")),R.drawable.interstellar,8.7,"Pluto TV (FREE)")
)

@Composable
fun CinemaAppNavigation() {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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
                        text = {
                            Text(tab.label)
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.MOVIES.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.MOVIES.route) {
                MovieListScreen(
                    movies = sampleMovies,
                    onMovieClick = { movieId ->
                        navController.navigate("movie_details/$movieId")
                    }
                )
            }
            composable(
                route = "movie_details/{movieId}",
                arguments = listOf(navArgument("movieId") { type = NavType.StringType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId")

                MovieDetailScreen(
                    movieId = movieId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(MainTab.THEATERS.route) {
                TheaterListScreen(
                    onTheaterClick = { theaterName ->
                        navController.navigate("theater_details/$theaterName")
                    }
                )
            }
            composable(
                route = "theater_details/{theaterName}",
                arguments = listOf(navArgument("theaterName") { type = NavType.StringType })
            ) { backStackEntry ->
                val theaterName = backStackEntry.arguments?.getString("theaterName")

                TheaterDetailScreen(
                    theaterName = theaterName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
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
        Image(
            painter = painterResource(movie.imageResource),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    movies: List<Movie>,
    onMovieClick: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.TITLE) }
    var expanded by remember { mutableStateOf(false) }

    val filteredMovies = movies
        .filter { it.title.contains(searchText, ignoreCase = true) }
        .sortedWith(
            when (sortOption) {
                SortOption.TITLE -> compareBy { it.title }
                SortOption.REVIEW_SCORE -> compareByDescending { it.reviewScore }
                SortOption.RATING -> compareBy { it.rating }
                SortOption.STREAMING -> compareBy { it.streamingPlatform }
            }
        )

    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("SPOTLIGHT LIVE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort By:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box {
                    Text(
                        text = sortOption.name.replace("_", " "),
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Title (A-Z)") },
                            onClick = {
                                sortOption = SortOption.TITLE
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Review Score") },
                            onClick = {
                                sortOption = SortOption.REVIEW_SCORE
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rating") },
                            onClick = {
                                sortOption = SortOption.RATING
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Streaming Platform") },
                            onClick = {
                                sortOption = SortOption.STREAMING
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (filteredMovies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No movies found",
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic
                    )
                }
            } else {
                LazyColumn {
                    items(filteredMovies) { movie ->
                        MovieRow(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) }
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
                title = { Text(text = "SPOTLIGHT LIVE", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
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
                Image(
                    painter = painterResource(movie.imageResource),
                    contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                )
                Text(text = movie.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = movie.description, style = MaterialTheme.typography.titleMedium)
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
                Row{
                    Text(text = "Review Score: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = movie.reviewScore.toString(), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Row{
                    Text(text = "Streaming Platform: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = movie.streamingPlatform, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Local Showtimes:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                text = if (expanded) {"▲"} else {"▼"},
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (expanded) {
            Column {
                times.forEach { time ->
                    Text(
                        text = "⬤ $time",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

data class TheaterMovie(
    val movieTitle: String,
    val times: List<String>
)

data class Theater(
    val name: String,
    val movies: List<TheaterMovie>
)

fun buildTheaters(movies: List<Movie>): List<Theater> {
    val theaterMap = mutableMapOf<String, MutableList<TheaterMovie>>()

    for (movie in movies) {
        for (showtime in movie.showtimes) {
            val theaterName = showtime.first
            val times = showtime.second
            val validTimes = mutableListOf<String>()

            for (time in times) {
                if (time != "N/A" && time != "Not Showing") {
                    validTimes.add(time)
                }
            }

            if (validTimes.isNotEmpty()) {
                val theaterMovie = TheaterMovie(
                    movieTitle = movie.title,
                    times = validTimes
                )

                if (theaterMap.containsKey(theaterName)) {
                    theaterMap[theaterName]?.add(theaterMovie)
                } else {
                    theaterMap[theaterName] = mutableListOf(theaterMovie)
                }
            }
        }
    }

    val theaterList = mutableListOf<Theater>()

    for (entry in theaterMap) {
        theaterList.add(
            Theater(
                name = entry.key,
                movies = entry.value
            )
        )
    }

    return theaterList
}

fun getTheaterAddress(theaterName: String?): String {
    if (theaterName == "Ada Theatre, Ada") {
        return "215 S Main St, Ada, OH 45810"
    }
    if (theaterName == "Regal American Mall, Lima") {
        return "2830 W Elm St, Lima, OH 45805"
    }
    if (theaterName == "AMC, Findlay") {
        return "906 Interstate Dr, Findlay, OH 45840"
    }
    return "Address not available"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheaterListScreen(
    onTheaterClick: (String) -> Unit
) {
    val theaters = buildTheaters(sampleMovies)
    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "SPOTLIGHT LIVE",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(10.dp)
        ) {
            items(theaters) { theater ->
                TheaterCard(
                    theater = theater,
                    onClick = { onTheaterClick(theater.name) }
                )
            }
        }
    }
}

@Composable
fun TheaterCard(
    theater: Theater,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
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
    theaterName: String?,
    onBack: () -> Unit
) {
    val theaters = buildTheaters(sampleMovies)
    val theater = theaters.find { it.name == theaterName }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "SPOTLIGHT LIVE",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
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
                    text = getTheaterAddress(theaterName),
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
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = movie.movieTitle,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            movie.times.forEach { time ->
                                Text(
                                    text = "⬤ $time"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}