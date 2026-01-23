#include <jni.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define TAG "VisionLink_CPP"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

using namespace cv;

extern "C" JNIEXPORT void JNICALL
Java_com_example_visionlink_VisionProcessor_nativeProcessFrame(
            JNIEnv *env,
            jobject,
            jlong matAddrInput,
            jlong matAddrOutput
        ){
    Mat& matInput = *(Mat*)matAddrInput;
    Mat& matOutput = *(Mat*)matAddrOutput;
    if(matInput.empty()){
        LOGD("Input frame is empty!");
        return;
    }

    // TODO core logic

    LOGD("Processing frame: %d x %d", matInput.cols, matInput.rows);
    Mat gray;
    if (matInput.channels() > 1) {
        cvtColor(matInput, gray, COLOR_RGBA2GRAY);
    } else {
        gray = matInput;
    }

    // 简单的边缘检测，作为显著性区域的粗略模拟
    Canny(gray, matOutput, 50, 150);
}
