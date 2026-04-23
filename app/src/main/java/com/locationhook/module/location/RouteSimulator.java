package com.locationhook.module.location;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.locationhook.module.config.HookConfig;

import java.util.List;

import de.robv.android.xposed.XposedBridge;

/**
 * 路线模拟器
 * 用于模拟沿指定路线的移动
 */
public class RouteSimulator {
    
    private static final String TAG = "RouteSimulator";
    
    // 模拟状态
    public static final int STATE_IDLE = 0;       // 空闲
    public static final int STATE_RUNNING = 1;      // 运行中
    public static final int STATE_PAUSED = 2;       // 暂停
    
    private HookConfig config;
    private MockLocationProvider mockProvider;
    private Handler handler;
    
    // 路线数据
    private List<HookConfig.RoutePoint> routePoints;
    private int currentPointIndex;
    
    // 模拟参数
    private float speedMps;        // 速度 (米/秒)
    private long updateInterval;   // 更新间隔 (毫秒)
    
    // 状态
    private int state = STATE_IDLE;
    private long startTime;
    private long pauseTime;
    private long totalPausedTime;
    
    // 当前模拟位置
    private double currentLat;
    private double currentLng;
    private double currentAlt;
    private float currentBearing;
    
    // 回调接口
    private RouteSimulatorCallback callback;
    
    public interface RouteSimulatorCallback {
        void onRouteStarted();
        void onRouteProgress(int currentPoint, int totalPoints, double lat, double lng);
        void onRouteCompleted();
        void onRouteError(String error);
        void onLocationUpdated(Location location);
    }
    
    public RouteSimulator(HookConfig config, MockLocationProvider mockProvider) {
        this.config = config;
        this.mockProvider = mockProvider;
        this.handler = new Handler(Looper.getMainLooper());
        
        // 从配置加载参数
        loadConfig();
    }
    
    private void loadConfig() {
        this.routePoints = config.getRoutePoints();
        this.speedMps = config.getRouteSpeed();
        this.updateInterval = config.getUpdateInterval();
        
        // 计算默认更新间隔（如果未设置）
        if (updateInterval <= 0) {
            updateInterval = 1000; // 默认1秒
        }
        
        // 默认速度（如果未设置）
        if (speedMps <= 0) {
            speedMps = 5.0f; // 默认5m/s = 18km/h
        }
    }
    
    /**
     * 设置路线点
     */
    public void setRoutePoints(List<HookConfig.RoutePoint> points) {
        this.routePoints = points;
        config.setRoutePoints(points);
    }
    
    /**
     * 开始模拟
     */
    public void start() {
        if (state == STATE_RUNNING) {
            XposedBridge.log(TAG + ": Already running");
            return;
        }
        
        if (routePoints == null || routePoints.isEmpty()) {
            if (callback != null) {
                callback.onRouteError("路线点为空");
            }
            return;
        }
        
        // 重置状态
        currentPointIndex = 0;
        startTime = SystemClock.elapsedRealtime();
        totalPausedTime = 0;
        
        // 设置起点
        HookConfig.RoutePoint startPoint = routePoints.get(0);
        currentLat = startPoint.getLatitude();
        currentLng = startPoint.getLongitude();
        currentAlt = startPoint.getAltitude();
        
        // 更新MockProvider
        mockProvider.updateLocation(currentLat, currentLng, currentAlt);
        
        // 开始更新循环
        state = STATE_RUNNING;
        scheduleNextUpdate();
        
        if (callback != null) {
            callback.onRouteStarted();
        }
        
        XposedBridge.log(TAG + ": Route simulation started, points=" + routePoints.size());
    }
    
    /**
     * 暂停模拟
     */
    public void pause() {
        if (state != STATE_RUNNING) {
            return;
        }
        
        state = STATE_PAUSED;
        pauseTime = SystemClock.elapsedRealtime();
        handler.removeCallbacksAndMessages(null);
        
        XposedBridge.log(TAG + ": Route simulation paused");
    }
    
