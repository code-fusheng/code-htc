package xyz.fusheng.code.htc.common.utils;

import static java.lang.Math.*;

public class CartesianToGeodeticConverter {
    private static final double A = 6378137; // WGS84椭球体参数：长半轴
    private static final double B = 6356752.314245; // WGS84椭球体参数：短半轴
    private static final double E = sqrt(1 - (B / A) * (B / A)); // 第一偏心率

    public static void main(String[] args) {
        double x = -2436014.2;
        double y = 4678879.4;
        double z = 3657403.1;

        double dx = 1.75 / 100;
        double dy = 3 / 100;
        double dz = 0;

        double lon = atan2(y, x); // 计算经度
        double lat = atan2(z, sqrt(x * x + y * y)); // 计算纬度
        double alt = sqrt(x * x + y * y + z * z); // 计算高度

        // 将偏移量加到经纬度坐标中
        lon += atan2(dy, dx);
        lat += atan2(dz, sqrt(dx * dx + dy * dy));
        alt += sqrt(dx * dx + dy * dy + dz * dz);

        // 转换为角度制，并将经度限制在[-180,180]的范围内
        lon *= 180 / PI;
        lat *= 180 / PI;
        lon = (lon + 360) % 360 - 180;

        System.out.printf("经度：%.8f, 纬度：%.8f, 高度：%.2f\n", lon, lat, alt);
    }
}