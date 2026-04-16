package com.example.forestsnap.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Data class matching your FastAPI AnalysisResponse
data class HistoryResponse(
    val latitude: Double,
    val longitude: Double,
    val fuel_load_score: Double,
    val dryness_risk_tier: Int,
    val temperature_c: Double,
    val humidity_percent: Int,
    val wind_speed_ms: Double,
    val final_fire_risk_percent: Double
)

// 2. The API Interface
interface ForestSnapApi {
    @GET("history")
    suspend fun getHistoricalData(): List<HistoryResponse>
}

// 3. A Singleton to easily access the API across your app
object NetworkModule {
    private const val BASE_URL = "http://10.20.34.117:8000/"

    val api: ForestSnapApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForestSnapApi::class.java)
    }
}