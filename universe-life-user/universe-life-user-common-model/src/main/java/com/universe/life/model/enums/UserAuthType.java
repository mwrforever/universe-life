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

    /**
     * 判断是否为微信认证
     */
    public boolean isWechat() {
        return this.equals(WECHAT);
    }

    /**
     * 判断是否为QQ认证
     */
    public boolean isQq() {
        return this.equals(QQ);
    }

    /**
     * 判断是否为支付宝认证
     */
    public boolean isAlipay() {
        return this.equals(ALIPAY);
    }

    /**
     * 判断是否为微博认证
     */
    public boolean isWeibo() {
        return this.equals(WEIBO);
    }

    /**
     * 判断是否为用户名认证
     */
    public boolean isUsername() {
        return this.equals(USERNAME);
    }

    /**
     * 判断是否为手机号认证
     */
    public boolean isPhone() {
        return this.equals(PHONE);
    }

    /**
     * 判断是否为邮箱认证
     */
    public boolean isEmail() {
        return this.equals(EMAIL);
    }

    /**
     * 判断是否为第三方认证（微信、QQ、支付宝、微博）
     */
    public boolean isThirdParty() {
        return this.equals(WECHAT) || this.equals(QQ) || this.equals(ALIPAY) || this.equals(WEIBO);
    }

    /**
     * 判断是否为传统认证（用户名、手机号、邮箱）
     */
    public boolean isTraditional() {
        return this.equals(USERNAME) || this.equals(PHONE) || this.equals(EMAIL);
    }
}
