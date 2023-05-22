package xyz.fusheng.code.htc.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.common.enums.MatchStatusEnum;
import xyz.fusheng.code.htc.core.mapper.DeviceMapper;
import xyz.fusheng.code.htc.core.mapper.LprRecordMapper;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.htc.model.entity.DeviceCarCameraRef;
import xyz.fusheng.code.htc.model.entity.LprRecord;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc ApiRecordService
 * @date 2023-04-09 23:48:14
 */

@Service
public class LprRecordService extends ServiceImpl<LprRecordMapper, LprRecord> {

    private static final Logger logger = LoggerFactory.getLogger(LprRecordService.class);

    public static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    public static final ThreadLocal<SimpleDateFormat> dateFormat2 = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyyMMddHHmmssSSS"));

    @Resource
    private LprRecordMapper lprRecordMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Value("${image.local-path}")
    private String localPath;

    public void saveLprRecord(MqttMessage message, Device device) throws IOException {
        JSONObject payloadJson = JSON.parseObject(new String(message.getPayload(), StandardCharsets.UTF_8), JSONObject.class);
        LprRecord lprRecord = new LprRecord();
        lprRecord.setDeviceId(device.getId());
        JSONObject timeValJson = payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").getJSONObject("result")
                .getJSONObject("PlateResult").getJSONObject("timeStamp").getJSONObject("Timeval");
        Timestamp timestamp = new Timestamp(timeValJson.getLong("sec") * 1000 + timeValJson.getLong("usec") / 1000);
        lprRecord.setEventTime(timestamp);
        JSONObject plateResult = Optional.of(payloadJson
                .getJSONObject("payload")
                .getJSONObject("AlarmInfoPlate")
                .getJSONObject("result")
                .getJSONObject("PlateResult")).orElse(new JSONObject());
        String licence = plateResult.containsKey("license") ? new String(plateResult.getBytes("license"), StandardCharsets.UTF_8) : null;
        // String licence =  !payloadJson.getJSONObject("payload").containsKey("AlarmInfoPlate") ?
        //         null : !payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").containsKey("result") ?
        //         null : !payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").getJSONObject("result").containsKey("PlateResult") ?
        //         null : !payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").getJSONObject("result").getJSONObject("PlateResult").containsKey("license") ?
        //         null : new String(payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").getJSONObject("result").getJSONObject("PlateResult").getBytes("license"), StandardCharsets.UTF_8);
        lprRecord.setLicencePlate(licence);
        // 处理照片
        if (StringUtils.isNotBlank(plateResult.getString("small_image_content"))) {
            byte[] smallImageContent = Base64.getDecoder().decode(plateResult.getString("small_image_content"));
            String smallFileName = localPath + "/small_" + licence + "_" + dateFormat2.get().format(timestamp.getTime()) + ".jpg";
            Path smallPath = Paths.get(smallFileName);
            try {
                Files.write(smallPath, smallImageContent);
                plateResult.put("small_image_content", smallPath);
                lprRecord.setSmallImage(smallFileName);
            } catch (Exception e) {
                logger.error("本地图片保存异常! => 异常信息:{}", e.getMessage(), e);
            }
        } else {
            plateResult.put("small_image_content", null);
        }
        if (StringUtils.isNotBlank(plateResult.getString("full_image_content"))) {
            byte[] fullImageContent = Base64.getDecoder().decode(plateResult.getString("full_image_content"));
            String fullFileName = localPath + "/full_" + licence + "_" + dateFormat2.get().format(timestamp.getTime()) + ".jpg";
            Path fullPath = Paths.get(fullFileName);
            try {
                Files.write(fullPath, fullImageContent);
                plateResult.put("full_image_content", fullPath);
                lprRecord.setFullImage(fullFileName);
            } catch (Exception e) {
                logger.error("本地图片保存异常! => 异常信息:{}", e.getMessage(), e);
            }
        } else {
            plateResult.put("full_image_content", null);
        }
        lprRecord.setDetail(JSON.toJSONString(plateResult));
        lprRecordMapper.insert(lprRecord);
        logger.info("车牌识别日志-插入车牌识别记录 => apiRecord:{}", lprRecord);
    }

    /**
     * 获取当前最近 size 条 未处理抓拍数据 其中时间范围为 前 10+5s ~ 5s 5s内的数据做缓冲处理
     * @param recordSize
     * @param offsetTime
     * @param delayTime
     * @return
     */
    public List<LprRecord> selectCurrentNeedHandleLprRecords(Integer recordSize, Long offsetTime, Long delayTime) {
        // 最近一秒的数据暂不处理
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).minusSeconds(offsetTime + delayTime);
        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).minusSeconds(delayTime);
        List<LprRecord> lprRecords = lprRecordMapper.selectList(new LambdaQueryWrapper<LprRecord>()
                .select(LprRecord::getId, LprRecord::getDeviceId, LprRecord::getLicencePlate, LprRecord::getEventTime, LprRecord::getMatchStatus)
                .eq(LprRecord::getMatchStatus, MatchStatusEnum.NONE.getCode())
                .between(LprRecord::getEventTime, String.valueOf(startTime), String.valueOf(endTime))
                .orderByDesc(LprRecord::getEventTime).last(" limit " + recordSize));
        if (CollectionUtils.isEmpty(lprRecords)) {
            return Collections.emptyList();
        }
        return lprRecords;
    }

    public DeviceCarCameraRef selectCarCameraRefByCamera(Long cameraId) {
        DeviceCarCameraRef ref = deviceMapper.selectCarCameraRefByCamera(cameraId);
        return ref;
    }

    public void saveParkViolation(MqttMessage message, Device device) {
        // 获取时间节点
        JSONObject payloadJson = JSON.parseObject(new String(message.getPayload(), StandardCharsets.UTF_8), JSONObject.class);
        JSONObject timeValJson = payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").getJSONObject("result")
                .getJSONObject("PlateResult").getJSONObject("timeStamp").getJSONObject("Timeval");
        Timestamp timestamp = new Timestamp(timeValJson.getLong("sec") * 1000 + timeValJson.getLong("usec") / 1000);
        LocalDateTime startTime = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()).minusSeconds(2);
        LocalDateTime endTime = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()).plusSeconds(2);
    }

    public PageVo<LprRecord> pageLpr(LimitDto<LprRecord> limitDto) {
        IPage<LprRecord> iPage = lprRecordMapper.selectPage(limitDto.getPage(), new LambdaQueryWrapper<LprRecord>()
                .orderByDesc(LprRecord::getEventTime));
        return new PageVo<>(iPage);
    }
}

