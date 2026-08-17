package com.example.forestsnap.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/** Extract latitude and longitude pair from image EXIF metadata if present. */
fun extractExifLocation(context: Context, uri: Uri): Pair<Double, Double>? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            val latLong = exif.latLong
            if (latLong != null && latLong.size == 2) {
                Pair(latLong[0], latLong[1])
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

/** Copy image stream from a content Uri to local cache storage file. */
fun copyGalleryUriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val photoDir = File(context.cacheDir, "snaps")
        if (!photoDir.exists()) photoDir.mkdirs()

        val file = File(photoDir, "gallery_snap_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/** Evaluate image sharpness using Laplacian variance edge detection. */
fun isImageBlurry(file: File, threshold: Double = 100.0): Boolean {
    return try {
        val options = BitmapFactory.Options().apply { inSampleSize = 8 }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return false

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val grayPixels = IntArray(width * height)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            grayPixels[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        var sum = 0.0
        var sqSum = 0.0
        var pixelCount = 0

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = grayPixels[y * width + x]
                val top = grayPixels[(y - 1) * width + x]
                val bottom = grayPixels[(y + 1) * width + x]
                val left = grayPixels[y * width + (x - 1)]
                val right = grayPixels[y * width + (x + 1)]

                val laplacianValue = (top + bottom + left + right - 4 * center).toDouble()
                sum += laplacianValue
                sqSum += laplacianValue * laplacianValue
                pixelCount++
            }
        }
        bitmap.recycle()

        val mean = sum / pixelCount
        val variance = (sqSum / pixelCount) - (mean * mean)
        variance < threshold
    } catch (e: Exception) {
        false
    }
}

/** Compress JPEG photo file while preserving original EXIF metadata tags. */
fun compressPhotoFile(file: File) {
    try {
        val oldExif = ExifInterface(file.absolutePath)

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        val maxDim = Math.max(options.outWidth, options.outHeight)
        val inSampleSize = if (maxDim > 1024) Math.round(maxDim.toFloat() / 1024f) else 1

        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return
        val tmpFile = File(file.absolutePath + ".tmp")

        FileOutputStream(tmpFile).use { fos ->
            ByteArrayOutputStream().use { bos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos)
                fos.write(bos.toByteArray())
                fos.flush()
            }
        }
        bitmap.recycle()

        val newExif = ExifInterface(tmpFile.absolutePath)
        val attributes = listOf(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_DATETIME
        )

        attributes.forEach { tag ->
            oldExif.getAttribute(tag)?.let { newExif.setAttribute(tag, it) }
        }
        newExif.saveAttributes()

        if (tmpFile.exists() && tmpFile.length() > 0) {
            file.delete()
            tmpFile.renameTo(file)
        } else {
            tmpFile.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        File(file.absolutePath + ".tmp").takeIf { it.exists() }?.delete()
    }
}