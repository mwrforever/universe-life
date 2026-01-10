package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话类型枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum ConversationType {

    /**
     * 私聊会话
     */
    PRIVATE(1, "私聊会话"),

    /**
     * 群聊会话
     */
    GROUP(2, "群聊会话"),

    /**
     * 聊天室会话
     */
    ROOM(3, "聊天室会话"),

    /**
     * 公共会话
     */
    PUBLIC(4, "公共会话");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ConversationType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConversationType type : values()) {
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
