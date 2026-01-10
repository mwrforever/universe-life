package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 * 用于区分不同的聊天场景
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    /**
     * 私信
     */
    PRIVATE(1, "私信"),

    /**
     * 群聊
     */
    GROUP(2, "群聊"),

    /**
     * 公共聊天室
     */
    ROOM(3, "公共聊天室"),

    /**
     * 公共单人会话
     */
    PUBLIC(4, "公共单人会话");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MessageType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public boolean isPrivate() {
        return this == PRIVATE;
    }

    public boolean isGroup() {
        return this == GROUP;
    }

    public boolean isRoom() {
        return this == ROOM;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }
}
