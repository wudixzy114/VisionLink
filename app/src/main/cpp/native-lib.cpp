#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>

#define TAG "VisionLink_Native"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

using namespace cv;

extern "C" JNIEXPORT void JNICALL
Java_com_example_visionlink_VisionProcessor_nativeProcessFrame(
        JNIEnv *env,
        jobject thiz,
        jobject yBuffer,   // Kotlin 传来的 ByteBuffer
        jint width,
        jint height,
        jint rowStride,     // CameraX 的 rowStride
        jint rotationDegrees
) {
    auto *srcData = static_cast<uint8_t *>(env->GetDirectBufferAddress(yBuffer));
    if (srcData == nullptr) {
        LOGE("Could not get buffer address!");
        return;
    }

    Mat yMat(height, width, CV_8UC1, srcData, rowStride);
    try {
        //TODO
    } catch (const cv::Exception &e) {
        LOGE("OpenCV Error: %s", e.what());
    }
}