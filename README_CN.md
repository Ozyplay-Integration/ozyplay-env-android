# OzyplayEnv Android SDK

[![](https://jitpack.io/v/Ozyplay-Support/ozyplay-env-android.svg)](https://jitpack.io/#Ozyplay-Support/ozyplay-env-android)

[English](README.md)

Android SDK，为 WebView 应用提供 JS Bridge 能力，包括设备信息采集、生命周期事件自动分发、屏幕状态查询、Google Ad ID 获取等。

## 功能

- **设备信息** — 品牌、型号、制造商、Android 版本、屏幕分辨率、DPI、语言/国家
- **硬件信息** — 内存、CPU 架构、Android ID、屏幕对角线尺寸（英寸）
- **存储信息** — ROM 和内置共享存储容量
- **应用信息** — 包名、版本号、UserAgent
- **生命周期事件** — SDK 自动感知 Activity 生命周期和屏幕开关，推送事件给 H5
- **屏幕状态** — H5 可同步查询当前屏幕状态
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
    implementation 'com.github.Ozyplay-Support:ozyplay-env-android:1.0.0'
}
```

## 使用

### 注入 JS Bridge（一行代码）

在 Activity 中 WebView 初始化后调用（必须在主线程）：

```java
OzyplayEnv bridge = OzyplayEnv.inject(webView);
```

完成。SDK 会自动：
- 注册 JS Bridge（`window.OzyplayEnv`）
- 监听宿主 Activity 生命周期（resume/pause/stop/destroy）
- 监听屏幕开关广播
- 在 Activity 销毁时自动释放资源

### 手动释放（可选）

正常情况下 SDK 会在 Activity destroy 时自动释放。如需提前释放：

```java
bridge.release();
```

## 单独使用 DeviceInfoManager

如果只需要设备信息采集（不需要 JS Bridge）：

```java
DeviceInfoManager.getInstance().init(context);
String allInfo = DeviceInfoManager.getInstance().getAllInfoJson();
```

---

## JS Bridge API 参考

H5 通过 `window.OzyplayEnv` 对象调用原生能力。

### 1. getDeviceInfo()

**类型：** 同步

返回所有设备信息的 JSON 字符串。

```javascript
var info = JSON.parse(window.OzyplayEnv.getDeviceInfo());
```

**返回格式：**

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

#### 字段说明

**deviceInfo**

| 字段 | 类型 | 说明 |
|------|------|------|
| brand | String | 设备品牌 |
| product | String | 产品名称/代号 |
| androidVersion | String | Android 系统版本（如 "9", "12"） |
| model | String | 设备型号 |
| manufacturer | String | 制造商 |
| board | String | 硬件主板名称 |
| screenHeight | int | 屏幕高度（像素） |
| screenWidth | int | 屏幕宽度（像素） |
| densityDpi | int | 屏幕密度（DPI） |
| country | String | 设备地区代码（ISO 3166-1 alpha-2，如 "US", "CN"） |
| language | String | 设备语言代码（ISO 639-1，如 "en", "zh"） |

**hardwareInfo**

| 字段 | 类型 | 说明 |
|------|------|------|
| memAvailable | long | 可用内存（字节） |
| memTotal | long | 总内存（字节） |
| androidId | String | Android 设备唯一 ID（Settings.Secure.ANDROID_ID） |
| cpuAbi | String | 主 CPU 架构（如 "arm64-v8a"） |
| screenInches | double | 屏幕物理对角线尺寸（英寸，包含状态栏和导航栏） |

**storageInfo**

| 字段 | 类型 | 说明 |
|------|------|------|
| romTotal | long | ROM（data 分区）总容量（字节） |
| romAvailable | long | ROM 可用容量（字节） |
| romUsed | long | ROM 已用容量（字节） |
| storageTotal | long | 内置共享存储总容量（字节，未挂载时为 0） |
| storageAvailable | long | 内置共享存储可用容量（字节） |
| storageUsed | long | 内置共享存储已用容量（字节） |

> **注意：** `storageTotal/Available/Used` 指的是设备主内置共享存储（`/storage/emulated/0`），不包含物理 SD 卡或 USB 存储。

**appInfo**

| 字段 | 类型 | 说明 |
|------|------|------|
| packageName | String | 宿主应用包名 |
| versionCode | long | 宿主应用版本号 |
| defaultUserAgent | String | 系统默认 WebView User-Agent |
| customUserAgent | String | 宿主应用实际使用的 WebView User-Agent |

---

### 2. getScreenState()

**类型：** 同步

```javascript
var state = window.OzyplayEnv.getScreenState();
// 返回: "screen_on" | "screen_off" | "unknown"
```

| 值 | 说明 |
|----|------|
| `"screen_on"` | 屏幕亮起/交互中 |
| `"screen_off"` | 屏幕关闭/待机 |
| `"unknown"` | 无法判断 |

---

### 3. registerLifecycleCallback(callbackName)

**类型：** 注册（持久）

注册一个全局 JS 函数接收生命周期和屏幕事件。SDK 自动推送事件。

```javascript
window.OzyplayEnv.registerLifecycleCallback('onAppLifecycle');
window.onAppLifecycle = function(event) {
  console.log('生命周期事件:', event);
};
```

| 事件 | 说明 |
|------|------|
| `"resume"` | 应用回到前台 |
| `"pause"` | 应用进入后台（部分可见） |
| `"stop"` | 应用完全不可见 |
| `"destroy"` | Activity 被销毁 |
| `"screen_off"` | 屏幕关闭 |
| `"screen_on"` | 屏幕亮起 |

---

### 4. unregisterLifecycleCallback()

**类型：** 同步

```javascript
window.OzyplayEnv.unregisterLifecycleCallback();
```

---

### 5. fetchGoogleAdId(callbackName)

**类型：** 异步（回调）

在后台线程获取 Google Advertising ID。

```javascript
window.onAdIdReceived = function(result) {
  console.log('Ad ID:', result.adId);
  console.log('限制追踪:', result.isLimitAdTrackingEnabled);
};
window.OzyplayEnv.fetchGoogleAdId('onAdIdReceived');
```

| 字段 | 类型 | 说明 |
|------|------|------|
| adId | String | Google Advertising ID（UUID 格式），失败时为空 |
| isLimitAdTrackingEnabled | boolean | 用户是否选择退出广告追踪 |
| error | String | 错误信息（仅失败时存在） |

---

## 快速开始（H5 端）

```javascript
// 1. 获取设备信息
var info = JSON.parse(window.OzyplayEnv.getDeviceInfo());
console.log('品牌:', info.deviceInfo.brand);
console.log('屏幕:', info.hardwareInfo.screenInches, '英寸');
console.log('包名:', info.appInfo.packageName);

// 2. 注册生命周期回调
window.OzyplayEnv.registerLifecycleCallback('onAppLifecycle');
window.onAppLifecycle = function(event) {
  console.log('事件:', event);
};

// 3. 查询屏幕状态
var state = window.OzyplayEnv.getScreenState();

// 4. 获取 Google Ad ID
window.OzyplayEnv.fetchGoogleAdId('onAdIdReceived');
window.onAdIdReceived = function(result) {
  console.log('Ad ID:', result.adId);
};
```

---

## 注意事项

1. Bridge 对象名：`window.OzyplayEnv`
2. 异步回调（`fetchGoogleAdId`）只触发一次，不会保留
3. 生命周期回调是持久的，调用 `unregisterLifecycleCallback()` 停止接收
4. 所有异步结果通过主线程 `evaluateJavascript` 传递
5. `screenInches` 使用 `Display.getRealMetrics()` 计算完整物理屏幕对角线

## 环境要求

- minSdk: 21 (Android 5.0)
- compileSdk: 34
- Java 8+

## License

```
Copyright 2025 Zeasn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
