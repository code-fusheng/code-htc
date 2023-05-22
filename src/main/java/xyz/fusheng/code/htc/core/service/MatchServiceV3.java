package xyz.fusheng.code.htc.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.geotools.geometry.DirectPosition3D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.opengis.referencing.crs.*;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.common.constants.HtcConstants;
import xyz.fusheng.code.htc.common.enums.MatchStatusEnum;
import xyz.fusheng.code.htc.core.mapper.*;
import xyz.fusheng.code.htc.model.entity.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;
import static java.lang.Math.sin;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchServiceV3
 * @date 2023-04-18 10:53:28
 * PS:
 * 做渐进式的数据融合升级? 比如我们先拿到一条识别记录，处理之后发现又来一条相关的识别记录，基于此联合优化数据
 */

@Service
@Slf4j
public class MatchServiceV3 {

    @Resource
    private LprRecordMapper lprRecordMapper;

    @Resource
    private HeartbeatRecordMapper heartbeatRecordMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private BerthMapper berthMapper;

    @Resource
    private MatchResultMapper matchResultMapper;

    @Resource
    private DeviceCarCameraRefMapper deviceCarCameraRefMapper;

    public void lprRecordHandleTask3() {
        // 1. 拿到待处理的车牌识别记录
        List<LprRecord> lprRecordList = lprRecordMapper.selectList(new LambdaQueryWrapper<LprRecord>()
                .select(LprRecord::getId,
                        LprRecord::getDeviceId,
                        LprRecord::getLicencePlate,
                        LprRecord::getEventTime,
                        LprRecord::getMatchStatus)
                .eq(LprRecord::getMatchStatus, MatchStatusEnum.NONE.getCode())
                .orderByDesc(LprRecord::getEventTime).last(" limit 10"));
        // 2. 没有待处理数据就直接返回
        if (CollectionUtils.isEmpty(lprRecordList)) {
            return;
        }
        // 3. 处理数据
        for (LprRecord lprRecord : lprRecordList) {
            log.info("车牌识别情况预览 => 车牌:{}, 时间:{}, 设备ID:{}", lprRecord.getLicencePlate(), lprRecord.getEventTime(), lprRecord.getDeviceId());
            // 无识别车牌 逻辑先不考虑 直接置为匹配失败
            if (HtcConstants.NO_PLATE_LICENCE.equals(lprRecord.getLicencePlate())) {
                lprRecord.setMatchStatus(MatchStatusEnum.FALSE);
                lprRecord.setRemark("未识别车牌");
                lprRecordMapper.updateById(lprRecord);
                continue;
            }
            // 识别到车牌 开始确认相机设备关系
            Device device = deviceMapper.selectById(lprRecord.getDeviceId());
            if (Objects.isNull(device)) {
                lprRecord.setMatchStatus(MatchStatusEnum.FALSE);
                lprRecord.setRemark("设备未注册");
                lprRecordMapper.updateById(lprRecord);
                continue;
            }
            DeviceCarCameraRef carCameraRef = deviceCarCameraRefMapper.selectOne(new LambdaQueryWrapper<DeviceCarCameraRef>()
                    .eq(DeviceCarCameraRef::getCameraId, lprRecord.getDeviceId()));
            if (Objects.isNull(carCameraRef)) {
                lprRecord.setMatchStatus(MatchStatusEnum.FALSE);
                lprRecord.setRemark("设备无关联小车移动平台");
                lprRecordMapper.updateById(lprRecord);
                continue;
            }
            // 通过移动平台设备ID 以及 相机与设备关系 // TODO 如果条件允许 可以带入移动平台速度信息
            Timestamp eventTime = lprRecord.getEventTime();
            Duration offset = Duration.ofMillis(999);
            LocalDateTime startTime = LocalDateTime.ofInstant(eventTime.toInstant(), ZoneId.systemDefault()).minus(offset);
            LocalDateTime endTime = LocalDateTime.ofInstant(eventTime.toInstant(), ZoneId.systemDefault()).plus(offset);
            List<HeartbeatRecord> hbRecords = heartbeatRecordMapper.selectList(new LambdaQueryWrapper<HeartbeatRecord>()
                    .eq(HeartbeatRecord::getDeviceId, carCameraRef.getCarId())
                    .between(HeartbeatRecord::getEventTime, String.valueOf(startTime), String.valueOf(endTime))
                    .orderByAsc(HeartbeatRecord::getEventTime)
                    .last(" limit 2"));
            if (CollectionUtils.isEmpty(hbRecords) || hbRecords.size() < 2) {
                // 未匹配到心跳内容: 此轮逻辑放弃处理该条数据
                log.info("此轮逻辑放弃处理该条数据 - 未匹配到心跳内容或匹配心跳数不够 => size:{}", hbRecords.size());
                lprRecord.setMatchStatus(MatchStatusEnum.FALSE);
                lprRecord.setRemark("匹配失败-心跳记录:" + hbRecords.size());
                lprRecordMapper.updateById(lprRecord);
                continue;
            }
            HeartbeatRecord frontHbRecord = hbRecords.get(0);
            HeartbeatRecord afterHbRecord = hbRecords.get(1);
            log.info("车牌识别关联心跳记录 => front:{} ~ {}, after:{} ~ {}",
                    frontHbRecord.getEventTime(), frontHbRecord.getLocation(),
                    afterHbRecord.getEventTime(), afterHbRecord.getLocation());
            log.info("车牌识别时间 => eventTime:{}", lprRecord.getEventTime());
            // 通过算法计算拍摄点相机经纬度坐标信息
            String locationStr = this.linearInterpolation(frontHbRecord, afterHbRecord, lprRecord);
            // TODO 拍摄点的位置计算应该结合相机的拍摄角度
            // case1: 如果是前后的相机抓拍到车牌，观察点坐标应该分别是 斜边-相机与物体成像距离 3.5  | 30度角邻边 √3/2 * 3.5
            // case2: 观测实际场景下的前后相机抓拍情况 直接根据当前位置匹配泊位判断是否为前后泊位
            lprRecord.setEventLocation(locationStr);

            // TODO 通过算法模拟计算目标车牌位置


            // 开始匹配泊位 这里泊位的匹配关键有几点:
            // 1. 泊位的坐标采用「实际值」还是「观察值」
            // 2. 相机抓拍分为前中后三个位置 如何匹配对应的泊位?
            // 3. 通过经纬度坐标如何计算坐标是否在泊位中
            Berth berth = this.matchBerth(locationStr);
            if (Objects.isNull(berth)) {
                lprRecord.setMatchStatus(MatchStatusEnum.FALSE);
                lprRecord.setRemark("泊位匹配失败: 当前位置无泊位");
                lprRecordMapper.updateById(lprRecord);
                continue;
            }
            lprRecord.setMatchStatus(MatchStatusEnum.TRUE);
            lprRecordMapper.updateById(lprRecord);
            // 4. 生成匹配记录
            MatchResult matchResult = new MatchResult();
            matchResult.setBerthNum(berth.getBerthNum());
            matchResult.setBerthId(berth.getId());
            matchResult.setLicencePlate(lprRecord.getLicencePlate());
            matchResult.setEventTime(lprRecord.getEventTime());
            matchResult.setCarId(carCameraRef.getCarId());
            matchResult.setCameraId(lprRecord.getDeviceId());
            matchResultMapper.insert(matchResult);
        }
    }

