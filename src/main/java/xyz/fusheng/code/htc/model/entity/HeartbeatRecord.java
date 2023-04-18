package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc LocationRecord
 * @date 2023-04-10 12:29:35
 */

@Data
@TableName(value = "patrol_heartbeat_record")
public class HeartbeatRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deviceId;

    private Timestamp eventTime;

    private String location;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private Date createdAt;

}

