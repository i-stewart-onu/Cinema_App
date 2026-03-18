package com.example.cinemaapp.data.api

import com.google.gson.annotations.SerializedName

data class TmdbNowPlayingResponse(
    @SerializedName("results") val results: List<TmdbMovie>,
    @SerializedName("total_pages") val totalPages: Int
)

data class TmdbMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?
)

data class TmdbReleaseDatesResponse(
    @SerializedName("results") val results: List<TmdbCountryRelease>
)

data class TmdbCountryRelease(
    @SerializedName("iso_3166_1") val country: String,
    @SerializedName("release_dates") val releaseDates: List<TmdbReleaseDate>
)

data class TmdbReleaseDate(
    @SerializedName("certification") val certification: String,
    @SerializedName("type") val type: Int
)

