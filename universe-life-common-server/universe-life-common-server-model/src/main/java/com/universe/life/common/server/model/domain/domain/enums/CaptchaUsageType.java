package com.universe.life.common.server.model.domain.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码使用类型枚举
 *
 * @author 毛伟然
 * @since 2025/11/17
 */
@Getter
@AllArgsConstructor
public enum CaptchaUsageType {

    /**
     * 登录验证码
     */
    LOGIN(1, "login", "安全登录验证"),

    /**
     * 注册验证码
     */
    REGISTER(2, "register", "用户注册验证"),

    /**
     * 修改密码验证码
     */
    RESET_PASSWORD(3, "reset_password", "密码重置验证"),

    /**
     * 绑定邮箱验证码
     */
    BIND_EMAIL(4, "bind_email", "邮箱绑定验证"),

    /**
     * 解绑邮箱验证码
     */
    UNBIND_EMAIL(5, "unbind_email", "邮箱解绑验证"),

    /**
     * 修改支付密码验证码
     */
    MODIFY_PAYMENT_PASSWORD(6, "modify_pay_password", "支付密码修改验证");


    @JsonValue
    private final Integer value;
    private final String displayName;
    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CaptchaUsageType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (CaptchaUsageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }


}