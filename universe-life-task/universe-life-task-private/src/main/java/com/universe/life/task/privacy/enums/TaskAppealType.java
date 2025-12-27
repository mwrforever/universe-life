package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申诉类型枚举
 */
@Getter
@AllArgsConstructor
public enum TaskAppealType {
    ACCEPTOR(1, "接受方申诉"),
    PUBLISHER(2, "发布方申诉");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TaskAppealType of(String name) {
        if (name == null) return null;
        for (TaskAppealType e : values()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static TaskAppealType ofCode(Integer code) {
        if (code == null) return null;
        for (TaskAppealType e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
