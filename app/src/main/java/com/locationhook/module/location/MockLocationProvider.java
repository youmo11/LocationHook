package com.locationhook.module.location;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import com.locationhook.module.config.HookConfig;
import com.locationhook.module.utils.LocationConverter;

import java.util.Random;

import de.robv.android.xposed.XposedBridge;

/**
 * 模拟位置提供者
 * 负责生成伪造的Location对象
 */
public class MockLocationProvider {
    
    private static final String TAG = "MockLocationProvider";
    private static final String PROVIDER_NAME = "gps";
    
    private HookConfig config;
    private Random random;
    
    // 当前模拟位置
    private double currentLatitude;
    private double currentLongitude;
    private double currentAltitude;
    private float currentSpeed;
    private float currentBearing;
    
    // 抖动偏移量
    private double jitterOffsetLat;
    private double jitterOffsetLng;
    
    public MockLocationProvider(HookConfig config) {
        this.config = config;
        this.random = new Random();
        loadFromConfig();
    }
    
    /**
     * 从配置加载位置信息
     */
    private void loadFromConfig() {
        currentLatitude = config.getLatitude();
        currentLongitude = config.getLongitude();
        currentAltitude = config.getAltitude();
        currentSpeed = config.getSpeed();
        currentBearing = config.getBearing();
        
        // 计算初始抖动偏移
        updateJitterOffset();
    }
    
    /**
     * 更新抖动偏移量
     */
    private void updateJitterOffset() {
        if (!config.isJitterEnabled()) {
            jitterOffsetLat = 0;
            jitterOffsetLng = 0;
            return;
        }
        
        float jitterRadius = config.getJitterRadius(); // 米
        
        // 随机角度和距离
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * jitterRadius;
        
        // 计算经纬度偏移（简化计算，假设1度≈111km）
        jitterOffsetLat = (distance * Math.cos(angle)) / 111000.0;
        jitterOffsetLng = (distance * Math.sin(angle)) / (111000.0 * Math.cos(Math.toRadians(currentLatitude)));
    }
    
    /**
     * 获取模拟的Location对象
     */
    public Location getMockLocation() {
        // 创建Location对象
        Location location = new Location(PROVIDER_NAME);
        
        // 应用抖动偏移
        double lat = currentLatitude + jitterOffsetLat;
        double lng = currentLongitude + jitterOffsetLng;
        
        // 设置坐标
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAltitude(currentAltitude);
        
        // 设置精度
        location.setAccuracy(config.getAccuracy());
        
        // 设置速度（如果启用）
        if (currentSpeed > 0) {
            location.setSpeed(currentSpeed);
        }
        
        // 设置方向（如果启用）
        if (currentBearing > 0) {
            location.setBearing(currentBearing);
        }
        
        // 设置时间戳
        long currentTime = System.currentTimeMillis();
        location.setTime(currentTime);
        
        // Android 17+ 使用 elapsed realtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        
        // 添加额外信息到Bundle
        Bundle extras = new Bundle();
        extras.putBoolean("is_mock", true);
        extras.putString("mock_provider", "LocationHook");
        location.setExtras(extras);
        
        // 定期更新抖动偏移（每次获取位置时更新）
        if (config.isJitterEnabled()) {
            updateJitterOffset();
        }
        
        return location;
    }
    
    /**
     * 更新当前位置（用于路线模拟）
     */
    public void updateLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        
        // 保存到配置
        config.setLatitude(latitude);
        config.setLongitude(longitude);
    }
    
    public void updateLocation(double latitude, double longitude, double altitude) {
        updateLocation(latitude, longitude);
        this.currentAltitude = altitude;
        config.setAltitude(altitude);
    }
    
    public void updateSpeed(float speed) {
        this.currentSpeed = speed;
        config.setSpeed(speed);
    }
    
    public void updateBearing(float bearing) {
        this.currentBearing = bearing;
        config.setBearing(bearing);
    }
    
    // Getters
    public double getCurrentLatitude() {
        return currentLatitude;
    }
    
    public double getCurrentLongitude() {
        return currentLongitude;
    }
    
    public double getCurrentAltitude() {
        return currentAltitude;
    }
}