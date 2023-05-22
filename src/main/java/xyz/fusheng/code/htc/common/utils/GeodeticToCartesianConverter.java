package xyz.fusheng.code.htc.common.utils;

import static java.lang.Math.*;

public class GeodeticToCartesianConverter {
    private static final double A = 6378137; // WGS84椭球体参数：长半轴
    private static final double B = 6356752.314245; // WGS84椭球体参数：短半轴
    private static final double E = sqrt(1 - (B / A) * (B / A)); // 第一偏心率

    public static void main(String[] args) {
        double lon = 117.35633670316666;
        double lat = 39.133370948166665;
        double alt = -6.6533999999999995;

        // 将经纬度坐标转换为弧度制，并计算一些常数
        double lonRad = toRadians(lon);
        double latRad = toRadians(lat);
        double N = A / sqrt(1 - E * E * sin(latRad) * sin(latRad));
        double X = (N + alt) * cos(latRad) * cos(lonRad);
        double Y = (N + alt) * cos(latRad) * sin(lonRad);
        double Z = (N * (1 - E * E) + alt) * sin(latRad);

        System.out.printf("X：%.2f, Y：%.2f, Z：%.2f\n", X, Y, Z);
    }
}