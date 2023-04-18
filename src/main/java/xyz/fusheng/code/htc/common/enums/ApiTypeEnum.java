package xyz.fusheng.code.htc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import xyz.fusheng.code.springboot.core.enums.BaseEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc ApiTypeEnum
 * @date 2023-04-09 22:20:08
 */

@Getter
@NoArgsConstructor
@ToString
public enum ApiTypeEnum implements BaseEnum<String> {

    /**
     * 主题订阅枚举项
     */
    CAMERA_HEARTBEAT("CAMERA_HEARTBEAT", "MQTT", "/patrol_robot/up/device/camera/heartbeat"),
    CAMERA_IVS_RESULT("CAMERA_IVS_RESULT", "MQTT", "/patrol_robot/up/device/camera/ivs_result"),

    CAR_HEARTBEAT("CAR_HEARTBEAT", "MQTT", "/patrol_robot/up/device/car/heartbeat"),

    // 发布-回执
    CAR_LOCATION("CAR_LOCATION", "MQTT", "/patrol_robot/down/device/car/location"),
    CAR_LOCATION_REPLY("CAR_LOCATION_REPLY", "MQTT", "/patrol_robot/up/device/car/location/reply"),

    ;

    @EnumValue
    @JsonValue
    private String code;
    private String type;
    private String value;

    ApiTypeEnum(String code, String type, String value) {
        this.code = code;
        this.type = type;
        this.value = value;
    }

    public String getFillValue(String vin) {
        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(getValue());
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (matcher.find()) {
            String key = matcher.group().substring(2, matcher.group().length() - 1);
            matcher.appendReplacement(sb, vin);
            index ++;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(CAR_LOCATION.getFillValue("xxxxx"));
    }

}