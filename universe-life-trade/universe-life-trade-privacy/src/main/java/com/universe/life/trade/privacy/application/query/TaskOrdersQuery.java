package com.universe.life.trade.privacy.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * 任务接单列表查询
 */
@Data
@Builder
public class TaskOrdersQuery {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 当前用户ID（用于权限验证）
     */
    private Long currentUserId;

    /**
     * 状态（可选）
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 获取页码（默认1）
     */
    public int getPageNumOrDefault() {
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }

    /**
     * 获取每页数量（默认20，最大100）
     */
    public int getPageSizeOrDefault() {
        if (pageSize == null || pageSize <= 0) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
