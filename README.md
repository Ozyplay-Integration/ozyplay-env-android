# OzyplayEnv Android SDK

[![](https://jitpack.io/v/AgnitumuS/ozyplay-env-android.svg)](https://jitpack.io/#AgnitumuS/ozyplay-env-android)

Android SDK，为 WebView 应用提供 JS Bridge 能力，包括设备信息采集、生命周期事件自动分发、屏幕状态查询、Google Ad ID 获取等。

## 功能

- **设备信息** — 品牌、型号、制造商、Android 版本、屏幕分辨率、DPI、屏幕尺寸（英寸）、语言/国家、内存、存储
- **应用信息** — 包名、版本号、UserAgent
- **生命周期事件** — SDK 自动感知，H5 注册回调即可接收 resume/pause/stop/destroy/screen_off/screen_on 事件
- **屏幕状态** — H5 主动查询当前屏幕亮灭状态
- **Google Ad ID** — 异步获取 Google Advertising ID

## 接入

### Step 1. 添加 JitPack 仓库

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

或在根 `build.gradle` 中：

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2. 添加依赖

```groovy
dependencies {
    implementation 'com.github.AgnitumuS:ozyplay-env-android:1.0.0'
}
```

## 使用

### 注入 JS Bridge（一行代码）

在 Activity 中，WebView 初始化后调用：

```java
OzyplayEnv bridge = OzyplayEnv.inject(webView);
```

完成。SDK 会自动：
- 注册 JS Bridge（`window.OzyplayEnv`）
- 监听宿主 Activity 生命周期
- 监听屏幕开关广播
- 在 Activity 销毁时自动释放资源

无需手动初始化 `DeviceInfoManager`，无需手动分发生命周期事件。

### 手动释放（可选）

正常情况下 SDK 会在 Activity destroy 时自动释放。如需提前释放：

```java
bridge.release();
```

## H5 端 API

H5 通过 `window.OzyplayEnv` 对象调用：

```javascript
// 获取设备信息（同步）
var info = JSON.parse(window.OzyplayEnv.getDeviceInfo());
console.log(info.deviceInfo.brand);
console.log(info.hardwareInfo.screenInches + ' inches');
console.log(info.storageInfo.storageTotal);
console.log(info.appInfo.packageName);

// 查询屏幕状态（同步）
var state = window.OzyplayEnv.getScreenState(); // "screen_on" | "screen_off" | "unknown"

// 注册生命周期回调（SDK 自动推送事件）
window.OzyplayEnv.registerLifecycleCallback('onAppLifecycle');
window.onAppLifecycle = function(event) {
    // event: "resume" | "pause" | "stop" | "destroy" | "screen_off" | "screen_on"
};

// 取消注册
window.OzyplayEnv.unregisterLifecycleCallback();

// 获取 Google Ad ID（异步）
window.onAdIdReceived = function(result) {
    console.log(result.adId, result.isLimitAdTrackingEnabled);
};
window.OzyplayEnv.fetchGoogleAdId('onAdIdReceived');
```

## 返回数据结构

```json
{
  "deviceInfo": {
    "brand": "Hisense",
    "product": "HE65A6100EUWTS",
    "androidVersion": "9",
    "model": "HE65A6100EUWTS",
    "manufacturer": "Hisense",
    "board": "bigfish",
    "screenHeight": 1080,
    "screenWidth": 1920,
    "densityDpi": 320,
    "country": "US",
    "language": "en"
  },
  "hardwareInfo": {
    "memAvailable": 1073741824,
    "memTotal": 2147483648,
    "androidId": "a1b2c3d4e5f6g7h8",
    "cpuAbi": "arm64-v8a",
    "screenInches": 65.0
  },
  "storageInfo": {
    "romTotal": 8589934592,
    "romAvailable": 4294967296,
    "romUsed": 4294967296,
    "storageTotal": 16106127360,
    "storageAvailable": 12884901888,
    "storageUsed": 3221225472
  },
  "appInfo": {
    "packageName": "com.example.app",
    "versionCode": 10,
    "defaultUserAgent": "Mozilla/5.0 ...",
    "customUserAgent": "Mozilla/5.0 ..."
  }
}
```

## 单独使用 DeviceInfoManager

如果只需要设备信息采集（不需要 JS Bridge）：

```java
DeviceInfoManager.getInstance().init(context);
String allInfo = DeviceInfoManager.getInstance().getAllInfoJson();
```

## 环境要求

- minSdk: 21 (Android 5.0)
- compileSdk: 34
- Java 8+

## API 文档

详细 API 文档见 [docs/OzyplayEnv_JS_Bridge_API.md](docs/OzyplayEnv_JS_Bridge_API.md)

## License

```
Copyright 2025 Zeasn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
