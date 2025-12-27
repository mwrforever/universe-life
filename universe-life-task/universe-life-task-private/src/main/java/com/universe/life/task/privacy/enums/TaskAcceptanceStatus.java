package com.universe.life.task.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务接受记录状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskAcceptanceStatus {
    PENDING_APPROVAL(-1, "待同意"),
    PROGRESS(0, "进行中"),
    WAIT_CONFIRM(1, "待确认"),
    COMPLETED(2, "已完成"),
    ABANDONED(3, "已放弃"),
    DISPUTE(4, "申诉中"),
    REJECTED(5, "已拒绝");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static TaskAcceptanceStatus of(String name) {
        if (name == null) return null;
        for (TaskAcceptanceStatus e : values()) {
            if (e.name().equals(name)) return e;
        }
        return null;
    }

    public static TaskAcceptanceStatus ofCode(Integer code) {
        if (code == null) return null;
        for (TaskAcceptanceStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
