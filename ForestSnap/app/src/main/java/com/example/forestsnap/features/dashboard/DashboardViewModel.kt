package com.example.forestsnap.features.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.data.remote.HistoryResponse
import com.example.forestsnap.data.remote.NetworkModule
import com.example.forestsnap.data.remote.WeatherService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// UI State including map caching and server data
data class DashboardUiState(
    val isOnline: Boolean = true,
    val locationText: String = "Fetching GPS...",
    val weatherText: String = "Loading...",
    val riskLevel: String = "Low",
    val isRefreshing: Boolean = false,
    val isMapDownloading: Boolean = false,
    val mapDownloadProgress: Int = 0,
    val isMapCached: Boolean = false,
    val mapPins: List<HistoryResponse> = emptyList() // Holds your server data
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Client-side weather for the dashboard display
    private val weatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherService::class.java)

    private var autoRefreshJob: Job? = null

    init {
        monitorNetworkConnection()
        refreshData() // Do an initial fetch immediately
        startAutoRefresh() // Start the silent background loop
    }

    // --- NEW: Function for the MapScreen to update the caching status ---
    fun updateMapCacheStatus(isDownloading: Boolean, progress: Int, isCached: Boolean) {
        _uiState.update {
            it.copy(
                isMapDownloading = isDownloading,
                mapDownloadProgress = progress,
                isMapCached = isCached
            )
        }
    }

    // --- AUTO-REFRESH LOOP ---
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel() // Cancel any existing loop just in case
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                // Wait for 5 minutes (300,000 milliseconds) before silently fetching again
                delay(300_000)

                // Silently fetch data without triggering the UI loading spinner
                fetchLocation()
                fetchServerHistory()
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            // This one shows the loading spinner because the user manually requested it
            _uiState.update { it.copy(isRefreshing = true, locationText = "Fetching GPS...", weatherText = "Updating...") }

            // Fire off both network requests concurrently
            fetchLocation()
            fetchServerHistory()

            delay(1000) // Minimum delay so the spinner doesn't flash too quickly
            _uiState.update { it.copy(isRefreshing = false) }
            startAutoRefresh()
        }
    }

    // Function to pull the seeded database records from FastAPI
    private fun fetchServerHistory() {
        viewModelScope.launch {
            try {
                // If offline, don't attempt the fetch
                if (!_uiState.value.isOnline) return@launch

                val history = NetworkModule.api.getHistoricalData()
                _uiState.update { it.copy(mapPins = history) }
                Log.i("DashboardViewModel", "Successfully fetched ${history.size} map pins.")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch map pins: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val context = getApplication<Application>().applicationContext

        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude

                    _uiState.update {
                        it.copy(locationText = "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}")
                    }

                    fetchRealWeather(lat, lng)
                } else {
                    if (_uiState.value.locationText.contains("Fetching")) {
                        _uiState.update { it.copy(locationText = "Location unavailable") }
                    }
                }
            }.addOnFailureListener {
                if (_uiState.value.locationText.contains("Fetching")) {
                    _uiState.update { it.copy(locationText = "GPS Fetch Failed") }
                }
            }
        } else {
            _uiState.update { it.copy(locationText = "Permissions Required") }
        }
    }

    private fun fetchRealWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getStatus(lat, lon)
                val temp = response.current_weather.temperature
                val description = mapWeatherCode(response.current_weather.weathercode)

                _uiState.update { it.copy(weatherText = "$temp°C - $description") }
            } catch (e: Exception) {
                _uiState.update { it.copy(weatherText = "Offline Mode - Weather N/A") }
            }
        }
    }

    private fun monitorNetworkConnection() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _uiState.update { it.copy(isOnline = true) }
                // Reconnected? Immediately fetch map pins and location!
                refreshData()
            }

            override fun onLost(network: Network) {
                _uiState.update { it.copy(isOnline = false) }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        val activeNetwork = connectivityManager.activeNetwork
        val isInitiallyOnline = activeNetwork != null
        _uiState.update { it.copy(isOnline = isInitiallyOnline) }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            95, 96, 99 -> "Thunderstorm"
            else -> "Cloudy"
        }
    }
}
