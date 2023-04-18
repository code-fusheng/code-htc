package xyz.fusheng.code.htc.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc BerthCollectRecord
 * @date 2023-04-11 23:09:55
 */

@Data
@TableName(value = "patrol_berth_collect_record")
public class BerthCollectRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String mark;

    private String taskMark;

    private String berthMark;

    private Integer partMark;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal altitude;

    private String detail;

}

