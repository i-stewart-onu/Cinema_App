package com.example.cinemaapp.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movie/now_playing?language=en-US&region=US&page=1")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String
    ): TmdbNowPlayingResponse

    @GET("movie/upcoming?language=en-US&region=US&page=1")
    suspend fun getUpcoming(
        @Query("api_key") apiKey: String
    ): TmdbNowPlayingResponse

    @GET("movie/popular?language=en-US")
    suspend fun getPopular(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): TmdbNowPlayingResponse

    @GET("movie/top_rated?language=en-US")
    suspend fun getTopRated(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): TmdbNowPlayingResponse

    @GET("discover/movie?language=en-US&watch_region=US&sort_by=popularity.desc")
    suspend fun discoverByProvider(
        @Query("api_key") apiKey: String,
        @Query("with_watch_providers") providerId: Int,
        @Query("page") page: Int
    ): TmdbNowPlayingResponse

    @GET("search/movie?language=en-US")
    suspend fun searchMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TmdbNowPlayingResponse

    @GET("movie/{movie_id}/release_dates")
    suspend fun getReleaseDates(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): TmdbReleaseDatesResponse

}
