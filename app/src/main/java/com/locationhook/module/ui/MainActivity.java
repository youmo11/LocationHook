package com.locationhook.module.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.locationhook.module.R;
import com.locationhook.module.amap.AmapManager;
import com.locationhook.module.config.HookConfig;
import com.locationhook.module.utils.PermissionHelper;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity implements AMap.OnMapLongClickListener {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // UI组件
    private MapView mapView;
    private AMap aMap;
    private Switch switchEnable;
    private Button btnRoutePlan;
    private Button btnAppSelect;
    private Button btnSettings;
    private TextView tvCurrentLocation;
    
    // 工具类
    private HookConfig config;
    private AmapManager amapManager;
    
    // 当前选中位置
    private LatLng currentLatLng;
    private Marker currentMarker;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化
        config = new HookConfig(this);
        amapManager = AmapManager.getInstance(this);
        
        // 初始化UI
        initViews();
        mapView.onCreate(savedInstanceState);
        
        // 检查权限
        if (!PermissionHelper.hasBasicPermissions(this)) {
            PermissionHelper.requestBasicPermissions(this, PERMISSION_REQUEST_CODE);
        } else {
            initMap();
        }
        
        // 加载保存的位置
        loadSavedLocation();
        
        // 绑定事件
        bindEvents();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        mapView = findViewById(R.id.map_view);
        switchEnable = findViewById(R.id.switch_enable);
        btnRoutePlan = findViewById(R.id.btn_route_plan);
        btnAppSelect = findViewById(R.id.btn_app_select);
        btnSettings = findViewById(R.id.btn_settings);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        
        // 显示当前开关状态
        switchEnable.setChecked(config.isEnabled());
    }
    
    /**
     * 初始化地图
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            amapManager.configureMapUi(aMap);
            aMap.setOnMapLongClickListener(this);
        }
    }
    
    /**
     * 绑定事件
     */
    private void bindEvents() {
        // 总开关
        switchEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            config.setEnabled(isChecked);
            Toast.makeText(MainActivity.this, 
                isChecked ? "定位模拟已启用" : "定位模拟已关闭", 
                Toast.LENGTH_SHORT).show();
        });
        
        // 路线规划
        btnRoutePlan.setOnClickListener(v -> {
            if (currentLatLng == null) {
                Toast.makeText(MainActivity.this, "请先在地图上选择起点", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, RoutePlanActivity.class);
            intent.putExtra("start_lat", currentLatLng.latitude);
            intent.putExtra("start_lng", currentLatLng.longitude);
            startActivity(intent);
        });
        
        // 应用选择
        btnAppSelect.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AppSelectActivity.class));
        });
        
        // 设置
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        
        // 地图事件回调
        amapManager.setCallback(new AmapManager.AmapCallback() {
            @Override
            public void onLocationSuccess(LatLng latLng, String address) {
                updateLocationInfo(latLng, address);
            }
            
            @Override
            public void onLocationError(int errorCode, String errorMessage) {
                Toast.makeText(MainActivity.this, "定位失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onGeocodeSuccess(LatLng latLng, String address) {
                // 地址搜索成功
            }
            
            @Override
            public void onRegeocodeSuccess(LatLng latLng, String address) {
                updateLocationInfo(latLng, address);
            }
            
            @Override
            public void onRoutePlanSuccess(java.util.List<LatLng> routePoints) {
                // 路线规划成功
            }
            
            @Override
            public void onRoutePlanError(int errorCode, String errorMessage) {
                Toast.makeText(MainActivity.this, "路线规划失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onAddressSearchSuccess(java.util.List<com.amap.api.services.geocoder.GeocodeAddress> addressList) {
                // 地址搜索结果
            }
        });
    }
    
    /**
     * 加载保存的位置
     */
    private void loadSavedLocation() {
        double lat = config.getLatitude();
        double lng = config.getLongitude();
        
        if (lat != 0 && lng != 0) {
            currentLatLng = new LatLng(lat, lng);
            showMarker(currentLatLng);
            
            // 移动地图到该位置
            aMap.animateCamera(com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            
            // 获取地址
            amapManager.regeocodeSearch(currentLatLng);
        }
    }
    
    /**
     * 地图长按选择位置
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        currentLatLng = latLng;
        showMarker(latLng);
        
        // 保存到配置
        config.setLatitude(latLng.latitude);
        config.setLongitude(latLng.longitude);
        
        // 查询地址
        amapManager.regeocodeSearch(latLng);
        
        Toast.makeText(this, "位置已选择", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示标记点
     */
    private void showMarker(LatLng latLng) {
        // 移除旧标记
        if (currentMarker != null) {
            currentMarker.remove();
        }
        
        // 添加新标记
        MarkerOptions options = new MarkerOptions()
            .position(latLng)
            .title("模拟位置")
            .draggable(true);
        currentMarker = aMap.addMarker(options);
        currentMarker.showInfoWindow();
    }
    
    /**
     * 更新位置信息显示
     */
    private void updateLocationInfo(LatLng latLng, String address) {
        String info = String.format("当前模拟位置：\n%.6f, %.6f\n%s", 
            latLng.latitude, latLng.longitude, address);
        tvCurrentLocation.setText(info);
    }
    
    /**
     * 权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            PermissionHelper.handlePermissionResult(requestCode, permissions, grantResults, 
                new PermissionHelper.PermissionCallback() {
                    @Override
                    public void onAllPermissionsGranted() {
                        initMap();
                        Toast.makeText(MainActivity.this, "权限获取成功", Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onPermissionsDenied(String[] deniedPermissions) {
                        Toast.makeText(MainActivity.this, "缺少必要权限，部分功能可能无法使用", 
                            Toast.LENGTH_LONG).show();
                    }
                });
        }
    }
    
    // 生命周期方法
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        amapManager.destroy();
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
