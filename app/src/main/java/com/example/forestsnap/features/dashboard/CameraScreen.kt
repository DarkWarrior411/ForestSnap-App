package com.example.forestsnap.features.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import java.io.File
import java.util.concurrent.Executor
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSaving by remember { mutableStateOf(false) }

    val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var hasPermissions by remember {
        mutableStateOf(requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasPermissions = permissions.values.all { it }
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasPermissions) launcher.launch(requiredPermissions)
    }

    if (hasPermissions) {
        CameraPreviewView(context, lifecycleOwner, isSaving) { file ->
            isSaving = true
            viewModel.processAndSavePhoto(file) { success, errorMsg ->
                if (!success && errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
                navController.popBackStack()
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera and Location permissions are required.")
        }
    }
}

@Composable
fun CameraPreviewView(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    isSaving: Boolean,
    onImageCaptured: (File) -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = ContextCompat.getMainExecutor(context)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = { takePhoto(imageCapture, context, executor, onImageCaptured) },
            enabled = !isSaving,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Snap & Save Data")
        }
    }
}

private fun takePhoto(
    imageCapture: ImageCapture,
    context: Context,
    executor: Executor,
    onImageCaptured: (File) -> Unit
) {
    // Phase 2: Save to filesDir to persist across cache clearing
    val photoDir = File(context.filesDir, "snaps")
    if (!photoDir.exists()) photoDir.mkdirs()
    
    val photoFile = File(photoDir, "forest_snap_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions, executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(photoFile)
            }

            override fun onError(exc: ImageCaptureException) {
                exc.printStackTrace()
            }
        }
    )
}