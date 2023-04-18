package xyz.fusheng.code.htc.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.common.enums.MatchStatusEnum;
import xyz.fusheng.code.htc.core.mapper.DeviceMapper;
import xyz.fusheng.code.htc.core.mapper.LprRecordMapper;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.htc.model.entity.DeviceCarCameraRef;
import xyz.fusheng.code.htc.model.entity.LprRecord;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @Resource
    private LprRecordMapper lprRecordMapper;

    @Resource
    private DeviceMapper deviceMapper;

    public void saveLprRecord(MqttMessage message, Device device) {
        JSONObject payloadJson = JSON.parseObject(new String(message.getPayload(), StandardCharsets.UTF_8), JSONObject.class);
        LprRecord lprRecord = new LprRecord();
        lprRecord.setDeviceId(device.getId());
        lprRecord.setDetail(JSON.toJSONString(JSON.parseObject(message.getPayload(), JSONObject.class)));
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
        String small_image_content = plateResult.getString("small_image_content");
        String full_image_content = plateResult.getString("full_image_content");
        plateResult.put("small_image_content", "");
        plateResult.put("full_image_content", "");
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
}

