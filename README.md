# VisionLink

> **Kotlin + CameraX + OpenCV 4 的 Android 实时视觉处理 App，通过 JNI 把每帧 YUV 直接交给原生 C++ 处理。**

## 项目定位

VisionLink 是一个 **Android 端实时计算机视觉** 工程脚手架。它使用 AndroidX CameraX 抓取后置摄像头的 720p YUV_420_888 帧，把每帧的 **Y plane 直接通过 JNI 传给原生 C++** 层，由 OpenCV 4 在 native 侧做后续处理。

当前仓库是"骨架期"：Java/Kotlin 侧的相机管线、权限流程、生命周期绑定、Analyzer 回调、JNI 桥接、CMake/NDK 集成、OpenCV 库加载链路 **全部已经跑通**，但 `native-lib.cpp` 里的处理函数体只有一行 `//TODO`——也就是 native 侧的处理逻辑还没有落地。整套项目被设计为：之后只需要在 `nativeProcessFrame` 函数里填入 OpenCV 算法即可。

## 仓库结构

```
VisionLink/
├── build.gradle.kts                       # 顶层 Gradle 脚本（仅声明 AGP 插件）
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml              # 依赖版本目录（version catalog）
├── gradle/wrapper/                        # Gradle wrapper
├── gradlew / gradlew.bat
└── app/
    ├── build.gradle.kts                   # App 模块构建脚本（CMake/NDK + CameraX 依赖）
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # 声明 CAMERA 权限 + LAUNCHER
        │   ├── java/com/example/visionlink/
        │   │   ├── MainActivity.kt        # 入口 Activity：权限申请 + 启动相机
        │   │   ├── CameraManager.kt       # CameraX 绑定（Preview + ImageAnalysis）
        │   │   ├── VisionAnalyzer.kt      # ImageAnalysis.Analyzer：转调 JNI
        │   │   └── VisionProcessor.kt     # 加载 .so 库 + 声明 external native 方法
        │   ├── cpp/
        │   │   ├── CMakeLists.txt         # 系统库 + 标准 C++17
        │   │   └── native-lib.cpp         # JNI 入口 + OpenCV 处理逻辑（TODO）
        │   └── res/                       # Material 主题、图标、布局
        ├── test/                          # 单元测试（JUnit）
        └── androidTest/                   # 仪器化测试（Espresso）
```

## 技术栈

| 层 | 选型 |
|---|---|
| 语言 | Kotlin 1.x（Java 11 字节码目标） |
| 构建 | Gradle Kotlin DSL + Version Catalog (`libs.versions.toml`) |
| Android Gradle Plugin | **9.0.0**（`agp = "9.0.0"`） |
| Android SDK | `compileSdk = 36`，`minSdk = 29`，`targetSdk = 36` |
| 相机 | **AndroidX CameraX 1.5.2**（`core` / `camera2` / `lifecycle` / `view` 四件套） |
| UI | Material Components 1.10.0 + AppCompat 1.6.1 + ConstraintLayout 2.1.4 + ViewBinding |
| 原生 | CMake 3.22.1 + C++17 + `c++_shared` STL |
| 视觉 | **OpenCV 4 for Android**（`opencv_java4`） + native `opencv2/core.hpp` / `imgproc.hpp` / `features2d.hpp` |
| 日志 | Android `__android_log_print`（在 `native-lib.cpp` 中） |

## 核心模块

### 1. `MainActivity` —— 入口与权限
- 在 `onCreate` 中先检查 `Manifest.permission.CAMERA` 是否已授予；
- 通过 `ActivityResultContracts.RequestPermission` 启动系统授权弹窗；
- 获得权限后通过 `CameraManager` 启动相机；
- 同时给窗口加 `FLAG_KEEP_SCREEN_ON` 防止息屏打断实时预览；
- `onDestroy` 中调用 `cameraManager.shutdown()` 关闭后台 executor。

