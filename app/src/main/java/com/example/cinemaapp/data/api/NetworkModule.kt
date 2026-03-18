package com.example.cinemaapp.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    private var serpLogCount = 0

    private val serpRawLogger = Interceptor { chain ->
        val response: Response = chain.proceed(chain.request())
        if (serpLogCount < 2 && chain.request().url.host.contains("serpapi")) {
            serpLogCount++
            val url = chain.request().url.toString()
            val body = response.peekBody(Long.MAX_VALUE).string()
            Log.d("SERP_RAW", "=== Response #$serpLogCount | ${url.take(120)} ===")
            body.chunked(3000).forEachIndexed { i, chunk ->
                Log.d("SERP_RAW", "[$i] $chunk")
            }
        }
        response
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(serpRawLogger)
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
