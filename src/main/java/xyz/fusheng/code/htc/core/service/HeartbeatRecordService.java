package xyz.fusheng.code.htc.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.core.mapper.HeartbeatRecordMapper;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.htc.model.entity.HeartbeatRecord;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc HeartbeatService
 * @date 2023-04-10 12:33:30
 */

@Service
public class HeartbeatRecordService extends ServiceImpl<HeartbeatRecordMapper, HeartbeatRecord> {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatRecordService.class);

    public static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    @Resource
    private HeartbeatRecordMapper heartbeatRecordMapper;

    public void saveHeartbeatRecord(MqttMessage message, Device device) {
        JSONObject payloadJson = JSON.parseObject(new String(message.getPayload(), StandardCharsets.UTF_8), JSONObject.class);
        HeartbeatRecord htbRecord = new HeartbeatRecord();
        htbRecord.setDeviceId(device.getId());
        // htbRecord.setDetail(JSON.toJSONString(JSON.parseObject(message.getPayload(), JSONObject.class)));
        htbRecord.setEventTime(Timestamp.valueOf(dateFormat.get().format(new Date((long) (payloadJson.getDouble("timestamp") * 1000)))));
        htbRecord.setLocation(device.getLastestLocation());
        heartbeatRecordMapper.insert(htbRecord);
        logger.debug("位置心跳日志-插入位置心跳记录 => htbRecord:{}", htbRecord);
    }

    public HeartbeatRecord matchHBRecord(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        HeartbeatRecord record = heartbeatRecordMapper.selectOne(new LambdaQueryWrapper<HeartbeatRecord>()
                .eq(HeartbeatRecord::getDeviceId, carId)
                .between(HeartbeatRecord::getEventTime, String.valueOf(startTime), String.valueOf(endTime))
                .orderByDesc(HeartbeatRecord::getEventTime).last(" limit 1"));
        return record;
    }
}

