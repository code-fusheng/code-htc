package xyz.fusheng.code.htc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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

    CAR("CAR", "小车设备"),
    CAMERA("CAMERA", "相机设备"),
    RTK("RTK", "定位设备"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String value;

    DeviceTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

}

