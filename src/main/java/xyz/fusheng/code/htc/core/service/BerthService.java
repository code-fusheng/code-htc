package xyz.fusheng.code.htc.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.common.enums.ApiTypeEnum;
import xyz.fusheng.code.htc.core.mapper.BerthCollectRecordMapper;
import xyz.fusheng.code.htc.core.mapper.BerthMapper;
import xyz.fusheng.code.htc.model.entity.Berth;
import xyz.fusheng.code.htc.model.entity.BerthCollectRecord;
import xyz.fusheng.code.htc.plugin.mqtt.MqttUtil;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc BerthService
 * @date 2023-04-11 23:14:38
 */

@Service
public class BerthService extends ServiceImpl<BerthMapper, Berth> {

    private static final Logger logger = LoggerFactory.getLogger(BerthService.class);

    @Resource
    private BerthMapper berthMapper;

    @Resource
    private BerthCollectRecordMapper berthCollectRecordMapper;

    @Resource
    private MqttUtil mqttUtil;

    public void doBerthCollect(String uuid, String key) throws MqttException {
        logger.info("泊位采集逻辑-开始");
        // 1. 泊位采集-发布订阅
        JSONObject messageJson = new JSONObject();
        messageJson.put("key", key);
        String vin = uuid.split(",")[-1];
        mqttUtil.publish(ApiTypeEnum.CAR_LOCATION.getValue(), JSON.toJSONString(messageJson), 1);
    }

    public BerthCollectRecord saveBerthCollectRecord(MqttMessage message) {
        JSONObject payloadJson = JSON.parseObject(new String(message.getPayload(), StandardCharsets.UTF_8), JSONObject.class);
        String mark = payloadJson.getString("key");
        JSONObject positionJson = payloadJson.getJSONObject("gps");
        BigDecimal latitude = positionJson.getBigDecimal("latitude").setScale(7, RoundingMode.HALF_UP);
        BigDecimal longitude = positionJson.getBigDecimal("longitude").setScale(7, RoundingMode.HALF_UP);
        BigDecimal altitude = positionJson.getBigDecimal("altitude").setScale(7, RoundingMode.HALF_UP);
        List<String> marks = Arrays.stream(mark.split("-")).collect(Collectors.toList());
        BerthCollectRecord record = berthCollectRecordMapper.selectOne(new LambdaQueryWrapper<BerthCollectRecord>()
                .eq(BerthCollectRecord::getMark, mark));
        if (Objects.nonNull(record)) {
            record.setTaskMark(marks.get(0));
            record.setBerthMark(marks.get(1));
            record.setPartMark(Integer.valueOf(marks.get(2)));
            record.setLatitude(latitude);
            record.setLongitude(longitude);
            record.setAltitude(altitude);
            record.setDetail(JSON.toJSONString(payloadJson));
            berthCollectRecordMapper.updateById(record);
            logger.info("泊位采集更新记录 => record:{}", record);
        } else {
            record = new BerthCollectRecord();
            record.setMark(mark);
            record.setTaskMark(marks.get(0));
            record.setBerthMark(marks.get(1));
            record.setPartMark(Integer.valueOf(marks.get(2)));
            record.setLatitude(latitude);
            record.setLongitude(longitude);
            record.setAltitude(altitude);
            record.setDetail(JSON.toJSONString(payloadJson));
            berthCollectRecordMapper.insert(record);
            logger.info("泊位采集新增记录 => record:{}", record);
        }
        return record;
    }

    public List<BerthCollectRecord> listBerthCollectRecords(String taskId) {
        List<BerthCollectRecord> records = berthCollectRecordMapper.selectList(new LambdaQueryWrapper<BerthCollectRecord>()
                .eq(BerthCollectRecord::getTaskMark, taskId));
        return records;
    }

    public List<Berth> doBuildBerths(String taskId) {
        List<Berth> berths = berthMapper.selectBerthsForRecord(taskId);
        AtomicInteger num = new AtomicInteger(1);
        berths.forEach(berth -> {
            berth.setId(IdWorker.getId());
            berth.setBerthNumVirtual("V-" + (num.getAndAdd(1) + 100000));
            berthMapper.saveBerth(berth);
        });
        return berths;
    }

    public Berth matchBerth(BigDecimal longitude, BigDecimal latitude) {
        Berth berth = berthMapper.matchBerth(longitude, latitude);
        return berth;
    }
}

