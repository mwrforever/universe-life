package com.universe.life.trade.privacy.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易申诉类型枚举
 * <p>
 * 用于数据库持久化的申诉类型枚举，与MyBatis-Plus集成。
 * 区分申诉发起方是接单者还是发布者。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TradeAppealType {
    
    /**
     * 接单方申诉
     * <p>接单者对订单结果发起的申诉，通常是认为发布者恶意拒绝验收</p>
     */
    ACCEPTOR(1, "接单方申诉"),
    
    /**
     * 发布方申诉
     * <p>发布者对订单结果发起的申诉，通常是认为成果不符合要求</p>
     */
    PUBLISHER(2, "发布方申诉");

    /**
     * 类型码
     * <p>用于数据库存储</p>
     */
    @EnumValue
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 获取类型码（用于JSON序列化）
     *
     * @return 类型码
     */
    @JsonValue
    public Integer getValue() {
        return this.code;
    }

    /**
     * 根据类型码获取枚举实例
     *
     * @param code 类型码
     * @return 对应的枚举实例，如果不存在则返回null
     */
    @JsonCreator
    public static TradeAppealType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeAppealType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为接单方申诉
     *
     * @return 如果是接单方申诉返回true
     */
    public boolean isAcceptorAppeal() {
        return this == ACCEPTOR;
    }
    
    /**
     * 判断是否为发布方申诉
     *
     * @return 如果是发布方申诉返回true
     */
    public boolean isPublisherAppeal() {
        return this == PUBLISHER;
    }
}
