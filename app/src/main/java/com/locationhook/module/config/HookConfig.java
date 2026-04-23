package com.locationhook.module.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Hook配置管理类
 */
public class HookConfig {
    
    // 工作模式
    public static final int MODE_GLOBAL = 0;      // 全局模式（Hook所有应用）
    public static final int MODE_WHITELIST = 1;   // 白名单模式（只Hook指定应用）
    public static final int MODE_BLACKLIST = 2;   // 黑名单模式（不Hook指定应用）
    
    // 定位模式
    public static final int LOCATION_MODE_FIXED = 0;      // 固定位置
    public static final int LOCATION_MODE_ROUTE = 1;       // 路线模拟
    public static final int LOCATION_MODE_RANDOM = 2;      // 随机漂移
    
    // SharedPreferences名称
    private static final String PREF_NAME = "LocationHookConfig";
    
    // 配置项Key
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_MODE = "mode";
    private static final String KEY_LOCATION_MODE = "location_mode";
    private static final String KEY_APP_LIST = "app_list";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_ACCURACY = "accuracy";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_UPDATE_INTERVAL = "update_interval";
    private static final String KEY_ROUTE_POINTS = "route_points";
    private static final String KEY_ROUTE_SPEED = "route_speed";
    private static final String KEY_AMAP_KEY = "amap_key";
    private static final String KEY_JITTER_ENABLED = "jitter_enabled";
    private static final String KEY_JITTER_RADIUS = "jitter_radius";
    private static final String KEY_CONTINUOUS_MODE = "continuous_mode";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public HookConfig(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // ========== 基础开关 ==========
    
    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, true);
    }
    
    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
    
    // ========== 工作模式 ==========
    
    public int getMode() {
        return prefs.getInt(KEY_MODE, MODE_GLOBAL);
    }
    
    public void setMode(int mode) {
        prefs.edit().putInt(KEY_MODE, mode).apply();
    }
    
    // ========== 定位模式 ==========
    
    public int getLocationMode() {
        return prefs.getInt(KEY_LOCATION_MODE, LOCATION_MODE_FIXED);
    }
    
    public void setLocationMode(int locationMode) {
        prefs.edit().putInt(KEY_LOCATION_MODE, locationMode).apply();
    }
    
    // ========== 应用列表 ==========
    
    public List<String> getAppList() {
        String json = prefs.getString(KEY_APP_LIST, "[]");
        return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
    }
    
    public void setAppList(List<String> appList) {
        prefs.edit().putString(KEY_APP_LIST, gson.toJson(appList)).apply();
    }
    
    public void addApp(String packageName) {
        List<String> list = getAppList();
        if (!list.contains(packageName)) {
            list.add(packageName);
            setAppList(list);
        }
    }
    
    public void removeApp(String packageName) {
        List<String> list = getAppList();
        list.remove(packageName);
        setAppList(list);
    }
    
    // ========== 位置坐标 ==========
    
    public double getLatitude() {
        return Double.parseDouble(prefs.getString(KEY_LATITUDE, "31.2304")); // 默认上海
    }
    
    public void setLatitude(double latitude) {
        prefs.edit().putString(KEY_LATITUDE, String.valueOf(latitude)).apply();
    }
    
    public double getLongitude() {
        return Double.parseDouble(prefs.getString(KEY_LONGITUDE, "121.4737")); // 默认上海
    }
    
    public void setLongitude(double longitude) {
        prefs.edit().putString(KEY_LONGITUDE, String.valueOf(longitude)).apply();
    }
    
    public double getAltitude() {
        return Double.parseDouble(prefs.getString(KEY_ALTITUDE, "10.0"));
    }
    
    public void setAltitude(double altitude) {
        prefs.edit().putString(KEY_ALTITUDE, String.valueOf(altitude)).apply();
    }
    
    // ========== 定位精度 ==========
    
    public float getAccuracy() {
        return prefs.getFloat(KEY_ACCURACY, 10.0f);
    }
    
    public void setAccuracy(float accuracy) {
        prefs.edit().putFloat(KEY_ACCURACY, accuracy).apply();
    }
    
    public float getSpeed() {
        return prefs.getFloat(KEY_SPEED, 0.0f);
    }
    
    public void setSpeed(float speed) {
        prefs.edit().putFloat(KEY_SPEED, speed).apply();
    }
    
    public float getBearing() {
        return prefs.getFloat(KEY_BEARING, 0.0f);
    }
    
    public void setBearing(float bearing) {
        prefs.edit().putFloat(KEY_BEARING, bearing).apply();
    }
    
    // ========== 更新间隔 ==========
    
    public long getUpdateInterval() {
        return prefs.getLong(KEY_UPDATE_INTERVAL, 1000); // 默认1秒
    }
    
    public void setUpdateInterval(long interval) {
        prefs.edit().putLong(KEY_UPDATE_INTERVAL, interval).apply();
    }
    
    public boolean isContinuousMode() {
        return prefs.getBoolean(KEY_CONTINUOUS_MODE, false);
    }
    
    public void setContinuousMode(boolean continuous) {
        prefs.edit().putBoolean(KEY_CONTINUOUS_MODE, continuous).apply();
    }
    
    // ========== 路线模拟 ==========
    
    public List<RoutePoint> getRoutePoints() {
        String json = prefs.getString(KEY_ROUTE_POINTS, "[]");
        return gson.fromJson(json, new TypeToken<List<RoutePoint>>(){}.getType());
    }
    
    public void setRoutePoints(List<RoutePoint> points) {
        prefs.edit().putString(KEY_ROUTE_POINTS, gson.toJson(points)).apply();
    }
    
    public float getRouteSpeed() {
        return prefs.getFloat(KEY_ROUTE_SPEED, 5.0f); // 默认5m/s = 18km/h
    }
    
    public void setRouteSpeed(float speed) {
        prefs.edit().putFloat(KEY_ROUTE_SPEED, speed).apply();
    }
    
    // ========== 高德地图Key ==========
    
    public String getAmapKey() {
        return prefs.getString(KEY_AMAP_KEY, "");
    }
    
    public void setAmapKey(String key) {
        prefs.edit().putString(KEY_AMAP_KEY, key).apply();
    }
    
    // ========== 位置抖动 ==========
    
    public boolean isJitterEnabled() {
        return prefs.getBoolean(KEY_JITTER_ENABLED, false);
    }
    
    public void setJitterEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_JITTER_ENABLED, enabled).apply();
    }
    
    public float getJitterRadius() {
        return prefs.getFloat(KEY_JITTER_RADIUS, 10.0f);
    }
    
    public void setJitterRadius(float radius) {
        prefs.edit().putFloat(KEY_JITTER_RADIUS, radius).apply();
    }
    
    // ========== 获取完整配置 ==========
    
    public HookConfig getDefaultConfig() {
        return this;
    }
    
    // ========== 路线点数据类 ==========
    
    public static class RoutePoint {
        private double latitude;
        private double longitude;
        private double altitude;
        private long timestamp;
        
        public RoutePoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = 0;
            this.timestamp = System.currentTimeMillis();
        }
        
        public RoutePoint(double latitude, double longitude, double altitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public double getAltitude() { return altitude; }
        public void setAltitude(double altitude) { this.altitude = altitude; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
