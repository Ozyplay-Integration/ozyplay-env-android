package com.zeasn.ozyplay.env;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 设备信息门面类 — 单例模式，提供统一的设备信息采集入口。
 * 仅供 SDK 内部使用。
 */
class DeviceInfoManager {

    private static volatile DeviceInfoManager sInstance;
    private Context mContext;
    private String mUserAgent;

    private DeviceInfoManager() {}

    static DeviceInfoManager getInstance() {
        if (sInstance == null) {
            synchronized (DeviceInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new DeviceInfoManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化，应在 Application.onCreate() 中调用。
     */
    void init(Context context) {
        mContext = context.getApplicationContext();
    }

    DeviceInfo getDeviceInfo() {
        checkInitialized();
        return new DeviceInfo(mContext);
    }

    HardwareInfo getHardwareInfo() {
        checkInitialized();
        return new HardwareInfo(mContext);
    }

    AppInfo getAppInfo() {
        checkInitialized();
        return new AppInfo(mContext);
    }

    JSONObject getStorageInfo() {
        checkInitialized();
        return StorageUtil.toJson();
    }

    /**
     * 设置宿主 WebView 实际使用的 User-Agent。
     * 由 OzyplayEnv 在 getDeviceInfo() 调用时自动注入。
     */
    void setUserAgent(String userAgent) {
        mUserAgent = userAgent;
    }

    /**
     * 获取所有设备信息的聚合 JSON 字符串。
     * 输出格式：{"deviceInfo":{...}, "hardwareInfo":{...}, "storageInfo":{...}, "appInfo":{...}}
     * <p>
     * 仅供 SDK 内部使用，外部请通过 OzyplayEnv JS Bridge 获取。
     */
    String getAllInfoJson() {
        checkInitialized();
        try {
            JSONObject result = new JSONObject();
            result.put("deviceInfo", new DeviceInfo(mContext).toJson());
            result.put("hardwareInfo", new HardwareInfo(mContext).toJson());
            result.put("storageInfo", StorageUtil.toJson());

            AppInfo appInfo = new AppInfo(mContext);
            if (mUserAgent != null && !mUserAgent.isEmpty()) {
                appInfo.setCustomUserAgent(mUserAgent);
            }
            result.put("appInfo", appInfo.toJson());

            return result.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private void checkInitialized() {
        if (mContext == null) {
            throw new IllegalStateException(
                    "DeviceInfoManager not initialized. Call DeviceInfoManager.getInstance().init(context) first.");
        }
    }
}
