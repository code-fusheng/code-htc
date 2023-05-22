package xyz.fusheng.code.htc.core.service;

import java.text.DecimalFormat;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc Test
 * @date 2023-04-20 17:23:55
 */

public class Test {

    /** 地球半径 **/
    private static final double R = 6371e3;
    /** 180° **/
    private static final DecimalFormat df = new DecimalFormat("0.000000");

    /**
     * 根据一点的坐标与距离，以及方向，计算另外一点的位置
     * @param angle 角度，从正北顺时针方向开始计算
     * @param startLong 起始点经度
     * @param startLat 起始点纬度
     * @param distance 距离，单位m
     * @return
     */
    public static String[] calLocationByDistanceAndLocationAndDirection(double angle, double startLong,double startLat, double distance){
        String[] result = new String[2];
        //将距离转换成经度的计算公式
        double δ = distance/R;
        // 转换为radian，否则结果会不正确
        angle = Math.toRadians(angle);
        startLong = Math.toRadians(startLong);
        startLat = Math.toRadians(startLat);
        double lat = Math.asin(Math.sin(startLat)*Math.cos(δ)+Math.cos(startLat)*Math.sin(δ)*Math.cos(angle));
        double lon = startLong + Math.atan2(Math.sin(angle)*Math.sin(δ)*Math.cos(startLat),Math.cos(δ)-Math.sin(startLat)*Math.sin(lat));
        // 转为正常的10进制经纬度
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);
        result[0] = df.format(lon);
        result[1] = df.format(lat);
        return result;
    }

    public static void main(String[] args) {
        String[] result = calLocationByDistanceAndLocationAndDirection(30,118.7403645,31.0287002, 3);
        String[] result2 = calLocationByDistanceAndLocationAndDirection(90,118.7403645,31.0287002, 3);
        String[] result3 = calLocationByDistanceAndLocationAndDirection(120,118.7403645,31.0287002, 3);
        System.out.print(result[0]+",");
        System.out.println(result[1]);
        System.out.print(result2[0]+",");
        System.out.println(result2[1]);
        System.out.print(result3[0]+",");
        System.out.println(result3[1]);
    }


}

