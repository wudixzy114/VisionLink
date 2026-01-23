package com.example.visionlink

object VisionProcessor {
    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("visionlink_core")
    }

    external fun nativeProcessFrame(inputMatAddr: Long, outputMatAddr: Long)
}