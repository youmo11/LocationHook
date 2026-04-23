package com.locationhook.module.utils;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Xposed偏好设置封装
 * 支持LSPosed数据共享
 */
public class XPreference {
    
    private static final String PREF_NAME = "locationhook_config";
    
    private XSharedPreferences xPrefs;
    private SharedPreferences prefs;
    
    /**
     * 从LSPosed模块调用（Hook环境）
     */
    public XPreference() {
        try {
            xPrefs = new XSharedPreferences("com.locationhook.module", PREF_NAME);
            xPrefs.makeWorldReadable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 从APP调用（非Hook环境）
     */
    public XPreference(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_WORLD_READABLE);
    }
    
    public String getString(String key, String defaultValue) {
        if (xPrefs != null) {
            return xPrefs.getString(key, defaultValue);
        }
        if (prefs != null) {
            return prefs.getString(key, defaultValue);
        }
        return defaultValue;
    }
    
    public int getInt(String key, int defaultValue) {
        if (xPrefs != null) {
            return xPrefs.getInt(key, defaultValue);
        }
        if (prefs != null) {
            return prefs.getInt(key, defaultValue);
        }
        return defaultValue;
    }
    
    public long getLong(String key, long defaultValue) {
        if (xPrefs != null) {
            return xPrefs.getLong(key, defaultValue);
        }
        if (prefs != null) {
            return prefs.getLong(key, defaultValue);
        }
        return defaultValue;
    }
    
    public float getFloat(String key, float defaultValue) {
        if (xPrefs != null) {
            return xPrefs.getFloat(key, defaultValue);
        }
        if (prefs != null) {
            return prefs.getFloat(key, defaultValue);
        }
        return defaultValue;
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        if (xPrefs != null) {
            return xPrefs.getBoolean(key, defaultValue);
        }
        if (prefs != null) {
            return prefs.getBoolean(key, defaultValue);
        }
        return defaultValue;
    }
    
    public void putString(String key, String value) {
        if (prefs != null) {
            prefs.edit().putString(key, value).apply();
        }
    }
    
    public void putInt(String key, int value) {
        if (prefs != null) {
            prefs.edit().putInt(key, value).apply();
        }
    }
    
    public void putLong(String key, long value) {
        if (prefs != null) {
            prefs.edit().putLong(key, value).apply();
        }
    }
    
    public void putFloat(String key, float value) {
        if (prefs != null) {
            prefs.edit().putFloat(key, value).apply();
        }
    }
    
    public void putBoolean(String key, boolean value) {
        if (prefs != null) {
            prefs.edit().putBoolean(key, value).apply();
        }
    }
    
    public void remove(String key) {
        if (prefs != null) {
            prefs.edit().remove(key).apply();
        }
    }
    
    public void clear() {
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }
}
