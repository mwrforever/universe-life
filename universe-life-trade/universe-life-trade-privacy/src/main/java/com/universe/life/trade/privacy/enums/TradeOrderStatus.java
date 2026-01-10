package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易订单状态枚举
 *
 * @author universe-life
 */
@Getter
@AllArgsConstructor
public enum TradeOrderStatus {
    PENDING(0, "待审批"),
    PROGRESS(1, "进行中"),
    SUBMIT(2, "待确认"),
    PAYMENT(3, "待收款"),
    DISPUTE(4, "争议中"),
    COMPLETED(5, "已完成"),
    REJECTED(6, "已拒绝"),
    ABANDONED(7, "已放弃"),
    RATE(8, "待评价");

    @EnumValue
    private final Integer code;
    private final String desc;

    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    @JsonCreator
    public static TradeOrderStatus of(Integer code) {
        if (code == null) return null;
        for (TradeOrderStatus e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    /**
     * 是否为终态（不可再变更）
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == REJECTED || this == ABANDONED || this == RATE;
    }

    /**
     * 是否为活跃状态（可进行操作）
     */
    public boolean isActiveStatus() {
        return this == PENDING || this == PROGRESS || this == SUBMIT || this == PAYMENT || this == DISPUTE;
    }
}
