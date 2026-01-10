package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息状态枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum MessageStatus {

    /**
     * 已发送
     */
    SENT(0, "已发送"),

    /**
     * 已送达
     */
    DELIVERED(1, "已送达"),

    /**
     * 已读
     */
    READ(2, "已读"),

    /**
     * 已撤回
     */
    RECALLED(3, "已撤回"),

    /**
     * 已删除
     */
    DELETED(4, "已删除");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MessageStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isRead() {
        return this == READ;
    }

    public boolean isRecalled() {
        return this == RECALLED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
