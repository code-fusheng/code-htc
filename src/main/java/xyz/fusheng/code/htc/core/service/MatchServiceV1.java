package xyz.fusheng.code.htc.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.common.enums.MatchStatusEnum;
import xyz.fusheng.code.htc.model.entity.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchService
 * @date 2023-04-17 09:20:33
 */

@Service
public class MatchServiceV1 {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceV1.class);

    public static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    private static final Long OFFSET_TIME = 15L;    // 时间偏移值
    private static final Long DELAY_TIME = 5L;  // 延迟时间
    private static final Integer RECORD_SIZE = 30;  // 实际消费速度需要结合 活动设备数&相机数

    @Resource
    private LprRecordService lprRecordService;

    @Resource
    private HeartbeatRecordService heartbeatRecordService;

    @Resource
    private BerthService berthService;

    @Resource
    private MatchResultService matchResultService;

    public void lprRecordHandleTask1() {
        List<LprRecord> lprRecords = lprRecordService.selectCurrentNeedHandleLprRecords(RECORD_SIZE, OFFSET_TIME, DELAY_TIME);
        logger.info("当前待处理匹配记录 => size:{}", lprRecords.size());
        lprRecords.forEach(item -> {
            logger.debug("item => id:{}", item.getId());
            // TODO 匹配逻辑
            // 1. 获取小车与相机关系
            DeviceCarCameraRef ref = lprRecordService.selectCarCameraRefByCamera(item.getDeviceId());
            if (Objects.nonNull(ref)) {
                logger.debug("小车与相机关系预览 => car:{}, cameraId:{}, direction:{}", ref.getCarId(), ref.getCameraId(), ref.getDirection().getValue());
                // 2. 心跳匹配
                HeartbeatRecord hbRecord = doMatchHBRecord(item, ref);
                if (Objects.nonNull(hbRecord)) {
                    Berth berth = doMatchBerth(hbRecord);
                    MatchResult matchResult = buildMatchResult(item, hbRecord, berth);
                    item.setMatchStatus(MatchStatusEnum.TRUE);
                } else {
                    item.setMatchStatus(MatchStatusEnum.FALSE);
                }
            } else {
                logger.info("当前相机设备未绑定小车");
            }

        });
        lprRecordService.updateBatchById(lprRecords);
    }

    public MatchResult buildMatchResult(LprRecord lprRecord, HeartbeatRecord hbRecord, Berth berth) {
        MatchResult matchResult = new MatchResult();
        matchResult.setBerthNum(berth.getBerthNum());
        matchResult.setLicencePlate(lprRecord.getLicencePlate());
        matchResult.setEventTime(lprRecord.getEventTime());
        matchResult.setCarId(hbRecord.getDeviceId());
        matchResult.setCameraId(lprRecord.getDeviceId());
        matchResult.setBerthId(berth.getId());
        matchResultService.save(matchResult);
        return matchResult;
    }

    /**
     * 车牌识别记录匹配心跳
     * @param record
     * @param ref
     * @return
     */
    public HeartbeatRecord doMatchHBRecord(LprRecord record, DeviceCarCameraRef ref) {
        // 2. 检索定位心跳中的小车位置信息
        Timestamp eventTime = record.getEventTime();
        LocalDateTime startTime = LocalDateTime.ofInstant(eventTime.toInstant(), ZoneId.systemDefault());
        LocalDateTime endTime = LocalDateTime.ofInstant(eventTime.toInstant(), ZoneId.systemDefault()).plusSeconds(1);
        switch (ref.getDirection()) {
            case E:
            case W:
                // 相机抓拍在心跳位置信息中间 - 无需修正时间
                break;
            case ES:
            case SW:
                // 后方相机抓拍 心跳位置 => 小的时间
                startTime = LocalDateTime.ofInstant(eventTime.toInstant(), ZoneId.systemDefault()).minusSeconds(1);
                break;
            case NE:
            case WN:
                // 前方的相机抓拍 心跳位置 => 大的时间
                endTime = LocalDateTime.ofInstant(eventTime.toInstant(), ZoneId.systemDefault()).plusSeconds(1);
                break;
            default:
        }
        HeartbeatRecord hbRecord = heartbeatRecordService.matchHBRecord(ref.getCarId(), startTime, endTime);
        logger.info("心跳匹配结果 => {}", hbRecord);
        return hbRecord;
    }

    /**
     * 心跳位置信息匹配泊位 - 就近匹配
     * @param hbRecord
     * @return
     */
    public Berth doMatchBerth(HeartbeatRecord hbRecord) {
        String[] split = hbRecord.getLocation().split(",");
        BigDecimal longitude = new BigDecimal(split[0]).setScale(7, RoundingMode.UNNECESSARY);
        BigDecimal latitude = new BigDecimal(split[1]).setScale(7, RoundingMode.UNNECESSARY);
        Berth berth = berthService.matchBerth(longitude, latitude);
        logger.info("泊位匹配结果 => {}", berth);
        return berth;
    }

    public String calculateLprLocation(LprRecord lprRecord, List<HeartbeatRecord> records) {
        List<String> locations = records.stream().map(HeartbeatRecord::getLocation).collect(Collectors.toList());
        // 通过定位信息匹配泊位
        List<Date> times = records.stream().map(HeartbeatRecord::getEventTime).collect(Collectors.toList());
        // 时间从早到晚排序
        Collections.sort(times);
        logger.info("车牌识别记录&位置心跳-粗略匹配结果 => 车牌:{} - 地点:{} - 抓拍时间:{} - 心跳时间:{}", lprRecord.getLicencePlate(), locations, lprRecord.getEventTime(), times);
        Date headHBDate = times.get(0);
        Date tailHBDate = times.get(-1);
        return "";
    }

}

