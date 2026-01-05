package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 毛伟然
 * @since 2025/12/2 09:49
 */
@Getter
@AllArgsConstructor
public enum ResourceStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");


    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ResourceStatus of(Integer code) {
        for (ResourceStatus value : ResourceStatus.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return this.equals(ENABLED);
    }

    public boolean isDisabled() {
        return this.equals(DISABLED);
    }

}
