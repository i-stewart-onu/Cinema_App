package com.example.cinemaapp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val tmdbService: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }

    val serpApiService: SerpApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://serpapi.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SerpApiService::class.java)
    }
}
