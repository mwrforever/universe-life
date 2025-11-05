package com.universe.life.model.enums;

/**
 * @author 毛伟然
 * @since 2025/11/4 11:01
 */

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ENABLE(0, "正常"),
    CAN_RECEIVE(1, "可接单"),
    DISABLE(2, "禁用"),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

}
