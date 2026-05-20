package com.zeasn.ozyplay.env;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

/**
 * Google Advertising ID 获取工具类。
 * 封装 AdvertisingIdClient 调用，返回 JSON 字符串结果。
 */
public class AdIdHelper {

    private static final String TAG = "AdIdHelper";

    private AdIdHelper() {}

    /**
     * 获取 Google Advertising ID 信息，返回 JSON 字符串。
     * 此方法为阻塞调用，必须在后台线程执行。
     *
     * @param context Application Context
     * @return JSON 字符串，格式：
     *         成功：{"adId":"xxx","isLimitAdTrackingEnabled":false}
     *         失败：{"adId":"","isLimitAdTrackingEnabled":true,"error":"错误信息"}
     */
    public static String fetchAdIdJson(Context context) {
        String adId = "";
        boolean limitTracking = true;
        String error = "";
        try {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            adId = adInfo.getId() != null ? adInfo.getId() : "";
            limitTracking = adInfo.isLimitAdTrackingEnabled();
        } catch (IOException | GooglePlayServicesNotAvailableException |
                 GooglePlayServicesRepairableException e) {
            Log.e(TAG, "fetchAdIdJson failed", e);
            error = e.getMessage() != null ? e.getMessage() : "unknown error";
        }

        return "{\"adId\":\"" + adId + "\","
                + "\"isLimitAdTrackingEnabled\":" + limitTracking
                + (error.isEmpty() ? "" : ",\"error\":\"" + error.replace("\"", "\\\"") + "\"")
                + "}";
    }
}
