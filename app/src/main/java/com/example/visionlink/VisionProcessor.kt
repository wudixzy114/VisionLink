package com.example.visionlink

import java.nio.ByteBuffer

object VisionProcessor {
    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("visionlink_core")
    }

    external fun nativeProcessFrame(
        yBuffer: ByteBuffer,
        width: Int,
        height: Int,
        rowStride: Int,
        rotationDegrees: Int
    )
}