    /**
     * 经纬度插值法
     * @return
     */
    public String linearInterpolation(HeartbeatRecord hbRecord1, HeartbeatRecord hbRecord2, LprRecord lprRecord) {
        Timestamp frontTime = hbRecord1.getEventTime();
        Timestamp afterTime = hbRecord2.getEventTime();
        Timestamp eventTime = lprRecord.getEventTime();
        long timeDiffFront = eventTime.getTime() - frontTime.getTime();
        long timeDiffAfter = afterTime.getTime() - eventTime.getTime();
        String[] split1 = hbRecord1.getLocation().split(",");
        BigDecimal frontLng = new BigDecimal(split1[0]).setScale(7, RoundingMode.HALF_UP);
        BigDecimal frontLat = new BigDecimal(split1[1]).setScale(7, RoundingMode.HALF_UP);
        String[] split2 = hbRecord2.getLocation().split(",");
        BigDecimal afterLng = new BigDecimal(split2[0]).setScale(7, RoundingMode.HALF_UP);
        BigDecimal afterLat = new BigDecimal(split2[1]).setScale(7, RoundingMode.HALF_UP);
        BigDecimal lat = frontLat.add(afterLat.subtract(frontLat).multiply(new BigDecimal(timeDiffFront)).divide(new BigDecimal(timeDiffFront + timeDiffAfter), 8, RoundingMode.HALF_UP));
        BigDecimal lng = frontLng.add(afterLng.subtract(frontLng).multiply(new BigDecimal(timeDiffFront)).divide(new BigDecimal(timeDiffFront + timeDiffAfter), 8, RoundingMode.HALF_UP));
        String location = lng + "," + lat;
        log.info("经纬度插值法求解中间点坐标 => location:{}", location);
        return location;
    }

