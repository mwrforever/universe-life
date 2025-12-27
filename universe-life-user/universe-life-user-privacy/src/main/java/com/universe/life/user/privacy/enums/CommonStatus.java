package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author Claude
 * @since 2025/11/25
 */
@Getter
@AllArgsConstructor
public enum CommonStatus {

    /**
     * 禁用
     */
    DISABLE(0, "禁用"),

    /**
     * 启用
     */
    ENABLE(1, "启用");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CommonStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommonStatus status : CommonStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isEnable() {
        return this.equals(ENABLE);
    }

    public boolean isDisable() {
        return this.equals(DISABLE);
    }
}