    /**
     * 恢复模拟
     */
    public void resume() {
        if (state != STATE_PAUSED) {
            return;
        }
        
        totalPausedTime += SystemClock.elapsedRealtime() - pauseTime;
        state = STATE_RUNNING;
        scheduleNextUpdate();
        
        XposedBridge.log(TAG + ": Route simulation resumed");
    }
    
    /**
     * 停止模拟
     */
    public void stop() {
        if (state == STATE_IDLE) {
            return;
        }
        
        state = STATE_IDLE;
        handler.removeCallbacksAndMessages(null);
        
        if (callback != null) {
            callback.onRouteCompleted();
        }
        
        XposedBridge.log(TAG + ": Route simulation stopped");
    }
    
    /**
     * 调度下一次更新
     */
    private void scheduleNextUpdate() {
        if (state != STATE_RUNNING) {
            return;
        }
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (state != STATE_RUNNING) {
                    return;
                }
                
                updatePosition();
                
                // 检查是否到达终点
                if (currentPointIndex >= routePoints.size() - 1) {
                    // 到达终点
                    if (config.isContinuousMode()) {
                        // 循环模式：回到起点
                        currentPointIndex = 0;
                    } else {
                        // 停止
                        stop();
                        return;
                    }
                }
                
                scheduleNextUpdate();
            }
        }, updateInterval);
    }
    
    /**
     * 更新当前位置
     */
    private void updatePosition() {
        if (currentPointIndex >= routePoints.size() - 1) {
            return;
        }
        
        // 获取当前点和下一个点
        HookConfig.RoutePoint currentPoint = routePoints.get(currentPointIndex);
        HookConfig.RoutePoint nextPoint = routePoints.get(currentPointIndex + 1);
        
        // 计算两点之间的距离
        double distance = calculateDistance(
            currentPoint.getLatitude(), currentPoint.getLongitude(),
            nextPoint.getLatitude(), nextPoint.getLongitude()
        );
        
        // 计算本次移动距离
        double moveDistance = speedMps * (updateInterval / 1000.0);
        
        // 计算进度比例
        double progress = Math.min(moveDistance / distance, 1.0);
        
        // 插值计算新位置
        currentLat = currentPoint.getLatitude() + 
            (nextPoint.getLatitude() - currentPoint.getLatitude()) * progress;
        currentLng = currentPoint.getLongitude() + 
            (nextPoint.getLongitude() - currentPoint.getLongitude()) * progress;
        currentAlt = currentPoint.getAltitude() + 
            (nextPoint.getAltitude() - currentPoint.getAltitude()) * progress;
        
        // 计算方向
        currentBearing = (float) calculateBearing(
            currentPoint.getLatitude(), currentPoint.getLongitude(),
            nextPoint.getLatitude(), nextPoint.getLongitude()
        );
        
        // 如果到达下一个点，移动到下一个线段
        if (progress >= 1.0) {
            currentPointIndex++;
        }
        
        // 更新MockProvider
        mockProvider.updateLocation(currentLat, currentLng, currentAlt);
        mockProvider.updateSpeed(currentBearing);
        mockProvider.updateBearing(currentBearing);
        
        // 回调通知
        if (callback != null) {
            callback.onRouteProgress(currentPointIndex, routePoints.size(), currentLat, currentLng);
            
            // 创建Location对象用于回调
            Location location = mockProvider.getMockLocation();
            callback.onLocationUpdated(location);
        }
    }
    
    /**
     * 计算两点间距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // 地球半径（米）
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * 计算两点间方向角（度）
     */
    private double calculateBearing(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        
        double y = Math.sin(dLng) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
    
    // ========== Getters and Setters ==========
    
    public int getState() {
        return state;
    }
    
    public int getCurrentPointIndex() {
        return currentPointIndex;
    }
    
    public double getCurrentLat() {
        return currentLat;
    }
    
    public double getCurrentLng() {
        return currentLng;
    }
    
    public float getSpeedMps() {
        return speedMps;
    }
    
    public void setSpeedMps(float speedMps) {
        this.speedMps = speedMps;
    }
    
    public void setCallback(RouteSimulatorCallback callback) {
        this.callback = callback;
    }
    
    public void removeCallback() {
        this.callback = null;
    }
}
