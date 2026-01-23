package com.example.visionlink

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCameraSystem()
            } else {
                Toast.makeText(this, "Camera permission required for VisionLink", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }

    private fun startCameraSystem() {
        val previewView = findViewById<PreviewView>(R.id.viewFinder)
        cameraManager = CameraManager(this, this, previewView)
        cameraManager.startCamera()
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (allPermissionsGranted()) {
            startCameraSystem()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraManager.isInitialized) {
            cameraManager.shutdown()
        }
    }
}