    public Berth matchBerth(String locationStr) {
        Berth finalBerth = null;
        String[] split = locationStr.split(",");
        BigDecimal longitude = new BigDecimal(split[0]).setScale(7, RoundingMode.HALF_UP);
        BigDecimal latitude = new BigDecimal(split[1]).setScale(8, RoundingMode.HALF_UP);
        log.info("泊位匹配 => longitude:{}, latitude:{}", longitude, latitude);
        String method = "method1";
        List<Berth> berths = new ArrayList<>();
        switch (method) {
            // method1: 最近点匹配策略
            case "method1":
                berths = berthMapper.matchBerths(longitude, latitude, 10);
                break;
            // method2: mysql 空间矢量计算 SRID 与 函数扩展  ST_Buffer + ST_Intersects
            case "method2":
                berths = berthMapper.matchBerthM2(longitude, latitude, 10);
                break;
            // 全查询代码逻辑匹配
            case "method3":
                berths = berthMapper.selectList(null);
                break;
            default:
        }
        finalBerth = berths.get(0);
        return finalBerth;
    }


    public static void calculateTargetLocation(String eventLocation) {
        // 0. 定义相机或者小车在地球上的坐标系位置 118.7403645,31.0287002,41.0
        String[] split = eventLocation.split(",");
        double cameraLon = 118.7403645;
        double cameraLat = 31.0287002;
        double cameraAlt = 41.0;
        log.info("相机和小车在WGS84坐标系中的坐标 => cameraLon:{}, cameraLat:{}, cameraAlt:{}", cameraLon, cameraLat, cameraAlt);

        // 1. 相机的朝向角度 这里以平台的行驶方向的垂直方向为 X 轴，相机的角度为 60、0、-60（这里的负号表示逆时针旋转）俯角为 12度（相机是斜向下拍摄的）
        // 相机与抓拍物体的距离是 3.5 米
        double horizontalAngle = Math.toRadians(60);
        double verticalAngle = Math.toRadians(12);
        double distance = 3.5;

        // 2. 以平台建立相机坐标系（笛卡尔坐标系）
        double sx = 0; double sy = 0; double sz = 0;
        Vector3D carPosition = new Vector3D(sx, sy, sz);

        // 3. 计算目标点在相机坐标系的坐标位置 方向向量
        // 这里根据三角函数等信息计算
        double tx = 0; double ty = 0; double tz = 0;

        double x = distance * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
        double y = distance * Math.cos(horizontalAngle) * Math.sin(verticalAngle);
        double z = distance * Math.sin(verticalAngle);
        // 方向向量
        Vector3D cameraPosition = new Vector3D(x, y, z);
        Vector3D cameraDirection = cameraPosition.normalize();

        double t = carPosition.getZ() / cameraDirection.getZ();
        System.out.println(t);

        Vector3D targetPosition = cameraPosition.add(cameraDirection.scalarMultiply(t));
        log.info("目标在笛卡尔坐标系中的大致坐标 => x:{}, y:{}, z:{}", targetPosition.getX(), targetPosition.getY(), targetPosition.getZ());

        // 4. 计算目标点在WGS84坐标系（经纬度）
        // 将笛卡尔坐标系中的点转换为以半径R为单位的WGS84坐标系中的坐标值
        double R = 6378137.0;
        double r = targetPosition.getNorm();
        double theta = Math.atan2(targetPosition.getY(), targetPosition.getX());
        double phi = Math.asin(targetPosition.getZ() / r);
        // 计算WGS84坐标系中的 经纬以及高度
        double lon = Math.toDegrees(theta);
        double lat = Math.toDegrees(phi);
        double alt = r - R;
        log.info("目标在WGS84坐标系中的大致坐标 => lon:{}, lat:{}, alt:{}", lon, lat, alt);
    }

