package com.locationhook.module.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理帮助类
 */
public class PermissionHelper {
    
    // 基础权限
    public static final String[] BASIC_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    
    // Android 10+ 需要后台定位权限
    public static final String[] BACKGROUND_LOCATION_PERMISSION = {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    
    // Android 11+ 需要管理外部存储权限
    public static final String MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
    
    /**
     * 检查是否已授权所有基础权限
     */
    public static boolean hasBasicPermissions(Context context) {
        for (String permission : BASIC_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取未授权的权限列表
     */
    public static String[] getDeniedPermissions(Context context, String[] permissions) {
        List<String> deniedList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedList.add(permission);
            }
        }
        return deniedList.toArray(new String[0]);
    }
    
    /**
     * 请求基础权限
     */
    public static void requestBasicPermissions(Activity activity, int requestCode) {
        String[] deniedPermissions = getDeniedPermissions(activity, BASIC_PERMISSIONS);
        if (deniedPermissions.length > 0) {
            ActivityCompat.requestPermissions(activity, deniedPermissions, requestCode);
        }
    }
    
    /**
     * 请求后台定位权限（Android 10+）
     */
    public static void requestBackgroundLocation(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, BACKGROUND_LOCATION_PERMISSION, requestCode);
            }
        }
    }
    
    /**
     * 检查是否有后台定位权限
     */
    public static boolean hasBackgroundLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 检查是否有管理外部存储权限（Android 11+）
     */
    public static boolean hasManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }
    
    /**
     * 处理权限请求结果
     */
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults, 
                                                   PermissionCallback callback) {
        if (grantResults.length == 0) {
            return false;
        }
        
        boolean allGranted = true;
        List<String> deniedPermissions = new ArrayList<>();
        
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                deniedPermissions.add(permissions[i]);
            }
        }
        
        if (callback != null) {
            if (allGranted) {
                callback.onAllPermissionsGranted();
            } else {
                callback.onPermissionsDenied(deniedPermissions.toArray(new String[0]));
            }
        }
        
        return allGranted;
    }
    
    /**
     * 权限回调接口
     */
    public interface PermissionCallback {
        void onAllPermissionsGranted();
        void onPermissionsDenied(String[] deniedPermissions);
    }
}
