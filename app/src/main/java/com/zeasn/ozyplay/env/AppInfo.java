package com.zeasn.ozyplay.env;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.WebSettings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 宿主应用信息数据类 — 封装包名、版本号、UserAgent 等应用属性。
 */
public class AppInfo {

    private final String packageName;
    private final long versionCode;
    private final String defaultUserAgent;
    private String customUserAgent;

    public AppInfo(Context context) {
        this.packageName = context.getPackageName();
        this.versionCode = getAppVersionCode(context);
        this.defaultUserAgent = WebSettings.getDefaultUserAgent(context);
        this.customUserAgent = this.defaultUserAgent;
    }

    public String getPackageName() { return packageName; }
    public long getVersionCode() { return versionCode; }
    public String getDefaultUserAgent() { return defaultUserAgent; }
    public String getCustomUserAgent() { return customUserAgent; }

    /**
     * 设置宿主 WebView 实际使用的 UA
     */
    public void setCustomUserAgent(String userAgent) {
        if (userAgent != null && !userAgent.isEmpty()) {
            this.customUserAgent = userAgent;
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("packageName", packageName);
            json.put("versionCode", versionCode);
            json.put("defaultUserAgent", defaultUserAgent);
            json.put("customUserAgent", customUserAgent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private long getAppVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return pi.getLongVersionCode();
            } else {
                return pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
