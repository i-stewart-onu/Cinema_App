package com.example.cinemaapp.data.repository

import android.util.Log
import com.example.cinemaapp.AppConstants
import com.example.cinemaapp.Movie
import com.example.cinemaapp.Theater
import com.example.cinemaapp.TheaterMovie
import com.example.cinemaapp.data.api.NetworkModule
import com.example.cinemaapp.data.api.TmdbMovie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

private const val TAG = "MovieRepository"

private fun usCertification(results: List<com.example.cinemaapp.data.api.TmdbCountryRelease>): String? {
    val usDates = results.firstOrNull { it.country == "US" }?.releaseDates ?: return null
    return usDates.firstOrNull { it.type == 3 && it.certification.isNotBlank() }?.certification
        ?: usDates.firstOrNull { it.certification.isNotBlank() }?.certification
}

private fun normalizeTitle(title: String): String =
    title.lowercase()
        .replace(Regex("[^a-z0-9 ]"), "")
        .replace(Regex("^(the |a |an )"), "")
        .trim()

data class CinemaData(
    val nowPlaying: List<Movie>,
    val upcoming: List<Movie>,
    val theaters: List<Theater>,
    val browseCatalog: List<Movie>
)

class MovieRepository {
    private val tmdbService = NetworkModule.tmdbService
    private val serpApiService = NetworkModule.serpApiService

