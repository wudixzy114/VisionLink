cmake_minimum_required(VERSION 3.22.1)

project("visionlink")

set(OPENCV_LIB_DIR ${CMAKE_SOURCE_DIR}/thirdparty/opencv/libs/${ANDROID_ABI})
set(OPENCV_INC_DIR ${CMAKE_SOURCE_DIR}/thirdparty/opencv/jni/include)
file(GLOB_RECURSE SOURCES "src/*.cpp" "src/*.h")

add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES IMPORTED_LOCATION ${OPENCV_LIB_DIR}/libopencv_java4.so)

add_library(visionlink_core SHARED
        ${SOURCES}
        native-lib.cpp)

target_include_directories(visionlink_core PRIVATE ${OPENCV_INC_DIR})

target_link_libraries(visionlink_core
        opencv_java4
        android
        log)