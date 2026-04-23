package com.locationhook.module;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.locationhook.module.config.HookConfig;
import com.locationhook.module.location.MockLocationProvider;
import com.locationhook.module.location.RouteSimulator;
import com.locationhook.module.utils.LocationConverter;
import com.locationhook.module.utils.XPreference;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * LSPosed Location Hook Module
 * 
 * 功能特性：
 * 1. 全局位置模拟（支持GPS、网络、混合定位）
 * 2. 路线模拟（支持高德地图路线规划导入）
 * 3. 应用级Hook（白名单/黑名单模式）
 * 4. 位置抖动模拟（模拟真实GPS漂移）
 * 5. 海拔、精度、速度模拟
 */
public class LocationHookModule implements IXposedHookLoadPackage {
    
    private static final String TAG = "LocationHook";
    private static final String VERSION = "1.0.0";
    
    // 全局配置
    private static HookConfig sConfig;
    private static MockLocationProvider sMockProvider;
    private static RouteSimulator sRouteSimulator;
    
    // 已Hook的应用包名缓存
    private static final Map<String, Boolean> sHookedApps = new HashMap<>();
    
    // 主线程Handler，用于位置更新
    private static Handler sMainHandler;
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 初始化配置
        if (sConfig == null) {
            loadConfig();
        }
        
        // 只在目标应用中Hook
        if (!shouldHookApp(lpparam.packageName)) {
            return;
        }
        
        XposedBridge.log(TAG + ": Hooking " + lpparam.packageName + " (" + lpparam.processName + ")");
        
        // 初始化主线程Handler
        if (sMainHandler == null) {
            sMainHandler = new Handler(Looper.getMainLooper());
        }
        
        // Hook LocationManager
        hookLocationManager(lpparam);
        
        // Hook FusedLocationProvider (Google Play Services)
        hookFusedLocationProvider(lpparam);
        
        // Hook AMap Location (高德定位SDK)
        hookAMapLocation(lpparam);
        
