package com.example.forestsnap.features.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.core.utils.LocationHelper
import com.example.forestsnap.core.utils.NetworkMonitor
import com.example.forestsnap.data.local.SyncSnapDao
import com.example.forestsnap.data.remote.ForestSnapApi
import com.example.forestsnap.data.remote.HistoryResponse
import com.example.forestsnap.data.remote.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isOnline: Boolean = true,
    val locationText: String = "Fetching GPS...",
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val weatherText: String = "Loading...",
    val riskLevel: String = "Low",
    val isRefreshing: Boolean = false,
    val isMapDownloading: Boolean = false,
    val mapDownloadProgress: Int = 0,
    val isMapCached: Boolean = false,
    val mapPins: List<HistoryResponse> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val syncDao: SyncSnapDao,
    private val networkMonitor: NetworkMonitor,
    private val locationHelper: LocationHelper,
    private val weatherApi: WeatherService,
    private val forestSnapApi: ForestSnapApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        monitorNetworkConnection()
        observeLocalRiskLevel()
        refreshData()
        startAutoRefresh()
    }

    private fun observeLocalRiskLevel() {
        viewModelScope.launch {
            syncDao.getLatestSyncedSnap().collectLatest { snap ->
                if (snap != null && snap.fireRiskPercent != null) {
                    val risk = snap.fireRiskPercent
                    val riskText = when {
                        risk > 75 -> "Critical"
                        risk > 50 -> "High"
                        risk > 25 -> "Moderate"
                        else -> "Low"
                    }
                    _uiState.update { it.copy(riskLevel = riskText) }
                }
            }
        }
    }

    fun updateMapCacheStatus(isDownloading: Boolean, progress: Int, isCached: Boolean) {
        _uiState.update {
            it.copy(
                isMapDownloading = isDownloading,
                mapDownloadProgress = progress,
                isMapCached = isCached
            )
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(300_000)
                fetchLocation()
                fetchServerHistory()
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    locationText = "Fetching GPS...",
                    weatherText = "Updating..."
                )
            }

            fetchLocation()
            fetchServerHistory()

            _uiState.update { it.copy(isRefreshing = false) }
            startAutoRefresh()
        }
    }

    private fun fetchServerHistory() {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isOnline) return@launch

                val history = forestSnapApi.getHistoricalData()
                _uiState.update { it.copy(mapPins = history) }
                Log.i("DashboardViewModel", "Successfully fetched ${history.size} map pins.")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch map pins: ${e.message}")
            }
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude

                _uiState.update {
                    it.copy(
                        locationText = "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
                    )
                }

                fetchRealWeather(lat, lng)
            } else {
                if (_uiState.value.locationText.contains("Fetching")) {
                    _uiState.update { it.copy(locationText = "Location unavailable or Permission required") }
                }
            }
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
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
                if (isOnline) {
                    refreshData()
                }
            }
        }
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
