package com.zeasn.ozyplay.env;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * 硬件信息数据类 — 封装内存、CPU 架构、屏幕尺寸等硬件属性。
 */
class HardwareInfo {

    private final long memAvailable;
    private final long memTotal;
    private final String androidId;
    private final String cpuAbi;
    private final double screenInches;

    public HardwareInfo(Context context) {
        this.memAvailable = readMemInfo("MemAvailable");
        this.memTotal = readMemInfo("MemTotal");
        this.androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.cpuAbi = Build.SUPPORTED_ABIS[0];
        this.screenInches = calcScreenInches(context);
    }

    public long getMemAvailable() { return memAvailable; }
    public long getMemTotal() { return memTotal; }
    public String getAndroidId() { return androidId; }
    public String getCpuAbi() { return cpuAbi; }
    public double getScreenInches() { return screenInches; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("memAvailable", memAvailable);
            json.put("memTotal", memTotal);
            json.put("androidId", androidId);
            json.put("cpuAbi", cpuAbi);
            json.put("screenInches", screenInches);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private long readMemInfo(String field) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/meminfo"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(field)) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1]) * 1024;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
        }
        return 0;
    }

    /**
     * 计算屏幕物理对角线英寸数（包含状态栏和导航栏的完整屏幕）。
     * 使用 getRealMetrics 获取真实物理分辨率，结合 xdpi/ydpi 计算。
     */
    private double calcScreenInches(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) return 0;
            Display display = wm.getDefaultDisplay();
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);

            double widthInches = realMetrics.widthPixels / (double) realMetrics.xdpi;
            double heightInches = realMetrics.heightPixels / (double) realMetrics.ydpi;
            double diagonalInches = Math.sqrt(widthInches * widthInches + heightInches * heightInches);
            // 保留一位小数
            return Math.round(diagonalInches * 10) / 10.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
