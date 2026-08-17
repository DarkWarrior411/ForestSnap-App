package com.example.forestsnap.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming

/** Response model returned after edge computer vision model analysis. */
data class AnalysisResponse(
    val fuel_load_score: Double,
    val dryness_risk_tier: Int,
    val temperature_c: Double,
    val humidity_percent: Int,
    val wind_speed_ms: Double,
    val wind_direction_deg: Int,
    val final_fire_risk_percent: Double
)

/** Historical telemetry record returned by server history endpoints. */
data class HistoryResponse(
    val id: Int,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val fuel_load_score: Double,
    val dryness_risk_tier: Int,
    val temperature_c: Double,
    val humidity_percent: Int,
    val wind_speed_ms: Double,
    val wind_direction_deg: Int?,
    val final_fire_risk_percent: Double
)

/** Weather proxy response payload from OpenWeather integration. */
data class WeatherProxyResponse(
    val temp: Double,
    val humidity: Int,
    val wind_speed: Double,
    val wind_direction: Int
)

/** Heatmap grid cell item for spatial risk aggregation. */
data class HeatmapSquare(
    val center_lat: Double,
    val center_lon: Double,
    val avg_risk: Double,
    val point_count: Int,
    val grid_size: Double
)

/** NASA FIRMS satellite fire anomaly coordinate item. */
data class FirmsFirePoint(
    val latitude: Double,
    val longitude: Double,
    val brightness: Double,
    val confidence: String
)

/** GeoJSON feature collection models for forest reserve boundary polygons. */
data class FeatureCollection(val features: List<Feature>)
data class Feature(val properties: Properties, val geometry: Geometry)
data class Properties(val name: String, val type: String)
data class Geometry(val type: String, val coordinates: List<List<List<Double>>>)

/** High-priority alert payload definition. */
data class SystemAlert(
    val id: String,
    val type: String,
    val severity: String,
    val title: String,
    val message: String,
    val lat: Double,
    val lon: Double
)

/** Retrofit REST API interface for communicating with the edge Python server. */
interface ForestSnapApi {
    @GET("history")
    suspend fun getHistoricalData(): List<HistoryResponse>

    @GET("history/region")
    suspend fun getRegionalData(
        @Query("minLat") minLat: Double,
        @Query("maxLat") maxLat: Double,
        @Query("minLon") minLon: Double,
        @Query("maxLon") maxLon: Double
    ): List<HistoryResponse>

    @GET("heatmap/region")
    suspend fun getHeatmapRegion(
        @Query("minLat") minLat: Double,
        @Query("maxLat") maxLat: Double,
        @Query("minLon") minLon: Double,
        @Query("maxLon") maxLon: Double,
        @Query("grid_size") gridSize: Double = 0.01
    ): List<HeatmapSquare>

    @GET("forests/boundaries")
    suspend fun getForestBoundaries(): FeatureCollection

    @GET("firms/active-fires")
    suspend fun getGlobalFires(): List<FirmsFirePoint>

    @GET("weather/current")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherProxyResponse

    @GET("alerts")
    suspend fun getSystemAlerts(): List<SystemAlert>

    @GET("stream")
    @Streaming
    fun getServerStream(): Call<ResponseBody>

    @Multipart
    @POST("analyze")
    suspend fun analyzeEnvironment(
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part image: MultipartBody.Part
    ): AnalysisResponse
}