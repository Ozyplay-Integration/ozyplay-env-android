package com.zeasn.ozyplay.env;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * 设备基本信息数据类 — 封装设备品牌、型号、屏幕、语言区域等静态属性。
 * <p>
 * 构造时从 {@link Build}、{@link DisplayMetrics} 和 {@link Locale} 一次性读取所有字段，
 * 所有字段为 final，保证线程安全。
 */
public class DeviceInfo {

    private final String brand;
    private final String product;
    private final String androidVersion;
    private final String model;
    private final String manufacturer;
    private final String board;
    private final int screenHeight;
    private final int screenWidth;
    private final int densityDpi;
    private final String country;
    private final String language;

    public DeviceInfo(Context context) {
        this.brand = Build.BRAND;
        this.product = Build.PRODUCT;
        this.androidVersion = Build.VERSION.RELEASE;
        this.model = Build.MODEL;
        this.manufacturer = Build.MANUFACTURER;
        this.board = Build.BOARD;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.screenHeight = metrics.heightPixels;
        this.screenWidth = metrics.widthPixels;
        this.densityDpi = metrics.densityDpi;

        Locale locale = Locale.getDefault();
        this.country = locale.getCountry();
        this.language = locale.getLanguage();
    }

    public String getBrand() { return brand; }
    public String getProduct() { return product; }
    public String getAndroidVersion() { return androidVersion; }
    public String getModel() { return model; }
    public String getManufacturer() { return manufacturer; }
    public String getBoard() { return board; }
    public int getScreenHeight() { return screenHeight; }
    public int getScreenWidth() { return screenWidth; }
    public int getDensityDpi() { return densityDpi; }
    public String getCountry() { return country; }
    public String getLanguage() { return language; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("brand", brand);
            json.put("product", product);
            json.put("androidVersion", androidVersion);
            json.put("model", model);
            json.put("manufacturer", manufacturer);
            json.put("board", board);
            json.put("screenHeight", screenHeight);
            json.put("screenWidth", screenWidth);
            json.put("densityDpi", densityDpi);
            json.put("country", country);
            json.put("language", language);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
