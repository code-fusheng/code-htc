package xyz.fusheng.code.htc.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.common.enums.ApiTypeEnum;
import xyz.fusheng.code.htc.common.enums.DeviceTypeEnum;
import xyz.fusheng.code.htc.core.mapper.DeviceMapper;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;
import xyz.fusheng.code.springboot.core.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc DeviceService
 * @date 2023-04-07 15:40:03
 */

@Service(value = "deviceService")
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    public static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    @Resource
    private DeviceMapper deviceMapper;

    @Resource(name = "customThreadPool")
    private ExecutorService customThreadPool;

    public Device saveOrUpdateDevice(DeviceTypeEnum deviceType, ApiTypeEnum apiType, MqttMessage message) throws ParseException {
        JSONObject payloadJson = JSON.parseObject(new String(message.getPayload(), StandardCharsets.UTF_8), JSONObject.class);
        Device device = new Device();
        device.setType(deviceType);
        switch(deviceType) {
            case CAMERA:
                device.setUuid(DeviceTypeEnum.CAMERA.getCode().toLowerCase() + "_" + payloadJson.getString("sn"));
                if (ApiTypeEnum.CAMERA_HEARTBEAT == apiType) {
                    device.setLastestHeartbeat(Timestamp.valueOf(dateFormat.get().format(new Date(payloadJson.getLong("timestamp") * 1000))));
                } else if (ApiTypeEnum.CAMERA_IVS_RESULT == apiType) {
                    JSONObject timeValJson = payloadJson.getJSONObject("payload").getJSONObject("AlarmInfoPlate").getJSONObject("result")
                            .getJSONObject("PlateResult").getJSONObject("timeStamp").getJSONObject("Timeval");
                    Timestamp timestamp = new Timestamp(timeValJson.getLong("sec") * 1000 + timeValJson.getLong("usec") / 1000);
                    device.setLastestHeartbeat(timestamp);
                }
                break;
            case CAR:
                device.setUuid(DeviceTypeEnum.CAR.getCode().toLowerCase() + "_" + payloadJson.getString("vin"));
                device.setLastestHeartbeat(Timestamp.valueOf(dateFormat.get().format(new Date((long) (payloadJson.getDouble("timestamp") * 1000)))));
                JSONObject position = payloadJson.getJSONObject("gps");
                String locationStr = new StringBuilder().append(position.getString("longitude")).append(",")
                        .append(position.getString("latitude")).append(",")
                        .append(position.getString("altitude")).toString();
                device.setLastestLocation(locationStr);
                break;
            default:
                break;
        }
        Device dbDevice = deviceMapper.selectOne(new LambdaQueryWrapper<Device>().eq(Device::getUuid, device.getUuid()));
        if (Objects.nonNull(dbDevice)) {
            dbDevice.setLastestHeartbeat(device.getLastestHeartbeat());
            dbDevice.setLastestLocation(device.getLastestLocation());
            deviceMapper.updateById(dbDevice);
            logger.debug("设备心跳-更新设备信息 => device:{}", dbDevice);
            return dbDevice;
        } else {
            deviceMapper.insert(device);
            logger.info("设备心跳-插入设备信息 => device:{}", device);
            return device;
        }
    }

    public List<Device> listAll() {
        return deviceMapper.selectList(null);
    }

    public PageVo<Device> pageDevice(LimitDto<Device> limitDto) {
        IPage<Device> iPage = deviceMapper.selectPage(limitDto.getPage(), null);
        return new PageVo<>(iPage);
    }
}

