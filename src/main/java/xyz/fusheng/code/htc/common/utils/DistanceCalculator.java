package xyz.fusheng.code.htc.common.utils;

import static java.lang.Math.*;

public class DistanceCalculator {
    public static void main(String[] args) {
        String str1 = "117.3561222,39.1334861";
        String str2 = "117.3561044,39.1334654";
        double lon1 = Double.parseDouble(str1.split(",")[0]);
        double lat1 = Double.parseDouble(str1.split(",")[1]);
        double lon2 = Double.parseDouble(str2.split(",")[0]);
        double lat2 = Double.parseDouble(str2.split(",")[1]);

        // 将经纬度坐标转换为弧度制
        double lat1Rad = toRadians(lat1);
        double lon1Rad = toRadians(lon1);
        double lat2Rad = toRadians(lat2);
        double lon2Rad = toRadians(lon2);

        // 计算大圆距离
        double dlon = lon2Rad - lon1Rad;
        double dlat = lat2Rad - lat1Rad;
        double a = sin(dlat / 2) * sin(dlat / 2)
                + cos(lat1Rad) * cos(lat2Rad)
                * sin(dlon / 2) * sin(dlon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        double R = 6371; // 地球半径，单位为千米
        double distance = R * c;

        System.out.printf("两点之间的距离为：%.6f 米", distance * 1000);
    }
}