package com.universe.life.trade.privacy.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拒绝信息值对象
 */
@Getter
@EqualsAndHashCode
public class RejectInfo implements Serializable {

    /**
     * 拒绝原因
     */
    private final String reason;

    /**
     * 拒绝时间
     */
    private final LocalDateTime rejectedAt;

    /**
     * 拒绝人ID
     */
    private final Long rejectedBy;

    private RejectInfo(String reason, LocalDateTime rejectedAt, Long rejectedBy) {
        this.reason = reason;
        this.rejectedAt = rejectedAt;
        this.rejectedBy = rejectedBy;
    }

    /**
     * 创建拒绝信息
     */
    public static RejectInfo of(String reason, Long rejectedBy) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("拒绝原因不能为空");
        }
        if (rejectedBy == null) {
            throw new IllegalArgumentException("拒绝人ID不能为空");
        }
        return new RejectInfo(reason, LocalDateTime.now(), rejectedBy);
    }

    /**
     * 从持久化数据重建
     */
    public static RejectInfo reconstitute(String reason, LocalDateTime rejectedAt, Long rejectedBy) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return new RejectInfo(reason, rejectedAt, rejectedBy);
    }

    @Override
    public String toString() {
        return String.format("RejectInfo{reason='%s', rejectedAt=%s, rejectedBy=%d}",
                reason, rejectedAt, rejectedBy);
    }
}
