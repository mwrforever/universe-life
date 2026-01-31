package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommonStatus {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static CommonStatus of(Integer code) {
        if (code == null) return null;
        for (CommonStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
