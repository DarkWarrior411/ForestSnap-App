package com.example.forestsnap.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("v1/forecast")
    suspend fun getStatus(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") current: Boolean = true
    ): WeatherResponse
}

data class WeatherResponse(
    val current_weather: CurrentWeather
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int
)