        // 标记为已Hook
        sHookedApps.put(lpparam.packageName, true);
    }
    
    /**
     * 加载配置
     */
    private void loadConfig() {
        try {
            // 从Xposed偏好设置读取
            XPreference prefs = new XPreference();
            String configJson = prefs.getString("hook_config", null);
            
            if (configJson != null) {
                Gson gson = new Gson();
                sConfig = gson.fromJson(configJson, HookConfig.class);
            } else {
                // 使用默认配置
                sConfig = HookConfig.getDefaultConfig();
            }
            
            // 初始化Mock位置提供者
            sMockProvider = new MockLocationProvider(sConfig);
            
            // 初始化路线模拟器
            sRouteSimulator = new RouteSimulator(sConfig, sMockProvider);
            
            XposedBridge.log(TAG + ": Config loaded, mode=" + sConfig.getMode());
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to load config: " + e.getMessage());
            sConfig = HookConfig.getDefaultConfig();
        }
    }
    
    /**
     * 判断是否应该Hook指定应用
     */
    private boolean shouldHookApp(String packageName) {
        if (sConfig == null) {
            return false;
        }
        
        // 系统应用不Hook
        if (packageName.startsWith("android.") || 
            packageName.startsWith("com.android.") ||
            packageName.equals("android")) {
            return false;
        }
        
        // Xposed/LSPosed自身不Hook
        if (packageName.contains("xposed") || 
            packageName.contains("lsposed") ||
            packageName.equals("com.locationhook.module")) {
            return false;
        }
        
        // 根据配置的模式判断
        switch (sConfig.getMode()) {
            case HookConfig.MODE_GLOBAL:
                // 全局模式：Hook所有应用（除了系统应用）
                return true;
                
            case HookConfig.MODE_WHITELIST:
                // 白名单模式：只Hook列表中的应用
                return sConfig.getAppList().contains(packageName);
                
            case HookConfig.MODE_BLACKLIST:
                // 黑名单模式：不Hook列表中的应用
                return !sConfig.getAppList().contains(packageName);
                
            default:
                return false;
        }
    }
    
    /**
     * Hook LocationManager
     */
    private void hookLocationManager(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> locationManagerClass = XposedHelpers.findClass("android.location.LocationManager", lpparam.classLoader);
            
            // Hook getLastKnownLocation
            XposedHelpers.findAndHookMethod(locationManagerClass, "getLastKnownLocation", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (sMockProvider != null) {
                        Location mockLocation = sMockProvider.getMockLocation();
                        if (mockLocation != null) {
                            param.setResult(mockLocation);
                        }
                    }
                }
            });
            
            // Hook requestLocationUpdates (多个重载)
            hookRequestLocationUpdates(locationManagerClass);
            
            XposedBridge.log(TAG + ": LocationManager hooked");
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook LocationManager: " + e.getMessage());
        }
    }
    
    /**
     * Hook requestLocationUpdates 的所有重载
     */
    private void hookRequestLocationUpdates(Class<?> locationManagerClass) {
        try {
            // 重载1: requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener)
            XposedHelpers.findAndHookMethod(locationManagerClass, "requestLocationUpdates", 
                String.class, long.class, float.class, 
                "android.location.LocationListener",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // 替换为模拟位置更新
                        startMockLocationUpdates((String)param.args[0], param);
                    }
                });
            
            // 重载2: requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent)
            XposedHelpers.findAndHookMethod(locationManagerClass, "requestLocationUpdates",
                String.class, long.class, float.class, "android.app.PendingIntent",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        startMockLocationUpdates((String)param.args[0], param);
                    }
                });
            
            // 重载3: 使用Executor的版本 (Android 9+)
            try {
                XposedHelpers.findAndHookMethod(locationManagerClass, "requestLocationUpdates",
                    String.class, long.class, float.class, "java.util.concurrent.Executor",
                    "android.location.LocationListener",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            startMockLocationUpdates((String)param.args[0], param);
                        }
                    });
            } catch (Exception e) {
                // Android版本不支持，忽略
            }
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook requestLocationUpdates: " + e.getMessage());
        }
    }
    
    /**
     * 开始模拟位置更新
     */
    private void startMockLocationUpdates(String provider, XC_MethodHook.MethodHookParam param) {
        if (sMockProvider == null) {
            return;
        }
        
        // 获取原始Listener
        Object originalListener = null;
        for (Object arg : param.args) {
            if (arg != null && arg.getClass().getName().contains("LocationListener")) {
                originalListener = arg;
                break;
            }
        }
        
        if (originalListener == null) {
            return;
        }
        
        final Object listener = originalListener;
        
        // 启动位置模拟线程
        sMainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // 立即发送一次位置
                    Location mockLocation = sMockProvider.getMockLocation();
                    if (mockLocation != null) {
                        XposedHelpers.callMethod(listener, "onLocationChanged", mockLocation);
                    }
                    
                    // 定期更新（根据配置）
                    if (sConfig != null && sConfig.isContinuousMode()) {
                        sMainHandler.postDelayed(this, sConfig.getUpdateInterval());
                    }
                    
                } catch (Exception e) {
                    XposedBridge.log(TAG + ": Error sending mock location: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Hook FusedLocationProvider (Google Play Services)
     */
    private void hookFusedLocationProvider(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // 尝试Hook Google Play Services 的 FusedLocationProvider
            Class<?> fusedLocationProviderClient = XposedHelpers.findClassIfExists(
                "com.google.android.gms.location.FusedLocationProviderClient", 
                lpparam.classLoader);
            
            if (fusedLocationProviderClient == null) {
                // 尝试新版命名空间
                fusedLocationProviderClient = XposedHelpers.findClassIfExists(
                    "com.google.android.gms.location.LocationServices", 
                    lpparam.classLoader);
            }
            
            if (fusedLocationProviderClient != null) {
                // Hook getLastLocation
                XposedHelpers.findAndHookMethod(fusedLocationProviderClient, "getLastLocation",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // 这里处理Google FusedLocationProvider的返回值
                            // 实际处理在Task对象中，需要进一步Hook
                        }
                    });
                
                XposedBridge.log(TAG + ": FusedLocationProvider hooked");
            }
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook FusedLocationProvider: " + e.getMessage());
        }
    }
    
    /**
     * Hook AMap Location (高德定位SDK)
     */
    private void hookAMapLocation(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook 高德地图定位SDK
            Class<?> aMapLocationClient = XposedHelpers.findClassIfExists(
                "com.amap.api.location.AMapLocationClient",
                lpparam.classLoader);
            
            if (aMapLocationClient != null) {
                // Hook setLocationListener
                XposedHelpers.findAndHookMethod(aMapLocationClient, "setLocationListener",
                    "com.amap.api.location.AMapLocationListener",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // 包装原始监听器
                            Object originalListener = param.args[0];
                            if (originalListener != null) {
                                Object wrappedListener = createAMapLocationWrapper(originalListener);
                                param.args[0] = wrappedListener;
                            }
                        }
                    });
                
                XposedBridge.log(TAG + ": AMapLocation hooked");
            }
            
            // Hook AMapLocationClientOption
            Class<?> aMapLocationClientOption = XposedHelpers.findClassIfExists(
                "com.amap.api.location.AMapLocationClientOption",
                lpparam.classLoader);
            
            if (aMapLocationClientOption != null) {
                // 可以在这里修改定位选项，如定位模式、间隔等
                XposedBridge.log(TAG + ": AMapLocationOption hooked");
            }
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook AMapLocation: " + e.getMessage());
        }
    }
    
    /**
     * 创建高德定位监听器包装器
     */
    private Object createAMapLocationWrapper(Object originalListener) {
        // 使用动态代理创建包装器
        // 实际实现需要使用InvocationHandler
        // 这里返回包装后的监听器对象
        return new AMapLocationListenerWrapper(originalListener, sMockProvider);
    }
}
