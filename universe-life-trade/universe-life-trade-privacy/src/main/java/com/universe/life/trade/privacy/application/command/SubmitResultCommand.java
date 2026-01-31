package com.universe.life.trade.privacy.application.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 提交成果命令
 */
@Data
@Builder
public class SubmitResultCommand {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 操作用户ID（接单者）
     */
    private Long operatorId;

    /**
     * 提交内容
     */
    private String content;

    /**
     * 提交图片列表
     */
    private List<String> images;
}
