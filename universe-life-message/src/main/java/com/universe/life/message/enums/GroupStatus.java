package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群组状态枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum GroupStatus {

    /**
     * 已解散
     */
    DISSOLVED(0, "已解散"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GroupStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (GroupStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isDissolved() {
        return this == DISSOLVED;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }
}
