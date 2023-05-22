package xyz.fusheng.code.htc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import xyz.fusheng.code.springboot.core.enums.BaseEnum;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc DeviceTypeEnum
 * @date 2023-04-07 16:08:07
 */

@Getter
@NoArgsConstructor
@ToString
public enum DeviceTypeEnum implements BaseEnum<String> {

    CAR("CAR", "小车"),
    CAMERA("CAMERA", "相机"),
    RTK("RTK", "定位"),
    ;

    @EnumValue
    private String code;
    @JsonValue
    private String value;

    DeviceTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public static DeviceTypeEnum of(String code) {
        return BaseEnum.of(DeviceTypeEnum.class, code);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DeviceTypeEnum ofValue(String value) {
        return BaseEnum.ofValue(DeviceTypeEnum.class, value);
    }


}

