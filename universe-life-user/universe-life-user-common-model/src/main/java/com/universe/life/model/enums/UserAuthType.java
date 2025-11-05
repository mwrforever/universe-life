package com.universe.life.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 毛伟然
 * @since 2025/11/4 10:07
 */
@Getter
@AllArgsConstructor
public enum UserAuthType {

    /**
     * 微信认证
     */
    WECHAT(0, "微信"),

    /**
     * QQ认证
     */
    QQ(1, "QQ"),

    /**
     * 支付宝认证
     */
    ALIPAY(2, "支付宝"),

    /**
     * 微博认证
     */
    WEIBO(3, "微博"),

    /**
     * 用户名认证
     */
    USERNAME(4, "用户名"),

    /**
     * 手机号认证
     */
    PHONE(5, "手机号"),

    /**
     * 邮箱认证
     */
    EMAIL(6, "邮箱");


    @EnumValue
    @JsonValue
    private final Integer value;

    private final String desc;


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserAuthType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserAuthType type : UserAuthType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }


}
