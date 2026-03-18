package com.example.cinemaapp.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {
    @GET("search.json?engine=google&q=movie+showtimes&gl=us&hl=en")
    suspend fun getMoviesPlaying(
        @Query("location") location: String,
        @Query("api_key") apiKey: String
    ): SerpApiResponse

    @GET("search.json?engine=google&gl=us&hl=en")
    suspend fun getMovieShowtimes(
        @Query("q") query: String,
        @Query("location") location: String,
        @Query("api_key") apiKey: String
    ): SerpApiResponse
}
