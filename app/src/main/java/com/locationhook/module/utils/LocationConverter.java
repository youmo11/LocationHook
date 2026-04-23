package com.locationhook.module.utils;

/**
 * 坐标系转换工具
 * 支持 WGS84 / GCJ-02 (火星坐标) / BD-09 (百度坐标) 互转
 */
public class LocationConverter {
    
    private static final double PI = Math.PI;
    private static final double X_PI = PI * 3000.0 / 180.0;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;
    
    /**
     * WGS84 转 GCJ-02 (火星坐标)
     */
    public static double[] wgs84ToGcj02(double wgLat, double wgLng) {
        if (outOfChina(wgLat, wgLng)) {
            return new double[]{wgLat, wgLng};
        }
        
        double dLat = transformLat(wgLng - 105.0, wgLat - 35.0);
        double dLng = transformLng(wgLng - 105.0, wgLat - 35.0);
        
        double radLat = wgLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        
        return new double[]{wgLat + dLat, wgLng + dLng};
    }
    
    /**
     * GCJ-02 转 WGS84
     */
    public static double[] gcj02ToWgs84(double gcjLat, double gcjLng) {
        double[] d = delta(gcjLat, gcjLng);
        return new double[]{gcjLat - d[0], gcjLng - d[1]};
    }
    
    /**
     * GCJ-02 转 BD-09 (百度坐标)
     */
    public static double[] gcj02ToBd09(double gcjLat, double gcjLng) {
        double x = gcjLng;
        double y = gcjLat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new double[]{bdLat, bdLng};
    }
    
    /**
     * BD-09 转 GCJ-02
     */
    public static double[] bd09ToGcj02(double bdLat, double bdLng) {
        double x = bdLng - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        return new double[]{gcjLat, gcjLng};
    }
    
    /**
     * WGS84 转 BD-09
     */
    public static double[] wgs84ToBd09(double wgsLat, double wgsLng) {
        double[] gcj = wgs84ToGcj02(wgsLat, wgsLng);
        return gcj02ToBd09(gcj[0], gcj[1]);
    }
    
    /**
     * BD-09 转 WGS84
     */
    public static double[] bd09ToWgs84(double bdLat, double bdLng) {
        double[] gcj = bd09ToGcj02(bdLat, bdLng);
        return gcj02ToWgs84(gcj[0], gcj[1]);
    }
    
    private static double[] delta(double lat, double lng) {
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        
        return new double[]{dLat, dLng};
    }
    
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    
    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    
    private static boolean outOfChina(double lat, double lng) {
        if (lng < 72.004 || lng > 137.8347) {
            return true;
        }
        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }
}
