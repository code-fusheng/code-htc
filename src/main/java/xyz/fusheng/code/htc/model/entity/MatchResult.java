package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.fusheng.code.springboot.core.entity.BaseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchResult
 * @date 2023-04-13 13:39:52
 */

@Data
@TableName(value = "patrol_match_result")
public class MatchResult {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long carId;

    private Long cameraId;

    private Long berthId;

    private String licencePlate;

    private String berthNum;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal altitude;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Timestamp eventTime;

    private Integer matchLevel;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private Date createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("修改时间")
    private Date updatedAt;


}

