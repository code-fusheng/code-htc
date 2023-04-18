package xyz.fusheng.code.htc.model.entity;

import lombok.Data;
import xyz.fusheng.code.htc.common.enums.DirectionEnum;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc CarCameraRef
 * @date 2023-04-11 09:32:10
 */

@Data
public class DeviceCarCameraRef {

    private Long carId;

    private Long cameraId;

    private DirectionEnum direction;

}

