package com.universe.life.trade.privacy.application.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 发起申诉命令
 */
@Data
@Builder
public class InitiateAppealCommand {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 操作用户ID
     */
    private Long operatorId;

    /**
     * 申诉类型: 1-成果不符合要求, 2-发布者恶意拒绝, 3-其他
     */
    private Integer appealType;

    /**
     * 申诉原因
     */
    private String reason;

    /**
     * 申诉证据图片
     */
    private List<String> evidenceImages;
}
