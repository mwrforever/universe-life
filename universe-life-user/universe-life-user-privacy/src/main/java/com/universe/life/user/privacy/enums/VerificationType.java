package com.universe.life.user.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证方式枚举
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Getter
@AllArgsConstructor
public enum VerificationType {

    /**
     * 原密码验证
     */
    PASSWORD(0, "原密码验证"),

    /**
     * 邮箱验证码
     */
    EMAIL_CAPTCHA(1, "邮箱验证码"),

    /**
     * 手机验证码
     */
    PHONE_CAPTCHA(2, "手机验证码");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VerificationType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (VerificationType type : VerificationType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为密码验证
     */
    public boolean isPassword() {
        return this.equals(PASSWORD);
    }

    /**
     * 是否为验证码验证
     */
    public boolean isCaptcha() {
        return this.equals(EMAIL_CAPTCHA) || this.equals(PHONE_CAPTCHA);
    }
}
