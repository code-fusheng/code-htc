package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import xyz.fusheng.code.htc.common.enums.DirectionEnum;
import xyz.fusheng.code.springboot.core.entity.BaseEntity;

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc 泊位
 * @date 2023-04-06 11:02:56
 */

@Data
@TableName(value = "patrol_berth")
public class Berth extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String berthNum;

    private String berthNumVirtual;

    private String startLocation;

    private String endLocation;

    private DirectionEnum direction;

    private BigDecimal length;

    private Integer partMark;

    @TableField(exist = false)
    private Long startDist;
    @TableField(exist = false)
    private Long endDist;

}

