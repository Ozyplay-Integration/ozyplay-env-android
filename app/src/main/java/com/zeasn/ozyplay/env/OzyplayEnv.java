package com.zeasn.ozyplay.env;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * OzyplayEnv JS Bridge — SDK 核心桥接类。
 * <p>
 * H5 通过 window.OzyplayEnv.xxx() 调用原生方法。
 * <p>
 * SDK 内部自动监听宿主 Activity 生命周期和屏幕开关事件，
 * H5 只需注册回调即可接收事件推送。
 * <p>
 * 使用方式：
 * <pre>
 * // 在主线程中调用（如 Activity.onCreate）
 * OzyplayEnv.inject(webView);
 * </pre>
 */
public class OzyplayEnv {

    private static final String TAG = "OzyplayEnv";
    private static final String JS_INTERFACE_NAME = "OzyplayEnv";

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final WeakReference<WebView> mWebViewRef;
    private final Context mContext;
    private WeakReference<Activity> mHostActivityRef;

    /**
     * H5 注册的生命周期回调函数名
     */
    private volatile String mLifecycleCallback;

    private Application.ActivityLifecycleCallbacks mLifecycleCallbacks;
    private BroadcastReceiver mScreenReceiver;
    private volatile boolean mReleased = false;

    /**
     * 将 OzyplayEnv JS Bridge 注入到指定 WebView。
     * 必须在主线程调用。
     *
     * @param webView 宿主 WebView 实例
     * @return OzyplayEnv 实例
     */
    public static OzyplayEnv inject(WebView webView) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView must not be null");
        }
        OzyplayEnv bridge = new OzyplayEnv(webView);
        webView.addJavascriptInterface(bridge, JS_INTERFACE_NAME);
        bridge.registerLifecycleObserver();
        bridge.registerScreenReceiver();
        return bridge;
    }

    private OzyplayEnv(WebView webView) {
        mWebViewRef = new WeakReference<>(webView);
        mContext = webView.getContext().getApplicationContext();
        // 尝试获取宿主 Activity
        Context ctx = webView.getContext();
        if (ctx instanceof Activity) {
            mHostActivityRef = new WeakReference<>((Activity) ctx);
        }
        DeviceInfoManager.getInstance().init(mContext);
    }

    /**
     * 释放资源，取消生命周期监听和广播接收器。
     * SDK 在 Activity.onDestroy 时自动调用，也可手动调用。
     */
    public void release() {
        if (mReleased) return;
        mReleased = true;

        unregisterLifecycleObserver();
        unregisterScreenReceiver();
        mLifecycleCallback = null;
        mHostActivityRef = null;

        // 移除 JS 接口，断开 WebView 对 bridge 的引用
        WebView webView = mWebViewRef.get();
        if (webView != null) {
            webView.removeJavascriptInterface(JS_INTERFACE_NAME);
        }

        // 清除 Handler 中待执行的消息
        mMainHandler.removeCallbacksAndMessages(null);
    }

    /***********************************************************************************************
     * 设备信息
     **********************************************************************************************/

    /**
     * 查询所有设备信息（同步）。
     * 输出格式：{"deviceInfo":{...}, "hardwareInfo":{...}, "storageInfo":{...}, "appInfo":{...}}
     */
    @JavascriptInterface
    public String getDeviceInfo() {
        if (mReleased) return "{}";

        String realUa = getWebViewUaOnMainThread();
        if (realUa != null && !realUa.isEmpty()) {
            DeviceInfoManager.getInstance().setUserAgent(realUa);
        }
        return DeviceInfoManager.getInstance().getAllInfoJson();
    }

    private String getWebViewUaOnMainThread() {
        WebView webView = mWebViewRef.get();
        if (webView == null) return null;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            return webView.getSettings().getUserAgentString();
        }

        final String[] result = {null};
        final CountDownLatch latch = new CountDownLatch(1);
        mMainHandler.post(() -> {
            try {
                WebView wv = mWebViewRef.get();
                if (wv != null) {
                    result[0] = wv.getSettings().getUserAgentString();
                }
            } catch (Exception e) {
                Log.e(TAG, "getWebViewUaOnMainThread failed", e);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "getWebViewUaOnMainThread interrupted", e);
        }
        return result[0];
    }

    /***********************************************************************************************
     * 屏幕状态
     **********************************************************************************************/

    /**
     * H5 主动查询当前屏幕状态。
     *
     * @return "screen_on" | "screen_off" | "unknown"
     */
    @JavascriptInterface
    public String getScreenState() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (pm == null) return "unknown";
        return pm.isInteractive() ? "screen_on" : "screen_off";
    }

    /***********************************************************************************************
     * 生命周期回调
     **********************************************************************************************/

    /**
     * H5 注册生命周期回调。
     * 注册后，SDK 自动感知宿主 Activity 生命周期和屏幕开关事件，
     * 通过 window[callbackName](event) 推送给 H5。
     * <p>
     * event 值为: "resume", "pause", "stop", "destroy", "screen_off", "screen_on"
     *
     * @param callbackName H5 端全局函数名，如 "onAppLifecycle"
     */
    @JavascriptInterface
    public void registerLifecycleCallback(String callbackName) {
        Log.i(TAG, "registerLifecycleCallback: " + callbackName);
        mLifecycleCallback = callbackName;
    }

    /**
     * H5 取消生命周期回调注册
     */
    @JavascriptInterface
    public void unregisterLifecycleCallback() {
        Log.i(TAG, "unregisterLifecycleCallback");
        mLifecycleCallback = null;
    }

    /**
     * 获取当前注册的生命周期回调函数名
     */
    public String getLifecycleCallback() {
        return mLifecycleCallback;
    }

    /***********************************************************************************************
     * 内部生命周期监听
     **********************************************************************************************/

    private void registerLifecycleObserver() {
        Application app = (Application) mContext;
        mLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                if (isHostActivity(activity)) {
                    dispatchLifecycleEvent("resume");
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (isHostActivity(activity)) {
                    dispatchLifecycleEvent("pause");
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (isHostActivity(activity)) {
                    dispatchLifecycleEvent("stop");
                }
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (isHostActivity(activity)) {
                    dispatchLifecycleEvent("destroy");
                    release();
                }
            }

            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        };
        app.registerActivityLifecycleCallbacks(mLifecycleCallbacks);
    }

    private void unregisterLifecycleObserver() {
        if (mLifecycleCallbacks != null) {
            ((Application) mContext).unregisterActivityLifecycleCallbacks(mLifecycleCallbacks);
            mLifecycleCallbacks = null;
        }
    }

    private void registerScreenReceiver() {
        mScreenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mReleased) return;
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    dispatchLifecycleEvent("screen_off");
                } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    dispatchLifecycleEvent("screen_on");
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mScreenReceiver, filter);
    }

    private void unregisterScreenReceiver() {
        if (mScreenReceiver != null) {
            try {
                mContext.unregisterReceiver(mScreenReceiver);
            } catch (Exception ignored) {}
            mScreenReceiver = null;
        }
    }

    private boolean isHostActivity(Activity activity) {
        if (mHostActivityRef == null) return false;
        Activity host = mHostActivityRef.get();
        return host != null && host == activity;
    }

    private void dispatchLifecycleEvent(String event) {
        if (mReleased) return;
        String callback = mLifecycleCallback;
        if (callback == null || callback.isEmpty()) return;

        WebView webView = mWebViewRef.get();
        if (webView == null) return;

        String js = "window." + callback + " && window." + callback + "('" + event + "')";
        Log.d(TAG, "dispatchLifecycleEvent: " + js);
        mMainHandler.post(() -> {
            WebView wv = mWebViewRef.get();
            if (wv != null && !mReleased) {
                wv.evaluateJavascript(js, null);
            }
        });
    }

    /***********************************************************************************************
     * Google Advertising ID
     **********************************************************************************************/

    /**
     * H5 请求获取 Google Advertising ID（异步）。
     * 完成后通过指定的回调函数推送结果给 H5。
     *
     * @param callbackName H5 端接收结果的全局函数名
     */
    @JavascriptInterface
    public void fetchGoogleAdId(String callbackName) {
        if (mReleased) return;

        new Thread(() -> {
            String jsonResult = AdIdHelper.fetchAdIdJson(mContext);
            mMainHandler.post(() -> {
                WebView wv = mWebViewRef.get();
                if (wv != null && !mReleased) {
                    wv.evaluateJavascript(
                            "window." + callbackName + " && window." + callbackName + "(" + jsonResult + ")",
                            null);
                }
            });
        }).start();
    }

}
