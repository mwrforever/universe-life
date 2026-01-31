package com.universe.life.trade.privacy.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 申请接单命令
 */
@Data
@Builder
public class ApplyOrderCommand {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 接单者ID
     */
    private Long acceptorId;

    /**
     * 申请说明（可选）
     */
    private String applyNote;
}
