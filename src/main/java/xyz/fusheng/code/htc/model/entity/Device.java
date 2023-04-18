package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import xyz.fusheng.code.htc.common.enums.DeviceTypeEnum;
import xyz.fusheng.code.springboot.core.entity.BaseEntity;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc 设备
 * @date 2023-04-06 11:02:40
 */

@Data
@TableName(value = "patrol_device")
public class Device extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String uuid;

    private String name;

    private DeviceTypeEnum type;

    private String brand;

    private Timestamp lastestHeartbeat;

    private String lastestLocation;

}

