package com.universe.life.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息内容类型枚举
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum ContentType {

    /**
     * 文本
     */
    TEXT(1, "文本"),

    /**
     * 图片
     */
    IMAGE(2, "图片"),

    /**
     * 文件
     */
    FILE(3, "文件"),

    /**
     * 语音
     */
    VOICE(4, "语音"),

    /**
     * 视频
     */
    VIDEO(5, "视频"),

    /**
     * 位置
     */
    LOCATION(6, "位置"),

    /**
     * 名片
     */
    CARD(7, "名片"),

    /**
     * 系统消息
     */
    SYSTEM(8, "系统消息");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ContentType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ContentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public boolean isText() {
        return this == TEXT;
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public boolean isFile() {
        return this == FILE;
    }

    public boolean isVoice() {
        return this == VOICE;
    }

    public boolean isVideo() {
        return this == VIDEO;
    }

    public boolean isLocation() {
        return this == LOCATION;
    }

    public boolean isCard() {
        return this == CARD;
    }

    public boolean isSystem() {
        return this == SYSTEM;
    }
}