    public static void calculateTargetLocationV2(String eventLocation) {
        // 0. 定义相机或者小车在地球上的坐标系位置
        String[] split = eventLocation.split(",");
        double cameraLon = Double.parseDouble(split[0]);
        double cameraLat = Double.parseDouble(split[1]);
        double cameraAlt = Double.parseDouble(split[2]);

        // 1. 相机的朝向角度
        double horizontalAngle = Math.toRadians(60);
        double verticalAngle = Math.toRadians(12);
        double distance = 3.5;

        // 2. 建立相机坐标系（笛卡尔坐标系）
        Vector3D carPosition = new Vector3D(cameraLon, cameraLat, cameraAlt);
        double sx = carPosition.getX();
        double sy = carPosition.getY();
        double sz = carPosition.getZ();
        double x = distance * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
        double y = distance * Math.cos(horizontalAngle) * Math.sin(verticalAngle);
        double z = distance * Math.sin(verticalAngle);
        Vector3D cameraDirection = new Vector3D(x, y, z);

        // 3. 计算目标点在笛卡尔坐标系的坐标位置
        Vector3D targetPosition = carPosition.add(cameraDirection);

        // 4. 计算目标点在WGS84坐标系（经纬度）
        double R = 6378137.0;
        double r = targetPosition.getNorm();
        double theta = Math.atan2(targetPosition.getY(), targetPosition.getX());
        double phi = Math.asin(targetPosition.getZ() / r);
        double lon = Math.toDegrees(theta);
        double lat = Math.toDegrees(phi);
        double alt = r - R;
        log.info("目标在WGS84坐标系中的大致坐标 => lon:{}, lat:{}, alt:{}", cameraLon + lon, cameraLat + lat, cameraAlt + alt);
    }

    private static final double A = 6378137; // WGS84椭球体参数：长半轴
    private static final double B = 6356752.314245; // WGS84椭球体参数：短半轴
    private static final double E = sqrt(1 - (B / A) * (B / A)); // 第一偏心率
    public static void calculateTargetLocationV3(String eventLocation) {

        // 0. 定义相机或者小车在地球上的坐标系位置
        String[] split = eventLocation.split(",");
        double lon = Double.parseDouble(split[0]);
        double lat = Double.parseDouble(split[1]);
        double alt = Double.parseDouble(split[2]);

        // 将经纬度坐标转换为弧度制，并计算一些常数
        double lonRad = toRadians(lon);
        double latRad = toRadians(lat);
        double N = A / sqrt(1 - E * E * sin(latRad) * sin(latRad));
        double X = (N + alt) * cos(latRad) * cos(lonRad);
        double Y = (N + alt) * cos(latRad) * sin(lonRad);
        double Z = (N * (1 - E * E) + alt) * sin(latRad);

        System.out.printf("X：%.2f, Y：%.2f, Z：%.2f\n", X, Y, Z);

        double dx = 1.75 / 100;
        double dy = 3 / 100;
        double dz = 0;

        double targetLon = atan2(Y, X); // 计算经度
        double targtLat = atan2(Z, sqrt(X * X + Y * Y)); // 计算纬度
        double targetAlt = sqrt(X * X + Y * Y + Z * Z); // 计算高度

        targetLon += atan2(dy, dx);
        targtLat += atan2(dz, sqrt(dx * dx + dy * dy));
        targetAlt += sqrt(dx * dx + dy * dy + dz * dz);

        // 转换为角度制，并将经度限制在[-180,180]的范围内
        targetLon *= 180 / PI;
        targtLat *= 180 / PI;
        targetLon = (targetLon + 360) % 360 - 180;

        log.info("目标在WGS84坐标系中的大致坐标 => lon:{}, lat:{}, alt:{}", targetLon, targtLat, targetAlt);

    }

    // 卡尔曼滤波
    public void krmFilter() {

    }

    public static void main(String[] args) {
        // calculateTargetLocation("");
        calculateTargetLocationV3("117.35633670316666,39.133370948166665,-6.6533999999999995");
        // String str = "117.35633670316666,39.133370948166665,-6.6533999999999995";
        // String[] split1 = str.split(",");
        // BigDecimal frontLng = new BigDecimal(split1[0]).setScale(7, RoundingMode.HALF_UP);
        // BigDecimal frontLat = new BigDecimal(split1[1]).setScale(7, RoundingMode.HALF_UP);
        // System.out.println(frontLng);
        // System.out.println(frontLat);
    }

}

