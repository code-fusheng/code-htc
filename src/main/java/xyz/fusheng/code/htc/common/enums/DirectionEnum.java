package xyz.fusheng.code.htc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import xyz.fusheng.code.springboot.core.enums.BaseEnum;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc BerthDirectionEnum
 * @date 2023-04-11 18:24:34
 */

@Getter
@AllArgsConstructor
@ToString
public enum DirectionEnum implements BaseEnum<String> {

    N("N", "北"),
    NE("NE", "东北"),
    E("E", "东"),
    ES("ES", "东南"),
    S("S", "南"),
    SW("SW", "西南"),
    W("W", "西"),
    WN("WN", "西北"),
    ;

    @EnumValue
    private String code;
    @JsonValue
    private String value;

    public static DirectionEnum of(String code) {
        return BaseEnum.of(DirectionEnum.class, code);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DirectionEnum ofValue(String value) {
        return BaseEnum.ofValue(DirectionEnum.class, value);
    }

}

