package com.locationhook.module.amap;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.locationhook.module.config.HookConfig;
import com.locationhook.module.utils.LocationConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 高德地图管理类
 * 负责地图显示、路线规划、地理编码等功能
 */
public class AmapManager {
    
    private static final String TAG = "AmapManager";
    private static AmapManager sInstance;
    
    private Context context;
    private HookConfig config;
    
    // 定位相关
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;
    
    // 搜索相关
    private GeocodeSearch geocodeSearch;
    private RouteSearch routeSearch;
    
    // 回调接口
    private AmapCallback callback;
    
    public interface AmapCallback {
        void onLocationSuccess(LatLng latLng, String address);
        void onLocationError(int errorCode, String errorMessage);
        void onGeocodeSuccess(LatLng latLng, String address);
        void onRegeocodeSuccess(LatLng latLng, String address);
        void onRoutePlanSuccess(List<LatLng> routePoints);
        void onRoutePlanError(int errorCode, String errorMessage);
        void onAddressSearchSuccess(List<GeocodeAddress> addressList);
    }
    
    private AmapManager(Context context) {
        this.context = context.getApplicationContext();
        this.config = new HookConfig(context);
        initAmap();
    }
    
    public static synchronized AmapManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AmapManager(context);
        }
        return sInstance;
    }
    
    /**
     * 初始化高德地图服务
     */
    private void initAmap() {
        try {
            // 初始化定位
            AMapLocationClient.updatePrivacyShow(context, true, true);
            AMapLocationClient.updatePrivacyAgree(context, true);
            
            locationClient = new AMapLocationClient(context);
            locationOption = new AMapLocationClientOption();
            
            // 配置定位参数
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOption.setOnceLocation(true);
            locationOption.setNeedAddress(true);
            locationOption.setHttpTimeOut(30000);
            
            locationClient.setLocationOption(locationOption);
            
            // 初始化地理编码搜索
            geocodeSearch = new GeocodeSearch(context);
            geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                @Override
                public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                    if (rCode == AMapException.CODE_AMAP_SUCCESS && result != null) {
                        RegeocodeAddress address = result.getRegeocodeAddress();
                        if (address != null && callback != null) {
                            LatLng latLng = new LatLng(
                                result.getRegeocodeQuery().getPoint().getLatitude(),
                                result.getRegeocodeQuery().getPoint().getLongitude()
                            );
                            callback.onRegeocodeSuccess(latLng, address.getFormatAddress());
                        }
                    } else {
                        if (callback != null) {
                            callback.onRoutePlanError(rCode, "逆地理编码失败: " + rCode);
                        }
                    }
                }
                
                @Override
                public void onGeocodeSearched(GeocodeResult result, int rCode) {
                    if (rCode == AMapException.CODE_AMAP_SUCCESS && result != null) {
                        List<GeocodeAddress> addressList = result.getGeocodeAddressList();
                        if (addressList != null && !addressList.isEmpty() && callback != null) {
                            GeocodeAddress first = addressList.get(0);
                            LatLng latLng = new LatLng(
                                first.getLatLonPoint().getLatitude(),
                                first.getLatLonPoint().getLongitude()
                            );
                            callback.onGeocodeSuccess(latLng, first.getFormatAddress());
                            callback.onAddressSearchSuccess(addressList);
                        }
                    } else {
                        if (callback != null) {
                            callback.onRoutePlanError(rCode, "地理编码失败: " + rCode);
                        }
                    }
                }
            });
            
            // 初始化路线搜索
            routeSearch = new RouteSearch(context);
            routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
                @Override
                public void onBusRouteSearched(BusRouteResult result, int rCode) {
                    // 公交路线，暂不实现
                }
                
                @Override
                public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
                    if (rCode == AMapException.CODE_AMAP_SUCCESS && result != null) {
                        List<DrivePath> pathList = result.getPaths();
                        if (pathList != null && !pathList.isEmpty() && callback != null) {
                            DrivePath firstPath = pathList.get(0);
                            List<LatLng> routePoints = new ArrayList<>();
                            
                            // 解析路线点
                            // 这里需要解析坐标串
                            if (callback != null) {
                                callback.onRoutePlanSuccess(routePoints);
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onRoutePlanError(rCode, "驾车路线规划失败: " + rCode);
                        }
                    }
                }
                
                @Override
                public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
                    // 步行路线
                }
                
                @Override
                public void onRideRouteSearched(RideRouteResult result, int rCode) {
                    // 骑行路线
                }
            });
            
            Log.d(TAG, "高德地图服务初始化成功");
            
        } catch (Exception e) {
            Log.e(TAG, "高德地图初始化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 配置地图UI设置
     */
    public void configureMapUi(AMap aMap) {
        if (aMap == null) return;
        
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);       // 显示缩放按钮
        uiSettings.setCompassEnabled(true);            // 显示指南针
        uiSettings.setScaleControlsEnabled(true);      // 显示比例尺
        uiSettings.setMyLocationButtonEnabled(true);   // 显示定位按钮
        uiSettings.setScrollGesturesEnabled(true);     // 允许滑动
        uiSettings.setZoomGesturesEnabled(true);       // 允许缩放
        uiSettings.setRotateGesturesEnabled(true);     // 允许旋转
        uiSettings.setTiltGesturesEnabled(true);       // 允许倾斜
        
        // 开启定位蓝点
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomTo(16));
    }
    
    /**
     * 开始定位当前位置
     */
    public void startLocation() {
        if (locationClient == null) return;
        
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    // 定位成功
                    LatLng latLng = new LatLng(
                        aMapLocation.getLatitude(),
                        aMapLocation.getLongitude()
                    );
                    String address = aMapLocation.getAddress();
                    
                    if (callback != null) {
                        callback.onLocationSuccess(latLng, address);
                    }
                    
                    Log.d(TAG, "定位成功: " + latLng + ", " + address);
                    
                } else {
                    // 定位失败
                    int errorCode = aMapLocation != null ? aMapLocation.getErrorCode() : -1;
                    String errorMsg = aMapLocation != null ? aMapLocation.getErrorInfo() : "未知错误";
                    
                    if (callback != null) {
                        callback.onLocationError(errorCode, errorMsg);
                    }
                    
                    Log.e(TAG, "定位失败: " + errorCode + ", " + errorMsg);
                }
            }
        });
        
        locationClient.startLocation();
    }
    
    /**
     * 地理编码（地址转坐标）
     */
    public void geocodeSearch(String address, String city) {
        GeocodeQuery query = new GeocodeQuery(address, city);
        geocodeSearch.getFromLocationNameAsyn(query);
    }
    
    /**
     * 逆地理编码（坐标转地址）
     */
    public void regeocodeSearch(LatLng latLng) {
        com.amap.api.services.core.LatLonPoint point = 
            new com.amap.api.services.core.LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 1000, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
    }
    
    /**
     * 驾车路线规划
     */
    public void planDriveRoute(LatLng start, LatLng end) {
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
            new com.amap.api.services.core.LatLonPoint(start.latitude, start.longitude),
            new com.amap.api.services.core.LatLonPoint(end.latitude, end.longitude)
        );
        
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(
            fromAndTo,
            RouteSearch.DRIVING_SINGLE_DEFAULT,
            null,
            null,
            ""
        );
        
        routeSearch.calculateDriveRouteAsyn(query);
    }
    
    /**
     * 在地图上添加标记
     */
    public Marker addMarker(AMap aMap, LatLng latLng, String title, String snippet) {
        if (aMap == null || latLng == null) return null;
        
        MarkerOptions options = new MarkerOptions()
            .position(latLng)
            .title(title)
            .snippet(snippet)
            .draggable(true);
            
        return aMap.addMarker(options);
    }
    
    /**
     * 在地图上绘制路线
     */
    public Polyline drawRoute(AMap aMap, List<LatLng> points, int color, float width) {
        if (aMap == null || points == null || points.isEmpty()) return null;
        
        PolylineOptions options = new PolylineOptions()
            .addAll(points)
            .color(color)
            .width(width)
            .useGradient(true);
            
        return aMap.addPolyline(options);
    }
    
    /**
     * 设置回调
     */
    public void setCallback(AmapCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 销毁资源
     */
    public void destroy() {
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }
        
        callback = null;
    }
}
