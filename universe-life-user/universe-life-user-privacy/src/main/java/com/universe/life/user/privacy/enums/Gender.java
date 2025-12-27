package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author 毛伟然
 * @since 2025/12/02
 */
@Getter
@AllArgsConstructor
public enum Gender {

    /**
     * 保密
     */
    SECRET(0, "保密"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Gender of(Integer code) {
        if (code == null) {
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gender.code.equals(code)) {
                return gender;
            }
        }
        return null;
    }

    /**
     * 判断是否为男性
     */
    public boolean isMale() {
        return this.equals(MALE);
    }

    /**
     * 判断是否为女性
     */
    public boolean isFemale() {
        return this.equals(FEMALE);
    }

    /**
     * 判断是否保密
     */
    public boolean isSecret() {
        return this.equals(SECRET);
    }
}