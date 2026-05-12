package com.example.forestsnap.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody

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

interface ForestSnapApi {
    @GET("history")
    suspend fun getHistoricalData(): List<HistoryResponse>

    @Multipart
    @POST("analyze")
    suspend fun analyzeEnvironment(
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part image: MultipartBody.Part
    ): HistoryResponse
}

object NetworkModule {
    const val BASE_URL = "http://10.20.34.117:8000/"

    val api: ForestSnapApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForestSnapApi::class.java)
    }
}