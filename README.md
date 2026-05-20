# OzyplayEnv Android SDK

[![](https://jitpack.io/v/AgnitumuS/ozyplay-env-android.svg)](https://jitpack.io/#AgnitumuS/ozyplay-env-android)

[中文文档](README_CN.md)

Android SDK providing JS Bridge for WebView apps — device info, lifecycle events, screen state, and Google Ad ID.

## Features

- **Device Info** — Brand, model, manufacturer, Android version, screen resolution, DPI, locale
- **Hardware Info** — Memory, CPU architecture, Android ID, screen diagonal size (inches)
- **Storage Info** — ROM and internal shared storage capacity
- **App Info** — Package name, version code, UserAgent
- **Lifecycle Events** — SDK auto-detects Activity lifecycle and screen on/off, pushes events to H5
- **Screen State** — H5 can query current screen state synchronously
- **Google Ad ID** — Async fetch of Google Advertising ID

## Integration

### Step 1. Add JitPack Repository

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Or in root `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2. Add Dependency

```groovy
dependencies {
    implementation 'com.github.AgnitumuS:ozyplay-env-android:1.0.0'
}
```

## Usage

### Inject JS Bridge (One Line)

Call after WebView initialization in your Activity (must be on main thread):

```java
OzyplayEnv bridge = OzyplayEnv.inject(webView);
```

That's it. The SDK will automatically:
- Register the JS Bridge (`window.OzyplayEnv`)
- Monitor host Activity lifecycle (resume/pause/stop/destroy)
- Listen for screen on/off broadcasts
- Release all resources when Activity is destroyed

### Manual Release (Optional)

The SDK auto-releases on Activity destroy. To release early:

```java
bridge.release();
```

## Using DeviceInfoManager Standalone

If you only need device info collection without the JS Bridge:

```java
DeviceInfoManager.getInstance().init(context);
String allInfo = DeviceInfoManager.getInstance().getAllInfoJson();
```

---

## JS Bridge API Reference

H5 accesses native capabilities through `window.OzyplayEnv`.

### 1. getDeviceInfo()

**Type:** Synchronous

Returns all device information as a JSON string.

```javascript
var info = JSON.parse(window.OzyplayEnv.getDeviceInfo());
```

**Response Format:**

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

#### Field Reference

**deviceInfo**

| Field | Type | Description |
|-------|------|-------------|
| brand | String | Device brand name |
| product | String | Product name / code |
| androidVersion | String | Android OS version (e.g. "9", "12") |
| model | String | Device model name |
| manufacturer | String | Device manufacturer |
| board | String | Hardware board name |
| screenHeight | int | Screen height in pixels |
| screenWidth | int | Screen width in pixels |
| densityDpi | int | Screen density in DPI |
| country | String | Locale country code (ISO 3166-1 alpha-2) |
| language | String | Locale language code (ISO 639-1) |

**hardwareInfo**

| Field | Type | Description |
|-------|------|-------------|
| memAvailable | long | Available memory in bytes |
| memTotal | long | Total memory in bytes |
| androidId | String | Android unique device ID (Settings.Secure.ANDROID_ID) |
| cpuAbi | String | Primary CPU architecture (e.g. "arm64-v8a") |
| screenInches | double | Screen diagonal size in inches (full physical screen) |

**storageInfo**

| Field | Type | Description |
|-------|------|-------------|
| romTotal | long | Total ROM (data partition) in bytes |
| romAvailable | long | Available ROM in bytes |
| romUsed | long | Used ROM in bytes |
| storageTotal | long | Total internal shared storage in bytes (0 if not mounted) |
| storageAvailable | long | Available internal shared storage in bytes |
| storageUsed | long | Used internal shared storage in bytes |

> **Note:** `storageTotal/Available/Used` refers to the primary internal shared storage (`/storage/emulated/0`), not physical SD card or USB storage.

**appInfo**

| Field | Type | Description |
|-------|------|-------------|
| packageName | String | Host app's package name |
| versionCode | long | Host app's version code |
| defaultUserAgent | String | System default WebView User-Agent |
| customUserAgent | String | Host app's actual WebView User-Agent |

---

### 2. getScreenState()

**Type:** Synchronous

```javascript
var state = window.OzyplayEnv.getScreenState();
// Returns: "screen_on" | "screen_off" | "unknown"
```

| Value | Description |
|-------|-------------|
| `"screen_on"` | Screen is on and interactive |
| `"screen_off"` | Screen is off / standby |
| `"unknown"` | Unable to determine |

---

### 3. registerLifecycleCallback(callbackName)

**Type:** Registration (persistent)

Registers a global JS function to receive lifecycle and screen events. The SDK automatically pushes events.

```javascript
window.OzyplayEnv.registerLifecycleCallback('onAppLifecycle');
window.onAppLifecycle = function(event) {
  console.log('Lifecycle event:', event);
};
```

| Event | Description |
|-------|-------------|
| `"resume"` | App returned to foreground |
| `"pause"` | App going to background |
| `"stop"` | App completely hidden |
| `"destroy"` | Activity being destroyed |
| `"screen_off"` | Screen turned off |
| `"screen_on"` | Screen turned on |

---

### 4. unregisterLifecycleCallback()

**Type:** Synchronous

```javascript
window.OzyplayEnv.unregisterLifecycleCallback();
```

---

### 5. fetchGoogleAdId(callbackName)

**Type:** Asynchronous (callback)

Fetches Google Advertising ID in a background thread.

```javascript
window.onAdIdReceived = function(result) {
  console.log('Ad ID:', result.adId);
  console.log('Limit tracking:', result.isLimitAdTrackingEnabled);
};
window.OzyplayEnv.fetchGoogleAdId('onAdIdReceived');
```

| Field | Type | Description |
|-------|------|-------------|
| adId | String | Google Advertising ID (UUID format), empty on failure |
| isLimitAdTrackingEnabled | boolean | Whether user opted out of ad tracking |
| error | String | Error message (only present on failure) |

---

## Quick Start (H5)

```javascript
// 1. Get device info
var info = JSON.parse(window.OzyplayEnv.getDeviceInfo());
console.log('Brand:', info.deviceInfo.brand);
console.log('Screen:', info.hardwareInfo.screenInches, 'inches');
console.log('Package:', info.appInfo.packageName);

// 2. Register lifecycle events
window.OzyplayEnv.registerLifecycleCallback('onAppLifecycle');
window.onAppLifecycle = function(event) {
  console.log('Event:', event);
};

// 3. Get screen state
var state = window.OzyplayEnv.getScreenState();

// 4. Fetch Google Ad ID
window.OzyplayEnv.fetchGoogleAdId('onAdIdReceived');
window.onAdIdReceived = function(result) {
  console.log('Ad ID:', result.adId);
};
```

---

## Notes

1. Bridge object name: `window.OzyplayEnv`
2. Async callbacks (`fetchGoogleAdId`) fire once and are not retained
3. Lifecycle callback is persistent until `unregisterLifecycleCallback()` is called
4. All async results are delivered on the main thread via `evaluateJavascript`
5. `screenInches` is calculated from full physical screen resolution using `Display.getRealMetrics()`

## Requirements

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
