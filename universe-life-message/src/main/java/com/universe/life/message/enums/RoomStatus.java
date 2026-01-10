package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公共聊天室状态枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum RoomStatus {

    /**
     * 已关闭
     */
    CLOSED(0, "已关闭"),

    /**
     * 正常
     */
    NORMAL(1, "正常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RoomStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoomStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }
}
