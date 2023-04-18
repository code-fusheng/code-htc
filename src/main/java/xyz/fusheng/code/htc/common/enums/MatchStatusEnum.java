package xyz.fusheng.code.htc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import xyz.fusheng.code.springboot.core.enums.BaseEnum;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchStatusEnum
 * @date 2023-04-10 23:11:02
 */

@Getter
@AllArgsConstructor
@ToString
public enum MatchStatusEnum implements BaseEnum<Integer> {

    TRUE(1, "true"),
    NONE(0, "none"),
    FALSE(2, "FALSE")

    ;

    @EnumValue
    @JsonValue
    private Integer code;
    private String value;

}