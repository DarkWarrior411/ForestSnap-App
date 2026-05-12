package com.example.forestsnap.features.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.core.utils.LocationHelper
import com.example.forestsnap.core.utils.PreferenceManager
import com.example.forestsnap.data.local.SyncSnapEntity
import com.example.forestsnap.data.repository.SyncSnapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: SyncSnapRepository,
    private val locationHelper: LocationHelper,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    fun processAndSavePhoto(photoFile: File, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val strictLocation = preferenceManager.strictLocationFlow.first()
                val compressImage = preferenceManager.compressionFlow.first()

                val location = locationHelper.getCurrentLocation()

                if (strictLocation && location == null) {
                    photoFile.delete()
                    onComplete(false, "Strict Location enabled. GPS signal required.")
                    return@launch
                }

                val lat = location?.latitude ?: 0.0
                val lon = location?.longitude ?: 0.0

                if (compressImage) {
                    compressPhotoFile(photoFile)
                }

                repository.insertSyncSnap(
                    SyncSnapEntity(
                        photoPath = photoFile.absolutePath,
                        latitude = lat,
                        longitude = lon,
                        timestamp = System.currentTimeMillis()
                    )
                )

                onComplete(true, null)
            } catch (e: Exception) {
                photoFile.delete()
                onComplete(false, "Error saving photo: ${e.message}")
            }
        }
    }

    private fun compressPhotoFile(file: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos) // 70% quality
            val bitmapData = bos.toByteArray()

            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            bitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace() // If compression fails, we just keep the original
        }
    }
}
