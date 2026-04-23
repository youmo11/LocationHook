package com.locationhook.module.location;

import android.location.Location;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.locationhook.module.config.HookConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.robv.android.xposed.XposedBridge;

/**
 * 高德定位监听器包装器
 * 用于拦截高德定位SDK返回的位置信息
 */
public class AMapLocationListenerWrapper implements InvocationHandler {
    
    private static final String TAG = "AMapListenerWrapper";
    
    private Object originalListener;
    private MockLocationProvider mockProvider;
    
    public AMapLocationListenerWrapper(Object originalListener, MockLocationProvider mockProvider) {
        this.originalListener = originalListener;
        this.mockProvider = mockProvider;
    }
    
    /**
     * 创建动态代理
     */
    public static Object create(Object original, MockLocationProvider mockProvider) {
        try {
            ClassLoader classLoader = original.getClass().getClassLoader();
            Class<?>[] interfaces = original.getClass().getInterfaces();
            
            return Proxy.newProxyInstance(
                classLoader,
                interfaces,
                new AMapLocationListenerWrapper(original, mockProvider)
            );
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to create proxy: " + e.getMessage());
            return original;
        }
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        
        // 拦截onLocationChanged方法
        if ("onLocationChanged".equals(methodName) && args != null && args.length > 0) {
            Object locationArg = args[0];
            
            if (locationArg instanceof AMapLocation) {
                // 替换为高德的AMapLocation
                AMapLocation mockLocation = createMockAMapLocation();
                args[0] = mockLocation;
                
            } else if (locationArg instanceof Location) {
                // 替换为原生Location
                Location mockLocation = mockProvider.getMockLocation();
                args[0] = mockLocation;
            }
        }
        
        // 调用原始方法
        return method.invoke(originalListener, args);
    }
    
    /**
     * 创建模拟的AMapLocation
     */
    private AMapLocation createMockAMapLocation() {
        try {
            Location androidLocation = mockProvider.getMockLocation();
            
            // 通过反射创建AMapLocation
            AMapLocation aMapLocation = new AMapLocation(androidLocation);
            
            // 设置高德特有字段
            aMapLocation.setLatitude(androidLocation.getLatitude());
            aMapLocation.setLongitude(androidLocation.getLongitude());
            aMapLocation.setAltitude(androidLocation.getAltitude());
            aMapLocation.setAccuracy(androidLocation.getAccuracy());
            aMapLocation.setSpeed(androidLocation.getSpeed());
            aMapLocation.setBearing(androidLocation.getBearing());
            aMapLocation.setTime(androidLocation.getTime());
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                aMapLocation.setElapsedRealtimeNanos(androidLocation.getElapsedRealtimeNanos());
            }
            
            // 设置为GPS模式
            aMapLocation.setLocationType(AMapLocation.LOCATION_TYPE_GPS);
            
            // 模拟地址信息
            aMapLocation.setCountry("中国");
            aMapLocation.setProvince("模拟省份");
            aMapLocation.setCity("模拟城市");
            aMapLocation.setDistrict("模拟区县");
            aMapLocation.setStreet("模拟街道");
            aMapLocation.setAddress("模拟地址信息");
            
            // 错误码设为0（成功）
            aMapLocation.setErrorCode(AMapLocation.LOCATION_SUCCESS);
            
            return aMapLocation;
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to create mock AMapLocation: " + e.getMessage());
            return null;
        }
    }
}
