package com.zeasn.ozyplay.env;

import android.os.Environment;
import android.os.StatFs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 存储信息工具类 — 提供 ROM 和内置存储容量查询
 */
class StorageUtil {

    public static long getRomTotalSize() {
        return new StatFs(Environment.getDataDirectory().getPath()).getTotalBytes();
    }

    public static long getRomAvailableSize() {
        return new StatFs(Environment.getDataDirectory().getPath()).getAvailableBytes();
    }

    public static long getRomUsedSize() {
        return getRomTotalSize() - getRomAvailableSize();
    }

    public static long getStorageTotalSize() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return 0;
        return new StatFs(Environment.getExternalStorageDirectory().getPath()).getTotalBytes();
    }

    public static long getStorageAvailableSize() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return 0;
        return new StatFs(Environment.getExternalStorageDirectory().getPath()).getAvailableBytes();
    }

    public static long getStorageUsedSize() {
        return getStorageTotalSize() - getStorageAvailableSize();
    }

    public static JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("romTotal", getRomTotalSize());
            json.put("romAvailable", getRomAvailableSize());
            json.put("romUsed", getRomUsedSize());
            json.put("storageTotal", getStorageTotalSize());
            json.put("storageAvailable", getStorageAvailableSize());
            json.put("storageUsed", getStorageUsedSize());
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
        return json;
    }
}
