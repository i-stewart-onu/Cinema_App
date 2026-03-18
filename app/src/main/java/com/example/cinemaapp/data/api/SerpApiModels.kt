package com.example.cinemaapp.data.api

import com.google.gson.annotations.SerializedName

data class SerpApiResponse(
    @SerializedName("showtimes") val showtimes: List<SerpShowtimeDay>?,
    @SerializedName("knowledge_graph") val knowledgeGraph: SerpKnowledgeGraph?
)

data class SerpKnowledgeGraph(
    @SerializedName("movies_playing") val moviesPlaying: List<SerpMoviePlaying>?
)

data class SerpMoviePlaying(
    @SerializedName("name") val name: String?,
    @SerializedName("serpapi_link") val serpapiLink: String?,
    @SerializedName("image") val image: String?
)

data class SerpShowtimeDay(
    @SerializedName("day") val day: String?,
    @SerializedName("theaters") val theaters: List<SerpTheater>?
)

data class SerpTheater(
    @SerializedName("name") val name: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("showing") val showing: List<SerpMovieShowing>?
)

data class SerpMovieShowing(
    @SerializedName("name") val name: String?,
    @SerializedName("time") val time: List<String>?,
    @SerializedName("type") val type: String?
)