### 2. `CameraManager` —— CameraX 用例绑定
- 使用 `ProcessCameraProvider.getInstance(...)` 异步拿到 provider；
- 目标分辨率固定为 `1280×720`（`Size TARGET_RESOLUTION`）；
- 通过 `ResolutionSelector` + `ResolutionStrategy(FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)` 在硬件不支持 720p 时优雅降级；
- 同时绑定两个用例：
  - `Preview` → 喂给 `PreviewView` 做实时显示；
  - `ImageAnalysis` → `OUTPUT_IMAGE_FORMAT_YUV_420_888`，backpressure 策略 `STRATEGY_KEEP_ONLY_LATEST`，单线程 executor 上跑 `VisionAnalyzer`；
- 默认 `CameraSelector.DEFAULT_BACK_CAMERA`。

### 3. `VisionAnalyzer` —— 把帧推给 JNI
- 实现 `ImageAnalysis.Analyzer`；
- 取 `planes[0]`（Y plane）拿到 `ByteBuffer`，连同 `width` / `height` / `rowStride` / `rotationDegrees` 一起调 `VisionProcessor.nativeProcessFrame(...)`；
- `finally` 中 **始终 `image.close()`**——这是 CameraX analyzer 契约，忘了 close 会被警告并影响后续帧投递。

### 4. `VisionProcessor` —— Kotlin 侧的 JNI 声明
- 静态 `init {}` 块按顺序加载 `opencv_java4`（公开 Java 绑定）→ `visionlink_core`（自定义 native 库）；
- `external fun nativeProcessFrame(...)` 是唯一的 native 调用入口。

### 5. `native-lib.cpp` —— JNI + OpenCV 入口
- C 命名风格导出 `Java_com_example_visionlink_VisionProcessor_nativeProcessFrame`；
- 通过 `env->GetDirectBufferAddress(yBuffer)` **零拷贝**地拿到 Y plane 原始指针；
- 包装成 OpenCV `Mat(height, width, CV_8UC1, srcData, rowStride)`——`rowStride` 是显式 stride，能正确处理硬件对齐 padding；
- `try { //TODO } catch (const cv::Exception &e)` 骨架已经就位，只剩处理逻辑没写。

## 已完成 / 进行中

- [x] Gradle 工程脚手架（version catalog、AGP 9、CMake 桥接）
- [x] 运行时权限申请流程
- [x] CameraX 双用例绑定（Preview + ImageAnalysis，720p 目标分辨率）
- [x] 单线程 executor + backpressure `KEEP_ONLY_LATEST` 防止掉帧
- [x] JNI 入口 + OpenCV 头文件引入 + 零拷贝 Y-plane 共享
- [x] 单元测试 + 仪器化测试占位
- [ ] `native-lib.cpp` 中具体处理逻辑（//TODO）
- [ ] OpenCV 库二进制接入（需要在 `app/src/main/cpp/CMakeLists.txt` 中 `find_package(OpenCV)` 或导入预编译的 AAR，并调整 `System.loadLibrary` 顺序）
- [ ] 应用内显示处理结果（Overlay / OpenGL SurfaceView）

## 本地构建

```powershell
# 需要 Android Studio Iguana+ (AGP 9) + Android SDK 36 + NDK
# 还需要把 OpenCV 4 Android SDK 放到合适位置并在 CMakeLists.txt 中 find_package

git clone <this repo>
cd VisionLink
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> **关键提示**：在填 TODO 之前，必须先把 OpenCV 的 `opencv_java4.so` 放进 APK（典型做法是把 OpenCV Android SDK 的 `native/libs/<abi>` 拷到 `app/src/main/jniLibs/`，或者通过 Gradle 依赖 `org.opencv:opencv:4.x.x`）。否则 `System.loadLibrary("opencv_java4")` 会在首次启动时崩溃。

## 状态

- **版本**：v0.1（工程脚手架完成，原生处理未实现）
- **维护状态**：暂停
- **可运行性**：可编译（前提：手动接入 OpenCV），启动后只显示相机预览，不会崩

## License

仓库内未附 LICENSE 文件。源码默认遵循 "All rights reserved"。如需复用代码，请先与作者协商授权。
