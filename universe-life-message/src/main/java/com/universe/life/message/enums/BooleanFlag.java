package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 布尔开关枚举
 * 用于 is_public、allow_member_invite、muted、is_top、is_muted 等字段
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum BooleanFlag {

    /**
     * 否
     */
    NO(0, "否"),

    /**
     * 是
     */
    YES(1, "是");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BooleanFlag of(Integer code) {
        if (code == null) {
            return null;
        }
        for (BooleanFlag flag : values()) {
            if (flag.code.equals(code)) {
                return flag;
            }
        }
        return null;
    }

    public boolean isYes() {
        return this == YES;
    }

    public boolean isNo() {
        return this == NO;
    }

    /**
     * 从布尔值创建枚举
     */
    public static BooleanFlag fromBoolean(boolean value) {
        return value ? YES : NO;
    }

    /**
     * 转换为布尔值
     */
    public boolean toBoolean() {
        return this == YES;
    }
}
