package com.universe.life.trade.privacy.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额值对象（以分为单位存储）
 * <p>
 * 金额在系统内部统一以分为单位存储和计算，避免浮点数精度问题。
 * 对外展示时转换为元。
 * </p>
 * <p>
 * 值对象特性：
 * <ul>
 *   <li>不可变性 - 所有字段都是final的</li>
 *   <li>值相等性 - 通过值而非引用判断相等</li>
 *   <li>无副作用 - 所有操作都返回新对象</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode
public class Money implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** 小数位数 */
    private static final int SCALE = 2;
    
    /** 分转元的除数 */
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 金额（分）
     */
    private final Long cents;

    /**
     * 私有构造函数
     *
     * @param cents 金额（分）
     * @throws IllegalArgumentException 当金额为空或小于0时抛出
     */
    private Money(Long cents) {
        if (cents == null || cents < 0) {
            throw new IllegalArgumentException("金额不能为空或小于0");
        }
        this.cents = cents;
    }

    /**
     * 从分创建金额
     *
     * @param cents 金额（分）
     * @return Money实例
     */
    public static Money ofCents(Long cents) {
        return new Money(cents);
    }

    /**
     * 从元创建金额
     *
     * @param yuan 金额（元）
     * @return Money实例
     * @throws IllegalArgumentException 当金额为空时抛出
     */
    public static Money ofYuan(BigDecimal yuan) {
        if (yuan == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        return new Money(yuan.multiply(HUNDRED).longValue());
    }

    /**
     * 从元字符串创建金额
     *
     * @param yuan 金额字符串（元）
     * @return Money实例
     */
    public static Money ofYuan(String yuan) {
        return ofYuan(new BigDecimal(yuan));
    }

    /**
     * 获取元金额
     *
     * @return 元金额
     */
    public BigDecimal toYuan() {
        return new BigDecimal(cents).divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 获取元金额字符串
     *
     * @return 元金额字符串
     */
    public String toYuanString() {
        return toYuan().toPlainString();
    }

    /**
     * 金额相加
     *
     * @param other 另一个金额
     * @return 相加后的新金额
     */
    public Money add(Money other) {
        return new Money(this.cents + other.cents);
    }

    /**
     * 金额相减
     *
     * @param other 另一个金额
     * @return 相减后的新金额
     */
    public Money subtract(Money other) {
        return new Money(this.cents - other.cents);
    }

    /**
     * 金额乘以比例
     *
     * @param rate 比例
     * @return 乘以比例后的新金额
     */
    public Money multiply(BigDecimal rate) {
        return new Money(new BigDecimal(this.cents).multiply(rate).longValue());
    }

    /**
     * 计算保证金（50%）
     *
     * @return 保证金金额
     */
    public Money calculateDeposit() {
        return multiply(new BigDecimal("0.5"));
    }

    /**
     * 是否大于
     *
     * @param other 另一个金额
     * @return 是否大于
     */
    public boolean isGreaterThan(Money other) {
        return this.cents > other.cents;
    }

    /**
     * 是否小于
     *
     * @param other 另一个金额
     * @return 是否小于
     */
    public boolean isLessThan(Money other) {
        return this.cents < other.cents;
    }

    /**
     * 是否为零
     *
     * @return 是否为零
     */
    public boolean isZero() {
        return this.cents == 0;
    }

    @Override
    public String toString() {
        return toYuanString() + "元";
    }
}
