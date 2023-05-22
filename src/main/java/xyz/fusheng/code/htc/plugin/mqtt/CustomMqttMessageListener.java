package xyz.fusheng.code.htc.plugin.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.fusheng.code.htc.common.enums.ApiTypeEnum;
import xyz.fusheng.code.htc.common.enums.DeviceTypeEnum;
import xyz.fusheng.code.htc.core.service.BerthService;
import xyz.fusheng.code.htc.core.service.DeviceService;
import xyz.fusheng.code.htc.core.service.HeartbeatRecordService;
import xyz.fusheng.code.htc.core.service.LprRecordService;
import xyz.fusheng.code.htc.model.entity.Device;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc CustomMqttMessageListener
 * @date 2023-04-07 11:30:16
 */

@Component
public class CustomMqttMessageListener implements IMqttMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomMqttMessageListener.class);

    @Resource
    private DeviceService deviceService;

    @Resource
    private LprRecordService lprRecordService;

    @Resource
    private HeartbeatRecordService heartbeatRecordService;

    @Resource
    private BerthService berthService;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info("MQTT消费者 => Topic:{}, Message:{}", topic, message.getId());
        // 测试验证逻辑
        try {
            Device device = new Device();
            if (ApiTypeEnum.CAMERA_HEARTBEAT.getValue().equals(topic)) {
                device = deviceService.saveOrUpdateDevice(DeviceTypeEnum.CAMERA, ApiTypeEnum.CAMERA_HEARTBEAT , message);
            }
            if (ApiTypeEnum.CAR_HEARTBEAT.getValue().equals(topic)) {
                device = deviceService.saveOrUpdateDevice(DeviceTypeEnum.CAR, ApiTypeEnum.CAR_HEARTBEAT, message);
                // 保存记录
                heartbeatRecordService.saveHeartbeatRecord(message, device);
            }
            if (ApiTypeEnum.CAMERA_IVS_RESULT.getValue().equals(topic)) {
                // 相机抓拍结果
                device = deviceService.saveOrUpdateDevice(DeviceTypeEnum.CAMERA, ApiTypeEnum.CAMERA_IVS_RESULT, message);
                // 保存记录
                lprRecordService.saveLprRecord(message, device);
                // TODO 如果当前处于违停抓拍模式 加入违停抓拍逻辑 - 保存视频流
                lprRecordService.saveParkViolation(message, device);
            }
            if (ApiTypeEnum.CAR_LOCATION_REPLY.getValue().equals(topic)) {
                // 保存采集记录
                berthService.saveBerthCollectRecord(message);
            }
        } catch (Exception e) {
            logger.info("e:{}", e.getMessage(), e);
        }
    }

}

