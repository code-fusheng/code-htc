package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.fusheng.code.htc.common.enums.MatchStatusEnum;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc LprRecord
 * @date 2023-04-09 23:31:28
 */

@Data
@TableName(value = "patrol_lpr_record")
public class LprRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deviceId;

    private Timestamp eventTime;

    private String licencePlate;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private Date createdAt;

    private MatchStatusEnum matchStatus;

}

