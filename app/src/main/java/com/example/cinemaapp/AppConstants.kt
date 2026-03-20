package com.example.cinemaapp

object AppConstants {
    const val TMDB_API_KEY = "1643a8d37f56c5ae6fce323b5f71b345"
    const val SERP_API_KEY = "451e88fff8571af10e2892771bad2ee9334263378109aa496d0871a826b7f37d"

    const val SERP_SEARCH_LOCATION = "Ada, Ohio, United States"

    val MAJOR_PROVIDER_IDS = listOf(8, 337, 15, 1899, 531, 350, 9, 386, 613, 283, 526, 43, 257)

    val GENRE_MAP = mapOf(
        28 to "Action", 12 to "Adventure", 16 to "Animation", 35 to "Comedy",
        80 to "Crime", 99 to "Documentary", 18 to "Drama", 10751 to "Family",
        14 to "Fantasy", 36 to "History", 27 to "Horror", 10402 to "Music",
        9648 to "Mystery", 10749 to "Romance", 878 to "Science Fiction",
        53 to "Thriller", 10752 to "War", 37 to "Western"
    )

    val PROVIDER_ID_TO_DISPLAY = mapOf(
        8    to "Netflix",
        337  to "Disney+",
        15   to "Hulu",
        1899 to "Max",
        531  to "Paramount+",
        350  to "Apple TV+",
        9    to "Prime Video",
        386  to "Peacock",
        613  to "Tubi",
        283  to "Crunchyroll",
        526  to "AMC+",
        43   to "Starz",
        257  to "fuboTV"
    )
}
