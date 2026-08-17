package com.example.forestsnap.features.dashboard

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.core.utils.LocationHelper
import com.example.forestsnap.core.utils.PreferenceManager
import com.example.forestsnap.core.utils.compressPhotoFile
import com.example.forestsnap.core.utils.isImageBlurry
import com.example.forestsnap.data.local.SyncSnapEntity
import com.example.forestsnap.data.repository.SyncSnapRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for camera photo processing, blur detection, light sensor anti-spoofing, and ML Kit verification.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SyncSnapRepository,
    private val locationHelper: LocationHelper,
    private val preferenceManager: PreferenceManager
) : ViewModel(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var currentLuxValue: Float = -1f

    private val _uploadStatus = MutableStateFlow<String?>(null)
    val uploadStatus: StateFlow<String?> = _uploadStatus.asStateFlow()

    // Polls GPS status every 2 seconds
    val isLocationReady: StateFlow<Boolean> = flow {
        while (true) {
            emit(locationHelper.getCurrentLocation() != null)
            delay(2000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /** Validate photo blur, ambient lighting, GPS accuracy, and nature ML labels before queueing for sync. */
    fun processAndSavePhotoOptimistically(photoFile: File) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {

                if (isImageBlurry(photoFile)) {
                    rejectPhoto(photoFile, "Image too blurry. Please hold still.")
                    return@launch
                }

                val location = locationHelper.getCurrentLocation()
                val strictLocation = preferenceManager.strictLocationFlow.first()
                if (strictLocation && location == null) {
                    rejectPhoto(photoFile, "GPS signal required for submission.")
                    return@launch
                }

                val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isDaytime = hourOfDay in 8..17
                if (isDaytime && currentLuxValue in 0f..200f) {
                    Log.w(
                        "CameraViewModel",
                        "Anti-Gaming Triggered: Lux is $currentLuxValue during daytime."
                    )
                    rejectPhoto(photoFile, "Environment too dim. Ensure you are outdoors.")
                    return@launch
                }

                val isActuallyForest = verifyForestContent(photoFile)
                if (!isActuallyForest) {
                    Log.w("CameraViewModel", "Anti-Gaming Triggered: No nature detected in photo.")
                    rejectPhoto(
                        photoFile,
                        "Validation failed: Image does not appear to be a forest."
                    )
                    return@launch
                }

                if (preferenceManager.compressionFlow.first()) {
                    compressPhotoFile(photoFile)
                }

                withContext(NonCancellable) {
                    repository.insertSyncSnap(
                        SyncSnapEntity(
                            photoPath = photoFile.absolutePath,
                            latitude = location?.latitude,
                            longitude = location?.longitude,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                _uploadStatus.value = "Snap queued for analysis!"

            } catch (e: Exception) {
                rejectPhoto(photoFile, "Processing error occurred.")
            }
        }
    }

    /** Perform on-device ML Kit image labeling to verify vegetation content. */
    private suspend fun verifyForestContent(photoFile: File): Boolean {
        return try {
            val image = InputImage.fromFilePath(context, Uri.fromFile(photoFile))
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            val labels = labeler.process(image).await()

            val validNatureTags =
                listOf("Tree", "Plant", "Forest", "Nature", "Wood", "Vegetation", "Outdoors")

            labels.any { validNatureTags.contains(it.text) }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "ML Kit failed: ${e.message}")
            true
        }
    }

    private fun rejectPhoto(file: File, reason: String) {
        file.delete()
        _uploadStatus.value = reason
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            currentLuxValue = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}