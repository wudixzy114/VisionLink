package com.example.visionlink

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class VisionAnalyzer : ImageAnalysis.Analyzer {
    companion object {
        private const val TAG = "VisionAnalyzer"
    }

    override fun analyze(image: ImageProxy) {
        val startTime = System.currentTimeMillis()
        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val width = image.width
            val height = image.height
            val rowStride = plane.rowStride
            val rotationDegrees = image.imageInfo.rotationDegrees
            VisionProcessor.nativeProcessFrame(buffer, width, height, rowStride, rotationDegrees)
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed", e)
        } finally {
            image.close()
        }
    }
}