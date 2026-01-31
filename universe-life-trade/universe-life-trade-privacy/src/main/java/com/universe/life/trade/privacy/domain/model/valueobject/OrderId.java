package com.universe.life.trade.privacy.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * 订单ID值对象
 */
@Getter
@EqualsAndHashCode
public class OrderId implements Serializable {

    private final Long value;

    private OrderId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("订单ID不能为空或小于等于0");
        }
        this.value = value;
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