    suspend fun fetchAll(): Result<CinemaData> = withContext(Dispatchers.IO) {
        try {
            val moviesPlayingResponse = runCatching {
                serpApiService.getMoviesPlaying(
                    location = AppConstants.SERP_SEARCH_LOCATION,
                    apiKey = AppConstants.SERP_API_KEY
                )
            }.onFailure { Log.e(TAG, "getMoviesPlaying failed: ${it.message}") }
             .getOrNull()

            val moviesPlaying = moviesPlayingResponse?.knowledgeGraph?.moviesPlaying ?: emptyList()
            Log.d(TAG, "Movies in knowledge_graph: ${moviesPlaying.size}")
            moviesPlaying.forEach { Log.d(TAG, "  - ${it.name}") }

            val tmdbUpcomingDeferred = async {
                runCatching { tmdbService.getUpcoming(apiKey = AppConstants.TMDB_API_KEY) }.getOrNull()
            }
            val popularDeferreds = (1..5).map { page ->
                async {
                    runCatching { tmdbService.getPopular(apiKey = AppConstants.TMDB_API_KEY, page = page) }.getOrNull()
                }
            }
            val topRatedDeferreds = (1..5).map { page ->
                async {
                    runCatching { tmdbService.getTopRated(apiKey = AppConstants.TMDB_API_KEY, page = page) }.getOrNull()
                }
            }
            // 2 pages per provider so each service has solid movie coverage
            val discoverDeferreds = AppConstants.MAJOR_PROVIDER_IDS.flatMap { providerId ->
                (1..2).map { page ->
                    async {
                        runCatching { tmdbService.discoverByProvider(apiKey = AppConstants.TMDB_API_KEY, providerId = providerId, page = page) }.getOrNull()
                    }
                }
            }

            val showtimeDeferreds = moviesPlaying.mapNotNull { it.name }.map { movieName ->
                async {
                    val result = runCatching {
                        serpApiService.getMovieShowtimes(
                            query = "$movieName showtimes",
                            location = AppConstants.SERP_SEARCH_LOCATION,
                            apiKey = AppConstants.SERP_API_KEY
                        )
                    }.onFailure { Log.e(TAG, "getMovieShowtimes failed for $movieName: ${it.message}") }
                     .getOrNull()
                    movieName to result
                }
            }

            val tmdbUpcoming = tmdbUpcomingDeferred.await()
            val showtimeResults = showtimeDeferreds.awaitAll()
            val popularPages = popularDeferreds.awaitAll()
            val topRatedPages = topRatedDeferreds.awaitAll()
            val discoverPages = discoverDeferreds.awaitAll()

            // Build streaming map: movieId -> sorted comma-separated display names
            // Derived directly from discover results — no individual watch provider calls needed
            val movieStreamingMap = mutableMapOf<Int, MutableSet<String>>()
            AppConstants.MAJOR_PROVIDER_IDS.forEachIndexed { i, providerId ->
                val displayName = AppConstants.PROVIDER_ID_TO_DISPLAY[providerId] ?: return@forEachIndexed
                listOf(discoverPages[i * 2], discoverPages[i * 2 + 1]).forEach { page ->
                    page?.results?.forEach { movie ->
                        movieStreamingMap.getOrPut(movie.id) { mutableSetOf() }.add(displayName)
                    }
                }
            }
            Log.d(TAG, "Movies with streaming data: ${movieStreamingMap.size}")

            val movieTheaterTimes = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()
            val theaterAddresses = mutableMapOf<String, String>()
            for ((movieName, response) in showtimeResults) {
                val todayShowtimes = response?.showtimes?.firstOrNull() ?: run {
                    Log.d(TAG, "$movieName: no showtimes in response")
                    continue
                }
                val theaterMap = movieTheaterTimes.getOrPut(movieName) { mutableMapOf() }
                for (serpTheater in todayShowtimes.theaters ?: emptyList()) {
                    val theaterName = serpTheater.name ?: continue
                    theaterAddresses.putIfAbsent(theaterName, serpTheater.address ?: "")
                    val times = theaterMap.getOrPut(theaterName) { mutableSetOf() }
                    serpTheater.showing?.forEach { showing ->
                        showing.time?.forEach { times.add(it) }
                    }
                }
                Log.d(TAG, "$movieName: ${theaterMap.size} theaters")
            }
            Log.d(TAG, "Total movies with theater data: ${movieTheaterTimes.size}")

            val tmdbSearchResults: Map<String, TmdbMovie?> = movieTheaterTimes.keys
                .map { title ->
                    async {
                        val result = runCatching {
                            tmdbService.searchMovie(
                                apiKey = AppConstants.TMDB_API_KEY,
                                query = title
                            ).results.firstOrNull()
                        }.getOrNull()
                        title to result
                    }
                }
                .awaitAll()
                .toMap()

            val upcomingRaw = tmdbUpcoming?.results ?: emptyList()
            val popularRaw = (popularPages + topRatedPages + discoverPages)
                .flatMap { it?.results ?: emptyList() }
                .distinctBy { it.id }
            val allTmdbMovies: List<TmdbMovie> =
                (tmdbSearchResults.values.filterNotNull() + upcomingRaw + popularRaw).distinctBy { it.id }

            val ratingsDeferreds = allTmdbMovies.map { tmdb ->
                async {
                    val cert = runCatching {
                        tmdbService.getReleaseDates(tmdb.id, AppConstants.TMDB_API_KEY)
                            .results.let { usCertification(it) }
                    }.getOrNull()
                    tmdb.id to cert
                }
            }

            val ratingsMap: Map<Int, String> = ratingsDeferreds.awaitAll()
                .mapNotNull { (id, cert) -> cert?.let { id to it } }
                .toMap()

            val nowPlayingMovies = movieTheaterTimes.entries.mapIndexed { index, (title, theaterTimesMap) ->
                val tmdb = tmdbSearchResults[title]
                if (tmdb == null) Log.d(TAG, "No TMDb match for: $title")
                Movie(
                    id = "np_${index + 1}",
                    title = title,
                    description = tmdb?.overview?.ifBlank { null } ?: "No description available.",
                    rating = tmdb?.id?.let { ratingsMap[it] } ?: "N/A",
                    showtimes = theaterTimesMap.map { (theater, times) -> theater to times.sorted() },
                    imageUrl = tmdb?.posterPath?.let { "${NetworkModule.TMDB_IMAGE_BASE_URL}$it" },
                    reviewScore = tmdb?.voteAverage ?: 0.0,
                    streamingPlatform = tmdb?.id?.let { movieStreamingMap[it]?.sorted()?.joinToString(", ") } ?: "N/A",
                    releaseDate = tmdb?.releaseDate ?: "",
                    genres = tmdb?.genreIds?.mapNotNull { AppConstants.GENRE_MAP[it] } ?: emptyList()
                )
            }

            val inTheatersTitles = movieTheaterTimes.keys.map { normalizeTitle(it) }.toSet()
            val upcomingMovies = upcomingRaw
                .filterNot { normalizeTitle(it.title) in inTheatersTitles }
                .mapIndexed { index, tmdb ->
                    Movie(
                        id = "cs_${index + 1}",
                        title = tmdb.title,
                        description = tmdb.overview.ifBlank { "No description available." },
                        rating = ratingsMap[tmdb.id] ?: "N/A",
                        showtimes = emptyList(),
                        imageUrl = tmdb.posterPath?.let { "${NetworkModule.TMDB_IMAGE_BASE_URL}$it" },
                        reviewScore = tmdb.voteAverage,
                        streamingPlatform = movieStreamingMap[tmdb.id]?.sorted()?.joinToString(", ") ?: "N/A",
                        releaseDate = tmdb.releaseDate ?: "",
                        genres = tmdb.genreIds?.mapNotNull { AppConstants.GENRE_MAP[it] } ?: emptyList()
                    )
                }

            val theaterMoviesMap = mutableMapOf<String, MutableList<TheaterMovie>>()
            for (movie in nowPlayingMovies) {
                for ((theater, times) in movie.showtimes) {
                    theaterMoviesMap.getOrPut(theater) { mutableListOf() }
                        .add(TheaterMovie(movie.title, times))
                }
            }
            val theaters = theaterMoviesMap.map { (name, movies) ->
                Theater(name, movies, theaterAddresses[name] ?: "")
            }

            val browseCatalog = popularRaw
                .mapIndexed { index, tmdb ->
                    Movie(
                        id = "browse_${index + 1}",
                        title = tmdb.title,
                        description = tmdb.overview.ifBlank { "No description available." },
                        rating = ratingsMap[tmdb.id] ?: "N/A",
                        showtimes = emptyList(),
                        imageUrl = tmdb.posterPath?.let { "${NetworkModule.TMDB_IMAGE_BASE_URL}$it" },
                        reviewScore = tmdb.voteAverage,
                        streamingPlatform = movieStreamingMap[tmdb.id]?.sorted()?.joinToString(", ") ?: "N/A",
                        releaseDate = tmdb.releaseDate ?: "",
                        genres = tmdb.genreIds?.mapNotNull { AppConstants.GENRE_MAP[it] } ?: emptyList()
                    )
                }
            Log.d(TAG, "Browse catalog: ${browseCatalog.size} movies")

            Result.success(CinemaData(nowPlayingMovies, upcomingMovies, theaters, browseCatalog))
        } catch (e: Exception) {
            Log.e(TAG, "fetchAll failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
