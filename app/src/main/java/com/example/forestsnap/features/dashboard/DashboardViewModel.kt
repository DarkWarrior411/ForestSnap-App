package com.example.forestsnap.features.dashboard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.forestsnap.core.utils.LocationHelper
import com.example.forestsnap.core.utils.NetworkMonitor
import com.example.forestsnap.core.utils.copyGalleryUriToFile
import com.example.forestsnap.data.local.SyncSnapDao
import com.example.forestsnap.data.local.SyncSnapEntity
import com.example.forestsnap.data.remote.FeatureCollection
import com.example.forestsnap.data.remote.FirmsFirePoint
import com.example.forestsnap.data.remote.ForestSnapApi
import com.example.forestsnap.data.remote.HeatmapSquare
import com.example.forestsnap.data.remote.HistoryResponse
import com.example.forestsnap.data.remote.SystemAlert
import com.example.forestsnap.data.repository.SyncSnapRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class RiskFilter { ALL, CRITICAL, HIGH, MODERATE, LOW }

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
    val mapPins: List<HistoryResponse> = emptyList(),

    val isHeatmapVisible: Boolean = false,
    val heatmapData: List<HeatmapSquare> = emptyList(),

    val isForestBoundariesVisible: Boolean = false,
    val forestBoundaries: FeatureCollection? = null,

    val selectedFilter: RiskFilter = RiskFilter.ALL,
    val isListVisible: Boolean = false,

    val isFirmsLayerVisible: Boolean = false,
    val isFirmsLoading: Boolean = false,
    val activeAlerts: List<SystemAlert> = emptyList(),
    val firmsPins: List<FirmsFirePoint> = emptyList()
) {
    val filteredMapPins: List<HistoryResponse>
        get() = when (selectedFilter) {
            RiskFilter.ALL -> mapPins
            RiskFilter.CRITICAL -> mapPins.filter { it.final_fire_risk_percent >= 75 }
            RiskFilter.HIGH -> mapPins.filter { it.final_fire_risk_percent >= 50 && it.final_fire_risk_percent < 75 }
            RiskFilter.MODERATE -> mapPins.filter { it.final_fire_risk_percent >= 25 && it.final_fire_risk_percent < 50 }
            RiskFilter.LOW -> mapPins.filter { it.final_fire_risk_percent < 25 }
        }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncDao: SyncSnapDao,
    private val networkMonitor: NetworkMonitor,
    private val locationHelper: LocationHelper,
    private val forestSnapApi: ForestSnapApi,
    private val repository: SyncSnapRepository,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var sseEventSource: EventSource? = null
    private var lastProcessedWorkId: UUID? = null

    init {
        monitorNetworkConnection()
        observeLocalRiskLevel()
        refreshData()
        startAutoRefresh()
        observeSyncWorker()
        connectToLiveStream()
    }

    private fun connectToLiveStream() {

        val request = Request.Builder().url("http://10.0.2.2:8000/stream").build()

        val sseClient = okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (type == "new_record" || type == null) {
                    try {
                        val newRecord = Gson().fromJson(data, HistoryResponse::class.java)
                        _uiState.update { state ->

                            val updatedPins = listOf(newRecord) + state.mapPins
                            state.copy(mapPins = updatedPins.distinctBy {
                                listOf(
                                    it.latitude,
                                    it.longitude,
                                    it.timestamp
                                )
                            })
                        }
                        Log.i(
                            "DashboardViewModel",
                            "Received live update via SSE: Risk ${newRecord.final_fire_risk_percent}%"
                        )
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "Failed to parse SSE data: ${e.message}")
                    }
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                Log.w("DashboardViewModel", "SSE Stream disconnected. Reconnecting in 5s...")
                viewModelScope.launch { delay(5000); connectToLiveStream() }
            }
        }
        sseEventSource = EventSources.createFactory(sseClient).newEventSource(request, listener)
    }

    private fun observeSyncWorker() {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("AutoCloudSync")
                .collectLatest { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.id != lastProcessedWorkId) {
                        lastProcessedWorkId = workInfo.id
                        silentRefresh()
                    }
                }
        }
    }

    private fun silentRefresh() {
        viewModelScope.launch {
            fetchServerHistory()
            fetchSystemAlerts()
            if (_uiState.value.isFirmsLayerVisible) fetchFirmsData()
        }
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

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(300_000)
                fetchLocation()
                fetchServerHistory()
                fetchSystemAlerts()
                if (_uiState.value.isFirmsLayerVisible) {
                    fetchFirmsData()
                }
            }
        }
    }

    fun updateRiskFilter(filter: RiskFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun toggleListVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isListVisible = isVisible) }
    }

    private fun fetchSystemAlerts() {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isOnline) return@launch
                val alerts = forestSnapApi.getSystemAlerts()
                _uiState.update { it.copy(activeAlerts = alerts) }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch alerts: ${e.message}")
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
            fetchSystemAlerts()
            if (_uiState.value.isFirmsLayerVisible) fetchFirmsData()

            _uiState.update { it.copy(isRefreshing = false) }
            startAutoRefresh()
        }
    }

    fun processGalleryPhoto(context: Context, uri: Uri, lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val photoFile = copyGalleryUriToFile(context, uri) ?: return@launch
            repository.insertSyncSnap(
                SyncSnapEntity(
                    photoPath = photoFile.absolutePath,
                    latitude = lat,
                    longitude = lon,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleFirmsLayer() {
        val currentVisibility = _uiState.value.isFirmsLayerVisible
        _uiState.update { it.copy(isFirmsLayerVisible = !currentVisibility) }
        if (!currentVisibility && _uiState.value.firmsPins.isEmpty()) {
            fetchFirmsData()
        }
    }

    private fun fetchFirmsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFirmsLoading = true) }
            try {
                val parsedPoints = forestSnapApi.getGlobalFires()
                _uiState.update { it.copy(firmsPins = parsedPoints, isFirmsLoading = false) }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch FIRMS data from server: ${e.message}")
                _uiState.update { it.copy(isFirmsLoading = false) }
            }
        }
    }

    private fun fetchServerHistory() {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isOnline) return@launch
                val lat = _uiState.value.currentLat
                val lon = _uiState.value.currentLng

                val history = if (lat != null && lon != null) {
                    val radius = 0.1
                    forestSnapApi.getRegionalData(
                        lat - radius,
                        lat + radius,
                        lon - radius,
                        lon + radius
                    )
                } else {
                    forestSnapApi.getHistoricalData()
                }
                _uiState.update { it.copy(mapPins = history) }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch map pins: ${e.message}")
            }
        }
    }

    fun fetchRegionalDataViews(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double) {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isOnline) return@launch
                val history = forestSnapApi.getRegionalData(minLat, maxLat, minLon, maxLon)
                _uiState.update { it.copy(mapPins = history) }

                if (_uiState.value.isHeatmapVisible) {
                    val heatmap = forestSnapApi.getHeatmapRegion(minLat, maxLat, minLon, maxLon)
                    _uiState.update { it.copy(heatmapData = heatmap) }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch regional map data: ${e.message}")
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
                        currentLat = lat,
                        currentLng = lng,
                        locationText = "Lat: ${
                            String.format(
                                "%.4f",
                                lat
                            )
                        }, Lng: ${String.format("%.4f", lng)}"
                    )
                }
            } else if (_uiState.value.locationText.contains("Fetching")) {
                _uiState.update { it.copy(locationText = "Location unavailable or Permission required") }
            }
        }
    }

    private fun monitorNetworkConnection() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
                if (isOnline) refreshData()
            }
        }
    }

    fun toggleHeatmapLayer() {
        val newState = !_uiState.value.isHeatmapVisible
        _uiState.update { it.copy(isHeatmapVisible = newState) }
        if (newState) {
            val lat = _uiState.value.currentLat
            val lon = _uiState.value.currentLng
            if (lat != null && lon != null) {
                fetchRegionalDataViews(lat - 0.1, lat + 0.1, lon - 0.1, lon + 0.1)
            }
        }
    }

    fun toggleBoundariesLayer() {
        val newState = !_uiState.value.isForestBoundariesVisible
        _uiState.update { it.copy(isForestBoundariesVisible = newState) }
        if (newState && _uiState.value.forestBoundaries == null) {
            fetchForestBoundaries()
        }
    }

    private fun fetchForestBoundaries() {
        viewModelScope.launch {
            try {
                val boundaries = forestSnapApi.getForestBoundaries()
                _uiState.update { it.copy(forestBoundaries = boundaries) }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to fetch boundaries: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sseEventSource?.cancel()
    }
}