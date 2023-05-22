package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import xyz.fusheng.code.htc.common.enums.DirectionEnum;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc CarCameraRef
 * @date 2023-04-11 09:32:10
 */

@Data
@TableName(value = "patrol_device_car_camera_ref")
public class DeviceCarCameraRef {

    private Long carId;

    private Long cameraId;

    private DirectionEnum direction;

}

